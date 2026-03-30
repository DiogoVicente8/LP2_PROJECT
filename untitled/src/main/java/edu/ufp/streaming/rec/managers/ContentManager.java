package edu.ufp.streaming.rec.managers;

import edu.princeton.cs.algs4.ST;
import edu.ufp.streaming.rec.models.Content;
import edu.ufp.streaming.rec.models.Documentary;
import edu.ufp.streaming.rec.models.Movie;
import edu.ufp.streaming.rec.models.Series;

import java.util.ArrayList;
import java.util.List;

public class ContentManager {

    private ST<String, Content> contentSt;
    private ContentBST contentBst;

    public ContentManager(ContentBST contentBst) {
        this.contentSt = new ST<>();
        this.contentBst = contentBst;
    }

    public boolean insert(Content content) {
        if (content == null || contentSt.contains(content.getId())) return false;
        contentSt.put(content.getId(), content);
        contentBst.insert(content);
        return true;
    }

    public Content remove(String id) {
        if (!contentSt.contains(id)) return null;
        Content removed = contentSt.get(id);
        contentSt.delete(id);
        contentBst.remove(id, removed.getReleaseDate());
        return removed;
    }

    public boolean editTitle(String id, String newTitle) {
        Content c = contentSt.get(id);
        if (c == null) return false;
        c.setTitle(newTitle);
        return true;
    }

    public Content get(String id) {
        return contentSt.get(id);
    }

    public int size() {
        return contentSt.size();
    }

    public List<Content> listAll() {
        List<Content> result = new ArrayList<>();
        for (String key : contentSt.keys()) {
            result.add(contentSt.get(key));
        }
        return result;
    }

    public List<Content> searchByTitleSubstring(String substring) {
        List<Content> result = new ArrayList<>();
        String lower = substring.toLowerCase();
        for (String key : contentSt.keys()) {
            Content c = contentSt.get(key);
            if (c.getTitle().toLowerCase().contains(lower)) {
                result.add(c);
            }
        }
        return result;
    }

    public List<Content> searchByGenre(String genreId) {
        List<Content> result = new ArrayList<>();
        for (String key : contentSt.keys()) {
            Content c = contentSt.get(key);
            if (c.getGenre().getId().equals(genreId)) result.add(c);
        }
        return result;
    }

    public List<Content> searchByRegion(String region) {
        List<Content> result = new ArrayList<>();
        for (String key : contentSt.keys()) {
            Content c = contentSt.get(key);
            if (c.getRegion().equalsIgnoreCase(region)) result.add(c);
        }
        return result;
    }

    public List<Content> searchByMinRating(double minRating) {
        List<Content> result = new ArrayList<>();
        for (String key : contentSt.keys()) {
            Content c = contentSt.get(key);
            if (c.getRating() >= minRating) result.add(c);
        }
        return result;
    }

    public List<Movie> listMovies() {
        List<Movie> result = new ArrayList<>();
        for (String key : contentSt.keys()) {
            Content c = contentSt.get(key);
            if (c instanceof Movie) result.add((Movie) c);
        }
        return result;
    }

    public List<Series> listSeries() {
        List<Series> result = new ArrayList<>();
        for (String key : contentSt.keys()) {
            Content c = contentSt.get(key);
            if (c instanceof Series) result.add((Series) c);
        }
        return result;
    }

    public List<Documentary> listDocumentaries() {
        List<Documentary> result = new ArrayList<>();
        for (String key : contentSt.keys()) {
            Content c = contentSt.get(key);
            if (c instanceof Documentary) result.add((Documentary) c);
        }
        return result;
    }

}