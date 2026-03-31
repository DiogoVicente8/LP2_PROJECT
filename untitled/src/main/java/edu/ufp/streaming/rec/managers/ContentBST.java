package edu.ufp.streaming.rec.managers;

import edu.princeton.cs.algs4.RedBlackBST;
import edu.ufp.streaming.rec.models.Content;
import edu.ufp.streaming.rec.models.Documentary;
import edu.ufp.streaming.rec.models.Movie;
import edu.ufp.streaming.rec.models.Series;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Estrutura de dados ordenada para conteúdos multimédia, baseada em Red-Black BST.
 * Indexa conteúdos por data de lançamento, permitindo pesquisas ordenadas e por intervalo.
 *
 * @author Pedro
 * @version 1.0
 */
public class ContentBST {

    /** Red-Black BST: chave = data de lançamento (String), valor = lista de conteúdos. */
    private RedBlackBST<String, List<Content>> bst;

    /**
     * Constrói uma nova BST de conteúdos vazia.
     */
    public ContentBST() {
        this.bst = new RedBlackBST<>();
    }

    /**
     * Insere um conteúdo na BST indexado pela data de lançamento.
     *
     * @param content conteúdo a inserir
     */
    public void insert(Content content) {
        if (content == null) return;
        String key = content.getReleaseDate().toString();
        List<Content> list = bst.contains(key) ? bst.get(key) : new ArrayList<>();
        list.add(content);
        bst.put(key, list);
    }

    /**
     * Remove um conteúdo da BST pelo ID e data de lançamento.
     *
     * @param contentId identificador do conteúdo
     * @param date      data de lançamento do conteúdo
     * @return {@code true} se removido, {@code false} se não encontrado
     */
    public boolean remove(String contentId, LocalDate date) {
        String key = date.toString();
        if (!bst.contains(key)) return false;
        List<Content> list = bst.get(key);
        boolean removed = list.removeIf(c -> c.getId().equals(contentId));
        if (list.isEmpty()) bst.delete(key);
        return removed;
    }

    /**
     * Devolve todos os conteúdos lançados numa data específica.
     *
     * @param date data de lançamento
     * @return lista de conteúdos com essa data
     */
    public List<Content> getByDate(LocalDate date) {
        String key = date.toString();
        List<Content> result = bst.get(key);
        return result != null ? new ArrayList<>(result) : new ArrayList<>();
    }

    /**
     * Devolve todos os conteúdos lançados entre duas datas (inclusive), por ordem cronológica.
     *
     * @param from data de início
     * @param to   data de fim
     * @return lista de conteúdos no intervalo
     */
    public List<Content> getByDateRange(LocalDate from, LocalDate to) {
        List<Content> result = new ArrayList<>();
        for (String key : bst.keys(from.toString(), to.toString())) {
            result.addAll(bst.get(key));
        }
        return result;
    }

    /**
     * Devolve conteúdos de um género específico, ordenados por data.
     *
     * @param genreId identificador do género
     * @return lista de conteúdos do género por ordem cronológica
     */
    public List<Content> getByGenreOrdered(String genreId) {
        List<Content> result = new ArrayList<>();
        for (String key : bst.keys()) {
            for (Content c : bst.get(key)) {
                if (c.getGenre().getId().equals(genreId)) result.add(c);
            }
        }
        return result;
    }

    /**
     * Devolve todos os filmes por ordem cronológica.
     *
     * @return lista de {@link Movie} ordenada por data
     */
    public List<Movie> getMoviesOrdered() {
        List<Movie> result = new ArrayList<>();
        for (String key : bst.keys()) {
            for (Content c : bst.get(key)) {
                if (c instanceof Movie) result.add((Movie) c);
            }
        }
        return result;
    }

    /**
     * Devolve todas as séries por ordem cronológica.
     *
     * @return lista de {@link Series} ordenada por data
     */
    public List<Series> getSeriesOrdered() {
        List<Series> result = new ArrayList<>();
        for (String key : bst.keys()) {
            for (Content c : bst.get(key)) {
                if (c instanceof Series) result.add((Series) c);
            }
        }
        return result;
    }

    /**
     * Devolve todos os documentários por ordem cronológica.
     *
     * @return lista de {@link Documentary} ordenada por data
     */
    public List<Documentary> getDocumentariesOrdered() {
        List<Documentary> result = new ArrayList<>();
        for (String key : bst.keys()) {
            for (Content c : bst.get(key)) {
                if (c instanceof Documentary) result.add((Documentary) c);
            }
        }
        return result;
    }

    /**
     * Devolve a data de lançamento mais antiga na BST.
     *
     * @return data mais antiga ou {@code null} se vazia
     */
    public LocalDate getOldestDate() {
        return bst.isEmpty() ? null : LocalDate.parse(bst.min());
    }

    /**
     * Devolve a data de lançamento mais recente na BST.
     *
     * @return data mais recente ou {@code null} se vazia
     */
    public LocalDate getNewestDate() {
        return bst.isEmpty() ? null : LocalDate.parse(bst.max());
    }

    /**
     * Devolve o número total de conteúdos na BST.
     *
     * @return número de conteúdos
     */
    public int size() {
        int total = 0;
        for (String key : bst.keys()) total += bst.get(key).size();
        return total;
    }

    /**
     * Imprime na consola todos os conteúdos por ordem cronológica.
     */
    public void printOrdered() {
        System.out.println("=== ContentBST (" + size() + " conteudos por data) ===");
        for (String key : bst.keys()) {
            for (Content c : bst.get(key)) {
                System.out.println("  " + key + " -> " + c);
            }
        }
    }
}