package edu.ufp.streaming.rec.managers;

import edu.princeton.cs.algs4.ST;
import edu.princeton.cs.algs4.RedBlackBST;
import edu.ufp.streaming.rec.models.Genre;
import edu.ufp.streaming.rec.models.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * *Gerenciador de Usuários: organiza usuários por ID, Data e Nome.
 * @author  Diogo Vicente
 */
public class UserManager {

    /** Tabela de Símbolos Primária: userId → User (busca média O(1)). */
    private final ST<String, User> userST;

    /** BST Ordenada: data de registro como dia da época (Long) → lista de Usuários registrados nessa data. */
    private final RedBlackBST<Long, List<User>> byDateBST;

    /** BST Ordenada: nome em minúsculas → lista de Usuários com esse nome. */
    private final RedBlackBST<String, List<User>> byNameBST;

    /**
     * Constrói um UserManager vazio.
     */
    public UserManager() {
        this.userST    = new ST<>();
        this.byDateBST = new RedBlackBST<>();
        this.byNameBST = new RedBlackBST<>();
    }

    /**
     * Insere um novo usuário em todas as estruturas.
     *
     * @param user o {@link User} a inserir; não deve ser {@code null}
     * @return {@code true} se inserido; {@code false} se for {@code null} ou se o ID já existir
     */
    public boolean insert(User user) {
        if (user == null || userST.contains(user.getId())) return false;
        userST.put(user.getId(), user);
        indexByDate(user);
        indexByName(user);
        return true;
    }

    /**
     * Remove um usuário de todas as estruturas através do ID.
     *
     * @param id o ID do usuário a remover
     * @return o {@link User} removido, ou {@code null} se não for encontrado
     */
    public User remove(String id) {
        if (!userST.contains(id)) return null;
        User u = userST.get(id);
        userST.delete(id);
        removeFromDateIndex(u);
        removeFromNameIndex(u);
        return u;
    }

    /**
     * Edita o nome de um usuário existente e re-indexa a BST de nomes.
     *
     * @param id      o ID do usuário
     * @param newName o novo nome a ser atribuído
     * @return {@code true} se for bem-sucedido; {@code false} se o usuário não for encontrado
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
     * Edita o e-mail de um usuário existente.
     *
     * @param id       o ID do usuário
     * @param newEmail o novo endereço de e-mail
     * @return {@code true} se for bem-sucedido; {@code false} se o usuário não for encontrado
     */
    public boolean editEmail(String id, String newEmail) {
        User u = userST.get(id);
        if (u == null) return false;
        u.setEmail(newEmail);
        return true;
    }

    /**
     * Edita a região de um usuário existente.
     *
     * @param id        o ID do usuário
     * @param newRegion a nova string de região
     * @return {@code true} se for bem-sucedido; {@code false} se o usuário não for encontrado
     */
    public boolean editRegion(String id, String newRegion) {
        User u = userST.get(id);
        if (u == null) return false;
        u.setRegion(newRegion);
        return true;
    }

    /**
     * Retorna o usuário com o ID fornecido.
     *
     * @param id o ID do usuário
     * @return o {@link User}, ou {@code null} se não for encontrado
     */
    public User get(String id) { return userST.get(id); }

    /**
     * Retorna {@code true} se um usuário com o ID fornecido existir.
     *
     * @param id o ID do usuário
     * @return {@code true} se presente
     */
    public boolean contains(String id) { return userST.contains(id); }

    /**
     * Retorna o número total de usuários registrados.
     *
     * @return número de usuários
     */
    public int size() { return userST.size(); }

    /**
     * Retorna todos os usuários como uma lista não ordenada.
     *
     * @return lista de todos os objetos {@link User}
     */
    public List<User> listAll() {
        List<User> result = new ArrayList<>();
        for (String key : userST.keys()) result.add(userST.get(key));
        return result;
    }

    /**
     * Retorna todos os usuários registrados em uma data exata.
     *
     * @param date a data de registro para pesquisa
     * @return lista de usuários correspondentes (pode estar vazia)
     */
    public List<User> searchByRegisterDate(LocalDate date) {
        List<User> list = byDateBST.get(date.toEpochDay());
        return list != null ? new ArrayList<>(list) : new ArrayList<>();
    }

    /**
     * Retorna todos os usuários registrados dentro de um intervalo de datas [de, até] (inclusive).
     *
     * @param from início do intervalo (inclusive)
     * @param to   fim do intervalo (inclusive)
     * @return lista de usuários cuja data de registro cai no intervalo
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
     * Retorna todos os usuários cujo nome contém a substring fornecida (insensível a maiúsculas).
     *
     * @param substring a substring a procurar nos nomes dos usuários
     * @return lista de usuários correspondentes
     */
    public List<User> searchByNameSubstring(String substring) {
        String lower = substring.toLowerCase();
        List<User> result = new ArrayList<>();
        for (String key : byNameBST.keys())
            if (key.contains(lower)) result.addAll(byNameBST.get(key));
        return result;
    }

    /**
     * Retorna todos os usuários de uma região específica (insensível a maiúsculas).
     *
     * @param region a região pela qual filtrar
     * @return lista de usuários correspondentes
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
     * Retorna todos os usuários de uma região específica registrados num intervalo de datas.
     *
     * @param region a região pela qual filtrar
     * @param from   início do intervalo de datas
     * @param to     fim do intervalo de datas
     * @return lista de usuários correspondentes
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
     * Retorna todos os usuários de uma região específica cujo nome contém a substring fornecida.
     *
     * @param substring substring para comparar com os nomes (insensível a maiúsculas)
     * @param region    a região pela qual filtrar
     * @return lista de usuários correspondentes
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
     * Adiciona um gênero às preferências de um usuário se ainda não estiver presente.
     *
     * @param userId o ID do usuário
     * @param genre  o {@link Genre} a adicionar
     * @return {@code true} se adicionado com sucesso; {@code false} se o usuário não for encontrado ou gênero já presente
     */
    public boolean addPreference(String userId, Genre genre) {
        User u = userST.get(userId);
        if (u == null || genre == null) return false;
        if (u.getPreferences().contains(genre)) return false;
        u.addPreference(genre);
        return true;
    }

    /**
     * Remove um gênero das preferências de um usuário.
     *
     * @param userId o ID do usuário
     * @param genre  o {@link Genre} a remover
     * @return {@code true} se removido com sucesso; {@code false} se o usuário não for encontrado ou gênero não presente
     */
    public boolean removePreference(String userId, Genre genre) {
        User u = userST.get(userId);
        if (u == null || genre == null) return false;
        return u.getPreferences().remove(genre);
    }

    /**
     * Retorna todos os usuários que possuem um gênero específico nas suas preferências.
     *
     * @param genreId o ID do gênero a procurar
     * @return lista de usuários correspondentes
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
    // --- Métodos Internos para organizar as árvores (Índices) ---
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