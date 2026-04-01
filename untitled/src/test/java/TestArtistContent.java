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
    // Ponto de entrada
    // -----------------------------------------------------------------------

    /**
     * Executa todos os casos de teste da classe ArtistContent.
     *
     * @param args não utilizado
     */
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println(" TestArtistContent — Testes Fase 1");
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
        System.out.println(" Todos os testes de ArtistContent concluídos.");
        System.out.println("========================================");
    }

    // -----------------------------------------------------------------------
    // Auxiliares de criação de dados (Fixtures)
    // -----------------------------------------------------------------------

    /** Cria um Genre (Género) minimalista para uso em Content (Conteúdo). */
    private static Genre genre(String id, String name) {
        return new Genre(id, name);
    }

    /** Cria um Movie (Filme) sem realizador definido via ArtistContentManager. */
    private static Movie movie(String id, String title, Genre g, LocalDate date) {
        return new Movie(id, title, g, date, 120, "PT", null);
    }

    /** Cria um Artist (Artista). */
    private static Artist artist(String id, String name, ArtistRole role) {
        return new Artist(id, name, "PT", "M", LocalDate.of(1980, 1, 1), role);
    }

    // -----------------------------------------------------------------------
    // Testes
    // -----------------------------------------------------------------------

    /**
     * Testa a adição de uma participação e a recuperação da filmografia.
     */
    public static void testAddAndGetFilmography() {
        System.out.println("--- testAddAndGetFilmography ---");
        ArtistContentManager acm = new ArtistContentManager();

        Artist a1 = artist("a1", "Ator Um", ArtistRole.ACTOR);
        Genre  g1 = genre("g1", "Ação");
        Movie  m1 = movie("m1", "Filme Alpha", g1, LocalDate.of(2020, 5, 1));

        ArtistContent ac = acm.addParticipation(a1, m1, ArtistRole.ACTOR, LocalDate.of(2020, 5, 1));
        assert ac != null : "Deveria criar a participação";
        assert acm.size() == 1 : "O tamanho deveria ser 1";

        List<ArtistContent> filmography = acm.getFilmography("a1");
        assert filmography.size() == 1 : "A filmografia deveria ter 1 entrada";
        assert filmography.get(0).getContent().getId().equals("m1") : "O conteúdo deveria ser m1";

        // A lista interna do Artista também deve ser atualizada
        assert a1.getFilmography().size() == 1 : "A filmografia interna do Artista deve estar atualizada";

        System.out.println("PASSOU: addParticipation / getFilmography\n");
    }

    /**
     * Testa se participações duplicadas (mesmo artista, conteúdo e papel) são rejeitadas.
     */
    public static void testDuplicateParticipation() {
        System.out.println("--- testDuplicateParticipation ---");
        ArtistContentManager acm = new ArtistContentManager();

        Artist a1 = artist("a1", "Ator Um", ArtistRole.ACTOR);
        Movie  m1 = movie("m1", "Filme Alpha", genre("g1", "Ação"), LocalDate.of(2020, 5, 1));

        acm.addParticipation(a1, m1, ArtistRole.ACTOR, LocalDate.of(2020, 5, 1));
        ArtistContent dup = acm.addParticipation(a1, m1, ArtistRole.ACTOR, LocalDate.of(2020, 5, 1));

        assert dup == null : "Participação duplicada deveria retornar null";
        assert acm.size() == 1 : "O tamanho deveria manter-se em 1";

        // Mesmo artista, papel diferente — deve ser permitido
        ArtistContent diff = acm.addParticipation(a1, m1, ArtistRole.DIRECTOR, LocalDate.of(2020, 5, 1));
        assert diff != null : "Mesmo artista com papel diferente deve ser permitido";
        assert acm.size() == 2 : "O tamanho deveria ser 2";

        System.out.println("PASSOU: rejeição de duplicados / papel diferente permitido\n");
    }

    /**
     * Testa getCastAndCrew — todos os artistas ligados a um item de conteúdo.
     */
    public static void testGetCastAndCrew() {
        System.out.println("--- testGetCastAndCrew ---");
        ArtistContentManager acm = new ArtistContentManager();
        Genre g1 = genre("g1", "Drama");
        Movie m1 = movie("m1", "Filme Beta", g1, LocalDate.of(2021, 3, 10));

        Artist a1 = artist("a1", "Ator A", ArtistRole.ACTOR);
        Artist a2 = artist("a2", "Ator B", ArtistRole.ACTOR);
        Artist a3 = artist("a3", "Realizador C", ArtistRole.DIRECTOR);

        acm.addParticipation(a1, m1, ArtistRole.ACTOR,    LocalDate.of(2021, 3, 10));
        acm.addParticipation(a2, m1, ArtistRole.ACTOR,    LocalDate.of(2021, 3, 10));
        acm.addParticipation(a3, m1, ArtistRole.DIRECTOR, LocalDate.of(2021, 3, 10));

        List<ArtistContent> crew = acm.getCastAndCrew("m1");
        assert crew.size() == 3 : "Elenco+equipa deve ter 3 entradas (obteve " + crew.size() + ")";

        List<ArtistContent> empty = acm.getCastAndCrew("m99");
        assert empty.isEmpty() : "Conteúdo desconhecido deve retornar lista vazia";

        System.out.println("PASSOU: getCastAndCrew\n");
    }

    /**
     * Testa a filtragem de getFilmographyByRole (filmografia por papel).
     */
    public static void testGetFilmographyByRole() {
        System.out.println("--- testGetFilmographyByRole ---");
        ArtistContentManager acm = new ArtistContentManager();
        Genre g1 = genre("g1", "Ação");

        Artist a1 = artist("a1", "Multi-talento", ArtistRole.ACTOR);
        Movie  m1 = movie("m1", "Filme 1", g1, LocalDate.of(2019, 1, 1));
        Movie  m2 = movie("m2", "Filme 2", g1, LocalDate.of(2020, 1, 1));
        Movie  m3 = movie("m3", "Filme 3", g1, LocalDate.of(2021, 1, 1));

        acm.addParticipation(a1, m1, ArtistRole.ACTOR,    LocalDate.of(2019, 1, 1));
        acm.addParticipation(a1, m2, ArtistRole.ACTOR,    LocalDate.of(2020, 1, 1));
        acm.addParticipation(a1, m3, ArtistRole.DIRECTOR, LocalDate.of(2021, 1, 1));

        List<ArtistContent> asActor = acm.getFilmographyByRole("a1", ArtistRole.ACTOR);
        assert asActor.size() == 2 : "Deveria encontrar 2 papéis de ator";

        List<ArtistContent> asDirector = acm.getFilmographyByRole("a1", ArtistRole.DIRECTOR);
        assert asDirector.size() == 1 : "Deveria encontrar 1 papel de realizador";

        System.out.println("PASSOU: getFilmographyByRole\n");
    }

    /**
     * Testa getFilmographyByDateRange (filmografia por intervalo de datas) para um artista específico.
     */
    public static void testGetFilmographyByDateRange() {
        System.out.println("--- testGetFilmographyByDateRange ---");
        ArtistContentManager acm = new ArtistContentManager();
        Genre g1 = genre("g1", "Thriller");

        Artist a1 = artist("a1", "Ator Veterano", ArtistRole.ACTOR);
        Movie m1 = movie("m1", "Filme Antigo",    g1, LocalDate.of(2000, 1, 1));
        Movie m2 = movie("m2", "Filme Recente", g1, LocalDate.of(2022, 6, 15));
        Movie m3 = movie("m3", "Filme Novo",    g1, LocalDate.of(2023, 3, 20));

        acm.addParticipation(a1, m1, ArtistRole.ACTOR, LocalDate.of(2000, 1, 1));
        acm.addParticipation(a1, m2, ArtistRole.ACTOR, LocalDate.of(2022, 6, 15));
        acm.addParticipation(a1, m3, ArtistRole.ACTOR, LocalDate.of(2023, 3, 20));

        List<ArtistContent> recent = acm.getFilmographyByDateRange(
                "a1", LocalDate.of(2022, 1, 1), LocalDate.of(2023, 12, 31));
        assert recent.size() == 2 : "Esperadas 2 participações recentes (obteve " + recent.size() + ")";

        List<ArtistContent> old = acm.getFilmographyByDateRange(
                "a1", LocalDate.of(1990, 1, 1), LocalDate.of(2005, 12, 31));
        assert old.size() == 1 : "Esperada 1 participação antiga";

        System.out.println("PASSOU: getFilmographyByDateRange\n");
    }

    /**
     * Testa getAllByDateRange para todos os artistas.
     */
    public static void testGetAllByDateRange() {
        System.out.println("--- testGetAllByDateRange ---");
        ArtistContentManager acm = new ArtistContentManager();
        Genre g1 = genre("g1", "Comédia");

        Artist a1 = artist("a1", "Ator A", ArtistRole.ACTOR);
        Artist a2 = artist("a2", "Ator B", ArtistRole.ACTOR);
        Movie  m1 = movie("m1", "Filme X", g1, LocalDate.of(2021, 4, 10));
        Movie  m2 = movie("m2", "Filme Y", g1, LocalDate.of(2022, 8, 20));
        Movie  m3 = movie("m3", "Filme Z", g1, LocalDate.of(2015, 1, 1));

        acm.addParticipation(a1, m1, ArtistRole.ACTOR, LocalDate.of(2021, 4, 10));
        acm.addParticipation(a2, m2, ArtistRole.ACTOR, LocalDate.of(2022, 8, 20));
        acm.addParticipation(a1, m3, ArtistRole.ACTOR, LocalDate.of(2015, 1, 1));

        List<ArtistContent> range = acm.getAllByDateRange(
                LocalDate.of(2020, 1, 1), LocalDate.of(2023, 12, 31));
        assert range.size() == 2 : "Esperadas 2 participações entre 2020-2023 (obteve " + range.size() + ")";

        System.out.println("PASSOU: getAllByDateRange\n");
    }

    /**
     * Testa os métodos de conveniência getDirectors (realizadores) e getActors (atores).
     */
    public static void testGetDirectorsAndActors() {
        System.out.println("--- testGetDirectorsAndActors ---");
        ArtistContentManager acm = new ArtistContentManager();
        Genre g1 = genre("g1", "Sci-Fi");
        Movie m1 = movie("m1", "Épico Espacial", g1, LocalDate.of(2023, 7, 4));

        Artist dir  = artist("d1", "Grande Realizador", ArtistRole.DIRECTOR);
        Artist act1 = artist("a1", "Ator Principal",   ArtistRole.ACTOR);
        Artist act2 = artist("a2", "Ator Secundário", ArtistRole.ACTOR);

        acm.addParticipation(dir,  m1, ArtistRole.DIRECTOR, LocalDate.of(2023, 7, 4));
        acm.addParticipation(act1, m1, ArtistRole.ACTOR,    LocalDate.of(2023, 7, 4));
        acm.addParticipation(act2, m1, ArtistRole.ACTOR,    LocalDate.of(2023, 7, 4));

        List<Artist> directors = acm.getDirectors("m1");
        assert directors.size() == 1 && directors.get(0).getId().equals("d1")
                : "Deveria encontrar 1 realizador";

        List<Artist> actors = acm.getActors("m1");
        assert actors.size() == 2 : "Deveria encontrar 2 atores";

        System.out.println("PASSOU: getDirectors / getActors\n");
    }

    /**
     * Testa a verificação de hasParticipation (tem participação).
     */
    public static void testHasParticipation() {
        System.out.println("--- testHasParticipation ---");
        ArtistContentManager acm = new ArtistContentManager();
        Genre g1 = genre("g1", "Terror");
        Artist a1 = artist("a1", "Estrela do Terror", ArtistRole.ACTOR);
        Movie  m1 = movie("m1", "Filme Assustador", g1, LocalDate.of(2022, 10, 31));

        acm.addParticipation(a1, m1, ArtistRole.ACTOR, LocalDate.of(2022, 10, 31));

        assert  acm.hasParticipation("a1", "m1") : "Deveria ter participação";
        assert !acm.hasParticipation("a1", "m99") : "Não deveria ter participação para conteúdo desconhecido";
        assert !acm.hasParticipation("a99", "m1") : "Não deveria ter participação para artista desconhecido";

        System.out.println("PASSOU: hasParticipation\n");
    }

    /**
     * Testa a remoção de um registo específico de participação.
     */
    public static void testRemoveParticipation() {
        System.out.println("--- testRemoveParticipation ---");
        ArtistContentManager acm = new ArtistContentManager();
        Genre g1 = genre("g1", "Drama");
        Artist a1 = artist("a1", "Ator Dramático", ArtistRole.ACTOR);
        Movie  m1 = movie("m1", "Filme de Drama", g1, LocalDate.of(2021, 2, 14));

        acm.addParticipation(a1, m1, ArtistRole.ACTOR, LocalDate.of(2021, 2, 14));
        assert acm.size() == 1;

        ArtistContent removed = acm.removeParticipation("a1", "m1", ArtistRole.ACTOR);
        assert removed != null : "Deveria retornar o registo removido";
        assert acm.size() == 0 : "O tamanho deveria ser 0 após a remoção";
        assert acm.getFilmography("a1").isEmpty() : "O índice de filmografia deveria estar vazio";
        assert acm.getCastAndCrew("m1").isEmpty() : "O índice de conteúdo deveria estar vazio";

        ArtistContent notFound = acm.removeParticipation("a1", "m1", ArtistRole.ACTOR);
        assert notFound == null : "Remover novamente deveria retornar null";

        System.out.println("PASSOU: removeParticipation\n");
    }

    /**
     * Testa removeAllByArtist — remove todas as participações de um determinado artista.
     */
    public static void testRemoveAllByArtist() {
        System.out.println("--- testRemoveAllByArtist ---");
        ArtistContentManager acm = new ArtistContentManager();
        Genre g1 = genre("g1", "Ação");
        Artist a1 = artist("a1", "Ator Prolífico", ArtistRole.ACTOR);
        Movie  m1 = movie("m1", "Filme 1", g1, LocalDate.of(2020, 1, 1));
        Movie  m2 = movie("m2", "Filme 2", g1, LocalDate.of(2021, 1, 1));
        Movie  m3 = movie("m3", "Filme 3", g1, LocalDate.of(2022, 1, 1));

        acm.addParticipation(a1, m1, ArtistRole.ACTOR, LocalDate.of(2020, 1, 1));
        acm.addParticipation(a1, m2, ArtistRole.ACTOR, LocalDate.of(2021, 1, 1));
        acm.addParticipation(a1, m3, ArtistRole.ACTOR, LocalDate.of(2022, 1, 1));
        assert acm.size() == 3;

        acm.removeAllByArtist("a1");
        assert acm.size() == 0 : "Todas as participações deveriam ser removidas";
        assert acm.getFilmography("a1").isEmpty() : "A filmografia deveria estar vazia";
        // Os índices de conteúdo também devem ser limpos
        assert acm.getCastAndCrew("m1").isEmpty() : "O elenco de m1 deveria estar vazio";
        assert acm.getCastAndCrew("m2").isEmpty() : "O elenco de m2 deveria estar vazio";

        System.out.println("PASSOU: removeAllByArtist\n");
    }

    /**
     * Testa removeAllByContent — remove todas as participações de um determinado item de conteúdo.
     */
    public static void testRemoveAllByContent() {
        System.out.println("--- testRemoveAllByContent ---");
        ArtistContentManager acm = new ArtistContentManager();
        Genre g1 = genre("g1", "Aventura");
        Movie m1 = movie("m1", "Grande Filme", g1, LocalDate.of(2023, 5, 5));

        Artist a1 = artist("a1", "Ator 1", ArtistRole.ACTOR);
        Artist a2 = artist("a2", "Ator 2", ArtistRole.ACTOR);
        Artist a3 = artist("a3", "Realizador", ArtistRole.DIRECTOR);

        acm.addParticipation(a1, m1, ArtistRole.ACTOR,    LocalDate.of(2023, 5, 5));
        acm.addParticipation(a2, m1, ArtistRole.ACTOR,    LocalDate.of(2023, 5, 5));
        acm.addParticipation(a3, m1, ArtistRole.DIRECTOR, LocalDate.of(2023, 5, 5));
        assert acm.size() == 3;

        acm.removeAllByContent("m1");
        assert acm.size() == 0 : "Todas as participações deveriam ser removidas";
        assert acm.getCastAndCrew("m1").isEmpty() : "O índice de conteúdo deveria estar vazio";
        assert acm.getFilmography("a1").isEmpty() : "O índice de artista deveria ser limpo";

        System.out.println("PASSOU: removeAllByContent\n");
    }

    /**
     * Testa a consistência R4: quando um artista é removido do ArtistManager,
     * as suas participações também devem ser limpas via ArtistContentManager.
     */
    public static void testR4ConsistencyArtistRemoval() {
        System.out.println("--- testR4ConsistencyArtistRemoval ---");
        ArtistManager am = new ArtistManager();
        ArtistContentManager acm = new ArtistContentManager();

        Genre g1 = genre("g1", "Drama");
        Artist a1 = new Artist("a1", "Artista Temporário", "PT", "M",
                LocalDate.of(1985, 3, 20), ArtistRole.ACTOR);
        Movie m1 = movie("m1", "Algum Filme", g1, LocalDate.of(2022, 4, 1));

        am.insert(a1);
        acm.addParticipation(a1, m1, ArtistRole.ACTOR, LocalDate.of(2022, 4, 1));

        // Simular eliminação de artista: limpar participações primeiro, depois remover da ST
        acm.removeAllByArtist("a1");
        am.remove("a1");

        assert am.get("a1") == null : "O artista deveria ser removido da ST";
        assert acm.getFilmography("a1").isEmpty() : "A filmografia deveria estar vazia";
        assert acm.getCastAndCrew("m1").isEmpty() : "A equipa do conteúdo m1 deveria estar vazia";

        System.out.println("PASSOU: Consistência R4 na remoção de artista\n");
    }
}