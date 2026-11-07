package Scaleflow;

import Scaleflow.model.Job;
import Scaleflow.model.JobStatus;
import Scaleflow.model.OperationType;
import Scaleflow.repository.JobRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class JobClaimConcurrencyTest {

    @Autowired
    private JobRepository jobRepo;

    @Test
    void onlyOneWorkerCanClaimTheSameJob() throws Exception {
        Job job = new Job();
        job.setFileName("test.png");
        job.setInputPath("test-input.png");
        job.setOperation(OperationType.THUMBNAIL);
        job.setStatus(JobStatus.QUEUED);

        Long jobId = jobRepo.saveAndFlush(job).getId();

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<Integer> workerA = executor.submit(
                    () -> claim(jobId, "worker-A", ready, start));

            Future<Integer> workerB = executor.submit(
                    () -> claim(jobId, "worker-B", ready, start));

            // Wait until both threads reach the starting gate.
            assertTrue(ready.await(5, TimeUnit.SECONDS));

            // Release both threads together.
            start.countDown();

            int resultA = workerA.get(10, TimeUnit.SECONDS);
            int resultB = workerB.get(10, TimeUnit.SECONDS);

            // One update succeeds; the other updates zero rows.
            assertTrue(
                    (resultA == 1 && resultB == 0)
                    || (resultA == 0 && resultB == 1)
            );

            Job claimed = jobRepo.findById(jobId).orElseThrow();

            assertEquals(JobStatus.PROCESSING, claimed.getStatus());
            assertEquals(
                    resultA == 1 ? "worker-A" : "worker-B",
                    claimed.getClaimToken()
            );
            assertNotNull(claimed.getLeaseExpiresAt());

        } finally {
            start.countDown();
            executor.shutdownNow();

            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException(
                        "Test threads did not stop");
            }

            jobRepo.deleteById(jobId);
        }
    }

    private int claim(
            Long jobId,
            String token,
            CountDownLatch ready,
            CountDownLatch start
    ) throws InterruptedException {

        ready.countDown();

        if (!start.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Start gate timed out");
        }

        Instant now = Instant.now();

        return jobRepo.claimJob(
                jobId,
                token,
                now,
                now.plusSeconds(300),
                JobStatus.PROCESSING,
                JobStatus.QUEUED,
                JobStatus.FAILED
        );
    }
    @Test
    void oldWorkerCannotCompleteAfterJobIsReclaimed() {
        Job job = new Job();
        job.setFileName("test.png");
        job.setInputPath("test-input.png");
        job.setOperation(OperationType.THUMBNAIL);
        job.setStatus(JobStatus.QUEUED);

        Long jobId = jobRepo.saveAndFlush(job).getId();

        // Simulated timestamps: no sleeping needed.
        Instant start = Instant.parse("2026-11-06T12:00:00Z");
        Instant afterExpiry = start.plusSeconds(11);

        try {
            // A claims the job with a 10-second lease.
            assertEquals(1, jobRepo.claimJob(
                    jobId, "worker-A",
                    start, start.plusSeconds(10),
                    JobStatus.PROCESSING,
                    JobStatus.QUEUED,
                    JobStatus.FAILED
            ));

            // B reclaims it after A's lease expires.
            assertEquals(1, jobRepo.claimJob(
                    jobId, "worker-B",
                    afterExpiry, afterExpiry.plusSeconds(300),
                    JobStatus.PROCESSING,
                    JobStatus.QUEUED,
                    JobStatus.FAILED
            ));

            // A finishes late: its token is no longer valid.
            assertEquals(0, jobRepo.completedJob(
                    jobId, "worker-A",
                    "outputs/worker-A.png",
                    afterExpiry.plusSeconds(1),
                    JobStatus.PROCESSING,
                    JobStatus.COMPLETED
            ));

            Job stillOwnedByB = jobRepo.findById(jobId).orElseThrow();
            assertEquals(JobStatus.PROCESSING, stillOwnedByB.getStatus());
            assertEquals("worker-B", stillOwnedByB.getClaimToken());
            assertNull(stillOwnedByB.getOutputPath());

            // B can complete using its valid token and lease.
            assertEquals(1, jobRepo.completedJob(
                    jobId, "worker-B",
                    "outputs/worker-B.png",
                    afterExpiry.plusSeconds(2),
                    JobStatus.PROCESSING,
                    JobStatus.COMPLETED
            ));

            Job completed = jobRepo.findById(jobId).orElseThrow();
            assertEquals(JobStatus.COMPLETED, completed.getStatus());
            assertEquals("outputs/worker-B.png", completed.getOutputPath());
            assertNull(completed.getClaimToken());
            assertNull(completed.getLeaseExpiresAt());

        } finally {
            jobRepo.deleteById(jobId);
        }
    }
    @Test
    void expiredWorkerCannotCompleteWithoutReclaim() {
        Job job = new Job();
        job.setFileName("test.png");
        job.setInputPath("test-input.png");
        job.setOperation(OperationType.THUMBNAIL);
        job.setStatus(JobStatus.QUEUED);

        Long jobId = jobRepo.saveAndFlush(job).getId();
        Instant start = Instant.parse("2026-11-06T12:00:00Z");

        try {
            assertEquals(1, jobRepo.claimJob(
                    jobId, "worker-A",
                    start, start.plusSeconds(10),
                    JobStatus.PROCESSING,
                    JobStatus.QUEUED,
                    JobStatus.FAILED
            ));

            // Same token, but the lease has expired.
            assertEquals(0, jobRepo.completedJob(
                    jobId, "worker-A",
                    "outputs/worker-A.png",
                    start.plusSeconds(11),
                    JobStatus.PROCESSING,
                    JobStatus.COMPLETED
            ));

            Job unchanged = jobRepo.findById(jobId).orElseThrow();

            assertEquals(JobStatus.PROCESSING, unchanged.getStatus());
            assertEquals("worker-A", unchanged.getClaimToken());
            assertNull(unchanged.getOutputPath());

        } finally {
            jobRepo.deleteById(jobId);
        }
    }
}