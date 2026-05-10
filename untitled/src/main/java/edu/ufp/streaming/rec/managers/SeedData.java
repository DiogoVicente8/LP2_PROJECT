package edu.ufp.streaming.rec.managers;

import edu.ufp.streaming.rec.enums.ArtistRole;
import edu.ufp.streaming.rec.models.*;

import java.time.LocalDate;

/**
 * Dados de seed realistas para a plataforma de streaming.
 * Chama SeedData.populate(db) no teu método de inicialização,
 * ANTES de AppStateSerializer.load(db), para que o load não duplique.
 *
 * Exemplo de uso no Main ou AppStateSerializer:
 *
 *   StreamingDatabase db = new StreamingDatabase();
 *   SeedData.populate(db);        // ← adiciona seed
 *   AppStateSerializer.load(db);  // ← carrega saves por cima (sem duplicar)
 */
public class SeedData {

    public static void populate(StreamingDatabase db) {
        addGenres(db);
        addArtists(db);
        addContents(db);
        addParticipations(db);
    }

    // =========================================================================
    // GÉNEROS
    // =========================================================================
    private static void addGenres(StreamingDatabase db) {
        db.addGenre(new Genre("g1",  "Ação"));
        db.addGenre(new Genre("g2",  "Drama"));
        db.addGenre(new Genre("g3",  "Comédia"));
        db.addGenre(new Genre("g4",  "Terror"));
        db.addGenre(new Genre("g5",  "Ficção Científica"));
        db.addGenre(new Genre("g6",  "Thriller"));
        db.addGenre(new Genre("g7",  "Romance"));
        db.addGenre(new Genre("g8",  "Animação"));
        db.addGenre(new Genre("g9",  "Documentário"));
        db.addGenre(new Genre("g10", "Crime"));
        db.addGenre(new Genre("g11", "Aventura"));
        db.addGenre(new Genre("g12", "Fantasia"));
    }

    // =========================================================================
    // ARTISTAS  (15 actores + 8 realizadores + 4 produtores = 27)
    // =========================================================================
    private static void addArtists(StreamingDatabase db) {
        // Actores
        db.addArtist(new Artist("a1",  "Leonardo DiCaprio",  "Americana",   "M", LocalDate.of(1974, 11, 11), ArtistRole.ACTOR));
        db.addArtist(new Artist("a2",  "Meryl Streep",       "Americana",   "F", LocalDate.of(1949,  6, 22), ArtistRole.ACTOR));
        db.addArtist(new Artist("a3",  "Tom Hanks",          "Americana",   "M", LocalDate.of(1956,  7,  9), ArtistRole.ACTOR));
        db.addArtist(new Artist("a4",  "Cate Blanchett",     "Australiana", "F", LocalDate.of(1969,  5, 14), ArtistRole.ACTOR));
        db.addArtist(new Artist("a5",  "Denzel Washington",  "Americana",   "M", LocalDate.of(1954, 12, 28), ArtistRole.ACTOR));
        db.addArtist(new Artist("a6",  "Natalie Portman",    "Israelita",   "F", LocalDate.of(1981,  6,  9), ArtistRole.ACTOR));
        db.addArtist(new Artist("a7",  "Brad Pitt",          "Americana",   "M", LocalDate.of(1963, 12, 18), ArtistRole.ACTOR));
        db.addArtist(new Artist("a8",  "Scarlett Johansson", "Americana",   "F", LocalDate.of(1984, 11, 22), ArtistRole.ACTOR));
        db.addArtist(new Artist("a9",  "Joaquin Phoenix",    "Americana",   "M", LocalDate.of(1974, 10, 28), ArtistRole.ACTOR));
        db.addArtist(new Artist("a10", "Emma Stone",         "Americana",   "F", LocalDate.of(1988, 11,  6), ArtistRole.ACTOR));
        db.addArtist(new Artist("a11", "Ryan Gosling",       "Canadiana",   "M", LocalDate.of(1980, 11, 12), ArtistRole.ACTOR));
        db.addArtist(new Artist("a12", "Viola Davis",        "Americana",   "F", LocalDate.of(1965,  8, 11), ArtistRole.ACTOR));
        db.addArtist(new Artist("a13", "Timothée Chalamet",  "Americana",   "M", LocalDate.of(1995, 12, 27), ArtistRole.ACTOR));
        db.addArtist(new Artist("a14", "Zendaya",            "Americana",   "F", LocalDate.of(1996,  9,  1), ArtistRole.ACTOR));
        db.addArtist(new Artist("a15", "Pedro Pascal",       "Chilena",     "M", LocalDate.of(1975,  4,  2), ArtistRole.ACTOR));

        // Realizadores
        db.addArtist(new Artist("d1", "Christopher Nolan",  "Britânica",   "M", LocalDate.of(1970,  7, 30), ArtistRole.DIRECTOR));
        db.addArtist(new Artist("d2", "Steven Spielberg",   "Americana",   "M", LocalDate.of(1946, 12, 18), ArtistRole.DIRECTOR));
        db.addArtist(new Artist("d3", "Martin Scorsese",    "Americana",   "M", LocalDate.of(1942, 11, 17), ArtistRole.DIRECTOR));
        db.addArtist(new Artist("d4", "Greta Gerwig",       "Americana",   "F", LocalDate.of(1983,  8,  4), ArtistRole.DIRECTOR));
        db.addArtist(new Artist("d5", "Denis Villeneuve",   "Canadiana",   "M", LocalDate.of(1967, 10,  3), ArtistRole.DIRECTOR));
        db.addArtist(new Artist("d6", "Bong Joon-ho",       "Sul-Coreana", "M", LocalDate.of(1969,  9, 14), ArtistRole.DIRECTOR));
        db.addArtist(new Artist("d7", "Sofia Coppola",      "Americana",   "F", LocalDate.of(1971,  5, 14), ArtistRole.DIRECTOR));
        db.addArtist(new Artist("d8", "Ridley Scott",       "Britânica",   "M", LocalDate.of(1937, 11, 30), ArtistRole.DIRECTOR));

        // Produtores
        db.addArtist(new Artist("p1", "Emma Thomas",        "Britânica",   "F", LocalDate.of(1971,  6, 28), ArtistRole.PRODUCER));
        db.addArtist(new Artist("p2", "Kathleen Kennedy",   "Americana",   "F", LocalDate.of(1953,  6,  5), ArtistRole.PRODUCER));
        db.addArtist(new Artist("p3", "Kevin Feige",        "Americana",   "M", LocalDate.of(1973,  6,  2), ArtistRole.PRODUCER));
        db.addArtist(new Artist("p4", "David Heyman",       "Britânica",   "M", LocalDate.of(1961,  7, 26), ArtistRole.PRODUCER));
    }

    // =========================================================================
    // CONTEÚDOS  (15 filmes + 10 séries + 5 documentários = 30)
    // =========================================================================
    private static void addContents(StreamingDatabase db) {
        Genre gAcao  = db.genres().get("g1");
        Genre gDrama = db.genres().get("g2");
        Genre gCom   = db.genres().get("g3");
        Genre gSciFi = db.genres().get("g5");
        Genre gThril = db.genres().get("g6");
        Genre gRom   = db.genres().get("g7");
        Genre gDoc   = db.genres().get("g9");
        Genre gCrime = db.genres().get("g10");
        Genre gFant  = db.genres().get("g12");

        // ── Filmes ───────────────────────────────────────────────────────────
        addMovie(db, "m1",  "Inception",                          gSciFi, 2010,  7, 16, 148, 4.5);
        addMovie(db, "m2",  "Interstellar",                       gSciFi, 2014, 11,  7, 169, 4.6);
        addMovie(db, "m3",  "O Lobo de Wall Street",              gDrama, 2013, 12, 25, 180, 4.3);
        addMovie(db, "m4",  "O Silêncio dos Inocentes",           gThril, 1991,  2, 14, 118, 4.4);
        addMovie(db, "m5",  "Parasitas",                          gThril, 2019,  5, 30, 132, 4.7);
        addMovie(db, "m6",  "Dune: Parte Um",                     gSciFi, 2021, 10, 22, 155, 4.2);
        addMovie(db, "m7",  "Barbie",                             gCom,   2023,  7, 21, 114, 3.9);
        addMovie(db, "m8",  "Joker",                              gDrama, 2019, 10,  4, 122, 4.3);
        addMovie(db, "m9",  "La La Land",                         gRom,   2016, 12,  9, 128, 4.1);
        addMovie(db, "m10", "Gladiador",                          gAcao,  2000,  5,  5, 155, 4.4);
        addMovie(db, "m11", "O Senhor dos Anéis: A Sociedade do Anel", gFant, 2001, 12, 19, 178, 4.8);
        addMovie(db, "m12", "Forrest Gump",                       gDrama, 1994,  7,  6, 142, 4.7);
        addMovie(db, "m13", "Dune: Parte Dois",                   gSciFi, 2024,  3,  1, 166, 4.5);
        addMovie(db, "m14", "Oppenheimer",                        gDrama, 2023,  7, 21, 180, 4.6);
        addMovie(db, "m15", "The Departed",                       gCrime, 2006, 10,  6, 151, 4.5);

        // ── Séries ───────────────────────────────────────────────────────────
        addSeries(db, "s1",  "Breaking Bad",           gCrime, 2008,  1, 20, 47, 5, 4.9);
        addSeries(db, "s2",  "Stranger Things",        gSciFi, 2016,  7, 15, 50, 4, 4.3);
        addSeries(db, "s3",  "The Last of Us",         gDrama, 2023,  1, 15, 60, 2, 4.7);
        addSeries(db, "s4",  "Chernobyl",              gDrama, 2019,  5,  6, 65, 1, 4.8);
        addSeries(db, "s5",  "Dark",                   gSciFi, 2017, 12,  1, 52, 3, 4.6);
        addSeries(db, "s6",  "The Bear",               gDrama, 2022,  6, 23, 30, 3, 4.5);
        addSeries(db, "s7",  "Severance",              gThril, 2022,  2, 18, 45, 2, 4.4);
        addSeries(db, "s8",  "House of the Dragon",    gFant,  2022,  8, 21, 60, 2, 4.0);
        addSeries(db, "s9",  "Succession",             gDrama, 2018,  6, 29, 55, 4, 4.7);
        addSeries(db, "s10", "The Mandalorian",        gAcao,  2019, 11, 12, 40, 3, 4.2);

        // ── Documentários ────────────────────────────────────────────────────
        addDoc(db, "doc1", "O Nosso Planeta",            gDoc, 2019,  4,  5,  49, "Natureza",     "David Attenborough", 4.8);
        addDoc(db, "doc2", "Making a Murderer",          gDoc, 2015, 12, 18,  55, "Crime Real",   "narrador interno",   4.5);
        addDoc(db, "doc3", "13th",                       gDoc, 2016, 10,  7, 100, "Sociedade",    "narrador interno",   4.6);
        addDoc(db, "doc4", "Cosmos: Mundos Possíveis",   gDoc, 2020,  3,  9,  44, "Ciência",      "Neil deGrasse Tyson",4.7);
        addDoc(db, "doc5", "Jiro Dreams of Sushi",       gDoc, 2011,  3,  9,  81, "Gastronomia",  "narrador interno",   4.4);
    }

    // ── Helpers de criação ────────────────────────────────────────────────────
    private static void addMovie(StreamingDatabase db, String id, String title,
                                  Genre genre, int y, int m, int d, int dur, double rating) {
        Movie mv = new Movie(id, title, genre, LocalDate.of(y, m, d), dur, "PT", null);
        mv.setRating(rating);
        db.addContent(mv);
    }

    private static void addSeries(StreamingDatabase db, String id, String title,
                                   Genre genre, int y, int m, int d, int dur, int seasons, double rating) {
        Series s = new Series(id, title, genre, LocalDate.of(y, m, d), dur, "PT", seasons);
        s.setRating(rating);
        db.addContent(s);
    }

    private static void addDoc(StreamingDatabase db, String id, String title,
                                Genre genre, int y, int m, int d, int dur,
                                String topic, String narrator, double rating) {
        Documentary doc = new Documentary(id, title, genre, LocalDate.of(y, m, d), dur, "PT", topic, narrator);
        doc.setRating(rating);
        db.addContent(doc);
    }

    // =========================================================================
    // PARTICIPAÇÕES  Artista ↔ Conteúdo
    // =========================================================================
    private static void addParticipations(StreamingDatabase db) {
        // Inception (m1)
        db.addParticipation("a1", "m1", ArtistRole.ACTOR,    LocalDate.of(2010,  7, 16));
        db.addParticipation("d1", "m1", ArtistRole.DIRECTOR, LocalDate.of(2010,  7, 16));
        db.addParticipation("p1", "m1", ArtistRole.PRODUCER, LocalDate.of(2010,  7, 16));

        // Interstellar (m2)
        db.addParticipation("a3", "m2", ArtistRole.ACTOR,    LocalDate.of(2014, 11,  7));
        db.addParticipation("d1", "m2", ArtistRole.DIRECTOR, LocalDate.of(2014, 11,  7));
        db.addParticipation("p1", "m2", ArtistRole.PRODUCER, LocalDate.of(2014, 11,  7));

        // O Lobo de Wall Street (m3)
        db.addParticipation("a1", "m3", ArtistRole.ACTOR,    LocalDate.of(2013, 12, 25));
        db.addParticipation("d3", "m3", ArtistRole.DIRECTOR, LocalDate.of(2013, 12, 25));

        // O Silêncio dos Inocentes (m4)
        db.addParticipation("a2", "m4", ArtistRole.ACTOR,    LocalDate.of(1991,  2, 14));

        // Parasitas (m5)
        db.addParticipation("d6", "m5", ArtistRole.DIRECTOR, LocalDate.of(2019,  5, 30));

        // Dune: Parte Um (m6)
        db.addParticipation("a13","m6", ArtistRole.ACTOR,    LocalDate.of(2021, 10, 22));
        db.addParticipation("a14","m6", ArtistRole.ACTOR,    LocalDate.of(2021, 10, 22));
        db.addParticipation("d5", "m6", ArtistRole.DIRECTOR, LocalDate.of(2021, 10, 22));

        // Barbie (m7)
        db.addParticipation("a10","m7", ArtistRole.ACTOR,    LocalDate.of(2023,  7, 21));
        db.addParticipation("a11","m7", ArtistRole.ACTOR,    LocalDate.of(2023,  7, 21));
        db.addParticipation("d4", "m7", ArtistRole.DIRECTOR, LocalDate.of(2023,  7, 21));
        db.addParticipation("p4", "m7", ArtistRole.PRODUCER, LocalDate.of(2023,  7, 21));

        // Joker (m8)
        db.addParticipation("a9", "m8", ArtistRole.ACTOR,    LocalDate.of(2019, 10,  4));

        // La La Land (m9)
        db.addParticipation("a10","m9", ArtistRole.ACTOR,    LocalDate.of(2016, 12,  9));
        db.addParticipation("a11","m9", ArtistRole.ACTOR,    LocalDate.of(2016, 12,  9));

        // Gladiador (m10)
        db.addParticipation("d8", "m10",ArtistRole.DIRECTOR, LocalDate.of(2000,  5,  5));
        db.addParticipation("p2", "m10",ArtistRole.PRODUCER, LocalDate.of(2000,  5,  5));

        // Forrest Gump (m12)
        db.addParticipation("a3", "m12",ArtistRole.ACTOR,    LocalDate.of(1994,  7,  6));

        // Dune: Parte Dois (m13)
        db.addParticipation("a13","m13",ArtistRole.ACTOR,    LocalDate.of(2024,  3,  1));
        db.addParticipation("a14","m13",ArtistRole.ACTOR,    LocalDate.of(2024,  3,  1));
        db.addParticipation("d5", "m13",ArtistRole.DIRECTOR, LocalDate.of(2024,  3,  1));

        // Oppenheimer (m14)
        db.addParticipation("d1", "m14",ArtistRole.DIRECTOR, LocalDate.of(2023,  7, 21));
        db.addParticipation("p1", "m14",ArtistRole.PRODUCER, LocalDate.of(2023,  7, 21));

        // The Departed (m15)
        db.addParticipation("a1", "m15",ArtistRole.ACTOR,    LocalDate.of(2006, 10,  6));
        db.addParticipation("a7", "m15",ArtistRole.ACTOR,    LocalDate.of(2006, 10,  6));
        db.addParticipation("d3", "m15",ArtistRole.DIRECTOR, LocalDate.of(2006, 10,  6));

        // Breaking Bad (s1)
        db.addParticipation("d3", "s1", ArtistRole.PRODUCER, LocalDate.of(2008,  1, 20));

        // The Last of Us (s3)
        db.addParticipation("a15","s3", ArtistRole.ACTOR,    LocalDate.of(2023,  1, 15));

        // Succession (s9)
        db.addParticipation("a12","s9", ArtistRole.ACTOR,    LocalDate.of(2018,  6, 29));

        // The Mandalorian (s10)
        db.addParticipation("a15","s10",ArtistRole.ACTOR,    LocalDate.of(2019, 11, 12));
        db.addParticipation("p2", "s10",ArtistRole.PRODUCER, LocalDate.of(2019, 11, 12));
    }
}
