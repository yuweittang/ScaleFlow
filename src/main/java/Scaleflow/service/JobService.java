package Scaleflow.service;

import Scaleflow.dto.CreateJobRequest;
import Scaleflow.dto.JobResponse;
import Scaleflow.model.Job;
import Scaleflow.model.JobStatus;
import Scaleflow.repository.JobRepository;
import org.springframework.stereotype.Service;

@Service
public class JobService {
    private final JobRepository jobRepo;
    private final JobWorker jobWorker;

    public JobService(JobRepository jobRepository,JobWorker jobWorker) {
        this.jobRepo = jobRepository;
        this.jobWorker=jobWorker;
    }

    public JobResponse createJob(CreateJobRequest request) {
        Job job = new Job();
        //set properties for job
        job.setFileName(request.getFileName());
        job.setOperation(request.getOperation());
        job.setStatus(JobStatus.QUEUED);
        job.setInputPath(request.getInputPath());

        Job savedjob = jobRepo.save(job);

        //create a job response for return info
        JobResponse response=toResponse(savedjob);

        //return output path
        response.setOutputPath(job.getOutputPath());
        //handle the process to 
        jobWorker.processJob(savedjob.getId());
        return response;
    }

    public JobResponse getJob(Long id) {
        Job job = jobRepo.findById(id).orElseThrow(() -> new RuntimeException("Job not found:" + id));
        return toResponse(job);
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
}
