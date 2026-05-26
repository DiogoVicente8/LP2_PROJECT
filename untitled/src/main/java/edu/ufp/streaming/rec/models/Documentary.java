package edu.ufp.streaming.rec.models;

import java.time.LocalDate;
import java.io.Serializable;
/**
 * Representa um documentário disponível na plataforma de streaming.
 * Herda de {@link Content} e adiciona o tema.
 *
 * @author Pedro
 */
public class Documentary extends Content implements Serializable {

  /** Tema principal do documentário. */
  private final String topic;

  /**
   * Constrói um novo documentário.
   *
   * @param id          identificador único
   * @param title       título do documentário
   * @param genre       género do documentário
   * @param releaseDate data de lançamento
   * @param duration    duração em minutos
   * @param region      região de disponibilidade
   * @param topic       tema principal
   */
  public Documentary(String id, String title, Genre genre, LocalDate releaseDate,
                     int duration, String region, String topic) {
    super(id, title, genre, releaseDate, duration, region);
    this.topic = topic;
  }

  /**
   * Devolve o tema principal do documentário.
   *
   * @return tema
   */
  public String getTopic() {
    return topic;
  }


  /**
   * Devolve uma representação textual do documentário.
   *
   * @return string com informação do documentário
   */
  @Override
  public String toString() {
    return "Documentary{" + super.toString() + ", topic='" + topic + "'}";
  }
}