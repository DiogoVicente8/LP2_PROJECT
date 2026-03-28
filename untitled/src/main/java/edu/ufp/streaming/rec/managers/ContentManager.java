package edu.ufp.streaming.rec.managers;

import edu.princeton.cs.algs4.ST;
import edu.ufp.streaming.rec.models.Content;
import edu.ufp.streaming.rec.models.Documentary;
import edu.ufp.streaming.rec.models.Movie;
import edu.ufp.streaming.rec.models.Series;

import java.util.ArrayList;
import java.util.List;

public class ContentManager {

    private ST<String, Content> contentST;

    public ContentManager() {
        this.contentST = new ST<>();
    }

    public boolean insert(Content content) {
        if (content == null || contentST.contains(content.getId())) return false;
        contentST.put(content.getId(), content);
        return true;
    }

    public Content remove(String id) {
        if (!contentST.contains(id)) return null;
        Content removed = contentST.get(id);
        contentST.delete(id);
        return removed;
    }

    public boolean editTitle(String id, String newTitle) {
        Content c = contentST.get(id);
        if (c == null) return false;
        c.setTitle(newTitle);
        return true;
    }

    public Content get(String id) {
        return contentST.get(id);
    }

    public int size() {
        return contentST.size();
    }

    public List<Content> listAll() {
        List<Content> result = new ArrayList<>();
        for (String key : contentST.keys()) {
            result.add(contentST.get(key));
        }
        return result;
    }
    public List<Content> searchByTitleSubstring(String substring) {
        List<Content> result = new ArrayList<>();
        String lower = substring.toLowerCase();
        for (String key : contentST.keys()) {
            Content c = contentST.get(key);
            if (c.getTitle().toLowerCase().contains(lower)) {
                result.add(c);
            }
        }
        return result;
    }
    public List<Content> searchByGenre(String genreId) {
        List<Content> result = new ArrayList<>();
        for (String key : contentST.keys()) {
            Content c = contentST.get(key);
            if (c.getGenre().getId().equals(genreId)) result.add(c);
        }
        return result;
    }

    public List<Content> searchByRegion(String region) {
        List<Content> result = new ArrayList<>();
        for (String key : contentST.keys()) {
            Content c = contentST.get(key);
            if (c.getRegion().equalsIgnoreCase(region)) result.add(c);
        }
        return result;
    }

    public List<Content> searchByMinRating(double minRating) {
        List<Content> result = new ArrayList<>();
        for (String key : contentST.keys()) {
            Content c = contentST.get(key);
            if (c.getRating() >= minRating) result.add(c);
        }
        return result;
    }

    public List<Movie> listMovies() {
        List<Movie> result = new ArrayList<>();
        for (String key : contentST.keys()) {
            Content c = contentST.get(key);
            if (c instanceof Movie) result.add((Movie) c);
        }
        return result;
    }

    public List<Series> listSeries() {
        List<Series> result = new ArrayList<>();
        for (String key : contentST.keys()) {
            Content c = contentST.get(key);
            if (c instanceof Series) result.add((Series) c);
        }
        return result;
    }

    public List<Documentary> listDocumentaries() {
        List<Documentary> result = new ArrayList<>();
        for (String key : contentST.keys()) {
            Content c = contentST.get(key);
            if (c instanceof Documentary) result.add((Documentary) c);
        }
        return result;
    }
}