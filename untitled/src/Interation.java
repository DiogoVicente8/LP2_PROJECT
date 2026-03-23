import java.time.LocalDateTime;

public class Interation {

  private User user;
  private Content content;
  private LocalDateTime watchDate;
  private double rating;
  private double progress;
  private InterationType type;

  public Interation(User user, Content content, InterationType type) {
    this.user = user;
    this.content = content;
    this.type = type;
    this.watchDate = LocalDateTime.now();
    this.rating = 0.0;
    this.progress = 0.0;
  }

  public User getUser() {
    return user;
  }

  public Content getContent() {
    return content;
  }

  public double getRating() {
    return rating;
  }

  public void setRating(double rating) {
    this.rating = rating;
  }

  public double getProgress() {
    return progress;
  }

  public void setProgress(double progress) {
    this.progress = progress;
  }

  public InterationType getType() {
    return type;
  }

  public LocalDateTime getWatchDate() {
    return watchDate;
  }
}