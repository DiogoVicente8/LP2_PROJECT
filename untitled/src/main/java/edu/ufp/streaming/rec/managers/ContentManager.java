package edu.ufp.streaming.rec.managers;

import edu.princeton.cs.algs4.ST;
import edu.ufp.streaming.rec.models.Content;
import edu.ufp.streaming.rec.models.Documentary;
import edu.ufp.streaming.rec.models.Movie;
import edu.ufp.streaming.rec.models.Series;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestor de conteúdos multimédia da plataforma de streaming.
 * Utiliza uma Symbol Table (ST) para armazenar conteúdos por ID
 * e mantém sincronização com a {@link ContentBST} para pesquisas ordenadas.
 *
 * @author Pedro
 */
public class ContentManager {

    /** Symbol Table principal: chave = id do conteúdo. */
    private ST<String, Content> contentSt;

    /** BST de conteúdos ordenados por data de lançamento. */
    private ContentBST contentBst;

    /**
     * Constrói um novo gestor de conteúdos.
     *
     * @param contentBst BST associada para manter consistência (R4)
     */
    public ContentManager(ContentBST contentBst) {
        this.contentSt = new ST<>();
        this.contentBst = contentBst;
    }

    /**
     * Insere um conteúdo na ST e na BST.
     *
     * @param content conteúdo a inserir
     * @return {@code true} se inserido, {@code false} se já existe ou é nulo
     */
    public boolean insert(Content content) {
        if (content == null || contentSt.contains(content.getId())) return false;
        contentSt.put(content.getId(), content);
        contentBst.insert(content);
        return true;
    }

    /**
     * Remove um conteúdo da ST e da BST (R4 - consistência).
     *
     * @param id identificador do conteúdo a remover
     * @return conteúdo removido ou {@code null} se não encontrado
     */
    public Content remove(String id) {
        if (!contentSt.contains(id)) return null;
        Content removed = contentSt.get(id);
        contentSt.delete(id);
        contentBst.remove(id, removed.getReleaseDate());
        return removed;
    }

    /**
     * Edita o título de um conteúdo existente.
     *
     * @param id       identificador do conteúdo
     * @param newTitle novo título
     * @return {@code true} se editado, {@code false} se não encontrado
     */
    public boolean editTitle(String id, String newTitle) {
        Content c = contentSt.get(id);
        if (c == null) return false;
        c.setTitle(newTitle);
        return true;
    }

    /**
     * Devolve um conteúdo pelo seu ID.
     *
     * @param id identificador do conteúdo
     * @return conteúdo ou {@code null} se não encontrado
     */
    public Content get(String id) {
        return contentSt.get(id);
    }

    /**
     * Devolve o número total de conteúdos na ST.
     *
     * @return número de conteúdos
     */
    public int size() {
        return contentSt.size();
    }

    /**
     * Lista todos os conteúdos da ST.
     *
     * @return lista com todos os conteúdos
     */
    public List<Content> listAll() {
        List<Content> result = new ArrayList<>();
        for (String key : contentSt.keys()) {
            result.add(contentSt.get(key));
        }
        return result;
    }

    /**
     * Pesquisa conteúdos cujo título contenha a substring (case-insensitive).
     *
     * @param substring substring a pesquisar
     * @return lista de conteúdos correspondentes
     */
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

    /**
     * Pesquisa conteúdos de um determinado género.
     *
     * @param genreId identificador do género
     * @return lista de conteúdos do género
     */
    public List<Content> searchByGenre(String genreId) {
        List<Content> result = new ArrayList<>();
        for (String key : contentSt.keys()) {
            Content c = contentSt.get(key);
            if (c.getGenre().getId().equals(genreId)) result.add(c);
        }
        return result;
    }

    /**
     * Pesquisa conteúdos disponíveis numa determinada região.
     *
     * @param region região a pesquisar
     * @return lista de conteúdos da região
     */
    public List<Content> searchByRegion(String region) {
        List<Content> result = new ArrayList<>();
        for (String key : contentSt.keys()) {
            Content c = contentSt.get(key);
            if (c.getRegion().equalsIgnoreCase(region)) result.add(c);
        }
        return result;
    }

    /**
     * Pesquisa conteúdos com rating acima de um valor mínimo.
     *
     * @param minRating rating mínimo (inclusive)
     * @return lista de conteúdos com rating suficiente
     */
    public List<Content> searchByMinRating(double minRating) {
        List<Content> result = new ArrayList<>();
        for (String key : contentSt.keys()) {
            Content c = contentSt.get(key);
            if (c.getRating() >= minRating) result.add(c);
        }
        return result;
    }

    /**
     * Lista todos os filmes da ST.
     *
     * @return lista de {@link Movie}
     */
    public List<Movie> listMovies() {
        List<Movie> result = new ArrayList<>();
        for (String key : contentSt.keys()) {
            Content c = contentSt.get(key);
            if (c instanceof Movie) result.add((Movie) c);
        }
        return result;
    }

    /**
     * Lista todas as séries da ST.
     *
     * @return lista de {@link Series}
     */
    public List<Series> listSeries() {
        List<Series> result = new ArrayList<>();
        for (String key : contentSt.keys()) {
            Content c = contentSt.get(key);
            if (c instanceof Series) result.add((Series) c);
        }
        return result;
    }

    /**
     * Lista todos os documentários da ST.
     *
     * @return lista de {@link Documentary}
     */
    public List<Documentary> listDocumentaries() {
        List<Documentary> result = new ArrayList<>();
        for (String key : contentSt.keys()) {
            Content c = contentSt.get(key);
            if (c instanceof Documentary) result.add((Documentary) c);
        }
        return result;
    }
}