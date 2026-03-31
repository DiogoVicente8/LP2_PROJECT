package edu.ufp.streaming.rec.models;

import java.time.LocalDate;

/**
 * Representa um filme disponível na plataforma de streaming.
 * Herda de {@link Content} e adiciona o realizador do filme.
 *
 * @author Pedro
 * @version 1.0
 */
public class Movie extends Content {

  /** Realizador do filme. */
  private Artist director;

  /**
   * Constrói um novo filme.
   *
   * @param id          identificador único
   * @param title       título do filme
   * @param genre       género do filme
   * @param releaseDate data de lançamento
   * @param duration    duração em minutos
   * @param region      região de disponibilidade
   * @param director    realizador do filme
   */
  public Movie(String id, String title, Genre genre, LocalDate releaseDate,
               int duration, String region, Artist director) {
    super(id, title, genre, releaseDate, duration, region);
    this.director = director;
  }

  /**
   * Devolve o realizador do filme.
   *
   * @return realizador
   */
  public Artist getDirector() {
    return director;
  }

  /**
   * Define o realizador do filme.
   *
   * @param director novo realizador
   */
  public void setDirector(Artist director) {
    this.director = director;
  }

  /**
   * Simula a reprodução do filme.
   */
  public void play() {
    System.out.println("A reproduzir filme: " + getTitle());
  }

  /**
   * Devolve uma representação textual do filme.
   *
   * @return string com informação do filme e realizador
   */
  @Override
  public String toString() {
    return "Movie{" + super.toString() + ", director=" + director + "}";
  }
}