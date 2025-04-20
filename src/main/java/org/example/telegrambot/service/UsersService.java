package org.example.telegrambot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.telegrambot.dto.MessageUserDto;
import org.example.telegrambot.entity.UserEntity;
import org.example.telegrambot.repository.TopicsRepository;
import org.example.telegrambot.repository.UsersRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UsersService {

  private final UsersRepository usersRepository;
  private final TopicsRepository topicsRepository;

  public void saveMessage(MessageUserDto message) {
    log.debug("save MessageUserDto");
    usersRepository.save(new UserEntity(message.name(), message.email(), message.password()));
  }

  public List<String> getAllUsersNames() {
    log.debug("Select all users names");
    return usersRepository.findAll().stream().map(UserEntity::getName).toList();
  }
}
