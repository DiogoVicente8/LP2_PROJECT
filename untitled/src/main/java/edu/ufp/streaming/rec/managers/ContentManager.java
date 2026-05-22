package edu.ufp.streaming.rec.managers;

import edu.princeton.cs.algs4.ST;
import edu.ufp.streaming.rec.models.Content;
import edu.ufp.streaming.rec.models.Documentary;
import edu.ufp.streaming.rec.models.Movie;
import edu.ufp.streaming.rec.models.Series;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gestor de conteúdos multimédia da plataforma de streaming.
 * Utiliza uma Symbol Table (ST) para armazenar conteúdos por ID
 * e mantém sincronização com a {@link ContentBST} para pesquisas ordenadas.
 *
 * @author Pedro
 */
public class ContentManager {

    private final ST<String, Content> contentSt;
    private ContentBST contentBst;

    public ContentManager(ContentBST contentBst) {
        this.contentSt = new ST<>();
        this.contentBst = contentBst;
    }

    // -------------------------------------------------------------------------
    // Consistência de Dados
    // -------------------------------------------------------------------------

    public boolean insert(Content content) {
        if (content == null || contentSt.contains(content.getId())) return false;

        //  Inserção síncrona! O conteúdo é guardado na ST para pesquisa rápida por ID,
        // e na BST para pesquisa ordenada por Data.
        contentSt.put(content.getId(), content);
        contentBst.insert(content);
        return true;
    }

    public Content remove(String id) {
        if (!contentSt.contains(id)) return null;

        // Remoção em cascata! Para garantir a consistência das estruturas,
        // removemos o conteúdo não só do dicionário principal, mas também da árvore binária.
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

    // -------------------------------------------------------------------------
    // Consultas Base
    // -------------------------------------------------------------------------

    public Content get(String id) {
        return contentSt.get(id);
    }

    public int size() {
        return contentSt.size();
    }

    public List<Content> listAll() {
        List<Content> result = new ArrayList<>();
        for (String contentId : contentSt.keys()) {
            result.add(contentSt.get(contentId));
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Consultas Avançadas
    // -------------------------------------------------------------------------

    public List<Content> searchByTitleSubstring(String substring) {
        String lower = substring.toLowerCase();

        // Legenda: Stream API. "Filtra a lista inteira mantendo apenas os títulos que contêm o texto"
        return listAll().stream()
                .filter(c -> c.getTitle().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    public List<Content> searchByGenre(String genreId) {
        return listAll().stream()
                .filter(c -> c.getGenre().getId().equals(genreId))
                .collect(Collectors.toList());
    }

    public List<Content> searchByRegion(String region) {
        return listAll().stream()
                .filter(c -> c.getRegion() != null && c.getRegion().equalsIgnoreCase(region))
                .collect(Collectors.toList());
    }

    public List<Content> searchByMinRating(double minRating) {
        return listAll().stream()
                .filter(c -> c.getRating() >= minRating)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Filtros por Tipologia Polimórfica
    // -------------------------------------------------------------------------

    public List<Movie> listMovies() {
        //  Polimorfismo + Streams. Filtramos apenas as instâncias de "Movie"
        // e fazemos o cast automático usando o .map()
        return listAll().stream()
                .filter(c -> c instanceof Movie)
                .map(c -> (Movie) c)
                .collect(Collectors.toList());
    }

    public List<Series> listSeries() {
        return listAll().stream()
                .filter(c -> c instanceof Series)
                .map(c -> (Series) c)
                .collect(Collectors.toList());
    }

    public List<Documentary> listDocumentaries() {
        return listAll().stream()
                .filter(c -> c instanceof Documentary)
                .map(c -> (Documentary) c)
                .collect(Collectors.toList());
    }
}