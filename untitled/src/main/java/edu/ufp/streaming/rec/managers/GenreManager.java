package edu.ufp.streaming.rec.managers;

import edu.princeton.cs.algs4.ST;
import edu.ufp.streaming.rec.models.Genre;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestor de géneros de conteúdos da plataforma de 'streaming'.
 * Utiliza uma Symbol Table (ST) para armazenar géneros por 'ID'.
 *
 * @author Pedro
 */
public class GenreManager {

    // Legenda: Cumprimento do Requisito R2. Usamos uma Symbol Table (ST) da biblioteca algs4
    // para garantir pesquisas super-rápidas baseadas num 'ID' único ('String').
    private final ST<String, Genre> genreSt;

    public GenreManager() {
        this.genreSt = new ST<>();
    }

    // -------------------------------------------------------------------------
    // CRUD e Edições (Refatorado - Clean Code)
    // -------------------------------------------------------------------------

    public boolean insert(Genre genre) {
        // Lógica humana: Aborta imediatamente se o objeto for nulo ou se o ID já existir na ST.
        if (genre == null || genreSt.contains(genre.getId())) {
            return false;
        }

        genreSt.put(genre.getId(), genre);
        return true;
    }

    public Genre remove(String id) {
        if (!genreSt.contains(id)) return null;

        Genre genreRemovido = genreSt.get(id);
        genreSt.delete(id);
        return genreRemovido;
    }

    public boolean editName(String id, String newName) {
        Genre genre = genreSt.get(id);

        if (genre == null) return false;

        genre.setName(newName);
        return true;
    }

    // -------------------------------------------------------------------------
    // Consultas Rápidas
    // -------------------------------------------------------------------------

    public Genre get(String id) {
        return genreSt.get(id);
    }

    public int size() {
        return genreSt.size();
    }

    public List<Genre> listAll() {
        List<Genre> result = new ArrayList<>();
        // A ST do algs4 devolve-nos as chaves ordenadas automaticamente.
        for (String genreId : genreSt.keys()) {
            result.add(genreSt.get(genreId));
        }

        return result;
    }
}