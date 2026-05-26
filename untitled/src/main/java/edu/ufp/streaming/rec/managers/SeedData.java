package edu.ufp.streaming.rec.managers;

import edu.ufp.streaming.rec.enums.ArtistRole;
import edu.ufp.streaming.rec.models.*;
import java.time.LocalDate;
import java.util.Random;

public class SeedData {

    private static final String[] REGIONS = {"PT", "US", "BR", "UK", "ES", "FR", "DE", "JP"};
    private static final Random rnd = new Random(42);

    public static void populate(StreamingDatabase db) {
        addGenres(db);
        addUsers(db);
        addArtists(db);
        addContents(db);
        addParticipations(db);
        populateMovieDirectors(db);
    }

    private static void addUsers(StreamingDatabase db) {
        User adm = new User("admin", "Administrador", "admin@streaming.com", "PT", java.time.LocalDate.of(2020, 1, 1), "admin123");
        adm.setAdmin(true);
        db.addUser(adm);
        db.addUser(new User("rui_pereira", "Rui Pereira", "rui_pereira@gmail.com", "PT", java.time.LocalDate.of(2026, 5, 20), "rui123"));
        db.addUser(new User("sofia46", "Sofia Fernandes", "sofia.fernandes@gmail.com", "PT", java.time.LocalDate.of(2026, 5, 24), "sofia123"));
        db.addUser(new User("mendes.joao", "Joao Mendes", "mendes.joao@gmail.com", "PT", java.time.LocalDate.of(2026, 5, 19), "joao123"));
        db.addUser(new User("user_beatriz", "Beatriz Santos", "user_beatriz@gmail.com", "BR", java.time.LocalDate.of(2026, 5, 23), "beatriz123"));
        db.addUser(new User("tiago_oliveira", "Tiago Oliveira", "tiago_oliveira@gmail.com", "PT", java.time.LocalDate.of(2026, 5, 21), "tiago123"));
        db.addUser(new User("matilde97", "Matilde Gomes", "matilde.gomes@gmail.com", "PT", java.time.LocalDate.of(2026, 5, 18), "matilde123"));
        db.addUser(new User("rodrigues.miguel", "Miguel Rodrigues", "rodrigues.miguel@gmail.com", "UK", java.time.LocalDate.of(2026, 5, 23), "miguel123"));
        db.addUser(new User("user_ines", "Ines Almeida", "user_ines@gmail.com", "PT", java.time.LocalDate.of(2026, 5, 22), "ines123"));
        db.addUser(new User("diogo_martins", "Diogo Martins", "diogo_martins@gmail.com", "PT", java.time.LocalDate.of(2026, 5, 12), "diogo123"));
        db.addUser(new User("claudia51", "Claudia Carvalho", "claudia.carvalho@gmail.com", "BR", java.time.LocalDate.of(2026, 5, 24), "claudia123"));
        db.addUser(new User("silva.pedro", "Pedro Silva", "silva.pedro@gmail.com", "PT", java.time.LocalDate.of(2026, 5, 24), "pedro123"));
        db.addUser(new User("user_ana", "Ana Costa", "user_ana@gmail.com", "US", java.time.LocalDate.of(2026, 5, 17), "ana123"));
        db.addUser(new User("lucas_ribeiro", "Lucas Ribeiro", "lucas_ribeiro@gmail.com", "PT", java.time.LocalDate.of(2026, 5, 24), "lucas123"));
        db.addUser(new User("mariana53", "Mariana Pinto", "mariana.pinto@gmail.com", "BR", java.time.LocalDate.of(2026, 5, 16), "mariana123"));
        db.addUser(new User("teixeira.ricardo", "Ricardo Teixeira", "teixeira.ricardo@gmail.com", "PT", java.time.LocalDate.of(2026, 5, 16), "ricardo123"));
        db.addUser(new User("user_sara", "Sara Sousa", "user_sara@gmail.com", "PT", java.time.LocalDate.of(2026, 5, 14), "sara123"));
        db.addUser(new User("andre_moreira", "Andre Moreira", "andre_moreira@gmail.com", "FR", java.time.LocalDate.of(2026, 5, 22), "andre123"));
        db.addUser(new User("catarina14", "Catarina Vieira", "catarina.vieira@gmail.com", "PT", java.time.LocalDate.of(2026, 5, 20), "catarina123"));
        db.addUser(new User("coelho.vasco", "Vasco Coelho", "coelho.vasco@gmail.com", "ES", java.time.LocalDate.of(2026, 5, 12), "vasco123"));
        db.addUser(new User("user_leonor", "Leonor Marques", "user_leonor@gmail.com", "PT", java.time.LocalDate.of(2026, 5, 20), "leonor123"));
        db.addFollow("rui_pereira", "sofia46");
        db.addFollow("rui_pereira", "mendes.joao");
        db.addFollow("sofia46", "mendes.joao");
        db.addFollow("mendes.joao", "user_beatriz");
        db.addFollow("user_beatriz", "tiago_oliveira");
        db.addFollow("tiago_oliveira", "rui_pereira");
        db.addFollow("matilde97", "user_ines");
        db.addFollow("user_ines", "diogo_martins");
        db.addFollow("diogo_martins", "claudia51");
        db.addFollow("silva.pedro", "user_ana");
        db.addFollow("user_ana", "lucas_ribeiro");
        db.addFollow("lucas_ribeiro", "mariana53");
    }

    private static void addGenres(StreamingDatabase db) {
        db.addGenre(new Genre("gAcao", "Acao"));
        db.addGenre(new Genre("gDrama", "Drama"));
        db.addGenre(new Genre("gCom", "Comedia"));
        db.addGenre(new Genre("gSciFi", "Ficcao Cientifica"));
        db.addGenre(new Genre("gThril", "Thriller"));
        db.addGenre(new Genre("gRom", "Romance"));
        db.addGenre(new Genre("gFant", "Fantasia"));
        db.addGenre(new Genre("gHorror", "Terror"));
        db.addGenre(new Genre("gDoc", "Documentario"));
        db.addGenre(new Genre("gCrime", "Crime"));
    }

    private static void addArtists(StreamingDatabase db) {
        db.addArtist(new Artist("a1", "Leonardo DiCaprio", "US", "M", LocalDate.of(1974, 11, 11), ArtistRole.ACTOR));
        db.addArtist(new Artist("a2", "Anthony Hopkins", "UK", "M", LocalDate.of(1937, 12, 31), ArtistRole.ACTOR));
        db.addArtist(new Artist("a3", "Matthew McConaughey", "US", "M", LocalDate.of(1969, 11, 4), ArtistRole.ACTOR));
        db.addArtist(new Artist("a4", "Scarlett Johansson", "US", "F", LocalDate.of(1984, 11, 22), ArtistRole.ACTOR));
        db.addArtist(new Artist("a5", "Morgan Freeman", "US", "M", LocalDate.of(1937, 6, 1), ArtistRole.ACTOR));
        db.addArtist(new Artist("a6", "Samuel L. Jackson", "US", "M", LocalDate.of(1948, 12, 21), ArtistRole.ACTOR));
        db.addArtist(new Artist("a7", "Jack Nicholson", "US", "M", LocalDate.of(1937, 4, 22), ArtistRole.ACTOR));
        db.addArtist(new Artist("a8", "Marion Cotillard", "FR", "F", LocalDate.of(1975, 9, 30), ArtistRole.ACTOR));
        db.addArtist(new Artist("a9", "Joaquin Phoenix", "US", "M", LocalDate.of(1974, 10, 28), ArtistRole.ACTOR));
        db.addArtist(new Artist("a10", "Ryan Gosling", "US", "M", LocalDate.of(1980, 11, 12), ArtistRole.ACTOR));
        db.addArtist(new Artist("a11", "Emma Stone", "US", "F", LocalDate.of(1988, 11, 6), ArtistRole.ACTOR));
        db.addArtist(new Artist("a12", "Brian Cox", "UK", "M", LocalDate.of(1948, 6, 14), ArtistRole.ACTOR));
        db.addArtist(new Artist("a13", "Timothee Chalamet", "US", "M", LocalDate.of(1995, 12, 27), ArtistRole.ACTOR));
        db.addArtist(new Artist("a14", "Zendaya", "US", "F", LocalDate.of(1996, 9, 1), ArtistRole.ACTOR));
        db.addArtist(new Artist("a15", "Pedro Pascal", "US", "M", LocalDate.of(1975, 4, 15), ArtistRole.ACTOR));
        db.addArtist(new Artist("d1", "Christopher Nolan", "UK", "M", LocalDate.of(1970, 7, 30), ArtistRole.DIRECTOR));
        db.addArtist(new Artist("d3", "Martin Scorsese", "US", "M", LocalDate.of(1942, 11, 17), ArtistRole.DIRECTOR));
        db.addArtist(new Artist("d4", "Greta Gerwig", "US", "F", LocalDate.of(1983, 8, 4), ArtistRole.DIRECTOR));
        db.addArtist(new Artist("d5", "Denis Villeneuve", "CA", "M", LocalDate.of(1967, 10, 3), ArtistRole.DIRECTOR));
        db.addArtist(new Artist("d6", "Bong Joon-ho", "KR", "M", LocalDate.of(1969, 9, 14), ArtistRole.DIRECTOR));
        db.addArtist(new Artist("d8", "Ridley Scott", "UK", "M", LocalDate.of(1937, 11, 30), ArtistRole.DIRECTOR));
        db.addArtist(new Artist("w1", "Aaron Sorkin", "US", "M", LocalDate.of(1961, 10, 23), ArtistRole.SCREENWRITER));
        db.addArtist(new Artist("w2", "Quentin Tarantino", "US", "M", LocalDate.of(1963, 3, 27), ArtistRole.SCREENWRITER));
        db.addArtist(new Artist("w3", "David Fincher", "US", "M", LocalDate.of(1962, 8, 28), ArtistRole.SCREENWRITER));
        db.addArtist(new Artist("p1", "Emma Thomas", "UK", "F", LocalDate.of(1971, 5, 7), ArtistRole.PRODUCER));
        db.addArtist(new Artist("p2", "Douglas Wick", "US", "M", LocalDate.of(1951, 6, 22), ArtistRole.PRODUCER));
        db.addArtist(new Artist("p4", "Tom Ackerley", "UK", "M", LocalDate.of(1981, 6, 14), ArtistRole.PRODUCER));
    }

    private static void addContents(StreamingDatabase db) {
        Genre gAcao = db.genres().get("gAcao");
        Genre gDrama = db.genres().get("gDrama");
        Genre gCom = db.genres().get("gCom");
        Genre gSciFi = db.genres().get("gSciFi");
        Genre gThril = db.genres().get("gThril");
        Genre gRom = db.genres().get("gRom");
        Genre gFant = db.genres().get("gFant");
        Genre gDoc = db.genres().get("gDoc");
        Genre gCrime = db.genres().get("gCrime");

        addMovie(db, "m1", "Inception", gSciFi, 2010, 7, 16, 148, "US", 4.5);
        addMovie(db, "m2", "Interstellar", gSciFi, 2014, 11, 7, 169, "US", 4.6);
        addMovie(db, "m3", "O Lobo de Wall Street", gDrama, 2013, 12, 25, 180, "US", 4.3);
        addMovie(db, "m4", "O Silencio dos Inocentes", gThril, 1991, 2, 14, 118, "US", 4.4);
        addMovie(db, "m5", "Parasitas", gThril, 2019, 5, 30, 132, "KR", 4.7);
        addMovie(db, "m6", "Dune: Parte Um", gSciFi, 2021, 10, 22, 155, "US", 4.2);
        addMovie(db, "m7", "Barbie", gCom, 2023, 7, 21, 114, "US", 3.9);
        addMovie(db, "m8", "Joker", gDrama, 2019, 10, 4, 122, "US", 4.3);
        addMovie(db, "m9", "La La Land", gRom, 2016, 12, 9, 128, "US", 4.1);
        addMovie(db, "m10", "Gladiador", gAcao, 2000, 5, 5, 155, "UK", 4.4);
        addMovie(db, "m11", "O Senhor dos Aneis", gFant, 2001, 12, 19, 178, "NZ", 4.8);
        addMovie(db, "m12", "Forrest Gump", gDrama, 1994, 7, 6, 142, "US", 4.7);
        addMovie(db, "m13", "Dune: Parte Dois", gSciFi, 2024, 3, 1, 166, "US", 4.5);
        addMovie(db, "m14", "Oppenheimer", gDrama, 2023, 7, 21, 180, "US", 4.6);
        addMovie(db, "m15", "The Departed", gCrime, 2006, 10, 6, 151, "US", 4.5);

        addSeriesWithEpisodes(db, "s1", "Breaking Bad", gCrime, 2008, 1, 20, 47, 5, new String[]{"Pilot", "Mr. Weeks", "Cats in the Bag", "And the Bags in the River", "Gresek", "Crazy Handful of Nothin", "A No-Rough-Stuff-Type Deal"});
        addSeriesWithEpisodes(db, "s2", "Stranger Things", gSciFi, 2016, 7, 15, 50, 4, new String[]{"The Vanishing of Will Byers", "The Weirdo on Maple Street", "Holly Jolly", "The Body", "Fleming and the Front"});
        addSeriesWithEpisodes(db, "s3", "The Last of Us", gDrama, 2023, 1, 15, 60, 2, new String[]{"When You Are Lost", "Infected", "Please Hold to My Hand", "Long Long Time"});
        addSeriesWithEpisodes(db, "s4", "Chernobyl", gDrama, 2019, 5, 6, 65, 1, new String[]{"1:23:45", "Please Remain Calm", "Open Wide O Earth", "The Radiated City"});
        addSeriesWithEpisodes(db, "s5", "Dark", gSciFi, 2017, 12, 1, 52, 3, new String[]{"Secrets", "Lies", "The End of a Journey"});
        addSeriesWithEpisodes(db, "s6", "The Bear", gDrama, 2022, 6, 23, 30, 3, new String[]{"Market", "Braciole", "Tipping Point"});
        addSeriesWithEpisodes(db, "s7", "Severance", gThril, 2022, 2, 18, 45, 2, new String[]{"Good News About Hell", "Half Loop"});
        addSeriesWithEpisodes(db, "s8", "House of the Dragon", gFant, 2022, 8, 21, 60, 2, new String[]{"The Heirs of the Dragon", "Rhaenyras Tale"});
        addSeriesWithEpisodes(db, "s9", "Succession", gDrama, 2018, 6, 29, 55, 4, new String[]{"Celebration", "The Summer Mansion", "The Direction of Travel"});
        addSeriesWithEpisodes(db, "s10", "The Mandalorian", gAcao, 2019, 11, 12, 40, 3, new String[]{"Chapter 1: The Mandalorian", "Chapter 2: The Child", "Chapter 3: The Sin"});

        addDoc(db, "doc1", "O Nosso Planeta", gDoc, 2019, 4, 5, 49, "Natureza", "UK", 4.8);
        addDoc(db, "doc2", "Making a Murderer", gDoc, 2015, 12, 18, 55, "Crime Real", "US", 4.5);
        addDoc(db, "doc3", "13th", gDoc, 2016, 10, 7, 100, "Sociedade", "US", 4.6);
        addDoc(db, "doc4", "Cosmos: Mundos Possiveis", gDoc, 2020, 3, 9, 44, "Ciencia", "US", 4.7);
        addDoc(db, "doc5", "Jiro Dreams of Sushi", gDoc, 2011, 3, 9, 81, "Gastronomia", "JP", 4.4);
    }

    private static void addParticipations(StreamingDatabase db) {
        db.addParticipation("a1", "m1", ArtistRole.ACTOR, LocalDate.of(2010, 7, 16));
        db.addParticipation("d1", "m1", ArtistRole.DIRECTOR, LocalDate.of(2010, 7, 16));
        db.addParticipation("p1", "m1", ArtistRole.PRODUCER, LocalDate.of(2010, 7, 16));
        db.addParticipation("w1", "m1", ArtistRole.SCREENWRITER, LocalDate.of(2010, 7, 16));
        db.addParticipation("a3", "m2", ArtistRole.ACTOR, LocalDate.of(2014, 11, 7));
        db.addParticipation("d1", "m2", ArtistRole.DIRECTOR, LocalDate.of(2014, 11, 7));
        db.addParticipation("p1", "m2", ArtistRole.PRODUCER, LocalDate.of(2014, 11, 7));
        db.addParticipation("w1", "m2", ArtistRole.SCREENWRITER, LocalDate.of(2014, 11, 7));
        db.addParticipation("a1", "m3", ArtistRole.ACTOR, LocalDate.of(2013, 12, 25));
        db.addParticipation("d3", "m3", ArtistRole.DIRECTOR, LocalDate.of(2013, 12, 25));
        db.addParticipation("w2", "m3", ArtistRole.SCREENWRITER, LocalDate.of(2013, 12, 25));
        db.addParticipation("a2", "m4", ArtistRole.ACTOR, LocalDate.of(1991, 2, 14));
        db.addParticipation("d3", "m4", ArtistRole.DIRECTOR, LocalDate.of(1991, 2, 14));
        db.addParticipation("d6", "m5", ArtistRole.DIRECTOR, LocalDate.of(2019, 5, 30));
        db.addParticipation("w2", "m5", ArtistRole.SCREENWRITER, LocalDate.of(2019, 5, 30));
        db.addParticipation("a13", "m6", ArtistRole.ACTOR, LocalDate.of(2021, 10, 22));
        db.addParticipation("a14", "m6", ArtistRole.ACTOR, LocalDate.of(2021, 10, 22));
        db.addParticipation("d5", "m6", ArtistRole.DIRECTOR, LocalDate.of(2021, 10, 22));
        db.addParticipation("a10", "m7", ArtistRole.ACTOR, LocalDate.of(2023, 7, 21));
        db.addParticipation("a11", "m7", ArtistRole.ACTOR, LocalDate.of(2023, 7, 21));
        db.addParticipation("d4", "m7", ArtistRole.DIRECTOR, LocalDate.of(2023, 7, 21));
        db.addParticipation("p4", "m7", ArtistRole.PRODUCER, LocalDate.of(2023, 7, 21));
        db.addParticipation("a9", "m8", ArtistRole.ACTOR, LocalDate.of(2019, 10, 4));
        db.addParticipation("d3", "m8", ArtistRole.DIRECTOR, LocalDate.of(2019, 10, 4));
        db.addParticipation("a10", "m9", ArtistRole.ACTOR, LocalDate.of(2016, 12, 9));
        db.addParticipation("a11", "m9", ArtistRole.ACTOR, LocalDate.of(2016, 12, 9));
        db.addParticipation("d4", "m9", ArtistRole.DIRECTOR, LocalDate.of(2016, 12, 9));
        db.addParticipation("d8", "m10", ArtistRole.DIRECTOR, LocalDate.of(2000, 5, 5));
        db.addParticipation("p2", "m10", ArtistRole.PRODUCER, LocalDate.of(2000, 5, 5));
        db.addParticipation("a3", "m12", ArtistRole.ACTOR, LocalDate.of(1994, 7, 6));
        db.addParticipation("a13", "m13", ArtistRole.ACTOR, LocalDate.of(2024, 3, 1));
        db.addParticipation("a14", "m13", ArtistRole.ACTOR, LocalDate.of(2024, 3, 1));
        db.addParticipation("d5", "m13", ArtistRole.DIRECTOR, LocalDate.of(2024, 3, 1));
        db.addParticipation("d1", "m14", ArtistRole.DIRECTOR, LocalDate.of(2023, 7, 21));
        db.addParticipation("p1", "m14", ArtistRole.PRODUCER, LocalDate.of(2023, 7, 21));
        db.addParticipation("w3", "m14", ArtistRole.SCREENWRITER, LocalDate.of(2023, 7, 21));
        db.addParticipation("a1", "m15", ArtistRole.ACTOR, LocalDate.of(2006, 10, 6));
        db.addParticipation("a7", "m15", ArtistRole.ACTOR, LocalDate.of(2006, 10, 6));
        db.addParticipation("d3", "m15", ArtistRole.DIRECTOR, LocalDate.of(2006, 10, 6));
        db.addParticipation("a1", "s1", ArtistRole.ACTOR, LocalDate.of(2008, 1, 20));
        db.addParticipation("d3", "s1", ArtistRole.PRODUCER, LocalDate.of(2008, 1, 20));
        db.addParticipation("a15", "s3", ArtistRole.ACTOR, LocalDate.of(2023, 1, 15));
        db.addParticipation("a12", "s9", ArtistRole.ACTOR, LocalDate.of(2018, 6, 29));
        db.addParticipation("a15", "s10", ArtistRole.ACTOR, LocalDate.of(2019, 11, 12));
        db.addParticipation("p2", "s10", ArtistRole.PRODUCER, LocalDate.of(2019, 11, 12));
    }

    private static void populateMovieDirectors(StreamingDatabase db) {
        String[] movieIds = {"m1", "m2", "m3", "m4", "m5", "m6", "m7", "m8", "m9", "m10", "m11", "m12", "m13", "m14", "m15"};
        for (String movieId : movieIds) {
            Content c = db.contents().get(movieId);
            if (c instanceof Movie mv) {
                var directors = db.participations().getDirectors(movieId);
                if (!directors.isEmpty()) {
                    mv.setDirector(directors.get(0));
                }
            }
        }
    }

    private static String randomRegion() {
        return REGIONS[rnd.nextInt(REGIONS.length)];
    }

    private static void addMovie(StreamingDatabase db, String id, String title, Genre genre, int y, int m, int d, int dur, String region, double rating) {
        Movie mv = new Movie(id, title, genre, LocalDate.of(y, m, d), dur, region, null);
        mv.setRating(rating);
        db.addContent(mv);
    }

    private static void addSeriesWithEpisodes(StreamingDatabase db, String id, String title, Genre genre, int y, int m, int d, int dur, int seasons, String[] episodeNames) {
        Series s = new Series(id, title, genre, LocalDate.of(y, m, d), dur, randomRegion(), seasons);
        for (String epName : episodeNames) {
            s.getEpisodes().add(epName);
        }
        s.setRating(4.0 + rnd.nextDouble() * 0.9);
        db.addContent(s);
    }

    private static void addDoc(StreamingDatabase db, String id, String title, Genre genre, int y, int m, int d, int dur, String topic, String region, double rating) {
        Documentary doc = new Documentary(id, title, genre, LocalDate.of(y, m, d), dur, region, topic);
        doc.setRating(rating);
        db.addContent(doc);
    }
}