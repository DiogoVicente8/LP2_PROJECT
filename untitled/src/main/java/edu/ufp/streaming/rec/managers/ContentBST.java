package edu.ufp.streaming.rec.managers;

import edu.princeton.cs.algs4.RedBlackBST;
import edu.ufp.streaming.rec.models.Content;
import edu.ufp.streaming.rec.models.Movie;

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
    private final RedBlackBST<String, List<Content>> bst;

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

        // Utilização funcional do Java para varrer a lista e remover o conteúdo que der "Match" no ‘ID’
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

    public List<Content> getByDateRange(LocalDate from, LocalDate to) {
        List<Content> result = new ArrayList<>();

        // A magia da BST: iteramos Apenas as datas que caem no intervalo pretendido
        for (String dataIso : bst.keys(from.toString(), to.toString())) {
            result.addAll(bst.get(dataIso));
        }
        return result;
    }
    /**
     * Retorna a data de lançamento mais antiga registada na árvore.
     */
    public LocalDate getOldestDate() {
        if (bst.isEmpty()) return null;
        // A BST armazena as chaves de forma ordenada, logo o "min" é a data mais antiga
        String oldest = bst.min();
        return LocalDate.parse(oldest);
    }

    /**
     * Retorna a data de lançamento mais recente registada na árvore.
     */
    public LocalDate getNewestDate() {
        if (bst.isEmpty()) return null;
        // O "max" da BST dá-nos a data mais recente
        String newest = bst.max();
        return LocalDate.parse(newest);
    }

    /**
     * Retorna uma lista de todos os Filmes (Movies) ordenados cronologicamente.
     */
    public List<Movie> getMoviesOrdered() {
        List<Movie> result = new ArrayList<>();
        // O método keys() da BST devolve as datas já em ordem cronológica
        for (String dataIso : bst.keys()) {
            for (Content c : bst.get(dataIso)) {
                // Filtra apenas os conteúdos que são instâncias de Movie
                if (c instanceof Movie) {
                    result.add((Movie) c);
                }
            }
        }
        return result;
    }



    // -------------------------------------------------------------------------
    // Utilitários de Data
    // -------------------------------------------------------------------------

    public int size() {
        int total = 0;
        for (String dataIso : bst.keys()) {
            total += bst.get(dataIso).size();
        }
        return total;
    }
}