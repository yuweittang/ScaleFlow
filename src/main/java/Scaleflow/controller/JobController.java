package Scaleflow.controller;

import org.springframework.web.bind.annotation.*;

import Scaleflow.dto.CreateJobRequest;
import Scaleflow.dto.JobResponse;
import Scaleflow.service.JobService;

@RestController
@RequestMapping("/jobs")
public class JobController {

    // @GetMapping("/hello")
    // public String hello() {
    // return "ScaleFlow is running";
    // }
    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public JobResponse CreateJob(@RequestBody CreateJobRequest request) {
        return jobService.createJob(request);
    }

    @GetMapping("/{id}")
    public JobResponse getJob(@PathVariable Long id) {
        return jobService.getJob(id);
    }
}
