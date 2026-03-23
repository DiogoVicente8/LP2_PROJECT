import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Artist {

  private String id;
  private String name;
  private String nationality;
  private String gender;
  private LocalDate birthDate;
  private ArtistRole role;
  private List<ArtistContent> participates;

  public Artist(String id, String name, String nationality, String gender,
                LocalDate birthDate, ArtistRole role) {
    this.id = id;
    this.name = name;
    this.nationality = nationality;
    this.gender = gender;
    this.birthDate = birthDate;
    this.role = role;
    this.participates = new ArrayList<>();
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getNationality() {
    return nationality;
  }

  public String getGender() {
    return gender;
  }

  public LocalDate getBirthDate() {
    return birthDate;
  }

  public ArtistRole getRole() {
    return role;
  }

  public List<ArtistContent> getFilmography() {
    return participates;
  }

  public void addParticipation(ArtistContent ac) {
    this.participates.add(ac);
  }
}