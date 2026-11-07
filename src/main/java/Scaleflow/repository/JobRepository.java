package Scaleflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import java.time.Instant;
import java.util.List;

import Scaleflow.model.JobStatus;
import Scaleflow.model.Job;
import jakarta.transaction.Transactional;


public interface JobRepository extends JpaRepository<Job, Long> {
    //case when job is failed or not started or processing but expired
    @Modifying(clearAutomatically=true)
    @Transactional 
    @Query ("""
            UPDATE Job j
                        SET j.status = :processing,
                                j.claimToken = :token,
                                j.leaseExpiresAt = :expiresAt
                        WHERE j.id = :jobId
                            AND (j.status IN (:queued, :failed)
                                OR (j.status = :processing
                                    AND (j.leaseExpiresAt IS NULL
                                        OR j.leaseExpiresAt <= :now))
                                )
            """)
    int claimJob(
        @Param ("jobId") Long jobId,
        @Param ("token") String token,
        @Param ("now") Instant now,
        @Param ("expiresAt") Instant expiresAt,
        @Param ("processing") JobStatus processing,
        @Param ("queued") JobStatus queued,
        @Param ("failed") JobStatus failed
    );

    //complete the task and this is the newest worker
    @Modifying(clearAutomatically=true)
    @Transactional 
    @Query (
        """
                UPDATE Job j
                SET j.status = :completed,
                    j.outputPath = :outputPath,
                    j.claimToken = NULL,
                    j.leaseExpiresAt = NULL
                WHERE j.id = :jobId
                  AND j.status = :processing
                  AND j.claimToken = :token
                  AND j.leaseExpiresAt > :now
                """
    )
    int completedJob(
            @Param("jobId") Long jobId,
            @Param("token") String token,
            @Param("outputPath") String outputPath,
            @Param("now") Instant now,
            @Param("processing") JobStatus processing,
            @Param("completed") JobStatus completed
    );

    //Label failed when status is processing but the task failed
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("""
        UPDATE Job j
        SET j.status = :failed,
            j.claimToken = NULL,
            j.leaseExpiresAt = NULL
        WHERE j.id = :jobId
          AND j.status = :processing
          AND j.claimToken = :token
          AND j.leaseExpiresAt > :now
        """)
    int failJob(
            @Param("jobId") Long jobId,
            @Param("token") String token,
            @Param("now") Instant now,
            @Param("processing") JobStatus processing,
            @Param("failed") JobStatus failed
    );

    //find all expired job ids
    @Query("""
        SELECT j.id FROM Job j
        WHERE j.status = :processing
          AND (
              j.leaseExpiresAt IS NULL
              OR j.leaseExpiresAt <= :now
          )
        """)
    List<Long> findExpiredJobIds(
            @Param("processing") JobStatus processing,
            @Param("now") Instant now
    );
    
}
