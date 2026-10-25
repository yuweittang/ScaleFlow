package Scaleflow.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Service 
public class JobProducer {


    private final KafkaTemplate<String,String> kafkaTemplate;

    public JobProducer(KafkaTemplate<String,String> kafkaTemplate){
        this.kafkaTemplate=kafkaTemplate;
    }

    /**
     * send jobs to services
     * @param jobId
     * @return
     */
    public CompletableFuture<SendResult<String,String>> sendJob(Long jobId){
        String id=jobId.toString();

        return kafkaTemplate.send("jobs",id ,id);
    }
}
