package edu.ufp.streaming.rec.managers;

import edu.princeton.cs.algs4.ST;
import edu.princeton.cs.algs4.RedBlackBST;
import edu.ufp.streaming.rec.models.Artist;
import edu.ufp.streaming.rec.enums.ArtistRole;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class ArtistManager {

    /** Primary Symbol Table: artistId → edu.pt.lp2.edu.ufp.streaming.rec.models.Artist (O(1) average lookup). */
    private final ST<String, Artist> artistST;

    /** Ordered BST: birthDate → list of Artists born on that date.
     * CORREÇÃO: Alterado de LocalDate para Long
     */
    private final RedBlackBST<Long, List<Artist>> byBirthDateBST;

    /** Ordered BST: lowercase name → list of Artists with that name. */
    private final RedBlackBST<String, List<Artist>> byNameBST;

    /**
     * Constructs an empty edu.pt.lp2.managers.edu.ufp.streaming.rec.managers.ArtistManager.
     */
    public ArtistManager() {
        this.artistST       = new ST<>();
        this.byBirthDateBST = new RedBlackBST<>();
        this.byNameBST      = new RedBlackBST<>();
    }

    // -------------------------------------------------------------------------
    // CRUD — Insert / Remove / Edit
    // -------------------------------------------------------------------------


    public boolean insert(Artist artist) {
        if (artist == null || artistST.contains(artist.getId())) return false;

        artistST.put(artist.getId(), artist);
        indexByBirthDate(artist);
        indexByName(artist);
        return true;
    }

    public Artist remove(String id) {
        if (!artistST.contains(id)) return null;

        Artist a = artistST.get(id);
        artistST.delete(id);
        removeFromBirthDateIndex(a);
        removeFromNameIndex(a);
        return a;
    }

    public boolean editName(String id, String newName) {
        Artist a = artistST.get(id);
        if (a == null) return false;

        removeFromNameIndex(a);
        a.setName(newName);
        indexByName(a);
        return true;
    }


    public boolean editNationality(String id, String newNationality) {
        Artist a = artistST.get(id);
        if (a == null) return false;
        a.setNationality(newNationality);
        return true;
    }

    public Artist get(String id) {
        return artistST.get(id);
    }

    public boolean contains(String id) {
        return artistST.contains(id);
    }

    public int size() {
        return artistST.size();
    }


    public List<Artist> listAll() {
        List<Artist> result = new ArrayList<>();
        for (String key : artistST.keys()) result.add(artistST.get(key));
        return result;
    }

    // -------------------------------------------------------------------------
    // Ordered searches using RedBlackBST (R3)
    // -------------------------------------------------------------------------


    public List<Artist> searchByBirthDate(LocalDate date) {
        // CORREÇÃO: date.toEpochDay()
        List<Artist> list = byBirthDateBST.get(date.toEpochDay());
        return list != null ? new ArrayList<>(list) : new ArrayList<>();
    }


    public List<Artist> searchByBirthDateRange(LocalDate from, LocalDate to) {
        List<Artist> result = new ArrayList<>();
        // CORREÇÃO: from.toEpochDay(), to.toEpochDay() e a variável 'd' passa a Long
        for (Long d : byBirthDateBST.keys(from.toEpochDay(), to.toEpochDay())) {
            List<Artist> bucket = byBirthDateBST.get(d);
            if (bucket != null) result.addAll(bucket);
        }
        return result;
    }

    public List<Artist> searchByNameSubstring(String substring) {
        String lower = substring.toLowerCase();
        List<Artist> result = new ArrayList<>();
        for (String key : byNameBST.keys()) {
            if (key.contains(lower)) result.addAll(byNameBST.get(key));
        }
        return result;
    }

    public List<Artist> searchByNationality(String nationality) {
        List<Artist> result = new ArrayList<>();
        for (String key : artistST.keys()) {
            Artist a = artistST.get(key);
            if (a.getNationality().equalsIgnoreCase(nationality)) result.add(a);
        }
        return result;
    }


    public List<Artist> searchByGender(String gender) {
        List<Artist> result = new ArrayList<>();
        for (String key : artistST.keys()) {
            Artist a = artistST.get(key);
            if (a.getGender().equalsIgnoreCase(gender)) result.add(a);
        }
        return result;
    }

    public List<Artist> searchByNationalityAndBirthDateRange(String nationality,
                                                             LocalDate from,
                                                             LocalDate to) {
        List<Artist> result = new ArrayList<>();
        // CORREÇÃO: from.toEpochDay(), to.toEpochDay() e a variável 'd' passa a Long
        for (Long d : byBirthDateBST.keys(from.toEpochDay(), to.toEpochDay())) {
            List<Artist> bucket = byBirthDateBST.get(d);
            if (bucket == null) continue;
            for (Artist a : bucket) {
                if (a.getNationality().equalsIgnoreCase(nationality)) result.add(a);
            }
        }
        return result;
    }

    public List<Artist> searchByNameSubstringNationalityAndGender(String substring,
                                                                  String nationality,
                                                                  String gender) {
        String lower = substring.toLowerCase();
        List<Artist> result = new ArrayList<>();
        for (String key : byNameBST.keys()) {
            if (!key.contains(lower)) continue;
            for (Artist a : byNameBST.get(key)) {
                if (a.getNationality().equalsIgnoreCase(nationality)
                        && a.getGender().equalsIgnoreCase(gender)) {
                    result.add(a);
                }
            }
        }
        return result;
    }

    public List<Artist> searchByRole(ArtistRole role) {
        List<Artist> result = new ArrayList<>();
        for (String key : artistST.keys()) {
            Artist a = artistST.get(key);
            if (a.getRole() == role) result.add(a);
        }
        return result;
    }

    private void indexByBirthDate(Artist artist) {
        // CORREÇÃO: artist.getBirthDate().toEpochDay() para guardar como Long
        Long date = artist.getBirthDate().toEpochDay();
        List<Artist> bucket = byBirthDateBST.get(date);
        if (bucket == null) {
            bucket = new ArrayList<>();
            byBirthDateBST.put(date, bucket);
        }
        bucket.add(artist);
    }

    private void removeFromBirthDateIndex(Artist artist) {
        // CORREÇÃO: artist.getBirthDate().toEpochDay()
        Long date = artist.getBirthDate().toEpochDay();
        List<Artist> bucket = byBirthDateBST.get(date);
        if (bucket != null) {
            bucket.remove(artist);
            if (bucket.isEmpty()) byBirthDateBST.delete(date);
        }
    }


    private void indexByName(Artist artist) {
        String key = artist.getName().toLowerCase();
        List<Artist> bucket = byNameBST.get(key);
        if (bucket == null) {
            bucket = new ArrayList<>();
            byNameBST.put(key, bucket);
        }
        bucket.add(artist);
    }

    private void removeFromNameIndex(Artist artist) {
        String key = artist.getName().toLowerCase();
        List<Artist> bucket = byNameBST.get(key);
        if (bucket != null) {
            bucket.remove(artist);
            if (bucket.isEmpty()) byNameBST.delete(key);
        }
    }
}