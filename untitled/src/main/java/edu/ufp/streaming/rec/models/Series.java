package edu.ufp.streaming.rec.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

/**
 * Representa uma série disponível na plataforma de streaming.
 * Herda de {@link Content} e adiciona temporadas e episódios.
 *
 * @author Pedro
 */
public class Series extends Content implements Serializable {

  /** Número de temporadas da série. */
  private int seasons;

  /** Lista de episódios da série. */
  private final List<String> episodes;

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
    if (seasons <= 0) {
      throw new IllegalArgumentException("seasons deve ser positivo, recebido: " + seasons);
    }
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
   * Adiciona um novo episódio à série.
   * Não permite adicionar episódios vazios nem duplicados.
   *
   * @param episodeName nome do episódio a adicionar
   */
  public void addEpisode(String episodeName) {
    if (episodeName != null && !episodeName.trim().isEmpty() && !this.episodes.contains(episodeName)) {
      this.episodes.add(episodeName.trim());
    }
  }

  /**
   * Remove um episódio da série pelo seu nome.
   *
   * @param episodeName nome do episódio a remover
   */
  public void removeEpisode(String episodeName) {
    if (episodeName != null) {
      this.episodes.remove(episodeName);
    }
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