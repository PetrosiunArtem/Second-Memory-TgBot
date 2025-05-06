package org.example.telegrambot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.example.telegrambot.entity.id.UserFollowerId;

@Table(name = "users_followers")
@Entity
@IdClass(UserFollowerId.class)
@Getter
@Setter
public final class CrsUserFollowerEntity {
  @Id private Long userId;

  @Id private Long followerId;
}
