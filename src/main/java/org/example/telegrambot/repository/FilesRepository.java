package org.example.telegrambot.repository;

import org.example.telegrambot.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FilesRepository extends JpaRepository<FileEntity, Long> {
  List<FileEntity> findFileEntitiesByOwnerId(Long ownerId);
}
