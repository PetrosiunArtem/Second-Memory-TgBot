package org.example.telegrambot.repository;

import org.example.telegrambot.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<UserEntity, Long> {
  Optional<UserEntity> findByNameLike(String name);

  @Query(
      value =
          "SELECT id FROM users WHERE id not in (select user_id from users_followers where follower_id=:chatId)",
      nativeQuery = true)
  List<Long> findAllUsersWithoutByChatId(@Param("chatId") Long chatId);
}
