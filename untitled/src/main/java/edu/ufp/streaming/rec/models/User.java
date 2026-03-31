package edu.ufp.streaming.rec.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user/client of the streaming platform.
 *
 * <p>A user has a unique ID, personal details, a preferred region,
 * a list of genre preferences, a watch history, and recorded interactions
 * with platform content.
 */
public class User {

  /** Unique identifier for this user. */
  private String id;

  /** Display name of the user. */
  private String name;

  /** Contact email address. */
  private String email;

  /** Geographic region (e.g. "PT", "US", "BR"). */
  private String region;

  /** Date the user registered on the platform. */
  private LocalDate registerDate;

  /** List of genres the user has marked as preferences. */
  private List<Genre> preferences;

  /** Ordered list of content the user has watched. */
  private List<Content> watchHistory;

  /** All recorded interactions (watch, rate, bookmark, skip). */
  private List<Interation> interactions;

  /**
   * Constructs a new User with the given details.
   *
   * @param id           unique user ID
   * @param name         display name
   * @param email        email address
   * @param region       geographic region
   * @param registerDate date of registration on the platform
   */
  public User(String id, String name, String email, String region, LocalDate registerDate) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.region = region;
    this.registerDate = registerDate;
    this.preferences = new ArrayList<>();
    this.watchHistory = new ArrayList<>();
    this.interactions = new ArrayList<>();
  }

  /**
   * Returns the user's unique ID.
   *
   * @return user ID string
   */
  public String getId() { return id; }

  /**
   * Returns the user's display name.
   *
   * @return name string
   */
  public String getName() { return name; }

  /**
   * Returns the user's email address.
   *
   * @return email string
   */
  public String getEmail() { return email; }

  /**
   * Returns the user's geographic region.
   *
   * @return region string
   */
  public String getRegion() { return region; }

  /**
   * Returns the date the user registered on the platform.
   *
   * @return registration date
   */
  public LocalDate getRegisterDate() { return registerDate; }

  /**
   * Returns the list of genres the user prefers.
   *
   * @return mutable list of {@link Genre} preferences
   */
  public List<Genre> getPreferences() { return preferences; }

  /**
   * Returns the user's watch history.
   *
   * @return mutable list of watched {@link Content}
   */
  public List<Content> getWatchHistory() { return watchHistory; }

  /**
   * Returns all interactions recorded for this user.
   *
   * @return mutable list of {@link Interation} objects
   */
  public List<Interation> getInteractions() { return interactions; }

  /**
   * Sets the user's unique ID.
   *
   * @param id new ID string
   */
  public void setId(String id) { this.id = id; }

  /**
   * Updates the user's display name.
   *
   * @param name new name
   */
  public void setName(String name) { this.name = name; }

  /**
   * Updates the user's email address.
   *
   * @param email new email address
   */
  public void setEmail(String email) { this.email = email; }

  /**
   * Updates the user's geographic region.
   *
   * @param region new region string
   */
  public void setRegion(String region) { this.region = region; }

  /**
   * Updates the user's registration date.
   *
   * @param registerDate new registration date
   */
  public void setRegisterDate(LocalDate registerDate) { this.registerDate = registerDate; }

  /**
   * Replaces the user's genre preferences list.
   *
   * @param preferences new list of {@link Genre} preferences
   */
  public void setPreferences(List<Genre> preferences) { this.preferences = preferences; }

  /**
   * Replaces the user's watch history list.
   *
   * @param watchHistory new list of watched {@link Content}
   */
  public void setWatchHistory(List<Content> watchHistory) { this.watchHistory = watchHistory; }

  /**
   * Replaces the user's interactions list.
   *
   * @param interactions new list of {@link Interation} objects
   */
  public void setInteractions(List<Interation> interactions) { this.interactions = interactions; }

  /**
   * Adds a genre to the user's preferences if not already present.
   *
   * @param genre the {@link Genre} to add; ignored if {@code null} or already present
   */
  public void addPreference(Genre genre) {
    if (this.preferences == null) this.preferences = new ArrayList<>();
    if (genre != null && !this.preferences.contains(genre)) this.preferences.add(genre);
  }

  /**
   * Records an interaction for this user.
   *
   * @param interation the {@link Interation} to add
   */
  public void addInteration(Interation interation) {
    this.interactions.add(interation);
  }

  /**
   * Placeholder for the follow action.
   * Actual follow logic is managed by {@link edu.ufp.streaming.rec.managers.FollowManager}.
   *
   * @param u the user to follow
   */
  public void follow(User u) {
    // Follow logic is delegated to FollowManager
  }

  public List<User> getFollowers() { return new ArrayList<>(); }

  @Override
  public String toString() {
    return "User{id='" + id + "', name='" + name + "', email='" + email
            + "', region='" + region + "', registerDate=" + registerDate + "}";
  }
}