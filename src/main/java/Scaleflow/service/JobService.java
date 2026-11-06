package Scaleflow.service;

import Scaleflow.dto.CreateJobRequest;
import Scaleflow.dto.JobResponse;
import Scaleflow.model.Job;
import Scaleflow.model.JobStatus;
import Scaleflow.repository.JobRepository;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import tools.jackson.databind.json.JsonMapper;;

@Service
public class JobService {
    private final JobRepository jobRepo;
    // private final JobWorker jobWorker;
    private final JobProducer jobProducer;
    private static final Logger log=LoggerFactory.getLogger(JobService.class);
    private final StringRedisTemplate redisTemplate;
    private final JsonMapper jsonMapper;

    public JobService(
        JobRepository jobRepository,
        JobProducer jobProducer,
        StringRedisTemplate redisTemplate,
        JsonMapper jsonMapper) {
        this.jobRepo = jobRepository;
        // this.jobWorker=jobWorker;
        this.jobProducer=jobProducer;
        this.redisTemplate=redisTemplate;
        this.jsonMapper=jsonMapper;
    }

    public JobResponse createJob(CreateJobRequest request) {
        Job job = new Job();
        //set properties for job
        job.setFileName(request.getFileName());
        job.setOperation(request.getOperation());
        job.setStatus(JobStatus.QUEUED);
        job.setInputPath(request.getInputPath());

        Job savedjob = jobRepo.save(job);

        try{
            jobProducer.sendJob(savedjob.getId())
                //it will block current thread and wait the kafka confirmation, 
                //at most wait for 10 sec
                .get(10,TimeUnit.SECONDS);

        }catch(InterruptedException e){
            //thread interrupted: end the waiting thread
            Thread.currentThread().interrupt();

            throw publishError(savedjob.getId(),e);
        }catch(ExecutionException e){
            // get() function throw error
            throw publishError(savedjob.getId(),e);

        }  catch(RuntimeException e){
            //cannot get response in 10 sec
            throw publishError(savedjob.getId(),e);

        }catch(TimeoutException e){
            //send function throw error
            throw publishError(savedjob.getId(),e);
        }
        
        return toResponse(savedjob);
    }



    public JobResponse getJob(Long id) {
        String key="job:"+id;

        //Try Redis first
        try{
            String cachedJson=redisTemplate.opsForValue().get(key);
            
            //record exist in cache
            if(cachedJson!=null){
                JobResponse cachedJobResponse=jsonMapper.readValue(cachedJson,JobResponse.class);

                log.info("Cache HIT for job {}",id);
                return cachedJobResponse;
            }

            log.info("Cache MISS for job {}",id);
        }catch(RuntimeException e){
            log.warn("Cache read failed for job {}; using database", id ,e);
        }

        Job job = jobRepo.findById(id).orElseThrow(() -> new RuntimeException("Job not found:" + id));

        JobResponse response=toResponse(job);
        //cache the response for 5 seconds
        try{
            String json=jsonMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(
                key, 
                json,
                Duration.ofSeconds(5)
                );
        }catch (RuntimeException e){
            log.warn("Cache write failed for job {}", id ,e);
        }
        return response;
    }

    private JobResponse toResponse(Job job) {
        JobResponse jobResponse = new JobResponse();
        jobResponse.setId(job.getId());
        jobResponse.setFileName(job.getFileName());
        jobResponse.setOperation(job.getOperation());
        jobResponse.setStatus(job.getStatus());
        jobResponse.setInputPath(job.getInputPath());
        jobResponse.setOutputPath(job.getOutputPath());

        return jobResponse;
    }
    /**
     * helper function to create exception 
     * @param jobId
     * @param cause
     * @return
     */
    private ResponseStatusException publishError(Long jobId, Exception cause){
        return new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "jobs"+jobId
                        +" was saved,but kafka publication was not confirmed",
                        cause
                );
    }
}
