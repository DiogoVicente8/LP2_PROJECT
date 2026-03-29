package edu.ufp.streaming.rec.managers;

import edu.princeton.cs.algs4.RedBlackBST;
import edu.ufp.streaming.rec.models.Content;
import edu.ufp.streaming.rec.models.Documentary;
import edu.ufp.streaming.rec.models.Movie;
import edu.ufp.streaming.rec.models.Series;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ContentBST {

    private RedBlackBST<String, List<Content>> bst;

    public ContentBST() {
        this.bst = new RedBlackBST<>();
    }

    public void insert(Content content) {
        if (content == null) return;
        String key = content.getReleaseDate().toString();
        List<Content> list = bst.contains(key) ? bst.get(key) : new ArrayList<>();
        list.add(content);
        bst.put(key, list);
    }

    public boolean remove(String contentId, LocalDate date) {
        String key = date.toString();
        if (!bst.contains(key)) return false;
        List<Content> list = bst.get(key);
        boolean removed = list.removeIf(c -> c.getId().equals(contentId));
        if (list.isEmpty()) bst.delete(key);
        return removed;
    }

    public List<Content> getByDate(LocalDate date) {
        String key = date.toString();
        List<Content> result = bst.get(key);
        return result != null ? new ArrayList<>(result) : new ArrayList<>();
    }

    public List<Content> getByDateRange(LocalDate from, LocalDate to) {
        List<Content> result = new ArrayList<>();
        for (String key : bst.keys(from.toString(), to.toString())) {
            result.addAll(bst.get(key));
        }
        return result;
    }

    public List<Content> getByGenreOrdered(String genreId) {
        List<Content> result = new ArrayList<>();
        for (String key : bst.keys()) {
            for (Content c : bst.get(key)) {
                if (c.getGenre().getId().equals(genreId)) result.add(c);
            }
        }
        return result;
    }

    public List<Movie> getMoviesOrdered() {
        List<Movie> result = new ArrayList<>();
        for (String key : bst.keys()) {
            for (Content c : bst.get(key)) {
                if (c instanceof Movie) result.add((Movie) c);
            }
        }
        return result;
    }

    public List<Series> getSeriesOrdered() {
        List<Series> result = new ArrayList<>();
        for (String key : bst.keys()) {
            for (Content c : bst.get(key)) {
                if (c instanceof Series) result.add((Series) c);
            }
        }
        return result;
    }

    public List<Documentary> getDocumentariesOrdered() {
        List<Documentary> result = new ArrayList<>();
        for (String key : bst.keys()) {
            for (Content c : bst.get(key)) {
                if (c instanceof Documentary) result.add((Documentary) c);
            }
        }
        return result;
    }

    public LocalDate getOldestDate() {
        return bst.isEmpty() ? null : LocalDate.parse(bst.min());
    }

    public LocalDate getNewestDate() {
        return bst.isEmpty() ? null : LocalDate.parse(bst.max());
    }

    public int size() {
        int total = 0;
        for (String key : bst.keys()) total += bst.get(key).size();
        return total;
    }

    public void printOrdered() {
        System.out.println("=== ContentBST (" + size() + " conteúdos por data) ===");
        for (String key : bst.keys()) {
            for (Content c : bst.get(key)) {
                System.out.println("  " + key + " -> " + c);
            }
        }
    }
}