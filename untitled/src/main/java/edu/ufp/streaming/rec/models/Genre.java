package edu.ufp.streaming.rec.models;

/**
 * Representa um género de conteúdo multimédia da plataforma de streaming.
 *
 * @author Pedro
 * @version 1.0
 */
public class Genre {

  /** Identificador único do género. */
  private String id;

  /** Nome do género. */
  private String name;

  /**
   * Constrói um novo género.
   *
   * @param id   identificador único
   * @param name nome do género
   */
  public Genre(String id, String name) {
    this.id = id;
    this.name = name;
  }

  /**
   * Devolve o identificador único do género.
   *
   * @return id do género
   */
  public String getId() {
    return id;
  }

  /**
   * Devolve o nome do género.
   *
   * @return nome do género
   */
  public String getName() {
    return name;
  }

  /**
   * Define o nome do género.
   *
   * @param name novo nome
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Devolve uma representação textual do género.
   *
   * @return string com id e nome
   */
  @Override
  public String toString() {
    return "Genre{id='" + id + "', name='" + name + "'}";
  }
}