package edu.ufp.streaming.rec.models;

import java.time.LocalDate;

/**
 * Representa um documentário disponível na plataforma de streaming.
 * Herda de {@link Content} e adiciona o tema e o narrador.
 *
 * @author Pedro
 * @version 1.0
 */
public class Documentary extends Content {

  /** Tema principal do documentário. */
  private String topic;

  /** Nome do narrador do documentário. */
  private String narrator;

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
   * @param narrator    nome do narrador
   */
  public Documentary(String id, String title, Genre genre, LocalDate releaseDate,
                     int duration, String region, String topic, String narrator) {
    super(id, title, genre, releaseDate, duration, region);
    this.topic = topic;
    this.narrator = narrator;
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
   * Define o tema principal do documentário.
   *
   * @param topic novo tema
   */
  public void setTopic(String topic) {
    this.topic = topic;
  }

  /**
   * Devolve o nome do narrador do documentário.
   *
   * @return nome do narrador
   */
  public String getNarrator() {
    return narrator;
  }

  /**
   * Define o nome do narrador do documentário.
   *
   * @param narrator novo narrador
   */
  public void setNarrator(String narrator) {
    this.narrator = narrator;
  }

  /**
   * Devolve uma representação textual do documentário.
   *
   * @return string com informação do documentário
   */
  @Override
  public String toString() {
    return "Documentary{" + super.toString() + ", topic='" + topic
            + "', narrator='" + narrator + "'}";
  }
}