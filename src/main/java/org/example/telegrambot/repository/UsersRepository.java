package org.example.telegrambot.repository;

import java.util.Optional;

import org.example.telegrambot.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends JpaRepository<UserEntity, Long> {
  Optional<UserEntity> findById(Long id);

  Optional<UserEntity> findByEmail(String email);
}
