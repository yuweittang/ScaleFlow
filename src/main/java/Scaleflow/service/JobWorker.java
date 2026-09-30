package Scaleflow.service;

import Scaleflow.model.Job;
import Scaleflow.model.JobStatus;
import Scaleflow.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

@Service 
public class JobWorker {
    private static final Logger log=LoggerFactory.getLogger(JobWorker.class);

    private final JobRepository jobRepo;

    public JobWorker(JobRepository jobRepo){
        this.jobRepo=jobRepo;
    }

    @Async  /**run in backstage thread */
    public void processJob(Long jobId){
        Job job=jobRepo.findById(jobId)
                .orElseThrow(()-> 
                new IllegalArgumentException("Job not found "+jobId));
        try{
            job.setStatus(JobStatus.PROCESSING);
            jobRepo.save(job);

            Thread.sleep(10000);
            job.setStatus(JobStatus.COMPLETED);
            jobRepo.save(job);
        }catch(InterruptedException e){
            Thread.currentThread().interrupt();
            job.setStatus(JobStatus.FAILED);
            jobRepo.save(job);
            log.error("Job interrupted: {}",jobId, e);
        }catch(Exception e){
            job.setStatus(JobStatus.FAILED);
            jobRepo.save(job);
            log.error("Job failed:{}", jobId, e);
        }
    }
}
