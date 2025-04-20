package org.example.telegrambot.controller;

import lombok.RequiredArgsConstructor;
import org.example.telegrambot.exception.CallNonExistentMethodException;
import org.example.telegrambot.kafka.KafkaListenerCreator;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class KafkaController {
  private final KafkaListenerCreator kafkaListenerCreator;
  private final KafkaTemplate<String, String> kafkaTemplate;

  @PostMapping(path = "/create")
  @ResponseStatus(HttpStatus.OK)
  public void create(@RequestParam String topic) throws CallNonExistentMethodException {
    kafkaListenerCreator.createAndRegisterListener(topic);
  }

  @PostMapping(path = "/send")
  @ResponseStatus(HttpStatus.OK)
  public void send(@RequestParam String topic, @RequestParam String message) {
    kafkaTemplate.send(topic, message);
  }
}
