package edu.ufp.streaming.rec.models;

import java.time.LocalDateTime;

public class UserFollow {

  private User follower;
  private User followed;
  private LocalDateTime followDate;

  public UserFollow(User follower, User followed) {
    this.follower = follower;
    this.followed = followed;
    this.followDate = LocalDateTime.now();
  }

  public User getFollower() {
    return follower;
  }

  public User getFollowed() {
    return followed;
  }

  public LocalDateTime getDate() {
    return followDate;
  }
}