package edu.ufp.streaming.rec.models;

import edu.ufp.streaming.rec.enums.ArtistRole;

import java.time.LocalDate;

/**
 * Representa a participação de um {@link Artist} num item de {@link Content}.
 *
 * <p>Esta é a classe de associação que liga artistas ao conteúdo em que participaram,
 * capturando a função desempenhada (ex: ACTOR, DIRECTOR) e a data da participação.
 *
 * <p>Na Fase 2, cada {@code ArtistContent} mapeia diretamente para uma aresta
 * pesada direcionada {@code Artista → Conteúdo} no grafo da plataforma.
 * @author  Diogo Vicente
 */
public class ArtistContent {

  /** O artista que participou no conteúdo. */
  private Artist artist;

  /** O item de conteúdo no qual o artista participou. */
  private Content content;

  /** A função (papel) que o artista desempenhou neste conteúdo. */
  private ArtistRole role;

  /** A data da participação (ex: data de lançamento ou data de filmagem). */
  private LocalDate date;

  /**
   * Constrói um novo registo de participação ArtistContent.
   *
   * @param artist  o {@link Artist} participante
   * @param content o item de {@link Content}
   * @param role    a função ({@link ArtistRole}) que o artista desempenhou
   * @param date    a data da participação
   */
  public ArtistContent(Artist artist, Content content, ArtistRole role, LocalDate date) {
    this.artist = artist;
    this.content = content;
    this.role = role;
    this.date = date;
  }

  /**
   * Retorna o artista que participou.
   *
   * @return o {@link Artist}
   */
  public Artist getArtist() { return artist; }

  /**
   * Retorna o item de conteúdo.
   *
   * @return o {@link Content}
   */
  public Content getContent() { return content; }

  /**
   * Retorna a função que o artista desempenhou neste conteúdo.
   *
   * @return a {@link ArtistRole}
   */
  public ArtistRole getRole() { return role; }

  /**
   * Retorna a data da participação.
   *
   * @return data da participação
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