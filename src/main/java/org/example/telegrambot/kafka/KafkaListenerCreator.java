package org.example.telegrambot.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.telegrambot.exception.CallNonExistentMethodException;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpoint;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.config.MethodKafkaListenerEndpoint;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class KafkaListenerCreator {
  private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
  private final KafkaListenerContainerFactory kafkaListenerContainerFactory;

  private static final String KAFKA_GROUP_ID = "kafkaGroupId";
  private static final String KAFKA_LISTENER_ID = "kafkaListenerId-";
  static AtomicLong endpointIdIndex = new AtomicLong(1);

  private KafkaListenerEndpoint createKafkaListenerEndpoint(String topic)
      throws CallNonExistentMethodException {
    MethodKafkaListenerEndpoint<String, String> kafkaListenerEndpoint =
        createDefaultMethodKafkaListenerEndpoint(topic);
    kafkaListenerEndpoint.setBean(new KafkaTemplateListener());
    try {
      kafkaListenerEndpoint.setMethod(
          KafkaTemplateListener.class.getMethod("onMessage", ConsumerRecord.class));
    } catch (NoSuchMethodException e) {
      throw new CallNonExistentMethodException("Attempt to call a non-existent method " + e);
    }
    return kafkaListenerEndpoint;
  }

  private MethodKafkaListenerEndpoint<String, String> createDefaultMethodKafkaListenerEndpoint(
      String topic) {
    MethodKafkaListenerEndpoint<String, String> kafkaListenerEndpoint =
        new MethodKafkaListenerEndpoint<>();
    kafkaListenerEndpoint.setId(generateListenerId());
    kafkaListenerEndpoint.setGroupId(KAFKA_GROUP_ID);
    kafkaListenerEndpoint.setAutoStartup(true);
    kafkaListenerEndpoint.setTopics(topic);
    kafkaListenerEndpoint.setMessageHandlerMethodFactory(new DefaultMessageHandlerMethodFactory());
    return kafkaListenerEndpoint;
  }

  private String generateListenerId() {
    return KAFKA_LISTENER_ID + endpointIdIndex.getAndIncrement();
  }

  public void createAndRegisterListener(String topic) throws CallNonExistentMethodException {
    KafkaListenerEndpoint listener = createKafkaListenerEndpoint(topic);
    kafkaListenerEndpointRegistry.registerListenerContainer(
        listener, kafkaListenerContainerFactory, true);
  }
}
