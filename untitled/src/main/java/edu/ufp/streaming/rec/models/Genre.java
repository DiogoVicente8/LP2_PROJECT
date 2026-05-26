package edu.ufp.streaming.rec.models;

import java.io.Serializable;

/**
 * Representa um género de conteúdo multimédia da plataforma de streaming.
 *
 * @author Pedro
 * @version 1.0
 */
public class Genre implements Serializable {

  private final String id;
  private String name;

  public Genre(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public String getId() { return id; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Genre)) return false;
    return id.equals(((Genre) o).id);
  }

  @Override
  public int hashCode() { return id.hashCode(); }

  @Override
  public String toString() {
    return "Genre{id='" + id + "', name='" + name + "'}";
  }
}