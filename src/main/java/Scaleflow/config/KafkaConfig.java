package Scaleflow.config;

import org.springframework.context.annotation.Configuration;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration 
public class KafkaConfig{

    
    @Bean
    public DefaultErrorHandler errorHandler(
        KafkaTemplate<String,String> kafkaTemplate)
    {
        DeadLetterPublishingRecoverer recoverer=
            new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record,exception)->
                    new TopicPartition("jobs.DLQ", 0)
            );
        
        recoverer.setFailIfSendResultIsError(true);

        return new DefaultErrorHandler(
            recoverer,
            new FixedBackOff(2000L,3L)
        );
    }
    /**
     * DQL: Dead Letter Queue,it stores the message that still cannot be processed after retires are exhausted
     * @return
     */
    @Bean
    public NewTopic jobDlqTopic(){
        return TopicBuilder.name("jobs.DLQ")
                .partitions(1)
                .replicas(1)
                .build();
    }

}   