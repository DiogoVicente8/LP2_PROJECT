    import edu.ufp.streaming.rec.managers.ContentBST;
    import edu.ufp.streaming.rec.managers.ContentManager;
    import edu.ufp.streaming.rec.managers.GenreManager;
    import edu.ufp.streaming.rec.models.Content;
    import edu.ufp.streaming.rec.models.Documentary;
    import edu.ufp.streaming.rec.models.Genre;
    import edu.ufp.streaming.rec.models.Movie;
    import edu.ufp.streaming.rec.models.Series;
    import edu.ufp.streaming.rec.models.Interation;
    import edu.ufp.streaming.rec.enums.InterationType;
    import edu.ufp.streaming.rec.managers.ContentFileManager;

    import java.time.LocalDate;
    import java.time.LocalDateTime;
    import java.util.List;

    /**
     * Casos de teste para as estruturas de dados de Conteúdos e Géneros.
     * Testa os requisitos R2, R3, R4 e R5 do enunciado.
     *
     * @author Pedro
     * @version 1.0
     */
    public class TestContent {

        /**
         * Ponto de entrada — executa todos os casos de teste.
         *
         * @param args argumentos da linha de comandos (não utilizados)
         */
        public static void main(String[] args) {
            System.out.println("========================================");
            System.out.println(" TestContent — Phase 1 Test Suite");
            System.out.println("========================================\n");

            testGenreManager();
            testContentManagerInsertAndList();
            testContentManagerSearch();
            testContentManagerEdit();
            testContentManagerRemove();
            testContentManagerR4Consistency();
            testContentBSTOrdered();
            testContentBSTDateRange();
            testInteration();
            testFileImportExport();

            System.out.println("========================================");
            System.out.println(" All TestContent tests completed.");
            System.out.println("========================================");
        }

        /** Cria um género de teste. */
        private static Genre genre(String id, String name) {
            return new Genre(id, name);
        }

        /** Cria um filme de teste. */
        private static Movie movie(String id, String title, Genre g, LocalDate date) {
            return new Movie(id, title, g, date, 120, "PT", null);
        }

        /** Cria uma série de teste. */
        private static Series series(String id, String title, Genre g, LocalDate date, int seasons) {
            return new Series(id, title, g, date, 45, "PT", seasons);
        }

        /** Cria um documentário de teste. */
        private static Documentary documentary(String id, String title, Genre g, LocalDate date) {
            return new Documentary(id, title, g, date, 90, "PT", "Natureza", "David Attenborough");
        }

        /** Cria um ContentManager com BST associada. */
        private static ContentManager newCm() {
            return new ContentManager(new ContentBST());
        }

        /**
         * Testa as operações básicas do GenreManager (R2).
         */
        public static void testGenreManager() {
            System.out.println("--- testGenreManager ---");
            GenreManager gm = new GenreManager();
            Genre g1 = genre("G01", "Acao");
            Genre g2 = genre("G02", "Drama");
            Genre g3 = genre("G03", "Documentario");

            assert gm.insert(g1) : "Should insert G01";
            assert gm.insert(g2) : "Should insert G02";
            assert gm.insert(g3) : "Should insert G03";
            assert !gm.insert(g1) : "Should reject duplicate G01";
            assert gm.size() == 3 : "Size should be 3";

            assert gm.editName("G01", "Acao e Aventura") : "Should edit G01";
            assert gm.get("G01").getName().equals("Acao e Aventura") : "Name should be updated";

            Genre removed = gm.remove("G03");
            assert removed != null : "Should remove G03";
            assert gm.size() == 2 : "Size should be 2";
            assert gm.listAll().size() == 2 : "listAll should return 2";

            System.out.println("PASS: GenreManager\n");
        }

        /**
         * Testa inserção e listagem no ContentManager (R2).
         */
        public static void testContentManagerInsertAndList() {
            System.out.println("--- testContentManagerInsertAndList ---");
            ContentManager cm = newCm();
            Genre g1 = genre("G01", "Acao");
            Genre g2 = genre("G02", "Drama");

            Movie m1 = movie("C01", "Inception", g1, LocalDate.of(2010, 7, 16));
            Movie m2 = movie("C02", "Interstellar", g2, LocalDate.of(2014, 11, 7));
            Series s1 = series("C03", "Breaking Bad", g2, LocalDate.of(2008, 1, 20), 5);
            Documentary d1 = documentary("C04", "Planet Earth", g1, LocalDate.of(2006, 3, 5));

            assert cm.insert(m1) : "Should insert m1";
            assert cm.insert(m2) : "Should insert m2";
            assert cm.insert(s1) : "Should insert s1";
            assert cm.insert(d1) : "Should insert d1";
            assert !cm.insert(m1) : "Should reject duplicate";
            assert cm.size() == 4 : "Size should be 4";
            assert cm.listMovies().size() == 2 : "Should list 2 movies";
            assert cm.listSeries().size() == 1 : "Should list 1 series";
            assert cm.listDocumentaries().size() == 1 : "Should list 1 documentary";

            System.out.println("PASS: ContentManager insert and list\n");
        }

        /**
         * Testa as pesquisas do ContentManager (R2, R3).
         */
        public static void testContentManagerSearch() {
            System.out.println("--- testContentManagerSearch ---");
            ContentManager cm = newCm();
            Genre g1 = genre("G01", "Acao");
            Genre g2 = genre("G02", "Drama");

            Movie m1 = movie("C01", "Inception", g1, LocalDate.of(2010, 7, 16));
            Movie m2 = movie("C02", "Interstellar", g2, LocalDate.of(2014, 11, 7));
            Series s1 = series("C03", "Breaking Bad", g2, LocalDate.of(2008, 1, 20), 5);
            cm.insert(m1);
            cm.insert(m2);
            cm.insert(s1);

            assert cm.searchByTitleSubstring("inter").size() == 1 : "Should find 1 for 'inter'";
            assert cm.searchByGenre("G02").size() == 2 : "Should find 2 Drama";
            assert cm.searchByRegion("PT").size() == 3 : "Should find 3 in PT";

            m1.setRating(4.5);
            m2.setRating(3.0);
            assert cm.searchByMinRating(4.0).size() == 1 : "Should find 1 with rating >= 4.0";

            System.out.println("PASS: ContentManager search\n");
        }

        /**
         * Testa a edição de conteúdos no ContentManager (R2).
         */
        public static void testContentManagerEdit() {
            System.out.println("--- testContentManagerEdit ---");
            ContentManager cm = newCm();
            Genre g1 = genre("G01", "Acao");
            Movie m1 = movie("C01", "Inception", g1, LocalDate.of(2010, 7, 16));
            cm.insert(m1);

            assert cm.editTitle("C01", "Inception 2") : "Should edit title";
            assert cm.get("C01").getTitle().equals("Inception 2") : "Title should be updated";
            assert !cm.editTitle("C99", "X") : "Should return false for unknown id";

            System.out.println("PASS: ContentManager edit\n");
        }

        /**
         * Testa a remoção de conteúdos no ContentManager (R2).
         */
        public static void testContentManagerRemove() {
            System.out.println("--- testContentManagerRemove ---");
            ContentManager cm = newCm();
            Genre g1 = genre("G01", "Acao");
            Movie m1 = movie("C01", "Inception", g1, LocalDate.of(2010, 7, 16));
            cm.insert(m1);

            Content removed = cm.remove("C01");
            assert removed != null : "Should return removed content";
            assert cm.size() == 0 : "Size should be 0";
            assert cm.remove("C01") == null : "Removing again should return null";

            System.out.println("PASS: ContentManager remove\n");
        }

        /**
         * Testa a consistência entre ST e BST ao remover conteúdos (R4).
         */
        public static void testContentManagerR4Consistency() {
            System.out.println("--- testContentManagerR4Consistency ---");
            ContentBST bst = new ContentBST();
            ContentManager cm = new ContentManager(bst);
            Genre g1 = genre("G01", "Acao");

            Movie m1 = movie("C01", "Inception", g1, LocalDate.of(2010, 7, 16));
            cm.insert(m1);

            assert bst.size() == 1 : "BST should have 1 after insert";
            cm.remove("C01");
            assert cm.size() == 0 : "ST should be empty after remove";
            assert bst.size() == 0 : "BST should also be empty after remove (R4)";

            System.out.println("PASS: R4 consistency\n");
        }

        /**
         * Testa a ordenação cronológica da ContentBST (R3).
         */
        public static void testContentBSTOrdered() {
            System.out.println("--- testContentBSTOrdered ---");
            ContentBST bst = new ContentBST();
            Genre g1 = genre("G01", "Acao");

            Movie m1 = movie("C01", "Inception", g1, LocalDate.of(2010, 7, 16));
            Movie m2 = movie("C02", "Interstellar", g1, LocalDate.of(2014, 11, 7));
            Series s1 = series("C03", "Breaking Bad", g1, LocalDate.of(2008, 1, 20), 5);
            bst.insert(m1);
            bst.insert(m2);
            bst.insert(s1);

            assert bst.size() == 3 : "Size should be 3";
            assert bst.getOldestDate().equals(LocalDate.of(2008, 1, 20)) : "Oldest should be 2008";
            assert bst.getNewestDate().equals(LocalDate.of(2014, 11, 7)) : "Newest should be 2014";

            List<Movie> movies = bst.getMoviesOrdered();
            assert movies.size() == 2 : "Should return 2 movies ordered";
            assert movies.get(0).getId().equals("C01") : "First movie should be Inception (2010)";

            System.out.println("PASS: ContentBST ordered\n");
        }

        /**
         * Testa a pesquisa por intervalo de datas na ContentBST (R3).
         */
        public static void testContentBSTDateRange() {
            System.out.println("--- testContentBSTDateRange ---");
            ContentBST bst = new ContentBST();
            Genre g1 = genre("G01", "Drama");

            bst.insert(movie("C01", "Film A", g1, LocalDate.of(2005, 1, 1)));
            bst.insert(movie("C02", "Film B", g1, LocalDate.of(2010, 6, 15)));
            bst.insert(movie("C03", "Film C", g1, LocalDate.of(2020, 3, 10)));

            List<Content> range = bst.getByDateRange(
                    LocalDate.of(2008, 1, 1), LocalDate.of(2015, 1, 1));
            assert range.size() == 1 : "Should find 1 in range (got " + range.size() + ")";
            assert range.get(0).getId().equals("C02") : "Should be Film B";

            System.out.println("PASS: ContentBST date range\n");
        }

        /**
         * Testa a criação e atributos da classe Interation.
         */
        public static void testInteration() {
            System.out.println("--- testInteration ---");
            Genre g1 = genre("G01", "Acao");
            Movie m1 = movie("C01", "Inception", g1, LocalDate.of(2010, 7, 16));

            Interation i1 = new Interation(null, m1,
                    LocalDateTime.of(2024, 3, 10, 20, 30),
                    0.0, 0.75, InterationType.WATCH, "I01");
            assert i1.getId().equals("I01") : "ID should be I01";
            assert i1.getType() == InterationType.WATCH : "Type should be WATCH";
            assert i1.getProgress() == 0.75 : "Progress should be 0.75";

            Interation i2 = new Interation(null, m1,
                    LocalDateTime.of(2024, 3, 10, 22, 0),
                    4.5, 1.0, InterationType.RATE, "I02");
            assert i2.getRating() == 4.5 : "Rating should be 4.5";

            System.out.println("PASS: Interation\n");
        }
        /**
         * Testa a exportação e importação de dados em ficheiros txt (R10).
         */
        public static void testFileImportExport() {
            System.out.println("--- testFileImportExport ---");
            ContentBST bst = new ContentBST();
            ContentManager cm = new ContentManager(bst);
            GenreManager gm = new GenreManager();

            Genre g1 = genre("G01", "Acao");
            Genre g2 = genre("G02", "Drama");
            gm.insert(g1);
            gm.insert(g2);

            cm.insert(movie("C01", "Inception", g1, LocalDate.of(2010, 7, 16)));
            cm.insert(series("C02", "Breaking Bad", g2, LocalDate.of(2008, 1, 20), 5));
            cm.insert(new Documentary("C03", "Planet Earth", g1,
                    LocalDate.of(2006, 3, 5), 90, "PT", "Natureza", "David Attenborough"));

            ContentFileManager.exportGenres(gm, "genres.txt");
            ContentFileManager.exportContents(cm, "contents.txt");

            GenreManager gm2 = new GenreManager();
            ContentBST bst2 = new ContentBST();
            ContentManager cm2 = new ContentManager(bst2);

            ContentFileManager.importGenres(gm2, "genres.txt");
            ContentFileManager.importContents(cm2, gm2, "contents.txt");

            assert gm2.size() == 2 : "Should import 2 genres";
            assert cm2.size() == 3 : "Should import 3 contents";
            assert cm2.get("C01").getTitle().equals("Inception") : "Title should match";

            System.out.println("PASS: File import/export\n");
        }
    }