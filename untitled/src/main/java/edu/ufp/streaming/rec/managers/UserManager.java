package edu.ufp.streaming.rec.managers;

import edu.princeton.cs.algs4.ST;
import edu.princeton.cs.algs4.RedBlackBST;
import edu.ufp.streaming.rec.models.Genre;
import edu.ufp.streaming.rec.models.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UserManager {

    /** Primary Symbol Table: userId → edu.pt.lp2.edu.ufp.streaming.rec.models.User (O(1) average lookup). */
    private final ST<String, User> userST;

    /** Ordered BST: registerDate → list of Users registered on that date.
     * CORREÇÃO: Alterado de LocalDate para Long
     */
    private final RedBlackBST<Long, List<User>> byDateBST;

    /** Ordered BST: lowercase name → list of Users with that name. */
    private final RedBlackBST<String, List<User>> byNameBST;


    public UserManager() {
        this.userST    = new ST<>();
        this.byDateBST = new RedBlackBST<>();
        this.byNameBST = new RedBlackBST<>();
    }


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

    public User get(String id) {
        return userST.get(id);
    }

    public boolean contains(String id) {
        return userST.contains(id);
    }

    public int size() {
        return userST.size();
    }

    public List<User> listAll() {
        List<User> result = new ArrayList<>();
        for (String key : userST.keys()) result.add(userST.get(key));
        return result;
    }

    // -------------------------------------------------------------------------
    // Ordered searches using RedBlackBST (R3)
    // -------------------------------------------------------------------------
    public List<User> searchByRegisterDate(LocalDate date) {
        // CORREÇÃO: date.toEpochDay()
        List<User> list = byDateBST.get(date.toEpochDay());
        return list != null ? new ArrayList<>(list) : new ArrayList<>();
    }

    public List<User> searchByRegisterDateRange(LocalDate from, LocalDate to) {
        List<User> result = new ArrayList<>();
        // CORREÇÃO: from.toEpochDay(), to.toEpochDay() e a variável 'd' passa a Long
        for (Long d : byDateBST.keys(from.toEpochDay(), to.toEpochDay())) {
            List<User> bucket = byDateBST.get(d);
            if (bucket != null) result.addAll(bucket);
        }
        return result;
    }

    public List<User> searchByNameSubstring(String substring) {
        String lower = substring.toLowerCase();
        List<User> result = new ArrayList<>();
        for (String key : byNameBST.keys()) {
            if (key.contains(lower)) {
                result.addAll(byNameBST.get(key));
            }
        }
        return result;
    }

    public List<User> searchByRegion(String region) {
        List<User> result = new ArrayList<>();
        for (String key : userST.keys()) {
            User u = userST.get(key);
            if (u.getRegion().equalsIgnoreCase(region)) result.add(u);
        }
        return result;
    }


    public List<User> searchByRegionAndDateRange(String region, LocalDate from, LocalDate to) {
        List<User> result = new ArrayList<>();
        // CORREÇÃO: from.toEpochDay(), to.toEpochDay() e a variável 'd' passa a Long
        for (Long d : byDateBST.keys(from.toEpochDay(), to.toEpochDay())) {
            List<User> bucket = byDateBST.get(d);
            if (bucket == null) continue;
            for (User u : bucket) {
                if (u.getRegion().equalsIgnoreCase(region)) result.add(u);
            }
        }
        return result;
    }

    public List<User> searchByNameSubstringAndRegion(String substring, String region) {
        List<User> result = new ArrayList<>();
        String lower = substring.toLowerCase();
        for (String key : byNameBST.keys()) {
            if (key.contains(lower)) {
                for (User u : byNameBST.get(key)) {
                    if (u.getRegion().equalsIgnoreCase(region)) result.add(u);
                }
            }
        }
        return result;
    }

    public List<User> searchByPreferredGenre(String genreId) {
        List<User> result = new ArrayList<>();
        for (String key : userST.keys()) {
            User u = userST.get(key);
            for (Genre g : u.getPreferences()) {
                if (g.getId().equals(genreId)) {
                    result.add(u);
                    break;
                }
            }
        }
        return result;
    }


    private void indexByDate(User user) {
        // CORREÇÃO: user.getRegisterDate().toEpochDay() para guardar como Long
        Long date = user.getRegisterDate().toEpochDay();
        List<User> bucket = byDateBST.get(date);
        if (bucket == null) {
            bucket = new ArrayList<>();
            byDateBST.put(date, bucket);
        }
        bucket.add(user);
    }

    private void removeFromDateIndex(User user) {
        Long date = user.getRegisterDate().toEpochDay();
        List<User> bucket = byDateBST.get(date);
        if (bucket != null) {
            bucket.remove(user);
            if (bucket.isEmpty()) byDateBST.delete(date);
        }
    }

    private void indexByName(User user) {
        String key = user.getName().toLowerCase();
        List<User> bucket = byNameBST.get(key);
        if (bucket == null) {
            bucket = new ArrayList<>();
            byNameBST.put(key, bucket);
        }
        bucket.add(user);
    }

    private void removeFromNameIndex(User user) {
        String key = user.getName().toLowerCase();
        List<User> bucket = byNameBST.get(key);
        if (bucket != null) {
            bucket.remove(user);
            if (bucket.isEmpty()) byNameBST.delete(key);
        }
    }
}