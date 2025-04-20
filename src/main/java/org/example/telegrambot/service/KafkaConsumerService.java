package org.example.telegrambot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.telegrambot.dto.MessageUserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {
  private static final Logger LOGGER = LoggerFactory.getLogger(KafkaListener.class);
  private final ObjectMapper objectMapper;
  private final UsersService usersService;

  public KafkaConsumerService(ObjectMapper objectMapper, UsersService usersService) {
    this.objectMapper = objectMapper;
    this.usersService = usersService;
  }

  @KafkaListener(topics = {"${topic-to-consume-message}"})
  public void consumeMessageUserDto(String message) throws JsonProcessingException {
    MessageUserDto parsedMessage = objectMapper.readValue(message, MessageUserDto.class);
    LOGGER.info("Retrieved message {}", message);
    usersService.saveMessage(parsedMessage);
  }
}
