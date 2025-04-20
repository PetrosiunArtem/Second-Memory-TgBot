package org.example.telegrambot.repository;

import org.example.telegrambot.entity.CrsUserFollowerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrsUsersFollowersRepository extends JpaRepository<CrsUserFollowerEntity, Long> {
  @Query(
      value = "SELECT follower_id FROM users_followers WHERE user_id = :userId",
      nativeQuery = true)
  List<Long> findByUserId(@Param("userId") Long userId);
}
