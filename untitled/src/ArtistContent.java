import java.time.LocalDate;

public class ArtistContent {

  private Artist artist;
  private Content content;
  private ArtistRole role;
  private LocalDate date;

  public ArtistContent(Artist artist, Content content, ArtistRole role, LocalDate date) {
    this.artist = artist;
    this.content = content;
    this.role = role;
    this.date = date;
  }

  public Artist getArtist() {
    return artist;
  }

  public Content getContent() {
    return content;
  }

  public ArtistRole getRole() {
    return role;
  }

  public LocalDate getDate() {
    return date;
  }
}