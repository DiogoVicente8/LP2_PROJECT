import edu.ufp.streaming.rec.enums.ArtistRole;
import edu.ufp.streaming.rec.enums.InterationType;
import edu.ufp.streaming.rec.managers.*;
import edu.ufp.streaming.rec.models.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Testes ao StreamingGraph — Fase 2 (R8a até R8g).
 *
 * @author Diogo Vicente & Pedro
 */
public class TestStreamingGraph {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println(" TestStreamingGraph — Testes Fase 2");
        System.out.println("========================================\n");

        testR8a_CaminhoMaisCurto();
        testR8b_SubgrafoByRegion();
        testR8b_SubgrafoByGenre();
        testR8c_GrafoConexo();
        testR8d_Recomendacoes();
        testR8e_EstatisticasVisualizacao();
        testR8f_UtilizadoresSeriesGenero();
        testR8g_SeguidoresQueViramConteudo();

        System.out.println("========================================");
        System.out.println(" Todos os testes do StreamingGraph concluídos.");
        System.out.println("========================================");
    }

    // -----------------------------------------------------------------------
    // Fixtures — dados reutilizáveis
    // -----------------------------------------------------------------------

    private static StreamingDatabase buildDB() {
        StreamingDatabase db = new StreamingDatabase();

        // Géneros
        Genre gAcao  = new Genre("g1", "Acao");
        Genre gDrama = new Genre("g2", "Drama");
        db.addGenre(gAcao);
        db.addGenre(gDrama);

        // Utilizadores
        User u1 = new User("u1", "Alice", "alice@mail.com", "PT", LocalDate.of(2020, 1, 1));
        User u2 = new User("u2", "Bruno", "bruno@mail.com", "PT", LocalDate.of(2020, 2, 1));
        User u3 = new User("u3", "Carla", "carla@mail.com", "BR", LocalDate.of(2021, 3, 1));
        User u4 = new User("u4", "David", "david@mail.com", "US", LocalDate.of(2022, 4, 1));
        db.addUser(u1); db.addUser(u2); db.addUser(u3); db.addUser(u4);

        // Conteúdos
        Movie  m1 = new Movie ("c1", "Inception",    gAcao,  LocalDate.of(2010, 7, 16), 148, "PT", null);
        Series s1 = new Series("c2", "Breaking Bad", gDrama, LocalDate.of(2008, 1, 20),  45, "PT", 5);
        Series s2 = new Series("c3", "Dark",         gAcao,  LocalDate.of(2017, 12, 1),  60, "BR", 3);
        db.addContent(m1); db.addContent(s1); db.addContent(s2);

        // Follows: u1→u2, u2→u3, u3→u4
        db.addFollow("u1", "u2");
        db.addFollow("u2", "u3");
        db.addFollow("u3", "u4");

        // Interações
        // u1 viu c1 (Inception)
        Interation i1 = new Interation(u1, m1, LocalDateTime.of(2024, 1, 10, 20, 0),
                0, 0.9, InterationType.WATCH, "i1");
        db.addInteraction(i1);

        // u2 viu c1 e c2
        Interation i2 = new Interation(u2, m1, LocalDateTime.of(2024, 1, 15, 21, 0),
                0, 1.0, InterationType.WATCH, "i2");
        Interation i3 = new Interation(u2, s1, LocalDateTime.of(2024, 2, 1, 19, 0),
                0, 0.5, InterationType.WATCH, "i3");
        db.addInteraction(i2);
        db.addInteraction(i3);

        // u2 avaliou c1
        Interation i4 = new Interation(u2, m1, LocalDateTime.of(2024, 1, 16, 10, 0),
                4.5, 1.0, InterationType.RATE, "i4");
        db.addInteraction(i4);

        // u3 viu c3 (Dark — série de Ação)
        Interation i5 = new Interation(u3, s2, LocalDateTime.of(2024, 3, 5, 20, 0),
                0, 0.8, InterationType.WATCH, "i5");
        db.addInteraction(i5);

        // u4 viu c2 (Breaking Bad — série de Drama)
        Interation i6 = new Interation(u4, s1, LocalDateTime.of(2024, 3, 10, 22, 0),
                0, 0.6, InterationType.WATCH, "i6");
        db.addInteraction(i6);

        return db;
    }

    // -----------------------------------------------------------------------
    // R8a — Caminho mais curto entre utilizadores
    // -----------------------------------------------------------------------

    public static void testR8a_CaminhoMaisCurto() {
        System.out.println("--- R8a: caminhoMaisCurtoBetweenUsers ---");
        StreamingDatabase db = buildDB();
        StreamingGraph g = db.getGraph();

        // u1 → u2 (direto)
        List<String> caminho12 = g.caminhoMaisCurtoBetweenUsers("u1", "u2");
        assert !caminho12.isEmpty() : "Deve existir caminho u1→u2";
        assert caminho12.get(0).equals("u1") : "Caminho deve começar em u1";
        assert caminho12.get(caminho12.size() - 1).equals("u2") : "Caminho deve terminar em u2";
        System.out.println("  u1→u2: " + caminho12);

        // u1 → u4 (via u2 e u3)
        List<String> caminho14 = g.caminhoMaisCurtoBetweenUsers("u1", "u4");
        assert !caminho14.isEmpty() : "Deve existir caminho u1→u4";
        assert caminho14.contains("u2") : "Caminho u1→u4 deve passar por u2";
        assert caminho14.contains("u3") : "Caminho u1→u4 deve passar por u3";
        System.out.println("  u1→u4: " + caminho14);

        // u4 → u1 (não existe — grafo direcionado)
        List<String> caminhoInverso = g.caminhoMaisCurtoBetweenUsers("u4", "u1");
        assert caminhoInverso.isEmpty() : "Não deve existir caminho u4→u1";
        System.out.println("  u4→u1 (inexistente): " + caminhoInverso);

        // ID desconhecido
        List<String> caminhoInvalido = g.caminhoMaisCurtoBetweenUsers("u99", "u1");
        assert caminhoInvalido.isEmpty() : "ID desconhecido deve retornar lista vazia";

        // Peso do caminho
        double peso = g.pesoCaminhoMaisCurto("u1", "u2");
        assert peso != Double.POSITIVE_INFINITY : "Peso do caminho u1→u2 deve ser finito";
        System.out.println("  Peso u1→u2: " + peso);

        System.out.println("PASSOU: R8a\n");
    }

    // -----------------------------------------------------------------------
    // R8b — Extração de subgrafos por região
    // -----------------------------------------------------------------------

    public static void testR8b_SubgrafoByRegion() {
        System.out.println("--- R8b: subgrafoByRegion ---");
        StreamingDatabase db = buildDB();
        StreamingGraph g = db.getGraph();

        // Subgrafo PT: u1 e u2 (u1→u2 existe, u2→u1 não)
        var subPT = g.subgrafoByRegion("PT", db.users());
        assert subPT.E() >= 1 : "Subgrafo PT deve ter pelo menos 1 aresta (u1→u2)";
        System.out.println("  Arestas subgrafo PT: " + subPT.E());

        // Subgrafo BR: apenas u3 — sem arestas internas
        var subBR = g.subgrafoByRegion("BR", db.users());
        assert subBR.E() == 0 : "Subgrafo BR deve ter 0 arestas (u3 não segue ninguém do BR)";
        System.out.println("  Arestas subgrafo BR: " + subBR.E());

        System.out.println("PASSOU: R8b subgrafoByRegion\n");
    }

    // -----------------------------------------------------------------------
    // R8b — Extração de subgrafos por género
    // -----------------------------------------------------------------------

    public static void testR8b_SubgrafoByGenre() {
        System.out.println("--- R8b: subgrafoByGenre ---");
        StreamingDatabase db = buildDB();
        StreamingGraph g = db.getGraph();

        // Subgrafo g1 (Ação): c1 (Inception) e c3 (Dark)
        // u1→c1 (WATCH), u2→c1 (WATCH+RATE), u3→c3 (WATCH)
        var subAcao = g.subgrafoByGenre("g1", db.contents());
        assert subAcao.E() >= 3 : "Subgrafo Ação deve ter pelo menos 3 arestas";
        System.out.println("  Arestas subgrafo Ação: " + subAcao.E());

        // Subgrafo g2 (Drama): c2 (Breaking Bad)
        // u2→c2 (WATCH), u4→c2 (WATCH)
        var subDrama = g.subgrafoByGenre("g2", db.contents());
        assert subDrama.E() >= 2 : "Subgrafo Drama deve ter pelo menos 2 arestas";
        System.out.println("  Arestas subgrafo Drama: " + subDrama.E());

        System.out.println("PASSOU: R8b subgrafoByGenre\n");
    }

    // -----------------------------------------------------------------------
    // R8c — Verificar se grafo é conexo
    // -----------------------------------------------------------------------

    public static void testR8c_GrafoConexo() {
        System.out.println("--- R8c: isGrafoUtilizadoresConexo ---");
        StreamingDatabase db = buildDB();
        StreamingGraph g = db.getGraph();

        // u1→u2→u3→u4 mas u4 não segue ninguém — não é fortemente conexo
        boolean conexo = g.isGrafoUtilizadoresConexo();
        assert !conexo : "O grafo não deve ser fortemente conexo (u4 não tem saídas)";
        System.out.println("  Grafo conexo (esperado false): " + conexo);

        // Tornar conexo: adicionar u4→u1
        db.addFollow("u4", "u1");
        boolean conexoAgora = g.isGrafoUtilizadoresConexo();
        assert conexoAgora : "Após u4→u1, o grafo deve ser fortemente conexo";
        System.out.println("  Grafo conexo após u4→u1 (esperado true): " + conexoAgora);

        System.out.println("PASSOU: R8c\n");
    }

    // -----------------------------------------------------------------------
    // R8d — Recomendações por proximidade
    // -----------------------------------------------------------------------

    public static void testR8d_Recomendacoes() {
        System.out.println("--- R8d: recomendarConteudosPorProximidade ---");
        StreamingDatabase db = buildDB();
        StreamingGraph g = db.getGraph();

        // u1 segue u2. u2 viu c1 e c2. u1 já viu c1 → recomendação deve ser c2
        List<Content> recom = g.recomendarConteudosPorProximidade("u1", db.follows(), db.users());
        assert !recom.isEmpty() : "Deve haver pelo menos uma recomendação para u1";
        boolean temC2 = recom.stream().anyMatch(c -> c.getId().equals("c2"));
        assert temC2 : "c2 (Breaking Bad) deve estar nas recomendações para u1";
        System.out.println("  Recomendações para u1: " + recom.stream().map(Content::getTitle).toList());

        // Utilizador sem follows — sem recomendações
        List<Content> semRecom = g.recomendarConteudosPorProximidade("u4", db.follows(), db.users());
        assert semRecom.isEmpty() : "u4 não segue ninguém — sem recomendações";
        System.out.println("  Recomendações para u4 (esperado vazio): " + semRecom);

        System.out.println("PASSOU: R8d\n");
    }

    // -----------------------------------------------------------------------
    // R8e — Estatísticas de visualização
    // -----------------------------------------------------------------------

    public static void testR8e_EstatisticasVisualizacao() {
        System.out.println("--- R8e: estatisticasVisualizacao ---");
        StreamingDatabase db = buildDB();
        StreamingGraph g = db.getGraph();

        // c1 (Inception): u1 viu (0.9), u2 viu (1.0) e avaliou (4.5)
        LocalDateTime de  = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime ate = LocalDateTime.of(2024, 1, 31, 23, 59);

        Map<String, Double> stats = g.estatisticasVisualizacao("c1", de, ate, db.users());
        assert stats.get("visualizacoes") == 2.0 : "Devem ser 2 visualizações de c1 em janeiro";
        assert stats.get("progressoMedio") > 0    : "Progresso médio deve ser > 0";
        assert stats.get("ratingMedio") == 4.5    : "Rating médio deve ser 4.5";

        System.out.println("  Stats c1 jan/2024: visualizações=" + stats.get("visualizacoes")
                + " progressoMedio=" + stats.get("progressoMedio")
                + " ratingMedio=" + stats.get("ratingMedio"));

        // Intervalo sem dados
        LocalDateTime fora = LocalDateTime.of(2023, 1, 1, 0, 0);
        LocalDateTime foraFim = LocalDateTime.of(2023, 12, 31, 23, 59);
        Map<String, Double> vazio = g.estatisticasVisualizacao("c1", fora, foraFim, db.users());
        assert vazio.get("visualizacoes") == 0.0 : "Deve ser 0 fora do intervalo";

        System.out.println("PASSOU: R8e\n");
    }

    // -----------------------------------------------------------------------
    // R8f — Utilizadores que viram séries de um género num período
    // -----------------------------------------------------------------------

    public static void testR8f_UtilizadoresSeriesGenero() {
        System.out.println("--- R8f: utilizadoresQueViramSeriesDeGenero ---");
        StreamingDatabase db = buildDB();
        StreamingGraph g = db.getGraph();

        // Drama (g2): u2 viu Breaking Bad (fev/2024), u4 viu Breaking Bad (mar/2024)
        LocalDateTime de  = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime ate = LocalDateTime.of(2024, 12, 31, 23, 59);

        List<User> drama = g.utilizadoresQueViramSeriesDeGenero("g2", de, ate,
                db.users(), db.contents());
        assert drama.size() == 2 : "Devem ser 2 utilizadores que viram séries de Drama (obteve " + drama.size() + ")";
        System.out.println("  Utilizadores Drama: " + drama.stream().map(User::getName).toList());

        // Ação (g1): u3 viu Dark (mar/2024)
        List<User> acao = g.utilizadoresQueViramSeriesDeGenero("g1", de, ate,
                db.users(), db.contents());
        assert acao.size() == 1 : "Deve ser 1 utilizador que viu séries de Ação (obteve " + acao.size() + ")";
        System.out.println("  Utilizadores Ação: " + acao.stream().map(User::getName).toList());

        // Género sem séries vistas
        List<User> nenhum = g.utilizadoresQueViramSeriesDeGenero("g99", de, ate,
                db.users(), db.contents());
        assert nenhum.isEmpty() : "Género inexistente deve retornar lista vazia";

        System.out.println("PASSOU: R8f\n");
    }

    // -----------------------------------------------------------------------
    // R8g — Seguidores que viram o mesmo conteúdo num intervalo
    // -----------------------------------------------------------------------

    public static void testR8g_SeguidoresQueViramConteudo() {
        System.out.println("--- R8g: seguidoresQueViramConteudo ---");
        StreamingDatabase db = buildDB();
        StreamingGraph g = db.getGraph();

        // Seguidores de u2: nenhum (u2 é seguido por u1, mas não tem seguidores que viram c1)
        // Seguidores de u1: nenhum
        // Vamos testar com u2 e c1:
        // u1 segue u2 (u1 é seguidor de u2). u1 viu c1. Janela cobre jan/2024.
        LocalDateTime de  = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime ate = LocalDateTime.of(2024, 1, 31, 23, 59);

        List<User> result = g.seguidoresQueViramConteudo("u2", "c1", de, ate,
                db.follows(), db.users());
        assert result.size() == 1 : "1 seguidor de u2 (u1) viu c1 em janeiro (obteve " + result.size() + ")";
        assert result.get(0).getId().equals("u1") : "O seguidor deve ser u1";
        System.out.println("  Seguidores de u2 que viram c1: " + result.stream().map(User::getName).toList());

        // Intervalo sem visualizações
        LocalDateTime foraInicio = LocalDateTime.of(2023, 1, 1, 0, 0);
        LocalDateTime foraFim    = LocalDateTime.of(2023, 12, 31, 23, 59);
        List<User> vazio = g.seguidoresQueViramConteudo("u2", "c1", foraInicio, foraFim,
                db.follows(), db.users());
        assert vazio.isEmpty() : "Fora do intervalo deve retornar lista vazia";

        System.out.println("PASSOU: R8g\n");
    }
}