package org.example.telegrambot.entity.id;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class UserFollowerId implements Serializable {
  private Long userId;
  private Long followerId;
}
