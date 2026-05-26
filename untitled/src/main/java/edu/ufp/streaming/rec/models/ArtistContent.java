package edu.ufp.streaming.rec.models;

import edu.ufp.streaming.rec.enums.ArtistRole;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Representa a participação de um {@link Artist} num item de {@link Content}.
 *
 * <p>Esta é a classe de associação que liga artistas ao conteúdo em que participaram,
 * capturando a função desempenhada e a data da participação.
 *
 * <p>Na Fase 2, cada {@code ArtistContent} mapeia diretamente para uma aresta
 * pesada direcionada {@code Artista → Conteúdo} no grafo da plataforma.
 *
 * @param artist  O artista que participou no conteúdo.
 * @param content O item de conteúdo no qual o artista participou.
 * @param role    A função (papel) que o artista desempenhou neste conteúdo.
 * @param date    A data da participação (ex: data de lançamento ou data de filmagem).
 * @author Diogo Vicente
 */
public record ArtistContent(Artist artist, Content content, ArtistRole role, LocalDate date) implements Serializable {

  /**
   * Constrói um novo registo de participação ArtistContent.
   *
   * @param artist  o {@link Artist} participante
   * @param content o item de {@link Content}
   * @param role    a função ({@link ArtistRole}) que o artista desempenhou
   * @param date    a data da participação
   */
  public ArtistContent {
  }

  /**
   * Retorna o artista que participou.
   *
   * @return o {@link Artist}
   */
  @Override
  public Artist artist() {
    return artist;
  }

  /**
   * Retorna o item de conteúdo.
   *
   * @return o {@link Content}
   */
  @Override
  public Content content() {
    return content;
  }

  /**
   * Retorna a função que o artista desempenhou neste conteúdo.
   *
   * @return a {@link ArtistRole}
   */
  @Override
  public ArtistRole role() {
    return role;
  }

  /**
   * Retorna a data da participação.
   *
   * @return data da participação
   */
  @Override
  public LocalDate date() {
    return date;
  }

  @Override
  public String toString() {
    return "ArtistContent{artist=" + artist.getName()
            + ", content=" + content.getTitle()
            + ", role=" + role
            + ", date=" + date + "}";
  }
}