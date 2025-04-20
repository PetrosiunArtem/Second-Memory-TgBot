package org.example.telegrambot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.telegrambot.repository.CrsUsersFollowersRepository;
import org.example.telegrambot.repository.FollowersRepository;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FollowersService {
  private final FollowersRepository followersRepository;
  private final CrsUsersFollowersRepository crsUsersFollowersRepository;

  public List<Long> getAllChatsIdsWithOwnerId(Long ownerId) {
    log.debug("Select chatId with OwnerId names");
    List<Long> followersIds = crsUsersFollowersRepository.findByUserId(ownerId);
    ArrayList<Long> chatsIds = new ArrayList<>(followersIds);
    for (Long id : followersIds) {
      chatsIds.add(followersRepository.findById(id).get().getChatId());
    }
    return chatsIds;
  }
}
