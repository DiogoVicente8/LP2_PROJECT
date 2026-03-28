package edu.ufp.streaming.rec.models;

import java.time.LocalDate;

public class Movie extends Content {

  private Artist director;

  public Movie(String id, String title, Genre genre, LocalDate releaseDate,
               int duration, String region, Artist director) {
    super(id, title, genre, releaseDate, duration, region);
    this.director = director;
  }

  public Artist getDirector() {
    return director;
  }

  public void play() {
    System.out.println("Playing movie: " + getTitle());
  }

  public void setDirector(Artist director) {
    this.director = director;
  }

  @Override
  public String toString() {
    return "edu.pt.lp2.edu.ufp.streaming.rec.models.Movie{" +
            "director=" + director +
            '}';
  }
}