package org.example.telegrambot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.telegrambot.entity.CrsUserFollowerEntity;
import org.example.telegrambot.entity.FollowerEntity;
import org.example.telegrambot.repository.CrsUsersFollowersRepository;
import org.example.telegrambot.repository.FollowersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FollowersService {
  private final FollowersRepository followersRepository;
  private final CrsUsersFollowersRepository crsUsersFollowersRepository;

  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
  public List<Long> getAllChatsIdsWithOwnerId(Long ownerId) {
    log.debug("Select chatId with OwnerId: {}", ownerId);
    return crsUsersFollowersRepository.findByUserId(ownerId);
  }

  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
  public List<Long> getAllUsersIdsWithChatId(Long chatId) {
    log.debug("Select userIds with chatId: {}", chatId);
    return crsUsersFollowersRepository.findByFollowerId(chatId);
  }

  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
  public void subscribeToUser(Long chatId, Long userId) {
    CrsUserFollowerEntity userFollower = new CrsUserFollowerEntity();
    userFollower.setFollowerId(chatId);
    userFollower.setUserId(userId);
    crsUsersFollowersRepository.save(userFollower);
  }

  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
  public void unsubscribeFromUser(Long chatId, Long userId) {
    CrsUserFollowerEntity userFollower = new CrsUserFollowerEntity();
    userFollower.setFollowerId(chatId);
    userFollower.setUserId(userId);
    crsUsersFollowersRepository.delete(userFollower);
  }

  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
  public void save(Long chatId) {
    followersRepository.save(FollowerEntity.builder().chatId(chatId).build());
  }
}
