package org.example.telegrambot.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;

@Configuration
public class KafkaListenerEndpointRegistryConfig {
  @Primary
  @Bean
  public KafkaListenerEndpointRegistry createKafkaListenerEndpointRegistry() {
    return new KafkaListenerEndpointRegistry();
  }
}
