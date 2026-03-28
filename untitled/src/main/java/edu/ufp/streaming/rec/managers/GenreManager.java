package edu.ufp.streaming.rec.managers;

import edu.princeton.cs.algs4.ST;
import edu.ufp.streaming.rec.models.Genre;

import java.util.ArrayList;
import java.util.List;

public class GenreManager {

    private ST<String, Genre> genreST;

    public GenreManager() {
        this.genreST = new ST<>();
    }
    public boolean insert(Genre genre) {
        if (genre == null || genreST.contains(genre.getId())) {
            return false;
        }
        genreST.put(genre.getId(), genre);
        return true;
    }
    public Genre remove(String id) {
        if (!genreST.contains(id)) return null;
        Genre removed = genreST.get(id);
        genreST.delete(id);
        return removed;
    }

    public boolean editName(String id, String newName) {
        Genre g = genreST.get(id);
        if (g == null) return false;
        g.setName(newName);
        return true;
    }

    public List<Genre> listAll() {
        List<Genre> result = new ArrayList<>();
        for (String key : genreST.keys()) {
            result.add(genreST.get(key));
        }
        return result;
    }
}