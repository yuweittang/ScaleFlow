package Scaleflow.service;

import Scaleflow.model.Job;
import Scaleflow.model.JobStatus;
import Scaleflow.model.OperationType;
import Scaleflow.repository.JobRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class JobWorker {

    private static final Logger log =
            LoggerFactory.getLogger(JobWorker.class);

    private final ThumbnailService thumbnailService;
    private final JobRepository jobRepo;
    private final Duration leaseDuration;

    public JobWorker(
            JobRepository jobRepo,
            ThumbnailService thumbnailService,
            @Value("${jobs.lease-duration:PT5M}") Duration leaseDuration
    ) {
        this.jobRepo = jobRepo;
        this.thumbnailService = thumbnailService;
        this.leaseDuration = leaseDuration;
    }

    public void processJob(Long jobId) {
        String token = UUID.randomUUID().toString();
        Instant now = Instant.now();

        int claimed = jobRepo.claimJob(
                jobId,
                token,
                now,
                now.plus(leaseDuration),
                JobStatus.PROCESSING,
                JobStatus.QUEUED,
                JobStatus.FAILED
        );

        if (claimed == 0) {
            Job job = jobRepo.findById(jobId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Job not found: " + jobId));

            if (job.getStatus() == JobStatus.COMPLETED) {
                log.info("Skipping completed job: {}", jobId);
            } else {
                log.info(
                        "Skipping unclaimed delivery: {}, status: {}",
                        jobId,
                        job.getStatus()
                );
            }

            // Recovery will resubmit expired PROCESSING jobs.
            return;
        }

        try {
            Job job = jobRepo.findById(jobId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Job not found: " + jobId));

            log.info("Claimed job: {}, token: {}", jobId, token);

            if (job.getOperation() != OperationType.THUMBNAIL) {
                throw new IllegalArgumentException(
                        "Unsupported operation: " + job.getOperation());
            }

            String outputPath = thumbnailService.generateThumbnail(
                    job.getInputPath(),
                    jobId
            );

            int completed = jobRepo.completedJob(
                    jobId,
                    token,
                    outputPath,
                    Instant.now(),
                    JobStatus.PROCESSING,
                    JobStatus.COMPLETED
            );

            if (completed == 0) {
                log.warn(
                        "Discarding result after lease expired "
                                + "or ownership changed: {}",
                        jobId
                );
                return;
            }

            log.info("Completed job: {}", jobId);

        } catch (Exception e) {
            int failed = jobRepo.failJob(
                    jobId,
                    token,
                    Instant.now(),
                    JobStatus.PROCESSING,
                    JobStatus.FAILED
            );

            if (failed == 0) {
                log.warn(
                        "Attempt failed after lease expired "
                                + "or ownership changed: {}",
                        jobId,
                        e
                );
                return;
            }

            log.error("Job failed: {}", jobId, e);

            // Propagate the original cause so Kafka can retry.
            throw new RuntimeException(
                    "Job processing failed: " + jobId,
                    e
            );
        }
    }
}