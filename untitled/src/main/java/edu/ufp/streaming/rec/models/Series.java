package edu.ufp.streaming.rec.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class Series extends Content implements Serializable {
/**
 * Representa uma série disponível na plataforma de streaming.
 * Herda de {@link Content} e adiciona temporadas e episódios.
 *
 * @author Pedro
 */

  /** Número de temporadas da série. */
  private int seasons;

  /** Lista de episódios da série. */
  private List<String> episodes;

  /**
   * Constrói uma nova série.
   *
   * @param id          identificador único
   * @param title       título da série
   * @param genre       género da série
   * @param releaseDate data de lançamento
   * @param duration    duração média por episódio em minutos
   * @param region      região de disponibilidade
   * @param seasons     número de temporadas
   */
  public Series(String id, String title, Genre genre, LocalDate releaseDate,
                int duration, String region, int seasons) {
    super(id, title, genre, releaseDate, duration, region);
    this.seasons = seasons;
    this.episodes = new ArrayList<>();
  }

  /**
   * Devolve o número de temporadas da série.
   *
   * @return número de temporadas
   */
  public int getSeasons() {
    return seasons;
  }

  /**
   * Define o número de temporadas da série.
   *
   * @param seasons novo número de temporadas
   */
  public void setSeasons(int seasons) {
    this.seasons = seasons;
  }

  /**
   * Devolve a lista de episódios da série.
   *
   * @return lista de episódios
   */
  public List<String> getEpisodes() {
    return episodes;
  }

  /**
   * Adiciona um episódio à série.
   *
   * @param episode nome ou identificador do episódio
   */
  public void addEpisode(String episode) {
    this.episodes.add(episode);
  }

  /**
   * Remove um episódio da série.
   *
   * @param episode nome ou identificador do episódio a remover
   */
  public void removeEpisode(String episode) {
    this.episodes.remove(episode);
  }

  /**
   * Devolve uma representação textual da série.
   *
   * @return string com informação da série
   */
  @Override
  public String toString() {
    return "Series{" + super.toString() + ", seasons=" + seasons
            + ", episodes=" + episodes + "}";
  }
}