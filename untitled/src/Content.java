import java.time.LocalDate;

public class Content {

  private String id;
  private String title;
  private Genre genre;
  private LocalDate releaseDate;
  private int duration;
  private String region;
  private double rating;

  public Content(String id, String title, Genre genre, LocalDate releaseDate,
                 int duration, String region) {
    this.id = id;
    this.title = title;
    this.genre = genre;
    this.releaseDate = releaseDate;
    this.duration = duration;
    this.region = region;
    this.rating = 0.0;
  }

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public Genre getGenre() {
    return genre;
  }

  public LocalDate getReleaseDate() {
    return releaseDate;
  }

  public int getDuration() {
    return duration;
  }

  public String getRegion() {
    return region;
  }

  public double getRating() {
    return rating;
  }

  public void setRating(double rating) {
    this.rating = rating;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setGenre(Genre genre) {
    this.genre = genre;
  }

  public void setReleaseDate(LocalDate releaseDate) {
    this.releaseDate = releaseDate;
  }

  public void setDuration(int duration) {
    this.duration = duration;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  @Override
  public String toString() {
    return "Content{" +
            "id='" + id + '\'' +
            ", title='" + title + '\'' +
            ", genre=" + genre +
            ", releaseDate=" + releaseDate +
            ", duration=" + duration +
            ", region='" + region + '\'' +
            ", rating=" + rating +
            '}';
  }
}