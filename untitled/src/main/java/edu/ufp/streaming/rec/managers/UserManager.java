package edu.ufp.streaming.rec.managers;

import edu.princeton.cs.algs4.ST;
import edu.princeton.cs.algs4.RedBlackBST;
import edu.ufp.streaming.rec.models.Genre;
import edu.ufp.streaming.rec.models.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestor de utilizadores: organiza utilizadores por ID, data de registo e nome.
 *
 * <p>Mantém três estruturas em paralelo para suporte eficiente a diferentes
 * tipos de pesquisa:
 * <ul>
 *   <li>ST primária (hash): {@code userId → User} — pesquisa O(1) amortizado</li>
 *   <li>RedBlackBST por data: {@code epochDay → List<User>} — pesquisa por intervalo</li>
 *   <li>RedBlackBST por nome: {@code nome → List<User>} — pesquisa por substring</li>
 * </ul>
 *
 * @author Diogo Vicente
 */
public class UserManager {

    /** Tabela de Símbolos primária: userId → User. */
    private final ST<String, User> userST;

    /** BST ordenada: dia da época (Long) → lista de utilizadores registados nessa data. */
    private final RedBlackBST<Long, List<User>> byDateBST;

    /** BST ordenada: nome em minúsculas → lista de utilizadores com esse nome. */
    private final RedBlackBST<String, List<User>> byNameBST;

    /**
     * Constrói um UserManager vazio.
     */
    public UserManager() {
        this.userST    = new ST<>();
        this.byDateBST = new RedBlackBST<>();
        this.byNameBST = new RedBlackBST<>();
    }

    // -------------------------------------------------------------------------
    // CRUD
    // -------------------------------------------------------------------------

    /**
     * Insere um novo utilizador em todas as estruturas.
     *
     * @param user o {@link User} a inserir; não deve ser {@code null}
     * @return {@code true} se inserido; {@code false} se {@code null} ou ID já existe
     */
    public boolean insert(User user) {
        if (user == null || userST.contains(user.getId())) return false;
        userST.put(user.getId(), user);
        indexByDate(user);
        indexByName(user);
        return true;
    }

    /**
     * Remove um utilizador de todas as estruturas através do ID.
     *
     * @param id o ID do utilizador a remover
     * @return o {@link User} removido, ou {@code null} se não encontrado
     */
    public User remove(String id) {
        if (!userST.contains(id)) return null;
        User u = userST.get(id);
        userST.delete(id);
        removeFromDateIndex(u);
        removeFromNameIndex(u);
        return u;
    }

    // -------------------------------------------------------------------------
    // Edições
    // -------------------------------------------------------------------------

    /**
     * Altera o nome de um utilizador e re-indexa a BST de nomes.
     *
     * @param id      ID do utilizador
     * @param newName novo nome
     * @return {@code true} se bem-sucedido; {@code false} se o utilizador não existir
     */
    public boolean editName(String id, String newName) {
        User u = userST.get(id);
        if (u == null) return false;
        removeFromNameIndex(u);
        u.setName(newName);
        indexByName(u);
        return true;
    }

    /**
     * Altera o e-mail de um utilizador.
     *
     * @param id       ID do utilizador
     * @param newEmail novo endereço de e-mail
     * @return {@code true} se bem-sucedido; {@code false} se o utilizador não existir
     */
    public boolean editEmail(String id, String newEmail) {
        User u = userST.get(id);
        if (u == null) return false;
        u.setEmail(newEmail);
        return true;
    }

    /**
     * Altera a região de um utilizador.
     *
     * @param id        ID do utilizador
     * @param newRegion nova região
     * @return {@code true} se bem-sucedido; {@code false} se o utilizador não existir
     */
    public boolean editRegion(String id, String newRegion) {
        User u = userST.get(id);
        if (u == null) return false;
        u.setRegion(newRegion);
        return true;
    }

    /**
     * Altera a password de um utilizador.
     * A nova password é armazenada como hash SHA-256.
     *
     * @param id             ID do utilizador
     * @param newRawPassword nova password em texto simples
     * @return {@code true} se bem-sucedido; {@code false} se o utilizador não existir
     */
    public boolean changePassword(String id, String newRawPassword) {
        User u = userST.get(id);
        if (u == null) return false;
        u.changePassword(newRawPassword);
        return true;
    }

    /**
     * Verifica as credenciais de um utilizador (autenticação).
     * Devolve {@code null} se o utilizador não existir, se a password ainda não
     * tiver sido definida, ou se a password for incorreta.
     *
     * @param id          ID do utilizador
     * @param rawPassword password em texto simples a verificar
     * @return o {@link User} autenticado, ou {@code null} se as credenciais forem inválidas
     */
    public User authenticate(String id, String rawPassword) {
        User u = userST.get(id);
        if (u == null) return null;
        if (!u.hasPassword()) return null;   // password ainda não definida
        return u.checkPassword(rawPassword) ? u : null;
    }

    /**
     * Define a password inicial de um utilizador que ainda não tem password.
     * Falha se o utilizador já tiver password definida (usar {@link #changePassword} nesse caso).
     *
     * @param id          ID do utilizador
     * @param rawPassword password em texto simples
     * @return {@code true} se definida com sucesso; {@code false} se utilizador não encontrado
     *         ou se já tinha password
     */
    public boolean setInitialPassword(String id, String rawPassword) {
        User u = userST.get(id);
        if (u == null) return false;
        return u.setInitialPassword(rawPassword);
    }

    /**
     * Indica se um utilizador já tem password definida.
     *
     * @param id ID do utilizador
     * @return {@code true} se a password estiver definida; {@code false} se ainda não definida
     *         ou se o utilizador não existir
     */
    public boolean hasPassword(String id) {
        User u = userST.get(id);
        return u != null && u.hasPassword();
    }

    // -------------------------------------------------------------------------
    // Consultas
    // -------------------------------------------------------------------------

    /**
     * Devolve o utilizador com o ID fornecido.
     *
     * @param id ID do utilizador
     * @return o {@link User}, ou {@code null} se não encontrado
     */
    public User get(String id) { return userST.get(id); }

    /**
     * Verifica se existe um utilizador com o ID fornecido.
     *
     * @param id ID do utilizador
     * @return {@code true} se presente
     */
    public boolean contains(String id) { return userST.contains(id); }

    /**
     * Devolve o número total de utilizadores registados.
     *
     * @return número de utilizadores
     */
    public int size() { return userST.size(); }

    /**
     * Devolve todos os utilizadores como lista não ordenada.
     *
     * @return lista de todos os objetos {@link User}
     */
    public List<User> listAll() {
        List<User> result = new ArrayList<>();
        for (String key : userST.keys()) result.add(userST.get(key));
        return result;
    }

    /**
     * Devolve todos os utilizadores registados numa data exata.
     *
     * @param date data de registo a pesquisar
     * @return lista de utilizadores correspondentes (pode estar vazia)
     */
    public List<User> searchByRegisterDate(LocalDate date) {
        List<User> list = byDateBST.get(date.toEpochDay());
        return list != null ? new ArrayList<>(list) : new ArrayList<>();
    }

    /**
     * Devolve todos os utilizadores registados dentro de um intervalo de datas [from, to] (inclusive).
     *
     * @param from início do intervalo (inclusive)
     * @param to   fim do intervalo (inclusive)
     * @return lista de utilizadores cuja data de registo cai no intervalo
     */
    public List<User> searchByRegisterDateRange(LocalDate from, LocalDate to) {
        List<User> result = new ArrayList<>();
        for (Long d : byDateBST.keys(from.toEpochDay(), to.toEpochDay())) {
            List<User> bucket = byDateBST.get(d);
            if (bucket != null) result.addAll(bucket);
        }
        return result;
    }

    /**
     * Devolve todos os utilizadores cujo nome contém a substring fornecida (sem distinção de maiúsculas).
     *
     * @param substring substring a procurar nos nomes
     * @return lista de utilizadores correspondentes
     */
    public List<User> searchByNameSubstring(String substring) {
        String lower = substring.toLowerCase();
        List<User> result = new ArrayList<>();
        for (String key : byNameBST.keys())
            if (key.contains(lower)) result.addAll(byNameBST.get(key));
        return result;
    }

    /**
     * Devolve todos os utilizadores de uma região específica (sem distinção de maiúsculas).
     *
     * @param region região pela qual filtrar
     * @return lista de utilizadores correspondentes
     */
    public List<User> searchByRegion(String region) {
        List<User> result = new ArrayList<>();
        for (String key : userST.keys()) {
            User u = userST.get(key);
            if (u.getRegion().equalsIgnoreCase(region)) result.add(u);
        }
        return result;
    }

    /**
     * Devolve todos os utilizadores de uma região registados num intervalo de datas.
     *
     * @param region região pela qual filtrar
     * @param from   início do intervalo de datas
     * @param to     fim do intervalo de datas
     * @return lista de utilizadores correspondentes
     */
    public List<User> searchByRegionAndDateRange(String region, LocalDate from, LocalDate to) {
        List<User> result = new ArrayList<>();
        for (Long d : byDateBST.keys(from.toEpochDay(), to.toEpochDay())) {
            List<User> bucket = byDateBST.get(d);
            if (bucket == null) continue;
            for (User u : bucket)
                if (u.getRegion().equalsIgnoreCase(region)) result.add(u);
        }
        return result;
    }

    /**
     * Devolve todos os utilizadores cujo nome contém a substring e pertencem à região indicada.
     *
     * @param substring substring para comparar com os nomes (sem distinção de maiúsculas)
     * @param region    região pela qual filtrar
     * @return lista de utilizadores correspondentes
     */
    public List<User> searchByNameSubstringAndRegion(String substring, String region) {
        String lower = substring.toLowerCase();
        List<User> result = new ArrayList<>();
        for (String key : byNameBST.keys()) {
            if (!key.contains(lower)) continue;
            for (User u : byNameBST.get(key))
                if (u.getRegion().equalsIgnoreCase(region)) result.add(u);
        }
        return result;
    }

    /**
     * Adiciona um género às preferências de um utilizador, se ainda não estiver presente.
     *
     * @param userId ID do utilizador
     * @param genre  o {@link Genre} a adicionar
     * @return {@code true} se adicionado; {@code false} se utilizador não encontrado ou género já presente
     */
    public boolean addPreference(String userId, Genre genre) {
        User u = userST.get(userId);
        if (u == null || genre == null) return false;
        if (u.getPreferences().contains(genre)) return false;
        u.addPreference(genre);
        return true;
    }

    /**
     * Remove um género das preferências de um utilizador.
     *
     * @param userId ID do utilizador
     * @param genre  o {@link Genre} a remover
     * @return {@code true} se removido; {@code false} se utilizador não encontrado ou género ausente
     */
    public boolean removePreference(String userId, Genre genre) {
        User u = userST.get(userId);
        if (u == null || genre == null) return false;
        return u.getPreferences().remove(genre);
    }

    /**
     * Devolve todos os utilizadores que têm um género específico nas suas preferências.
     *
     * @param genreId ID do género a procurar
     * @return lista de utilizadores correspondentes
     */
    public List<User> searchByPreferredGenre(String genreId) {
        List<User> result = new ArrayList<>();
        for (String key : userST.keys()) {
            User u = userST.get(key);
            for (Genre g : u.getPreferences()) {
                if (g.getId().equals(genreId)) { result.add(u); break; }
            }
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Métodos internos de indexação
    // -------------------------------------------------------------------------

    private void indexByDate(User user) {
        Long date = user.getRegisterDate().toEpochDay();
        List<User> bucket = byDateBST.get(date);
        if (bucket == null) { bucket = new ArrayList<>(); byDateBST.put(date, bucket); }
        bucket.add(user);
    }

    private void removeFromDateIndex(User user) {
        Long date = user.getRegisterDate().toEpochDay();
        List<User> bucket = byDateBST.get(date);
        if (bucket != null) { bucket.remove(user); if (bucket.isEmpty()) byDateBST.delete(date); }
    }

    private void indexByName(User user) {
        String key = user.getName().toLowerCase();
        List<User> bucket = byNameBST.get(key);
        if (bucket == null) { bucket = new ArrayList<>(); byNameBST.put(key, bucket); }
        bucket.add(user);
    }

    private void removeFromNameIndex(User user) {
        String key = user.getName().toLowerCase();
        List<User> bucket = byNameBST.get(key);
        if (bucket != null) { bucket.remove(user); if (bucket.isEmpty()) byNameBST.delete(key); }
    }
}