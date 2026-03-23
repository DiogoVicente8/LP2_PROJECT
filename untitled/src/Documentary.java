import java.time.LocalDate;

public class Documentary extends Content {

  private String topic;
  private String narrator;

  public Documentary(String id, String title, Genre genre, LocalDate releaseDate,
                     int duration, String region, String topic, String narrator) {
    super(id, title, genre, releaseDate, duration, region);
    this.topic = topic;
    this.narrator = narrator;
  }

  public String getTopic() {
    return topic;
  }

  public String getNarrator() {
    return narrator;
  }
}}