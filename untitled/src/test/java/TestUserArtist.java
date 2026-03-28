import edu.ufp.streaming.rec.enums.ArtistRole;
import edu.ufp.streaming.rec.managers.ArtistManager;
import edu.ufp.streaming.rec.managers.FollowManager;
import edu.ufp.streaming.rec.managers.UserManager;
import edu.ufp.streaming.rec.models.Artist;
import edu.ufp.streaming.rec.models.Genre;
import edu.ufp.streaming.rec.models.User;
import edu.ufp.streaming.rec.models.UserFollow;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TestUserArtist {

    // -----------------------------------------------------------------------
    // Entry point
    // -----------------------------------------------------------------------

    /**
     * Runs all test cases in sequence.
     *
     * @param args not used
     */
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println(" TestUserArtist — Phase 1 Test Suite");
        System.out.println("========================================\n");

        testUserManagerInsertAndGet();
        testUserManagerRemove();
        testUserManagerEdit();
        testUserManagerSearchByDate();
        testUserManagerSearchByDateRange();
        testUserManagerSearchByNameSubstring();
        testUserManagerSearchByRegion();
        testUserManagerSearchByRegionAndDateRange();
        testUserManagerSearchByNameSubstringAndRegion();
        testUserManagerSearchByPreferredGenre();
        testArtistManagerInsertAndGet();
        testArtistManagerRemove();
        testArtistManagerEdit();
        testArtistManagerSearchByBirthDateRange();
        testArtistManagerSearchByNameSubstring();
        testArtistManagerSearchByNationality();
        testArtistManagerSearchByGender();
        testArtistManagerSearchByNationalityAndBirthDateRange();
        testArtistManagerSearchByNameSubstringNationalityGender();
        testArtistManagerSearchByRole();
        testFollowManagerFollowAndUnfollow();
        testFollowManagerGetFollowersAndFollowing();
        testFollowManagerConsistencyOnUserRemoval();
        testFollowManagerDateRange();

        System.out.println("\n========================================");
        System.out.println(" All tests completed.");
        System.out.println("========================================");
    }

    // -----------------------------------------------------------------------
    // Shared fixture helpers
    // -----------------------------------------------------------------------

    /** Creates a populated {@link UserManager} with 5 sample users. */
    private static UserManager buildUserManager() {
        UserManager um = new UserManager();
        Genre g1 = new Genre("g1", "Action");
        Genre g2 = new Genre("g2", "Drama");

        User u1 = new User("u1", "Alice Silva",  "alice@mail.com",  "PT", LocalDate.of(2020, 1, 10));
        User u2 = new User("u2", "Bruno Costa",  "bruno@mail.com",  "PT", LocalDate.of(2020, 3, 15));
        User u3 = new User("u3", "Carla Pereira", "carla@mail.com", "BR", LocalDate.of(2021, 6, 20));
        User u4 = new User("u4", "David Alves",  "david@mail.com",  "US", LocalDate.of(2022, 9,  5));
        User u5 = new User("u5", "Eva Martins",  "eva@mail.com",    "PT", LocalDate.of(2023, 2,  1));

        u1.addPreference(g1);
        u2.addPreference(g1);
        u3.addPreference(g2);

        um.insert(u1); um.insert(u2); um.insert(u3); um.insert(u4); um.insert(u5);
        return um;
    }

    /** Creates a populated {@link ArtistManager} with 5 sample artists. */
    private static ArtistManager buildArtistManager() {
        ArtistManager am = new ArtistManager();

        am.insert(new Artist("a1", "Leonardo DiCaprio", "US", "M", LocalDate.of(1974, 11, 11), ArtistRole.ACTOR));
        am.insert(new Artist("a2", "Meryl Streep",       "US", "F", LocalDate.of(1949,  6, 22), ArtistRole.ACTOR));
        am.insert(new Artist("a3", "Christopher Nolan",  "GB", "M", LocalDate.of(1970,  7, 30), ArtistRole.DIRECTOR));
        am.insert(new Artist("a4", "Sofia Coppola",      "US", "F", LocalDate.of(1971,  5, 14), ArtistRole.DIRECTOR));
        am.insert(new Artist("a5", "Joaquin Phoenix",    "US", "M", LocalDate.of(1974, 10, 28), ArtistRole.ACTOR));
        return am;
    }

    // -----------------------------------------------------------------------
    // edu.ufp.streaming.rec.managers.UserManager tests
    // -----------------------------------------------------------------------

    /**
     * Tests insert and get operations on {@link UserManager}.
     */
    public static void testUserManagerInsertAndGet() {
        System.out.println("--- testUserManagerInsertAndGet ---");
        UserManager um = buildUserManager();

        assert um.size() == 5 : "Expected 5 users";
        assert um.get("u1").getName().equals("Alice Silva") : "u1 should be Alice Silva";
        assert um.get("u99") == null : "Unknown ID should return null";

        // Duplicate insert should fail
        User dup = new User("u1", "Duplicate", "dup@mail.com", "PT", LocalDate.now());
        assert !um.insert(dup) : "Duplicate insert should return false";

        System.out.println("PASS: insert/get/duplicate\n");
    }

    /**
     * Tests remove from {@link UserManager} and BST consistency.
     */
    public static void testUserManagerRemove() {
        System.out.println("--- testUserManagerRemove ---");
        UserManager um = buildUserManager();

        User removed = um.remove("u3");
        assert removed != null && removed.getId().equals("u3") : "Should return removed user";
        assert um.size() == 4 : "Size should be 4 after removal";
        assert um.get("u3") == null : "Removed user should not be retrievable";

        // BST should no longer contain u3
        List<User> byDate = um.searchByRegisterDate(LocalDate.of(2021, 6, 20));
        assert byDate.stream().noneMatch(u -> u.getId().equals("u3"))
                : "Date index should not contain removed user";

        assert um.remove("u99") == null : "Removing non-existent user should return null";
        System.out.println("PASS: remove + BST consistency\n");
    }

    /**
     * Tests edit operations on {@link UserManager}.
     */
    public static void testUserManagerEdit() {
        System.out.println("--- testUserManagerEdit ---");
        UserManager um = buildUserManager();

        assert um.editName("u1", "Alice Ferreira") : "Edit name should succeed";
        assert um.get("u1").getName().equals("Alice Ferreira") : "Name should be updated";

        // Name BST should reflect the change
        List<User> found = um.searchByNameSubstring("ferreira");
        assert found.stream().anyMatch(u -> u.getId().equals("u1"))
                : "Name BST should reflect renamed user";

        // Old name should not appear
        List<User> old = um.searchByNameSubstring("alice silva");
        assert old.stream().noneMatch(u -> u.getId().equals("u1"))
                : "Old name should no longer be indexed";

        assert !um.editName("u99", "Ghost") : "Edit on missing user should return false";
        System.out.println("PASS: editName + BST re-index\n");
    }

    /**
     * Tests exact-date search on the registration date BST.
     */
    public static void testUserManagerSearchByDate() {
        System.out.println("--- testUserManagerSearchByDate ---");
        UserManager um = buildUserManager();

        List<User> result = um.searchByRegisterDate(LocalDate.of(2020, 1, 10));
        assert result.size() == 1 && result.get(0).getId().equals("u1")
                : "Should find exactly u1 on 2020-01-10";

        List<User> empty = um.searchByRegisterDate(LocalDate.of(1999, 1, 1));
        assert empty.isEmpty() : "No users should be found on 1999-01-01";

        System.out.println("PASS: searchByRegisterDate\n");
    }

    /**
     * Tests date-range search on the registration date BST.
     */
    public static void testUserManagerSearchByDateRange() {
        System.out.println("--- testUserManagerSearchByDateRange ---");
        UserManager um = buildUserManager();

        // Range covers u1 (Jan 2020) and u2 (Mar 2020)
        List<User> result = um.searchByRegisterDateRange(
                LocalDate.of(2020, 1, 1), LocalDate.of(2020, 12, 31));
        assert result.size() == 2 : "Expected 2 users in 2020 (got " + result.size() + ")";

        // Range covers no users
        List<User> empty = um.searchByRegisterDateRange(
                LocalDate.of(2018, 1, 1), LocalDate.of(2019, 12, 31));
        assert empty.isEmpty() : "No users registered in 2018-2019";

        System.out.println("PASS: searchByRegisterDateRange\n");
    }

    /**
     * Tests name-substring search on the name BST.
     */
    public static void testUserManagerSearchByNameSubstring() {
        System.out.println("--- testUserManagerSearchByNameSubstring ---");
        UserManager um = buildUserManager();

        List<User> result = um.searchByNameSubstring("silva");
        assert result.size() == 1 && result.get(0).getId().equals("u1")
                : "Should find Alice Silva";

        List<User> noMatch = um.searchByNameSubstring("xyz");
        assert noMatch.isEmpty() : "Should find nobody for 'xyz'";

        System.out.println("PASS: searchByNameSubstring\n");
    }

    /**
     * Tests region filter.
     */
    public static void testUserManagerSearchByRegion() {
        System.out.println("--- testUserManagerSearchByRegion ---");
        UserManager um = buildUserManager();

        List<User> pt = um.searchByRegion("PT");
        assert pt.size() == 3 : "Expected 3 PT users (got " + pt.size() + ")";

        List<User> br = um.searchByRegion("BR");
        assert br.size() == 1 : "Expected 1 BR user";

        System.out.println("PASS: searchByRegion\n");
    }

    /**
     * Tests combined region + date-range search.
     */
    public static void testUserManagerSearchByRegionAndDateRange() {
        System.out.println("--- testUserManagerSearchByRegionAndDateRange ---");
        UserManager um = buildUserManager();

        // u1 and u2 are PT in 2020
        List<User> result = um.searchByRegionAndDateRange(
                "PT", LocalDate.of(2020, 1, 1), LocalDate.of(2020, 12, 31));
        assert result.size() == 2 : "Expected 2 PT users in 2020 (got " + result.size() + ")";

        System.out.println("PASS: searchByRegionAndDateRange\n");
    }

    /**
     * Tests name-substring + region combined search.
     */
    public static void testUserManagerSearchByNameSubstringAndRegion() {
        System.out.println("--- testUserManagerSearchByNameSubstringAndRegion ---");
        UserManager um = buildUserManager();

        List<User> result = um.searchByNameSubstringAndRegion("silva", "PT");
        assert result.size() == 1 && result.get(0).getId().equals("u1")
                : "Should find Alice Silva in PT";

        List<User> noMatch = um.searchByNameSubstringAndRegion("silva", "BR");
        assert noMatch.isEmpty() : "Alice Silva is not in BR";

        System.out.println("PASS: searchByNameSubstringAndRegion\n");
    }

    /**
     * Tests preferred-genre search.
     */
    public static void testUserManagerSearchByPreferredGenre() {
        System.out.println("--- testUserManagerSearchByPreferredGenre ---");
        UserManager um = buildUserManager();

        List<User> action = um.searchByPreferredGenre("g1");
        assert action.size() == 2 : "Expected 2 users with Action preference";

        List<User> none = um.searchByPreferredGenre("g99");
        assert none.isEmpty() : "No users should prefer genre g99";

        System.out.println("PASS: searchByPreferredGenre\n");
    }

    // -----------------------------------------------------------------------
    // edu.ufp.streaming.rec.managers.ArtistManager tests
    // -----------------------------------------------------------------------

    /**
     * Tests insert and get on {@link ArtistManager}.
     */
    public static void testArtistManagerInsertAndGet() {
        System.out.println("--- testArtistManagerInsertAndGet ---");
        ArtistManager am = buildArtistManager();

        assert am.size() == 5 : "Expected 5 artists";
        assert am.get("a1").getName().equals("Leonardo DiCaprio") : "a1 should be DiCaprio";
        assert am.get("a99") == null : "Unknown ID should return null";

        Artist dup = new Artist("a1", "Dup", "US", "M", LocalDate.now(), ArtistRole.ACTOR);
        assert !am.insert(dup) : "Duplicate insert should return false";

        System.out.println("PASS: insert/get/duplicate\n");
    }

    /**
     * Tests remove from {@link ArtistManager}.
     */
    public static void testArtistManagerRemove() {
        System.out.println("--- testArtistManagerRemove ---");
        ArtistManager am = buildArtistManager();

        Artist removed = am.remove("a3");
        assert removed != null && removed.getId().equals("a3") : "Should return removed artist";
        assert am.size() == 4 : "Size should be 4";
        assert am.get("a3") == null : "Removed artist not retrievable";

        System.out.println("PASS: remove\n");
    }

    /**
     * Tests edit + BST re-index on {@link ArtistManager}.
     */
    public static void testArtistManagerEdit() {
        System.out.println("--- testArtistManagerEdit ---");
        ArtistManager am = buildArtistManager();

        assert am.editName("a1", "Leo DiCaprio") : "Edit should succeed";
        assert am.get("a1").getName().equals("Leo DiCaprio") : "Name should be updated";

        List<Artist> found = am.searchByNameSubstring("leo");
        assert found.stream().anyMatch(a -> a.getId().equals("a1"))
                : "BST should reflect new name";

        System.out.println("PASS: editName + BST re-index\n");
    }

    /**
     * Tests birth-date range search on {@link ArtistManager}.
     */
    public static void testArtistManagerSearchByBirthDateRange() {
        System.out.println("--- testArtistManagerSearchByBirthDateRange ---");
        ArtistManager am = buildArtistManager();

        // Born in the 1970s: a3 (1970), a4 (1971), a1 (1974), a5 (1974)
        List<Artist> result = am.searchByBirthDateRange(
                LocalDate.of(1970, 1, 1), LocalDate.of(1979, 12, 31));
        assert result.size() == 4 : "Expected 4 artists born in the 70s (got " + result.size() + ")";

        System.out.println("PASS: searchByBirthDateRange\n");
    }

    /**
     * Tests name-substring search on {@link ArtistManager}.
     */
    public static void testArtistManagerSearchByNameSubstring() {
        System.out.println("--- testArtistManagerSearchByNameSubstring ---");
        ArtistManager am = buildArtistManager();

        List<Artist> result = am.searchByNameSubstring("nolan");
        assert result.size() == 1 && result.get(0).getId().equals("a3")
                : "Should find Christopher Nolan";

        System.out.println("PASS: searchByNameSubstring\n");
    }

    /**
     * Tests nationality filter on {@link ArtistManager}.
     */
    public static void testArtistManagerSearchByNationality() {
        System.out.println("--- testArtistManagerSearchByNationality ---");
        ArtistManager am = buildArtistManager();

        List<Artist> us = am.searchByNationality("US");
        assert us.size() == 4 : "Expected 4 US artists (got " + us.size() + ")";

        List<Artist> gb = am.searchByNationality("GB");
        assert gb.size() == 1 : "Expected 1 GB artist";

        System.out.println("PASS: searchByNationality\n");
    }

    /**
     * Tests gender filter on {@link ArtistManager}.
     */
    public static void testArtistManagerSearchByGender() {
        System.out.println("--- testArtistManagerSearchByGender ---");
        ArtistManager am = buildArtistManager();

        List<Artist> female = am.searchByGender("F");
        assert female.size() == 2 : "Expected 2 female artists (got " + female.size() + ")";

        System.out.println("PASS: searchByGender\n");
    }

    /**
     * Tests nationality + birth-date-range combined search.
     */
    public static void testArtistManagerSearchByNationalityAndBirthDateRange() {
        System.out.println("--- testArtistManagerSearchByNationalityAndBirthDateRange ---");
        ArtistManager am = buildArtistManager();

        // US artists born in the 70s: a1 (1974), a4 (1971), a5 (1974)
        List<Artist> result = am.searchByNationalityAndBirthDateRange(
                "US", LocalDate.of(1970, 1, 1), LocalDate.of(1979, 12, 31));
        assert result.size() == 3 : "Expected 3 US artists born in 70s (got " + result.size() + ")";

        System.out.println("PASS: searchByNationalityAndBirthDateRange\n");
    }

    /**
     * Tests combined name-substring + nationality + gender search.
     */
    public static void testArtistManagerSearchByNameSubstringNationalityGender() {
        System.out.println("--- testArtistManagerSearchByNameSubstringNationalityGender ---");
        ArtistManager am = buildArtistManager();

        List<Artist> result = am.searchByNameSubstringNationalityAndGender("coppola", "US", "F");
        assert result.size() == 1 && result.get(0).getId().equals("a4")
                : "Should find Sofia Coppola";

        System.out.println("PASS: searchByNameSubstringNationalityAndGender\n");
    }

    /**
     * Tests role filter on {@link ArtistManager}.
     */
    public static void testArtistManagerSearchByRole() {
        System.out.println("--- testArtistManagerSearchByRole ---");
        ArtistManager am = buildArtistManager();

        List<Artist> actors = am.searchByRole(ArtistRole.ACTOR);
        assert actors.size() == 3 : "Expected 3 actors (got " + actors.size() + ")";

        List<Artist> directors = am.searchByRole(ArtistRole.DIRECTOR);
        assert directors.size() == 2 : "Expected 2 directors (got " + directors.size() + ")";

        System.out.println("PASS: searchByRole\n");
    }

    // -----------------------------------------------------------------------
    // edu.ufp.streaming.rec.managers.FollowManager tests
    // -----------------------------------------------------------------------

    /**
     * Tests follow and unfollow operations in {@link FollowManager}.
     */
    public static void testFollowManagerFollowAndUnfollow() {
        System.out.println("--- testFollowManagerFollowAndUnfollow ---");
        UserManager um = buildUserManager();
        FollowManager fm = new FollowManager();

        User u1 = um.get("u1");
        User u2 = um.get("u2");
        User u3 = um.get("u3");

        UserFollow uf = fm.follow(u1, u2);
        assert uf != null : "First follow should succeed";
        assert fm.isFollowing("u1", "u2") : "u1 should be following u2";
        assert fm.follow(u1, u2) == null : "Duplicate follow should return null";

        fm.follow(u1, u3);
        assert fm.size() == 2 : "Should have 2 follow relationships";

        fm.unfollow("u1", "u2");
        assert !fm.isFollowing("u1", "u2") : "u1 should no longer follow u2";
        assert fm.size() == 1 : "Should have 1 follow relationship after unfollow";

        System.out.println("PASS: follow/unfollow/isFollowing\n");
    }

    /**
     * Tests getFollowers and getFollowing on {@link FollowManager}.
     */
    public static void testFollowManagerGetFollowersAndFollowing() {
        System.out.println("--- testFollowManagerGetFollowersAndFollowing ---");
        UserManager um = buildUserManager();
        FollowManager fm = new FollowManager();

        User u1 = um.get("u1");
        User u2 = um.get("u2");
        User u3 = um.get("u3");
        User u4 = um.get("u4");

        fm.follow(u1, u3);
        fm.follow(u2, u3);
        fm.follow(u4, u3);

        List<User> followers = fm.getFollowers("u3");
        assert followers.size() == 3 : "u3 should have 3 followers";

        fm.follow(u1, u2);
        fm.follow(u1, u4);

        List<User> following = fm.getFollowing("u1");
        assert following.size() == 3 : "u1 should follow 3 users";

        assert fm.followerCount("u3") == 3 : "Follower count mismatch";
        assert fm.followingCount("u1") == 3 : "Following count mismatch";

        System.out.println("PASS: getFollowers/getFollowing/counts\n");
    }

    /**
     * Tests R4 consistency: removing a user removes all their follow relationships.
     */
    public static void testFollowManagerConsistencyOnUserRemoval() {
        System.out.println("--- testFollowManagerConsistencyOnUserRemoval ---");
        UserManager um = buildUserManager();
        FollowManager fm = new FollowManager();

        User u1 = um.get("u1");
        User u2 = um.get("u2");
        User u3 = um.get("u3");

        fm.follow(u1, u2);
        fm.follow(u3, u1);

        // Simulate removing u1: first clean up follow data, then remove from ST
        fm.removeAllRelationships("u1");
        um.remove("u1");

        assert !fm.isFollowing("u1", "u2") : "Relationship u1→u2 should be removed";
        assert !fm.isFollowing("u3", "u1") : "Relationship u3→u1 should be removed";
        assert fm.size() == 0 : "No follow relationships should remain";
        assert um.get("u1") == null : "u1 should be removed from ST";

        System.out.println("PASS: R4 consistency on user removal\n");
    }

    /**
     * Tests follow date-range search using the BST in {@link FollowManager}.
     */
    public static void testFollowManagerDateRange() {
        System.out.println("--- testFollowManagerDateRange ---");
        UserManager um = buildUserManager();
        FollowManager fm = new FollowManager();

        User u1 = um.get("u1");
        User u2 = um.get("u2");
        User u3 = um.get("u3");

        // All follows happen now; range query should catch them
        fm.follow(u1, u2);
        fm.follow(u1, u3);

        LocalDateTime from = LocalDateTime.now().minusMinutes(1);
        LocalDateTime to   = LocalDateTime.now().plusMinutes(1);

        List<UserFollow> result = fm.searchByDateRange(from, to);
        assert result.size() == 2 : "Expected 2 follow events in range (got " + result.size() + ")";

        System.out.println("PASS: searchByDateRange\n");
    }
}
