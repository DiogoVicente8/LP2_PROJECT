import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class User {

  private String id;
  private String name;
  private String email;
  private String region;
  private LocalDate registerDate;
  private List<Genre> preferences;
  private List<Content> watchHistory;
  private List<Interation> interactions;

  public User(String id, String name, String email, String region, LocalDate registerDate) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.region = region;
    this.registerDate = registerDate;
    this.preferences = new ArrayList<>();
    this.watchHistory = new ArrayList<>();
    this.interactions = new ArrayList<>();
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public String getRegion() {
    return region;
  }

  public LocalDate getRegisterDate() {
    return registerDate;
  }

  public List<Genre> getPreferences() {
    return preferences;
  }

  public List<Content> getWatchHistory() {
    return watchHistory;
  }

  public List<Interation> getInteractions() {
    return interactions;
  }

  public void addInteration(Interation interation) {
    this.interactions.add(interation);
  }

  public void follow(User u) {
    // lógica de seguir outro utilizador (via UserFollow)
  }

  public List<User> getFollowers() {
    return new ArrayList<>();
  }
}