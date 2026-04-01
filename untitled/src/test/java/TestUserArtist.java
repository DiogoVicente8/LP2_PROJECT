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
    // Ponto de entrada
    // -----------------------------------------------------------------------

    /**
     * Executa todos os casos de teste em sequência.
     *
     * @param args não utilizado
     */
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println(" TestUserArtist — Testes Fase 1");
        System.out.println("========================================\n");

        // Testes do UserManager
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

        // Testes do ArtistManager
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

        // Testes do FollowManager
        testFollowManagerFollowAndUnfollow();
        testFollowManagerGetFollowersAndFollowing();
        testFollowManagerConsistencyOnUserRemoval();
        testFollowManagerDateRange();
        testUserManagerPreferences();

        System.out.println("\n========================================");
        System.out.println(" Todos os testes concluídos.");
        System.out.println("========================================");
    }

    // -----------------------------------------------------------------------
    // Auxiliares para criação de dados (fixtures)
    // -----------------------------------------------------------------------

    /** Cria um {@link UserManager} populado com 5 utilizadores de exemplo. */
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

    /** Cria um {@link ArtistManager} populado com 5 artistas de exemplo. */
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
    // Testes de edu.ufp.streaming.rec.managers.UserManager
    // -----------------------------------------------------------------------

    /**
     * Testa operações de inserção e obtenção no {@link UserManager}.
     */
    public static void testUserManagerInsertAndGet() {
        System.out.println("--- testUserManagerInsertAndGet ---");
        UserManager um = buildUserManager();

        assert um.size() == 5 : "Esperados 5 utilizadores";
        assert um.get("u1").getName().equals("Alice Silva") : "u1 deveria ser Alice Silva";
        assert um.get("u99") == null : "ID desconhecido deveria retornar null";

        // Inserção duplicada deve falhar
        User dup = new User("u1", "Duplicado", "dup@mail.com", "PT", LocalDate.now());
        assert !um.insert(dup) : "Inserção duplicada deveria retornar false";

        System.out.println("PASSOU: inserção/obtenção/duplicados\n");
    }

    /**
     * Testa a remoção do {@link UserManager} e a consistência da BST.
     */
    public static void testUserManagerRemove() {
        System.out.println("--- testUserManagerRemove ---");
        UserManager um = buildUserManager();

        User removed = um.remove("u3");
        assert removed != null && removed.getId().equals("u3") : "Deveria retornar o utilizador removido";
        assert um.size() == 4 : "O tamanho deveria ser 4 após a remoção";
        assert um.get("u3") == null : "O utilizador removido não deve ser recuperável";

        // A BST não deve conter mais o u3
        List<User> byDate = um.searchByRegisterDate(LocalDate.of(2021, 6, 20));
        assert byDate.stream().noneMatch(u -> u.getId().equals("u3"))
                : "O índice de data não deve conter o utilizador removido";

        assert um.remove("u99") == null : "Remover utilizador inexistente deve retornar null";
        System.out.println("PASSOU: remoção + consistência da BST\n");
    }

    /**
     * Testa operações de edição no {@link UserManager}.
     */
    public static void testUserManagerEdit() {
        System.out.println("--- testUserManagerEdit ---");
        UserManager um = buildUserManager();

        assert um.editName("u1", "Alice Ferreira") : "A edição do nome deve ter sucesso";
        assert um.get("u1").getName().equals("Alice Ferreira") : "O nome deve estar atualizado";

        // A BST de nomes deve refletir a mudança
        List<User> found = um.searchByNameSubstring("ferreira");
        assert found.stream().anyMatch(u -> u.getId().equals("u1"))
                : "A BST de nomes deve refletir o utilizador renomeado";

        // O nome antigo não deve aparecer
        List<User> old = um.searchByNameSubstring("alice silva");
        assert old.stream().noneMatch(u -> u.getId().equals("u1"))
                : "O nome antigo não deve estar mais indexado";

        assert !um.editName("u99", "Fantasma") : "A edição em utilizador inexistente deve retornar false";
        System.out.println("PASSOU: editName + re-indexação na BST\n");
    }

    /**
     * Testa a pesquisa por data exata na BST de data de registo.
     */
    public static void testUserManagerSearchByDate() {
        System.out.println("--- testUserManagerSearchByDate ---");
        UserManager um = buildUserManager();

        List<User> result = um.searchByRegisterDate(LocalDate.of(2020, 1, 10));
        assert result.size() == 1 && result.get(0).getId().equals("u1")
                : "Deveria encontrar exatamente u1 em 2020-01-10";

        List<User> empty = um.searchByRegisterDate(LocalDate.of(1999, 1, 1));
        assert empty.isEmpty() : "Nenhum utilizador deve ser encontrado em 1999-01-01";

        System.out.println("PASSOU: searchByRegisterDate\n");
    }

    /**
     * Testa a pesquisa por intervalo de datas na BST de data de registo.
     */
    public static void testUserManagerSearchByDateRange() {
        System.out.println("--- testUserManagerSearchByDateRange ---");
        UserManager um = buildUserManager();

        // Intervalo cobre u1 (Jan 2020) e u2 (Mar 2020)
        List<User> result = um.searchByRegisterDateRange(
                LocalDate.of(2020, 1, 1), LocalDate.of(2020, 12, 31));
        assert result.size() == 2 : "Esperados 2 utilizadores em 2020 (obteve " + result.size() + ")";

        // Intervalo não cobre nenhum utilizador
        List<User> empty = um.searchByRegisterDateRange(
                LocalDate.of(2018, 1, 1), LocalDate.of(2019, 12, 31));
        assert empty.isEmpty() : "Nenhum utilizador registado em 2018-2019";

        System.out.println("PASSOU: searchByRegisterDateRange\n");
    }

    /**
     * Testa a pesquisa por substring do nome na BST de nomes.
     */
    public static void testUserManagerSearchByNameSubstring() {
        System.out.println("--- testUserManagerSearchByNameSubstring ---");
        UserManager um = buildUserManager();

        List<User> result = um.searchByNameSubstring("silva");
        assert result.size() == 1 && result.get(0).getId().equals("u1")
                : "Deveria encontrar Alice Silva";

        List<User> noMatch = um.searchByNameSubstring("xyz");
        assert noMatch.isEmpty() : "Não deve encontrar ninguém para 'xyz'";

        System.out.println("PASSOU: searchByNameSubstring\n");
    }

    /**
     * Testa o filtro por região.
     */
    public static void testUserManagerSearchByRegion() {
        System.out.println("--- testUserManagerSearchByRegion ---");
        UserManager um = buildUserManager();

        List<User> pt = um.searchByRegion("PT");
        assert pt.size() == 3 : "Esperados 3 utilizadores de PT (obteve " + pt.size() + ")";

        List<User> br = um.searchByRegion("BR");
        assert br.size() == 1 : "Esperado 1 utilizador de BR";

        System.out.println("PASSOU: searchByRegion\n");
    }

    /**
     * Testa a pesquisa combinada de região + intervalo de datas.
     */
    public static void testUserManagerSearchByRegionAndDateRange() {
        System.out.println("--- testUserManagerSearchByRegionAndDateRange ---");
        UserManager um = buildUserManager();

        // u1 e u2 são PT em 2020
        List<User> result = um.searchByRegionAndDateRange(
                "PT", LocalDate.of(2020, 1, 1), LocalDate.of(2020, 12, 31));
        assert result.size() == 2 : "Esperados 2 utilizadores PT em 2020 (obteve " + result.size() + ")";

        System.out.println("PASSOU: searchByRegionAndDateRange\n");
    }

    /**
     * Testa a pesquisa combinada de substring de nome + região.
     */
    public static void testUserManagerSearchByNameSubstringAndRegion() {
        System.out.println("--- testUserManagerSearchByNameSubstringAndRegion ---");
        UserManager um = buildUserManager();

        List<User> result = um.searchByNameSubstringAndRegion("silva", "PT");
        assert result.size() == 1 && result.get(0).getId().equals("u1")
                : "Deveria encontrar Alice Silva em PT";

        List<User> noMatch = um.searchByNameSubstringAndRegion("silva", "BR");
        assert noMatch.isEmpty() : "Alice Silva não está no BR";

        System.out.println("PASSOU: searchByNameSubstringAndRegion\n");
    }

    /**
     * Testa a pesquisa por género preferido.
     */
    public static void testUserManagerSearchByPreferredGenre() {
        System.out.println("--- testUserManagerSearchByPreferredGenre ---");
        UserManager um = buildUserManager();

        List<User> action = um.searchByPreferredGenre("g1");
        assert action.size() == 2 : "Esperados 2 utilizadores com preferência por Action";

        List<User> none = um.searchByPreferredGenre("g99");
        assert none.isEmpty() : "Nenhum utilizador deve preferir o género g99";

        System.out.println("PASSOU: searchByPreferredGenre\n");
    }

    // -----------------------------------------------------------------------
    // Testes de edu.ufp.streaming.rec.managers.ArtistManager
    // -----------------------------------------------------------------------

    /**
     * Testa inserção e obtenção no {@link ArtistManager}.
     */
    public static void testArtistManagerInsertAndGet() {
        System.out.println("--- testArtistManagerInsertAndGet ---");
        ArtistManager am = buildArtistManager();

        assert am.size() == 5 : "Esperados 5 artistas";
        assert am.get("a1").getName().equals("Leonardo DiCaprio") : "a1 deveria ser DiCaprio";
        assert am.get("a99") == null : "ID desconhecido deveria retornar null";

        Artist dup = new Artist("a1", "Dup", "US", "M", LocalDate.now(), ArtistRole.ACTOR);
        assert !am.insert(dup) : "Inserção duplicada deveria retornar false";

        System.out.println("PASSOU: inserção/obtenção/duplicados\n");
    }

    /**
     * Testa a remoção no {@link ArtistManager}.
     */
    public static void testArtistManagerRemove() {
        System.out.println("--- testArtistManagerRemove ---");
        ArtistManager am = buildArtistManager();

        Artist removed = am.remove("a3");
        assert removed != null && removed.getId().equals("a3") : "Deveria retornar o artista removido";
        assert am.size() == 4 : "O tamanho deveria ser 4";
        assert am.get("a3") == null : "Artista removido não deve ser recuperável";

        System.out.println("PASSOU: remoção\n");
    }

    /**
     * Testa a edição + re-indexação na BST no {@link ArtistManager}.
     */
    public static void testArtistManagerEdit() {
        System.out.println("--- testArtistManagerEdit ---");
        ArtistManager am = buildArtistManager();

        assert am.editName("a1", "Leo DiCaprio") : "A edição deve ter sucesso";
        assert am.get("a1").getName().equals("Leo DiCaprio") : "O nome deve estar atualizado";

        List<Artist> found = am.searchByNameSubstring("leo");
        assert found.stream().anyMatch(a -> a.getId().equals("a1"))
                : "A BST deve refletir o novo nome";

        System.out.println("PASSOU: editName + re-indexação na BST\n");
    }

    /**
     * Testa a pesquisa por intervalo de data de nascimento no {@link ArtistManager}.
     */
    public static void testArtistManagerSearchByBirthDateRange() {
        System.out.println("--- testArtistManagerSearchByBirthDateRange ---");
        ArtistManager am = buildArtistManager();

        // Nascidos na década de 70: a3 (1970), a4 (1971), a1 (1974), a5 (1974)
        List<Artist> result = am.searchByBirthDateRange(
                LocalDate.of(1970, 1, 1), LocalDate.of(1979, 12, 31));
        assert result.size() == 4 : "Esperados 4 artistas nascidos nos anos 70 (obteve " + result.size() + ")";

        System.out.println("PASSOU: searchByBirthDateRange\n");
    }

    /**
     * Testa a pesquisa por substring de nome no {@link ArtistManager}.
     */
    public static void testArtistManagerSearchByNameSubstring() {
        System.out.println("--- testArtistManagerSearchByNameSubstring ---");
        ArtistManager am = buildArtistManager();

        List<Artist> result = am.searchByNameSubstring("nolan");
        assert result.size() == 1 && result.get(0).getId().equals("a3")
                : "Deveria encontrar Christopher Nolan";

        System.out.println("PASSOU: searchByNameSubstring\n");
    }

    /**
     * Testa o filtro por nacionalidade no {@link ArtistManager}.
     */
    public static void testArtistManagerSearchByNationality() {
        System.out.println("--- testArtistManagerSearchByNationality ---");
        ArtistManager am = buildArtistManager();

        List<Artist> us = am.searchByNationality("US");
        assert us.size() == 4 : "Esperados 4 artistas dos EUA (obteve " + us.size() + ")";

        List<Artist> gb = am.searchByNationality("GB");
        assert gb.size() == 1 : "Esperado 1 artista do Reino Unido";

        System.out.println("PASSOU: searchByNationality\n");
    }

    /**
     * Testa o filtro por género no {@link ArtistManager}.
     */
    public static void testArtistManagerSearchByGender() {
        System.out.println("--- testArtistManagerSearchByGender ---");
        ArtistManager am = buildArtistManager();

        List<Artist> female = am.searchByGender("F");
        assert female.size() == 2 : "Esperadas 2 artistas do sexo feminino (obteve " + female.size() + ")";

        System.out.println("PASSOU: searchByGender\n");
    }

    /**
     * Testa a pesquisa combinada de nacionalidade + intervalo de data de nascimento.
     */
    public static void testArtistManagerSearchByNationalityAndBirthDateRange() {
        System.out.println("--- testArtistManagerSearchByNationalityAndBirthDateRange ---");
        ArtistManager am = buildArtistManager();

        // Artistas dos EUA nascidos nos anos 70: a1 (1974), a4 (1971), a5 (1974)
        List<Artist> result = am.searchByNationalityAndBirthDateRange(
                "US", LocalDate.of(1970, 1, 1), LocalDate.of(1979, 12, 31));
        assert result.size() == 3 : "Esperados 3 artistas dos EUA nos anos 70 (obteve " + result.size() + ")";

        System.out.println("PASSOU: searchByNationalityAndBirthDateRange\n");
    }

    /**
     * Testa a pesquisa combinada de substring de nome + nacionalidade + género.
     */
    public static void testArtistManagerSearchByNameSubstringNationalityGender() {
        System.out.println("--- testArtistManagerSearchByNameSubstringNationalityGender ---");
        ArtistManager am = buildArtistManager();

        List<Artist> result = am.searchByNameSubstringNationalityAndGender("coppola", "US", "F");
        assert result.size() == 1 && result.get(0).getId().equals("a4")
                : "Deveria encontrar Sofia Coppola";

        System.out.println("PASSOU: searchByNameSubstringNationalityAndGender\n");
    }

    /**
     * Testa o filtro por papel (Role) no {@link ArtistManager}.
     */
    public static void testArtistManagerSearchByRole() {
        System.out.println("--- testArtistManagerSearchByRole ---");
        ArtistManager am = buildArtistManager();

        List<Artist> actors = am.searchByRole(ArtistRole.ACTOR);
        assert actors.size() == 3 : "Esperados 3 atores (obteve " + actors.size() + ")";

        List<Artist> directors = am.searchByRole(ArtistRole.DIRECTOR);
        assert directors.size() == 2 : "Esperados 2 realizadores (obteve " + directors.size() + ")";

        System.out.println("PASSOU: searchByRole\n");
    }

    // -----------------------------------------------------------------------
    // Testes de edu.ufp.streaming.rec.managers.FollowManager
    // -----------------------------------------------------------------------

    /**
     * Testa as operações de seguir (follow) e deixar de seguir (unfollow) no {@link FollowManager}.
     */
    public static void testFollowManagerFollowAndUnfollow() {
        System.out.println("--- testFollowManagerFollowAndUnfollow ---");
        UserManager um = buildUserManager();
        FollowManager fm = new FollowManager();

        User u1 = um.get("u1");
        User u2 = um.get("u2");
        User u3 = um.get("u3");

        UserFollow uf = fm.follow(u1, u2);
        assert uf != null : "O primeiro seguimento deve ter sucesso";
        assert fm.isFollowing("u1", "u2") : "u1 deveria estar a seguir u2";
        assert fm.follow(u1, u2) == null : "Seguimento duplicado deve retornar null";

        fm.follow(u1, u3);
        assert fm.size() == 2 : "Deveria haver 2 relações de seguimento";

        fm.unfollow("u1", "u2");
        assert !fm.isFollowing("u1", "u2") : "u1 não deveria mais seguir u2";
        assert fm.size() == 1 : "Deveria haver apenas 1 relação após o unfollow";

        System.out.println("PASSOU: follow/unfollow/isFollowing\n");
    }

    /**
     * Testa getFollowers e getFollowing no {@link FollowManager}.
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
        assert followers.size() == 3 : "u3 deveria ter 3 seguidores";

        fm.follow(u1, u2);
        fm.follow(u1, u4);

        List<User> following = fm.getFollowing("u1");
        assert following.size() == 3 : "u1 deveria estar a seguir 3 utilizadores";

        assert fm.followerCount("u3") == 3 : "Inconsistência na contagem de seguidores";
        assert fm.followingCount("u1") == 3 : "Inconsistência na contagem de seguidos";

        System.out.println("PASSOU: getFollowers/getFollowing/contagens\n");
    }

    /**
     * Testa a consistência R4: remover um utilizador remove todas as suas relações de seguimento.
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

        // Simula a remoção do u1: primeiro limpa os dados de follow, depois remove da tabela de símbolos
        fm.removeAllRelationships("u1");
        um.remove("u1");

        assert !fm.isFollowing("u1", "u2") : "A relação u1→u2 deveria ter sido removida";
        assert !fm.isFollowing("u3", "u1") : "A relação u3→u1 deveria ter sido removida";
        assert fm.size() == 0 : "Não deveriam restar relações de seguimento";
        assert um.get("u1") == null : "u1 deveria ter sido removido da ST";

        System.out.println("PASSOU: Consistência R4 na remoção de utilizador\n");
    }

    /**
     * Testa a pesquisa de intervalo de datas de seguimento usando a BST no {@link FollowManager}.
     */
    public static void testFollowManagerDateRange() {
        System.out.println("--- testFollowManagerDateRange ---");
        UserManager um = buildUserManager();
        FollowManager fm = new FollowManager();

        User u1 = um.get("u1");
        User u2 = um.get("u2");
        User u3 = um.get("u3");

        // Todos os seguimentos ocorrem agora; a consulta por intervalo deve capturá-los
        fm.follow(u1, u2);
        fm.follow(u1, u3);

        LocalDateTime from = LocalDateTime.now().minusMinutes(1);
        LocalDateTime to   = LocalDateTime.now().plusMinutes(1);

        List<UserFollow> result = fm.searchByDateRange(from, to);
        assert result.size() == 2 : "Esperados 2 eventos de follow no intervalo (obteve " + result.size() + ")";

        System.out.println("PASSOU: searchByDateRange\n");
    }

    /**
     * Testa addPreference e removePreference no {@link UserManager}.
     */
    public static void testUserManagerPreferences() {
        System.out.println("--- testUserManagerPreferences ---");
        UserManager um = buildUserManager();

        Genre g3 = new Genre("g3", "Sci-Fi");

        // addPreference através do manager
        assert um.addPreference("u1", g3) : "Deveria adicionar g3 à u1";
        assert um.get("u1").getPreferences().contains(g3) : "u1 deveria ter g3 nas preferências";

        // duplicado deve retornar false
        assert !um.addPreference("u1", g3) : "Adição duplicada deveria retornar false";

        // utilizador desconhecido
        assert !um.addPreference("u99", g3) : "Utilizador desconhecido deveria retornar false";

        // removePreference através do manager
        assert um.removePreference("u1", g3) : "Deveria remover g3 da u1";
        assert !um.get("u1").getPreferences().contains(g3) : "u1 não deveria mais ter g3";

        // remover preferência inexistente
        assert !um.removePreference("u1", g3) : "Remover género ausente deve retornar false";

        // searchByPreferredGenre ainda funciona após a remoção
        assert um.searchByPreferredGenre("g3").isEmpty() : "Nenhum utilizador deve ter g3 após a remoção";

        System.out.println("PASSOU: addPreference / removePreference\n");
    }
}