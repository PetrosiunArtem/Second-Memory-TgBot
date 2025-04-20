package org.example.telegrambot.repository;

import org.example.telegrambot.entity.FollowerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowersRepository extends JpaRepository<FollowerEntity, Long> {}
