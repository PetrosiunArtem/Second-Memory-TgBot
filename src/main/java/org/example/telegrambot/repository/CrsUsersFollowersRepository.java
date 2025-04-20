package org.example.telegrambot.repository;

import org.example.telegrambot.entity.CrsUserFollowerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrsUsersFollowersRepository extends JpaRepository<CrsUserFollowerEntity, Long> {
  List<Long> findByUserId(Long userId);
}
