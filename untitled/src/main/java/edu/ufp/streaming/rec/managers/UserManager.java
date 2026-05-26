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
 * <li>ST primária (hash): {@code userId → User} — pesquisa O(1) amortizado</li>
 * <li>RedBlackBST por data: {@code epochDay → List<User>} — pesquisa por intervalo</li>
 * <li>RedBlackBST por nome: {@code nome → List<User>} — pesquisa por substring</li>
 * </ul>
 *
 * @author Diogo Vicente
 */
public class UserManager {

    private final ST<String, User> userST;
    private final RedBlackBST<Long, List<User>> byDateBST;
    private final RedBlackBST<String, List<User>> byNameBST;

    public UserManager() {
        this.userST    = new ST<>();
        this.byDateBST = new RedBlackBST<>();
        this.byNameBST = new RedBlackBST<>();
    }

    // -------------------------------------------------------------------------
    // CRUD e Edições
    // -------------------------------------------------------------------------

    public boolean insert(User user) {
        if (user == null || userST.contains(user.getId())) return false;
        userST.put(user.getId(), user);
        indexByDate(user);
        indexByName(user);
        return true;
    }

    public User remove(String id) {
        if (!userST.contains(id)) return null;
        User u = userST.get(id);
        userST.delete(id);
        removeFromDateIndex(u);
        removeFromNameIndex(u);
        return u;
    }

    public boolean editName(String id, String newName) {
        User u = userST.get(id);
        if (u == null) return false;
        removeFromNameIndex(u);
        u.setName(newName);
        indexByName(u);
        return true;
    }

    public boolean editEmail(String id, String newEmail) {
        User u = userST.get(id);
        if (u == null) return false;
        u.setEmail(newEmail);
        return true;
    }

    public boolean editRegion(String id, String newRegion) {
        User u = userST.get(id);
        if (u == null) return false;
        u.setRegion(newRegion);
        return true;
    }

    public boolean changePassword(String id, String newRawPassword) {
        User u = userST.get(id);
        if (u == null) return false;
        u.changePassword(newRawPassword);
        return true;
    }

    public User authenticate(String id, String rawPassword) {
        User u = userST.get(id);
        if (u == null) return null;
        if (!u.hasPassword()) return null;
        return u.checkPassword(rawPassword) ? u : null;
    }

    // -------------------------------------------------------------------------
    // Consultas
    // -------------------------------------------------------------------------

    public User get(String id) { return userST.get(id); }

    public boolean contains(String id) { return userST.contains(id); }

    public int size() { return userST.size(); }

    public List<User> listAll() {
        List<User> result = new ArrayList<>();
        for (String key : userST.keys()) {
            result.add(userST.get(key));
        }
        return result;
    }

    public List<User> searchByRegisterDate(LocalDate date) {
        List<User> list = byDateBST.get(date.toEpochDay());
        return list != null ? new ArrayList<>(list) : new ArrayList<>();
    }

    public List<User> searchByRegisterDateRange(LocalDate from, LocalDate to) {
        List<User> result = new ArrayList<>();

        // Iteramos apenas as chaves contidas no intervalo, reduzindo esforço computacional
        for (Long dataNaArvore : byDateBST.keys(from.toEpochDay(), to.toEpochDay())) {
            List<User> utilizadoresDestaData = byDateBST.get(dataNaArvore);

            //  SE a lista não for vazia, adicionamos
            if (utilizadoresDestaData != null) {
                result.addAll(utilizadoresDestaData);
            }
        }
        return result;
    }

    public List<User> searchByNameSubstring(String substring) {
        String lower = substring.toLowerCase();
        List<User> result = new ArrayList<>();

        for (String nomeNaArvore : byNameBST.keys()) {
            // SE o nome na árvore contiver a pesquisa, adiciona todos
            if (nomeNaArvore.contains(lower)) {
                result.addAll(byNameBST.get(nomeNaArvore));
            }
        }
        return result;
    }

    public List<User> searchByRegion(String region) {
        List<User> result = new ArrayList<>();

        for (User u : listAll()) {
            // Protege contra região nula e verifica se é igual à procurada
            if (u.getRegion() != null && region.equalsIgnoreCase(u.getRegion())) {
                result.add(u);
            }
        }
        return result;
    }

    public List<User> searchByRegionAndDateRange(String region, LocalDate from, LocalDate to) {
        List<User> result = new ArrayList<>();

        // Extraímos da Árvore Binária só as datas no intervalo
        for (Long dataNaArvore : byDateBST.keys(from.toEpochDay(), to.toEpochDay())) {
            List<User> utilizadoresDestaData = byDateBST.get(dataNaArvore);

            // Filtramos os resultados pela Região sem usar "continue"
            if (utilizadoresDestaData != null) {
                for (User u : utilizadoresDestaData) {
                    if (u.getRegion() != null && region.equalsIgnoreCase(u.getRegion())) {
                        result.add(u);
                    }
                }
            }
        }
        return result;
    }

    public List<User> searchByNameSubstringAndRegion(String substring, String region) {
        String lower = substring.toLowerCase();
        List<User> result = new ArrayList<>();

        for (String nomeNaArvore : byNameBST.keys()) {
            if (nomeNaArvore.contains(lower)) {
                for (User u : byNameBST.get(nomeNaArvore)) {
                    if (u.getRegion() != null && region.equalsIgnoreCase(u.getRegion())) {
                        result.add(u);
                    }
                }
            }
        }
        return result;
    }

    public boolean addPreference(String userId, Genre genre) {
        User u = userST.get(userId);
        if (u == null || genre == null) return false;
        if (u.getPreferences().contains(genre)) return false;
        u.addPreference(genre);
        return true;
    }

    public boolean removePreference(String userId, Genre genre) {
        User u = userST.get(userId);
        if (u == null || genre == null) return false;
        return u.getPreferences().remove(genre);
    }

    public List<User> searchByPreferredGenre(String genreId) {
        List<User> result = new ArrayList<>();

        for (User u : listAll()) {
            // "Existe algum género na lista deste utilizador com este 'ID'?"
            boolean gostaDesteGenero = u.getPreferences().stream()
                    .anyMatch(g -> g.getId().equals(genreId));

            if (gostaDesteGenero) {
                result.add(u);
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