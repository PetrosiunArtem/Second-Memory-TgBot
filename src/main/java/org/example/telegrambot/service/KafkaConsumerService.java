package org.example.telegrambot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.telegrambot.dto.MessageFileDto;
import org.example.telegrambot.dto.MessageToUsers;
import org.example.telegrambot.dto.MessageUserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {
  private static final Logger LOGGER = LoggerFactory.getLogger(KafkaListener.class);
  private final ObjectMapper objectMapper;
  private final UsersService usersService;
  private final FilesService filesService;
  private final FollowersService followersService;

  @KafkaListener(topics = {"${topic-to-consume-users-message}"})
  public void consumeMessageUserDto(String message) throws JsonProcessingException {
    MessageUserDto parsedMessage = objectMapper.readValue(message, MessageUserDto.class);
    LOGGER.info("Retrieved MessageUserDto {}", message);
    usersService.saveMessage(parsedMessage);
  }

  @KafkaListener(topics = {"${topic-to-consume-files-message}"})
  public MessageToUsers consumeMessageFileDto(String message) throws JsonProcessingException {
    MessageFileDto parsedMessage = objectMapper.readValue(message, MessageFileDto.class);
    LOGGER.info("Retrieved MessageFileDto {}", message);
    filesService.saveMessage(parsedMessage);
    Long ownerId = parsedMessage.ownerId();
    List<Long> chatsIds = followersService.getAllChatsIdsWithOwnerId(ownerId);
    return new MessageToUsers(chatsIds, message);
  }
}
