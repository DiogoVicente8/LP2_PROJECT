package edu.ufp.streaming.rec.managers;

import edu.princeton.cs.algs4.ST;
import edu.ufp.streaming.rec.models.Genre;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestor de géneros de conteúdos da plataforma de streaming.
 * Utiliza uma Symbol Table (ST) para armazenar géneros por ID.
 *
 * @author Pedro
 */
public class GenreManager {

    /** Symbol Table principal: chave = id do género. */
    private ST<String, Genre> genreSt;

    /**
     * Constrói um novo gestor de géneros com a ST vazia.
     */
    public GenreManager() {
        this.genreSt = new ST<>();
    }

    /**
     * Insere um género na ST.
     *
     * @param genre género a inserir
     * @return {@code true} se inserido, {@code false} se já existe ou é nulo
     */
    public boolean insert(Genre genre) {
        if (genre == null || genreSt.contains(genre.getId())) {
            return false;
        }
        genreSt.put(genre.getId(), genre);
        return true;
    }

    /**
     * Remove um género da ST pelo seu ID.
     *
     * @param id identificador do género a remover
     * @return género removido ou {@code null} se não encontrado
     */
    public Genre remove(String id) {
        if (!genreSt.contains(id)) return null;
        Genre removed = genreSt.get(id);
        genreSt.delete(id);
        return removed;
    }

    /**
     * Edita o nome de um género existente.
     *
     * @param id      identificador do género
     * @param newName novo nome
     * @return {@code true} se editado, {@code false} se não encontrado
     */
    public boolean editName(String id, String newName) {
        Genre g = genreSt.get(id);
        if (g == null) return false;
        g.setName(newName);
        return true;
    }

    /**
     * Devolve um género pelo seu ID.
     *
     * @param id identificador do género
     * @return género ou {@code null} se não encontrado
     */
    public Genre get(String id) {
        return genreSt.get(id);
    }

    /**
     * Devolve o número total de géneros na ST.
     *
     * @return número de géneros
     */
    public int size() {
        return genreSt.size();
    }

    /**
     * Lista todos os géneros da ST.
     *
     * @return lista com todos os géneros
     */
    public List<Genre> listAll() {
        List<Genre> result = new ArrayList<>();
        for (String key : genreSt.keys()) {
            result.add(genreSt.get(key));
        }
        return result;
    }
}