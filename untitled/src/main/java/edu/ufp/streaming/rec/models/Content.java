package edu.ufp.streaming.rec.models;

import java.time.LocalDate;

/**
 * Classe base que representa um conteúdo multimédia da plataforma de streaming.
 * Serve de superclasse para {@link Movie}, {@link Series} e {@link Documentary}.
 *
 * @author Pedro
 * @version 1.0
 */
public class Content {

  /** Identificador único do conteúdo. */
  private String id;

  /** Título do conteúdo. */
  private String title;

  /** Género do conteúdo. */
  private Genre genre;

  /** Data de lançamento do conteúdo. */
  private LocalDate releaseDate;

  /** Duração do conteúdo em minutos. */
  private int duration;

  /** Região onde o conteúdo está disponível. */
  private String region;

  /** Classificação média do conteúdo (0.0 a 5.0). */
  private double rating;

  /**
   * Constrói um novo conteúdo multimédia.
   *
   * @param id          identificador único
   * @param title       título do conteúdo
   * @param genre       género do conteúdo
   * @param releaseDate data de lançamento
   * @param duration    duração em minutos
   * @param region      região de disponibilidade
   */
  public Content(String id, String title, Genre genre, LocalDate releaseDate,
                 int duration, String region) {
    this.id = id;
    this.title = title;
    this.genre = genre;
    this.releaseDate = releaseDate;
    this.duration = duration;
    this.region = region;
    this.rating = 0.0;
  }

  /**
   * Devolve o identificador único do conteúdo.
   *
   * @return id do conteúdo
   */
  public String getId() {
    return id;
  }

  /**
   * Devolve o título do conteúdo.
   *
   * @return título
   */
  public String getTitle() {
    return title;
  }

  /**
   * Define o título do conteúdo.
   *
   * @param title novo título
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Devolve o género do conteúdo.
   *
   * @return género
   */
  public Genre getGenre() {
    return genre;
  }

  /**
   * Define o género do conteúdo.
   *
   * @param genre novo género
   */
  public void setGenre(Genre genre) {
    this.genre = genre;
  }

  /**
   * Devolve a data de lançamento do conteúdo.
   *
   * @return data de lançamento
   */
  public LocalDate getReleaseDate() {
    return releaseDate;
  }

  /**
   * Define a data de lançamento do conteúdo.
   *
   * @param releaseDate nova data de lançamento
   */
  public void setReleaseDate(LocalDate releaseDate) {
    this.releaseDate = releaseDate;
  }

  /**
   * Devolve a duração do conteúdo em minutos.
   *
   * @return duração em minutos
   */
  public int getDuration() {
    return duration;
  }

  /**
   * Define a duração do conteúdo em minutos.
   *
   * @param duration nova duração em minutos
   */
  public void setDuration(int duration) {
    this.duration = duration;
  }

  /**
   * Devolve a região de disponibilidade do conteúdo.
   *
   * @return região
   */
  public String getRegion() {
    return region;
  }

  /**
   * Define a região de disponibilidade do conteúdo.
   *
   * @param region nova região
   */
  public void setRegion(String region) {
    this.region = region;
  }

  /**
   * Devolve a classificação média do conteúdo.
   *
   * @return rating (0.0 a 5.0)
   */
  public double getRating() {
    return rating;
  }

  /**
   * Define a classificação média do conteúdo.
   *
   * @param rating novo rating (0.0 a 5.0)
   */
  public void setRating(double rating) {
    this.rating = rating;
  }

  /**
   * Devolve uma representação textual do conteúdo.
   *
   * @return string com os campos principais
   */
  @Override
  public String toString() {
    return "Content{id='" + id + "', title='" + title + "', genre=" + genre
            + ", releaseDate=" + releaseDate + ", duration=" + duration
            + ", region='" + region + "', rating=" + rating + "}";
  }
}