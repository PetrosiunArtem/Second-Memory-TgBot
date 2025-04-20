package org.example.telegrambot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.telegrambot.dto.MessageUserDto;
import org.example.telegrambot.entity.UserEntity;
import org.example.telegrambot.repository.UsersRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
@RequiredArgsConstructor
public class UsersService {

  private final UsersRepository usersRepository;
  private static final String KAFKA_TOPIC_DEFAULT = "topic-";
  static AtomicLong endpointIdIndex = new AtomicLong(1);

  public void saveMessage(MessageUserDto message) {
    log.debug("save MessageUserDto");
    usersRepository.save(
        new UserEntity(
            message.name(),
            message.email(),
            message.password(),
            KAFKA_TOPIC_DEFAULT + endpointIdIndex.getAndIncrement()));
  }

  public List<String> getAllUsersNames() {
    log.debug("Select all users names");
    return usersRepository.findAll().stream().map(UserEntity::getName).toList();
  }
}
