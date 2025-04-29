package org.example.telegrambot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.telegrambot.entity.UserEntity;
import org.example.telegrambot.repository.UsersRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UsersService {

  private final UsersRepository usersRepository;

  public List<String> getAllUsersNames() {
    log.debug("Select all users names");
    return usersRepository.findAll().stream().map(UserEntity::getName).toList();
  }

  public Optional<UserEntity> getUserByName(String name) {
    return usersRepository.findByNameLike(name);
  }

  public List<Long> getAllUsersIds() {
    return usersRepository.findAll().stream().map(UserEntity::getId).toList();
  }

  public List<Long> getAllUsersWithoutChatId(Long chatId) {
    return usersRepository.findAllUsersWithoutByChatId(chatId);
  }
}
