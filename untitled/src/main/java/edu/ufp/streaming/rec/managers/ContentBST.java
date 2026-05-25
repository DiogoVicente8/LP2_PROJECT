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
 * Indexa conteúdos por data de lançamento, permitindo pesquisas cronológicas incrivelmente rápidas.
 *
 * @author Pedro
 * @version 1.0
 */
public class ContentBST {

    // A árvore usa a data no formato ISO-8601 (YYYY-MM-DD) como chave para garantir
    // a ordenação cronológica automática, e uma lista para lidar com colisões (vários filmes no mesmo dia).
    private RedBlackBST<String, List<Content>> bst;

    public ContentBST() {
        this.bst = new RedBlackBST<>();
    }

    // -------------------------------------------------------------------------
    // Inserção e Remoção
    // -------------------------------------------------------------------------

    public void insert(Content content) {
        if (content == null) return;

        String dataIso = content.getReleaseDate().toString();
        List<Content> bucketDeConteudos = bst.contains(dataIso) ? bst.get(dataIso) : new ArrayList<>();

        bucketDeConteudos.add(content);
        bst.put(dataIso, bucketDeConteudos);
    }

    public boolean remove(String contentId, LocalDate date) {
        String dataIso = date.toString();
        if (!bst.contains(dataIso)) return false;

        List<Content> bucketDeConteudos = bst.get(dataIso);

        // Utilização funcional do Java para varrer a lista e remover o conteúdo que der "Match" no ID
        boolean foiRemovido = bucketDeConteudos.removeIf(c -> c.getId().equals(contentId));

        // Se o dia ficar sem nenhum conteúdo, apagamos o nó da árvore para poupar memória
        if (bucketDeConteudos.isEmpty()) {
            bst.delete(dataIso);
        }

        return foiRemovido;
    }

    // -------------------------------------------------------------------------
    // Pesquisas Cronológicas
    // -------------------------------------------------------------------------

    public List<Content> getByDate(LocalDate date) {
        String dataIso = date.toString();
        List<Content> resultados = bst.get(dataIso);

        return resultados != null ? new ArrayList<>(resultados) : new ArrayList<>();
    }

    public List<Content> getByDateRange(LocalDate from, LocalDate to) {
        List<Content> result = new ArrayList<>();

        // A magia da BST: iteramos APENAS as datas que caem no intervalo pretendido
        for (String dataIso : bst.keys(from.toString(), to.toString())) {
            result.addAll(bst.get(dataIso));
        }
        return result;
    }

    public List<Content> getByGenreOrdered(String genreId) {
        List<Content> result = new ArrayList<>();

        for (String dataIso : bst.keys()) {
            for (Content conteudo : bst.get(dataIso)) {
                if (conteudo.getGenre().getId().equals(genreId)) {
                    result.add(conteudo);
                }
            }
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Pesquisas Polimórficas Ordenadas
    // -------------------------------------------------------------------------

    public List<Movie> getMoviesOrdered() {
        List<Movie> result = new ArrayList<>();

        // A BST já nos devolve as chaves pré-ordenadas do mais antigo para o mais recente
        for (String dataIso : bst.keys()) {
            for (Content conteudo : bst.get(dataIso)) {
                // Pattern Matching: Se for Filme, o Java faz o cast automático para a variável "m"
                if (conteudo instanceof Movie m) {
                    result.add(m);
                }
            }
        }
        return result;
    }

    public List<Series> getSeriesOrdered() {
        List<Series> result = new ArrayList<>();

        for (String dataIso : bst.keys()) {
            for (Content conteudo : bst.get(dataIso)) {
                if (conteudo instanceof Series s) {
                    result.add(s);
                }
            }
        }
        return result;
    }

    public List<Documentary> getDocumentariesOrdered() {
        List<Documentary> result = new ArrayList<>();

        for (String dataIso : bst.keys()) {
            for (Content conteudo : bst.get(dataIso)) {
                if (conteudo instanceof Documentary d) {
                    result.add(d);
                }
            }
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Utilitários de Data
    // -------------------------------------------------------------------------

    public LocalDate getOldestDate() {
        return bst.isEmpty() ? null : LocalDate.parse(bst.min());
    }

    public LocalDate getNewestDate() {
        return bst.isEmpty() ? null : LocalDate.parse(bst.max());
    }

    public int size() {
        int total = 0;
        for (String dataIso : bst.keys()) {
            total += bst.get(dataIso).size();
        }
        return total;
    }

    public void printOrdered() {
        System.out.println("=== ContentBST (" + size() + " conteudos por data) ===");
        for (String dataIso : bst.keys()) {
            for (Content conteudo : bst.get(dataIso)) {
                System.out.println("  " + dataIso + " -> " + conteudo);
            }
        }
    }
}