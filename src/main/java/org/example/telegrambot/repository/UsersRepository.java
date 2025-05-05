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

  @Query(
      value =
          "SELECT name FROM users ORDER BY id OFFSET ((:page - 1) * :skipSize) ROWS FETCH NEXT :skipSize ROWS ONLY",
      nativeQuery = true)
  List<String> findAllUsersWithPagination(
      @Param("page") long page, @Param("skipSize") long skipSize);

  @Query(value = "SELECT COUNT(name) FROM users", nativeQuery = true)
  Integer countAllUsers();

  @Query(
      value =
          """
                  SELECT name
                  FROM users
                  WHERE id not in (
                      SELECT user_id
                      FROM users_followers
                      WHERE follower_id=:chatId
                  )
                  ORDER BY id
                  OFFSET ((:page - 1) * :skipSize) ROWS FETCH NEXT :skipSize ROWS ONLY
                  """,
      nativeQuery = true)
  List<String> findAllUsersWithoutByChatIdWithPagination(
      @Param("chatId") Long chatId, @Param("page") long page, @Param("skipSize") long skipSize);

  @Query(
      value =
          """
                  SELECT COUNT(name)
                  FROM users
                  WHERE id not in (
                      SELECT user_id
                      FROM users_followers
                      WHERE follower_id=:chatId
                  )
                  """,
      nativeQuery = true)
  Integer countAllUsersWithoutByChatId(@Param("chatId") Long chatId);

  @Query(
      value =
          """
                          SELECT name
                          FROM users
                          WHERE id  in (
                              SELECT user_id
                              FROM users_followers
                              WHERE follower_id=:chatId
                          )
                          ORDER BY id
                          OFFSET ((:page - 1) * :skipSize) ROWS FETCH NEXT :skipSize ROWS ONLY
                          """,
      nativeQuery = true)
  List<String> findAllUsersWithByChatIdWithPagination(
      @Param("chatId") Long chatId, @Param("page") long page, @Param("skipSize") long skipSize);

  @Query(
      value =
          """
                          SELECT COUNT(name)
                          FROM users
                          WHERE id in (
                              SELECT user_id
                              FROM users_followers
                              WHERE follower_id=:chatId
                          )
                          """,
      nativeQuery = true)
  Integer countAllUsersWithByChatId(@Param("chatId") Long chatId);
}
