package Scaleflow.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service 
public class JobConsumer {
    private static final Logger log=LoggerFactory.getLogger(JobConsumer.class);

    private final JobWorker jobWorker;

    public JobConsumer(JobWorker jobWorker){
        this.jobWorker=jobWorker;
    }

    @KafkaListener (topics="jobs")
    public void consume(String message){
        Long jobId=Long.valueOf(message);

        log.info("Received job: {}", jobId);

        jobWorker.processJob(jobId);

        log.info("Finished handling job: {}", jobId);
    }
}
