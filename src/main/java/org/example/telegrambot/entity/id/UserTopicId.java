package org.example.telegrambot.entity.id;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class UserTopicId implements Serializable {
  private Long userId;
  private Long topicId;
}
