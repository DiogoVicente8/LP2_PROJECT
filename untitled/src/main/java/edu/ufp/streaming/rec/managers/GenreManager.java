package edu.ufp.streaming.rec.managers;

import edu.princeton.cs.algs4.ST;
import edu.ufp.streaming.rec.models.Genre;

import java.util.ArrayList;
import java.util.List;

public class GenreManager {

    private ST<String, Genre> genreSt;

    public GenreManager() {
        this.genreSt = new ST<>();
    }

    public boolean insert(Genre genre) {
        if (genre == null || genreSt.contains(genre.getId())) {
            return false;
        }
        genreSt.put(genre.getId(), genre);
        return true;
    }

    public Genre remove(String id) {
        if (!genreSt.contains(id)) return null;
        Genre removed = genreSt.get(id);
        genreSt.delete(id);
        return removed;
    }

    public boolean editName(String id, String newName) {
        Genre g = genreSt.get(id);
        if (g == null) return false;
        g.setName(newName);
        return true;
    }

    public Genre get(String id) {
        return genreSt.get(id);
    }

    public int size() {
        return genreSt.size();
    }

    public List<Genre> listAll() {
        List<Genre> result = new ArrayList<>();
        for (String key : genreSt.keys()) {
            result.add(genreSt.get(key));
        }
        return result;
    }
}