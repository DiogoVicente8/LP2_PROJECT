package edu.ufp.streaming.rec.models;

import java.time.LocalDateTime;
/**
 * Represents a follow relationship between two {@link User} entities.
 *
 * <p>Stores the follower, the followed user, and the timestamp of when
 * the follow relationship was created.
 *
 * <p>In Phase 2, each {@code UserFollow} maps directly to a directed
 * weighted edge {@code follower → followed} in the platform graph.
 *
 **/
public class UserFollow {

  /** The user who initiated the follow. */
  private User follower;

  /** The user being followed. */
  private User followed;

  /** The date and time when the follow relationship was created. */
  private LocalDateTime followDate;

  /**
   * Constructs a new UserFollow relationship between two users.
   * The follow date is automatically set to the current date and time.
   *
   * @param follower the {@link User} who is following
   * @param followed the {@link User} being followed
   */
  public UserFollow(User follower, User followed) {
    this.follower = follower;
    this.followed = followed;
    this.followDate = LocalDateTime.now();
  }

  /**
   * Returns the user who initiated the follow.
   *
   * @return the follower {@link User}
   */
  public User getFollower() { return follower; }

  /**
   * Returns the user being followed.
   *
   * @return the followed {@link User}
   */
  public User getFollowed() { return followed; }

  /**
   * Returns the date and time when this follow relationship was created.
   *
   * @return follow timestamp
   */
  public LocalDateTime getDate() { return followDate; }

  @Override
  public String toString() {
    return "UserFollow{follower=" + follower.getId()
            + ", followed=" + followed.getId()
            + ", date=" + followDate + "}";
  }
}