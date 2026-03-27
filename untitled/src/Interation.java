import java.time.LocalDateTime;

public class Interation {

  private User user;
  private Content content;
  private LocalDateTime watchDate;
  private double rating;
  private double progress;
  private InterationType type;
  private String id;

  public Interation(User user, Content content, LocalDateTime watchDate, double rating, double progress, InterationType type, String id) {
    this.user = user;
    this.content = content;
    this.watchDate = watchDate;
    this.rating = rating;
    this.progress = progress;
    this.type = type;
    this.id = id;
  }

  public User getUser() {
    return user;
  }

  public Content getContent() {
    return content;
  }

  public LocalDateTime getWatchDate() {
    return watchDate;
  }

  public double getRating() {
    return rating;
  }

  public double getProgress() {
    return progress;
  }

  public InterationType getType() {
    return type;
  }

  public String getId() {
    return id;
  }

  public void setWatchDate(LocalDateTime watchDate) {
    this.watchDate = watchDate;
  }

  public void setRating(double rating) {
    this.rating = rating;
  }

  public void setProgress(double progress) {
    this.progress = progress;
  }

  public void setType(InterationType type) {
    this.type = type;
  }


  @Override
  public String toString() {
    return "Interation{" +
            "user=" + user +
            ", content=" + content +
            ", watchDate=" + watchDate +
            ", rating=" + rating +
            ", progress=" + progress +
            ", type=" + type +
            ", id='" + id + '\'' +
            '}';
  }
}