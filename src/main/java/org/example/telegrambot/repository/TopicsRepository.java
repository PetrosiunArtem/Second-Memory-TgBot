package org.example.telegrambot.repository;

import org.example.telegrambot.entity.TopicEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TopicsRepository extends JpaRepository<TopicEntity, Long> {}
