package Scaleflow.service;

import Scaleflow.model.Job;
import Scaleflow.model.JobStatus;
import Scaleflow.model.OperationType;
import Scaleflow.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

@Service 
public class JobWorker {

    private static final Logger log=LoggerFactory.getLogger(JobWorker.class);
    private final ThumbnailService thumbnailService;
    private final JobRepository jobRepo;


    public JobWorker(JobRepository jobRepo,ThumbnailService thumbnailService){
        this.jobRepo=jobRepo;
        this.thumbnailService=thumbnailService;

    }

    @Async  /**run in backstage thread */
    public void processJob(Long jobId){
        Job job=jobRepo.findById(jobId)
                .orElseThrow(()-> 
                new IllegalArgumentException("Job not found "+jobId));
        try{
            job.setStatus(JobStatus.PROCESSING);
            jobRepo.save(job);

            if(job.getOperation()!=OperationType.THUMBNAIL){
                throw new IllegalArgumentException("Unsupported operation: "+job.getOperation());
            }
            String outputpath=thumbnailService.generateThumbnail(job.getInputPath(), jobId);
            job.setOutputPath(outputpath);
            job.setStatus(JobStatus.COMPLETED);
            jobRepo.save(job);
        }catch(Exception e){
            job.setStatus(JobStatus.FAILED);
            jobRepo.save(job);
            log.error("Job failed:{}", jobId, e);
        }
    }
}
