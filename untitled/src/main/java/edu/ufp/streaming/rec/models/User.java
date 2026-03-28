package edu.ufp.streaming.rec.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class User {

  private String id;
  private String name;
  private String email;
  private String region;
  private LocalDate registerDate;
  private List<Genre> preferences;
  private List<Content> watchHistory;
  private List<Interation> interactions;

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

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public void setRegisterDate(LocalDate registerDate) {
    this.registerDate = registerDate;
  }

  public void setPreferences(List<Genre> preferences) {
    this.preferences = preferences;
  }

  public void setWatchHistory(List<Content> watchHistory) {
    this.watchHistory = watchHistory;
  }

  public void setInteractions(List<Interation> interactions) {
    this.interactions = interactions;
  }

  public String getEmail() {
    return email;
  }

  public String getRegion() {
    return region;
  }

  public LocalDate getRegisterDate() {
    return registerDate;
  }

  public List<Genre> getPreferences() {
    return preferences;
  }


  public void addPreference(Genre genre) {
    // Previne nulos e evita adicionar o mesmo género duas vezes
    if (this.preferences == null) {
      this.preferences = new ArrayList<>();
    }
    if (genre != null && !this.preferences.contains(genre)) {
      this.preferences.add(genre);
    }
  }

  public List<Content> getWatchHistory() {
    return watchHistory;
  }

  public List<Interation> getInteractions() {
    return interactions;
  }

  public void addInteration(Interation interation) {
    this.interactions.add(interation);
  }

  public void follow(User u) {
    // lógica de seguir outro utilizador (via edu.pt.lp2.edu.ufp.streaming.rec.models.UserFollow)
  }

  public List<User> getFollowers() {
    return new ArrayList<>();
  }
}