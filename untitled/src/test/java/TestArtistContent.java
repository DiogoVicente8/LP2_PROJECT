import edu.ufp.streaming.rec.enums.ArtistRole;
import edu.ufp.streaming.rec.managers.ArtistContentManager;
import edu.ufp.streaming.rec.managers.ArtistManager;
import edu.ufp.streaming.rec.models.Artist;
import edu.ufp.streaming.rec.models.ArtistContent;
import edu.ufp.streaming.rec.models.Genre;
import edu.ufp.streaming.rec.models.Movie;

import java.time.LocalDate;
import java.util.List;


public class TestArtistContent {

    // -----------------------------------------------------------------------
    // Entry point
    // -----------------------------------------------------------------------

    /**
     * Runs all edu.ufp.streaming.rec.models.ArtistContent test cases.
     *
     * @param args not used
     */
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println(" TestArtistContent — Phase 1 Test Suite");
        System.out.println("========================================\n");

        testAddAndGetFilmography();
        testDuplicateParticipation();
        testGetCastAndCrew();
        testGetFilmographyByRole();
        testGetFilmographyByDateRange();
        testGetAllByDateRange();
        testGetDirectorsAndActors();
        testHasParticipation();
        testRemoveParticipation();
        testRemoveAllByArtist();
        testRemoveAllByContent();
        testR4ConsistencyArtistRemoval();

        System.out.println("========================================");
        System.out.println(" All edu.ufp.streaming.rec.models.ArtistContent tests completed.");
        System.out.println("========================================");
    }

    // -----------------------------------------------------------------------
    // Fixture helpers
    // -----------------------------------------------------------------------

    /** Builds a minimal edu.ufp.streaming.rec.models.Genre for use in edu.ufp.streaming.rec.models.Content. */
    private static Genre genre(String id, String name) {
        return new Genre(id, name);
    }

    /** Builds a edu.ufp.streaming.rec.models.Movie (edu.ufp.streaming.rec.models.Content subclass) without a director set via edu.ufp.streaming.rec.managers.ArtistContentManager. */
    private static Movie movie(String id, String title, Genre g, LocalDate date) {
        return new Movie(id, title, g, date, 120, "PT", null);
    }

    /** Builds an edu.ufp.streaming.rec.models.Artist. */
    private static Artist artist(String id, String name, ArtistRole role) {
        return new Artist(id, name, "PT", "M", LocalDate.of(1980, 1, 1), role);
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    /**
     * Tests adding a participation and retrieving the filmography.
     */
    public static void testAddAndGetFilmography() {
        System.out.println("--- testAddAndGetFilmography ---");
        ArtistContentManager acm = new ArtistContentManager();

        Artist a1 = artist("a1", "Actor One", ArtistRole.ACTOR);
        Genre  g1 = genre("g1", "Action");
        Movie  m1 = movie("m1", "Film Alpha", g1, LocalDate.of(2020, 5, 1));

        ArtistContent ac = acm.addParticipation(a1, m1, ArtistRole.ACTOR, LocalDate.of(2020, 5, 1));
        assert ac != null : "Should create participation";
        assert acm.size() == 1 : "Size should be 1";

        List<ArtistContent> filmography = acm.getFilmography("a1");
        assert filmography.size() == 1 : "Filmography should have 1 entry";
        assert filmography.get(0).getContent().getId().equals("m1") : "edu.ufp.streaming.rec.models.Content should be m1";

        // edu.ufp.streaming.rec.models.Artist's internal list should also be updated
        assert a1.getFilmography().size() == 1 : "edu.ufp.streaming.rec.models.Artist internal filmography should be updated";

        System.out.println("PASS: addParticipation / getFilmography\n");
    }

    /**
     * Tests that duplicate participations (same artist, content, role) are rejected.
     */
    public static void testDuplicateParticipation() {
        System.out.println("--- testDuplicateParticipation ---");
        ArtistContentManager acm = new ArtistContentManager();

        Artist a1 = artist("a1", "Actor One", ArtistRole.ACTOR);
        Movie  m1 = movie("m1", "Film Alpha", genre("g1", "Action"), LocalDate.of(2020, 5, 1));

        acm.addParticipation(a1, m1, ArtistRole.ACTOR, LocalDate.of(2020, 5, 1));
        ArtistContent dup = acm.addParticipation(a1, m1, ArtistRole.ACTOR, LocalDate.of(2020, 5, 1));

        assert dup == null : "Duplicate participation should return null";
        assert acm.size() == 1 : "Size should remain 1";

        // Same artist, different role — should be allowed
        ArtistContent diff = acm.addParticipation(a1, m1, ArtistRole.DIRECTOR, LocalDate.of(2020, 5, 1));
        assert diff != null : "Same artist, different role should be allowed";
        assert acm.size() == 2 : "Size should be 2";

        System.out.println("PASS: duplicate rejection / different role allowed\n");
    }

    /**
     * Tests getCastAndCrew — all artists linked to a content item.
     */
    public static void testGetCastAndCrew() {
        System.out.println("--- testGetCastAndCrew ---");
        ArtistContentManager acm = new ArtistContentManager();
        Genre g1 = genre("g1", "Drama");
        Movie m1 = movie("m1", "Film Beta", g1, LocalDate.of(2021, 3, 10));

        Artist a1 = artist("a1", "Actor A", ArtistRole.ACTOR);
        Artist a2 = artist("a2", "Actor B", ArtistRole.ACTOR);
        Artist a3 = artist("a3", "Director C", ArtistRole.DIRECTOR);

        acm.addParticipation(a1, m1, ArtistRole.ACTOR,    LocalDate.of(2021, 3, 10));
        acm.addParticipation(a2, m1, ArtistRole.ACTOR,    LocalDate.of(2021, 3, 10));
        acm.addParticipation(a3, m1, ArtistRole.DIRECTOR, LocalDate.of(2021, 3, 10));

        List<ArtistContent> crew = acm.getCastAndCrew("m1");
        assert crew.size() == 3 : "Cast+crew should have 3 entries (got " + crew.size() + ")";

        List<ArtistContent> empty = acm.getCastAndCrew("m99");
        assert empty.isEmpty() : "Unknown content should return empty list";

        System.out.println("PASS: getCastAndCrew\n");
    }

    /**
     * Tests getFilmographyByRole filtering.
     */
    public static void testGetFilmographyByRole() {
        System.out.println("--- testGetFilmographyByRole ---");
        ArtistContentManager acm = new ArtistContentManager();
        Genre g1 = genre("g1", "Action");

        Artist a1 = artist("a1", "Multi-talent", ArtistRole.ACTOR);
        Movie  m1 = movie("m1", "Film 1", g1, LocalDate.of(2019, 1, 1));
        Movie  m2 = movie("m2", "Film 2", g1, LocalDate.of(2020, 1, 1));
        Movie  m3 = movie("m3", "Film 3", g1, LocalDate.of(2021, 1, 1));

        acm.addParticipation(a1, m1, ArtistRole.ACTOR,    LocalDate.of(2019, 1, 1));
        acm.addParticipation(a1, m2, ArtistRole.ACTOR,    LocalDate.of(2020, 1, 1));
        acm.addParticipation(a1, m3, ArtistRole.DIRECTOR, LocalDate.of(2021, 1, 1));

        List<ArtistContent> asActor = acm.getFilmographyByRole("a1", ArtistRole.ACTOR);
        assert asActor.size() == 2 : "Should find 2 actor roles";

        List<ArtistContent> asDirector = acm.getFilmographyByRole("a1", ArtistRole.DIRECTOR);
        assert asDirector.size() == 1 : "Should find 1 director role";

        System.out.println("PASS: getFilmographyByRole\n");
    }

    /**
     * Tests getFilmographyByDateRange for a specific artist.
     */
    public static void testGetFilmographyByDateRange() {
        System.out.println("--- testGetFilmographyByDateRange ---");
        ArtistContentManager acm = new ArtistContentManager();
        Genre g1 = genre("g1", "Thriller");

        Artist a1 = artist("a1", "Veteran Actor", ArtistRole.ACTOR);
        Movie m1 = movie("m1", "Old Film",    g1, LocalDate.of(2000, 1, 1));
        Movie m2 = movie("m2", "Recent Film", g1, LocalDate.of(2022, 6, 15));
        Movie m3 = movie("m3", "New Film",    g1, LocalDate.of(2023, 3, 20));

        acm.addParticipation(a1, m1, ArtistRole.ACTOR, LocalDate.of(2000, 1, 1));
        acm.addParticipation(a1, m2, ArtistRole.ACTOR, LocalDate.of(2022, 6, 15));
        acm.addParticipation(a1, m3, ArtistRole.ACTOR, LocalDate.of(2023, 3, 20));

        List<ArtistContent> recent = acm.getFilmographyByDateRange(
                "a1", LocalDate.of(2022, 1, 1), LocalDate.of(2023, 12, 31));
        assert recent.size() == 2 : "Expected 2 recent participations (got " + recent.size() + ")";

        List<ArtistContent> old = acm.getFilmographyByDateRange(
                "a1", LocalDate.of(1990, 1, 1), LocalDate.of(2005, 12, 31));
        assert old.size() == 1 : "Expected 1 old participation";

        System.out.println("PASS: getFilmographyByDateRange\n");
    }

    /**
     * Tests getAllByDateRange across all artists.
     */
    public static void testGetAllByDateRange() {
        System.out.println("--- testGetAllByDateRange ---");
        ArtistContentManager acm = new ArtistContentManager();
        Genre g1 = genre("g1", "Comedy");

        Artist a1 = artist("a1", "Actor A", ArtistRole.ACTOR);
        Artist a2 = artist("a2", "Actor B", ArtistRole.ACTOR);
        Movie  m1 = movie("m1", "Film X", g1, LocalDate.of(2021, 4, 10));
        Movie  m2 = movie("m2", "Film Y", g1, LocalDate.of(2022, 8, 20));
        Movie  m3 = movie("m3", "Film Z", g1, LocalDate.of(2015, 1, 1));

        acm.addParticipation(a1, m1, ArtistRole.ACTOR, LocalDate.of(2021, 4, 10));
        acm.addParticipation(a2, m2, ArtistRole.ACTOR, LocalDate.of(2022, 8, 20));
        acm.addParticipation(a1, m3, ArtistRole.ACTOR, LocalDate.of(2015, 1, 1));

        List<ArtistContent> range = acm.getAllByDateRange(
                LocalDate.of(2020, 1, 1), LocalDate.of(2023, 12, 31));
        assert range.size() == 2 : "Expected 2 participations in 2020-2023 (got " + range.size() + ")";

        System.out.println("PASS: getAllByDateRange\n");
    }

    /**
     * Tests getDirectors and getActors convenience methods.
     */
    public static void testGetDirectorsAndActors() {
        System.out.println("--- testGetDirectorsAndActors ---");
        ArtistContentManager acm = new ArtistContentManager();
        Genre g1 = genre("g1", "Sci-Fi");
        Movie m1 = movie("m1", "Space Epic", g1, LocalDate.of(2023, 7, 4));

        Artist dir  = artist("d1", "Big Director", ArtistRole.DIRECTOR);
        Artist act1 = artist("a1", "Lead Actor",   ArtistRole.ACTOR);
        Artist act2 = artist("a2", "Support Actor", ArtistRole.ACTOR);

        acm.addParticipation(dir,  m1, ArtistRole.DIRECTOR, LocalDate.of(2023, 7, 4));
        acm.addParticipation(act1, m1, ArtistRole.ACTOR,    LocalDate.of(2023, 7, 4));
        acm.addParticipation(act2, m1, ArtistRole.ACTOR,    LocalDate.of(2023, 7, 4));

        List<Artist> directors = acm.getDirectors("m1");
        assert directors.size() == 1 && directors.get(0).getId().equals("d1")
                : "Should find 1 director";

        List<Artist> actors = acm.getActors("m1");
        assert actors.size() == 2 : "Should find 2 actors";

        System.out.println("PASS: getDirectors / getActors\n");
    }

    /**
     * Tests hasParticipation check.
     */
    public static void testHasParticipation() {
        System.out.println("--- testHasParticipation ---");
        ArtistContentManager acm = new ArtistContentManager();
        Genre g1 = genre("g1", "Horror");
        Artist a1 = artist("a1", "Horror Star", ArtistRole.ACTOR);
        Movie  m1 = movie("m1", "Scary Film", g1, LocalDate.of(2022, 10, 31));

        acm.addParticipation(a1, m1, ArtistRole.ACTOR, LocalDate.of(2022, 10, 31));

        assert  acm.hasParticipation("a1", "m1") : "Should have participation";
        assert !acm.hasParticipation("a1", "m99") : "Should not have participation for unknown content";
        assert !acm.hasParticipation("a99", "m1") : "Should not have participation for unknown artist";

        System.out.println("PASS: hasParticipation\n");
    }

    /**
     * Tests removing a specific participation record.
     */
    public static void testRemoveParticipation() {
        System.out.println("--- testRemoveParticipation ---");
        ArtistContentManager acm = new ArtistContentManager();
        Genre g1 = genre("g1", "Drama");
        Artist a1 = artist("a1", "Dramatic Actor", ArtistRole.ACTOR);
        Movie  m1 = movie("m1", "Drama Film", g1, LocalDate.of(2021, 2, 14));

        acm.addParticipation(a1, m1, ArtistRole.ACTOR, LocalDate.of(2021, 2, 14));
        assert acm.size() == 1;

        ArtistContent removed = acm.removeParticipation("a1", "m1", ArtistRole.ACTOR);
        assert removed != null : "Should return removed record";
        assert acm.size() == 0 : "Size should be 0 after removal";
        assert acm.getFilmography("a1").isEmpty() : "Filmography index should be empty";
        assert acm.getCastAndCrew("m1").isEmpty() : "edu.ufp.streaming.rec.models.Content index should be empty";

        ArtistContent notFound = acm.removeParticipation("a1", "m1", ArtistRole.ACTOR);
        assert notFound == null : "Removing again should return null";

        System.out.println("PASS: removeParticipation\n");
    }

    /**
     * Tests removeAllByArtist — removes all participations for a given artist.
     */
    public static void testRemoveAllByArtist() {
        System.out.println("--- testRemoveAllByArtist ---");
        ArtistContentManager acm = new ArtistContentManager();
        Genre g1 = genre("g1", "Action");
        Artist a1 = artist("a1", "Prolific Actor", ArtistRole.ACTOR);
        Movie  m1 = movie("m1", "Film 1", g1, LocalDate.of(2020, 1, 1));
        Movie m2 = movie("m2", "Film 2", g1, LocalDate.of(2021, 1, 1));
        Movie  m3 = movie("m3", "Film 3", g1, LocalDate.of(2022, 1, 1));

        acm.addParticipation(a1, m1, ArtistRole.ACTOR, LocalDate.of(2020, 1, 1));
        acm.addParticipation(a1, m2, ArtistRole.ACTOR, LocalDate.of(2021, 1, 1));
        acm.addParticipation(a1, m3, ArtistRole.ACTOR, LocalDate.of(2022, 1, 1));
        assert acm.size() == 3;

        acm.removeAllByArtist("a1");
        assert acm.size() == 0 : "All participations should be removed";
        assert acm.getFilmography("a1").isEmpty() : "Filmography should be empty";
        // edu.ufp.streaming.rec.models.Content indices should also be cleaned
        assert acm.getCastAndCrew("m1").isEmpty() : "m1 cast should be empty";
        assert acm.getCastAndCrew("m2").isEmpty() : "m2 cast should be empty";

        System.out.println("PASS: removeAllByArtist\n");
    }

    /**
     * Tests removeAllByContent — removes all participations for a given content item.
     */
    public static void testRemoveAllByContent() {
        System.out.println("--- testRemoveAllByContent ---");
        ArtistContentManager acm = new ArtistContentManager();
        Genre g1 = genre("g1", "Adventure");
        Movie m1 = movie("m1", "Big Film", g1, LocalDate.of(2023, 5, 5));

        Artist a1 = artist("a1", "Actor 1", ArtistRole.ACTOR);
        Artist a2 = artist("a2", "Actor 2", ArtistRole.ACTOR);
        Artist a3 = artist("a3", "Director", ArtistRole.DIRECTOR);

        acm.addParticipation(a1, m1, ArtistRole.ACTOR,    LocalDate.of(2023, 5, 5));
        acm.addParticipation(a2, m1, ArtistRole.ACTOR,    LocalDate.of(2023, 5, 5));
        acm.addParticipation(a3, m1, ArtistRole.DIRECTOR, LocalDate.of(2023, 5, 5));
        assert acm.size() == 3;

        acm.removeAllByContent("m1");
        assert acm.size() == 0 : "All participations should be removed";
        assert acm.getCastAndCrew("m1").isEmpty() : "edu.ufp.streaming.rec.models.Content index should be empty";
        assert acm.getFilmography("a1").isEmpty() : "edu.ufp.streaming.rec.models.Artist index should be cleaned";

        System.out.println("PASS: removeAllByContent\n");
    }

    /**
     * Tests R4 consistency: when an artist is removed from edu.ufp.streaming.rec.managers.ArtistManager,
     * their participations are also cleaned up via edu.ufp.streaming.rec.managers.ArtistContentManager.
     */
    public static void testR4ConsistencyArtistRemoval() {
        System.out.println("--- testR4ConsistencyArtistRemoval ---");
        ArtistManager am = new ArtistManager();
        ArtistContentManager acm = new ArtistContentManager();

        Genre g1 = genre("g1", "Drama");
        Artist a1 = new Artist("a1", "Temp edu.ufp.streaming.rec.models.Artist", "PT", "M",
                LocalDate.of(1985, 3, 20), ArtistRole.ACTOR);
        Movie m1 = movie("m1", "Some Film", g1, LocalDate.of(2022, 4, 1));

        am.insert(a1);
        acm.addParticipation(a1, m1, ArtistRole.ACTOR, LocalDate.of(2022, 4, 1));

        // Simulate artist deletion: clean participations first, then remove from ST
        acm.removeAllByArtist("a1");
        am.remove("a1");

        assert am.get("a1") == null : "edu.ufp.streaming.rec.models.Artist should be removed from ST";
        assert acm.getFilmography("a1").isEmpty() : "Filmography should be empty";
        assert acm.getCastAndCrew("m1").isEmpty() : "edu.ufp.streaming.rec.models.Content crew should be empty";

        System.out.println("PASS: R4 consistency on artist removal\n");
    }
}
