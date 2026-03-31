package edu.ufp.streaming.rec.models;

import edu.ufp.streaming.rec.enums.ArtistRole;

import java.time.LocalDate;

/**
 * Represents the participation of an {@link Artist} in a {@link Content} item.
 *
 * <p>This is the association class linking artists to the content they participated in,
 * capturing the role played (e.g. ACTOR, DIRECTOR) and the date of participation.
 *
 * <p>In Phase 2, each {@code ArtistContent} maps directly to a directed weighted
 * edge {@code Artist → Content} in the platform graph.
 *
 */
public class ArtistContent {

  /** The artist who participated in the content. */
  private Artist artist;

  /** The content item the artist participated in. */
  private Content content;

  /** The role the artist played in this content. */
  private ArtistRole role;

  /** The date of participation (e.g. release date or filming date). */
  private LocalDate date;

  /**
   * Constructs a new ArtistContent participation record.
   *
   * @param artist  the participating {@link Artist}
   * @param content the {@link Content} item
   * @param role    the {@link ArtistRole} the artist played
   * @param date    the date of participation
   */
  public ArtistContent(Artist artist, Content content, ArtistRole role, LocalDate date) {
    this.artist = artist;
    this.content = content;
    this.role = role;
    this.date = date;
  }

  /**
   * Returns the artist who participated.
   *
   * @return the {@link Artist}
   */
  public Artist getArtist() { return artist; }

  /**
   * Returns the content item.
   *
   * @return the {@link Content}
   */
  public Content getContent() { return content; }

  /**
   * Returns the role the artist played in this content.
   *
   * @return the {@link ArtistRole}
   */
  public ArtistRole getRole() { return role; }

  /**
   * Returns the date of participation.
   *
   * @return participation date
   */
  public LocalDate getDate() { return date; }

  @Override
  public String toString() {
    return "ArtistContent{artist=" + artist.getName()
            + ", content=" + content.getTitle()
            + ", role=" + role
            + ", date=" + date + "}";
  }
}