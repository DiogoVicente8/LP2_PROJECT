package edu.ufp.streaming.rec.models;

import edu.ufp.streaming.rec.enums.ArtistRole;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an artist (actor, director, producer, or writer) on the streaming platform.
 *
 * <p>Artists have a unique ID, personal details, a primary {@link ArtistRole},
 * and a filmography — a list of {@link ArtistContent} associations linking
 * this artist to the content they participated in.
 *
 */
public class Artist {

  /** Unique identifier for this artist. */
  private String id;

  /** Full name of the artist. */
  private String name;

  /** Country of origin/nationality. */
  private String nationality;

  /** Gender string (e.g. "M", "F"). */
  private String gender;

  /** Date of birth. */
  private LocalDate birthDate;

  /** Primary role on the platform (e.g. ACTOR, DIRECTOR). */
  private ArtistRole role;

  /** List of content participations (filmography). */
  private List<ArtistContent> participates;

  /**
   * Constructs a new Artist with the given details.
   *
   * @param id          unique artist ID
   * @param name        full name
   * @param nationality country of origin
   * @param gender      gender string
   * @param birthDate   date of birth
   * @param role        primary {@link ArtistRole}
   */
  public Artist(String id, String name, String nationality, String gender,
                LocalDate birthDate, ArtistRole role) {
    this.id = id;
    this.name = name;
    this.nationality = nationality;
    this.gender = gender;
    this.birthDate = birthDate;
    this.role = role;
    this.participates = new ArrayList<>();
  }

  /**
   * Returns the artist's unique ID.
   *
   * @return ID string
   */
  public String getId() { return id; }

  /**
   * Returns the artist's full name.
   *
   * @return name string
   */
  public String getName() { return name; }

  /**
   * Returns the artist's nationality.
   *
   * @return nationality string
   */
  public String getNationality() { return nationality; }

  /**
   * Returns the artist's gender.
   *
   * @return gender string
   */
  public String getGender() { return gender; }

  /**
   * Returns the artist's date of birth.
   *
   * @return birth date
   */
  public LocalDate getBirthDate() { return birthDate; }

  /**
   * Returns the artist's primary role.
   *
   * @return {@link ArtistRole}
   */
  public ArtistRole getRole() { return role; }

  /**
   * Returns the artist's filmography (list of content participations).
   *
   * @return mutable list of {@link ArtistContent}
   */
  public List<ArtistContent> getFilmography() { return participates; }

  /**
   * Returns the artist's participation list (alias for {@link #getFilmography()}).
   *
   * @return mutable list of {@link ArtistContent}
   */
  public List<ArtistContent> getParticipates() { return participates; }

  /**
   * Sets the artist's unique ID.
   *
   * @param id new ID string
   */
  public void setId(String id) { this.id = id; }

  /**
   * Updates the artist's display name.
   *
   * @param name new name
   */
  public void setName(String name) { this.name = name; }

  /**
   * Updates the artist's nationality.
   *
   * @param nationality new nationality string
   */
  public void setNationality(String nationality) { this.nationality = nationality; }

  /**
   * Updates the artist's gender.
   *
   * @param gender new gender string
   */
  public void setGender(String gender) { this.gender = gender; }

  /**
   * Updates the artist's date of birth.
   *
   * @param birthDate new birth date
   */
  public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

  /**
   * Updates the artist's primary role.
   *
   * @param role new {@link ArtistRole}
   */
  public void setRole(ArtistRole role) { this.role = role; }

  /**
   * Replaces the artist's participation list.
   *
   * @param participates new list of {@link ArtistContent}
   */
  public void setParticipates(List<ArtistContent> participates) { this.participates = participates; }

  /**
   * Adds a content participation to this artist's filmography.
   * Also called automatically by {@link edu.ufp.streaming.rec.managers.ArtistContentManager}
   * when a new participation is recorded.
   *
   * @param ac the {@link ArtistContent} association to add
   */
  public void addParticipation(ArtistContent ac) { this.participates.add(ac); }

  @Override
  public String toString() {
    return "Artist{id='" + id + "', name='" + name + "', nationality='" + nationality
            + "', gender='" + gender + "', birthDate=" + birthDate + ", role=" + role + "}";
  }
}