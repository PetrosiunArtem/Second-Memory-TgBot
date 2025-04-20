package org.example.telegrambot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.telegrambot.entity.TopicEntity;
import org.example.telegrambot.repository.TopicsRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TopicsService {
  private final TopicsRepository topicsRepository;

  public List<String> getAllTopicsNames() {
    log.debug("Select all topics names");
    return topicsRepository.findAll().stream().map(TopicEntity::getName).toList();
  }
}
