package org.example.telegrambot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.telegrambot.dto.MessageFileDto;
import org.example.telegrambot.repository.UsersRepository;
import org.example.telegrambot.tgbot.SecondMemoryBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KafkaService {
  private static final Logger LOGGER = LoggerFactory.getLogger(KafkaListener.class);
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final FollowersService followersService;
  private final SecondMemoryBot secondMemoryBot;
  private final UsersRepository usersRepository;

  @KafkaListener(topics = {"${topic-to-consume-files-message}"})
  public void consumeMessageFileDto(String message) throws JsonProcessingException {
    MessageFileDto parsedMessage = objectMapper.readValue(message, MessageFileDto.class);
    LOGGER.info("Retrieved MessageFileDto {}", message);
    Long ownerId = parsedMessage.ownerId();
    String name = usersRepository.findById(ownerId).orElseThrow().getName();
    List<Long> chatsIds = followersService.getAllChatsIdsWithOwnerId(ownerId);
    for (Long chatId : chatsIds) {
      String currentMessage =
          "User with name: "
              + name
              + " upload the new file: "
              + parsedMessage.key()
              + " in bucket: "
              + parsedMessage.bucketName();
      secondMemoryBot.getResponseHandler().push(chatId, currentMessage);
    }
  }
}
