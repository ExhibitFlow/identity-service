package com.exhibitflow.identity.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.topics.user-events}")
    private String userEventsTopic;

    @Value("${spring.kafka.topics.auth-events}")
    private String authEventsTopic;

    @Bean
    public NewTopic userEventsTopic() {
        return TopicBuilder.name(userEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic authEventsTopic() {
        return TopicBuilder.name(authEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
