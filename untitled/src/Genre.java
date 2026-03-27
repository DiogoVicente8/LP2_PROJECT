public class Genre {

  private String id;
  private String name;

  public Genre(String id, String name) {
    this.id = id;
    this.name = name;
  }
  public String getName() {
    return name;
  }
  public String getId() {
    return id;
  }
  public void setName(String name) {
    this.name = name;
  }
  @java.lang.Override
  public java.lang.String toString() {
    return "Genre{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            '}';
  }
}