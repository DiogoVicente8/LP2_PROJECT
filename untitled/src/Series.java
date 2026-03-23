import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Series extends Content {

  private int seasons;
  private List<String> episodes;

  public Series(String id, String title, Genre genre, LocalDate releaseDate,
                int duration, String region, int seasons) {
    super(id, title, genre, releaseDate, duration, region);
    this.seasons = seasons;
    this.episodes = new ArrayList<>();
  }

  public int getSeasons() {
    return seasons;
  }

  public List<String> getEpisodes() {
    return episodes;
  }

  public void addEpisode(String episode) {
    this.episodes.add(episode);
  }
}