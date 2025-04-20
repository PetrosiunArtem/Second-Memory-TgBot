package org.example.telegrambot.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.MessageListener;

public class KafkaTemplateListener implements MessageListener<String, String> {
  @Override
  public void onMessage(ConsumerRecord<String, String> record) {
    System.out.println("RECORD PROCESSING: " + record);
  }
}
