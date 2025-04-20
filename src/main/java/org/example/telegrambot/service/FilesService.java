package org.example.telegrambot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.telegrambot.dto.MessageFileDto;
import org.example.telegrambot.entity.FileEntity;
import org.example.telegrambot.repository.FilesRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilesService {
  private final FilesRepository filesRepository;

  public void saveMessage(MessageFileDto message) {
    log.debug("save MessageFileDto");
    filesRepository.save(
        FileEntity.builder().ownerId(message.ownerId()).key(message.key()).build());
  }

  public List<String> getAllFilesKeysWithOwnerId(Long ownerId) {
    log.debug("Select all files with ownerId: {}", ownerId);
    return filesRepository.findFileEntitiesByOwnerId(ownerId).stream()
        .map(FileEntity::getKey)
        .toList();
  }
}
