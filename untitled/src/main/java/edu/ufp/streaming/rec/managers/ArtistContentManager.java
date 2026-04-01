package edu.ufp.streaming.rec.managers;

import edu.princeton.cs.algs4.ST;
import edu.princeton.cs.algs4.RedBlackBST;
import edu.ufp.streaming.rec.models.Artist;
import edu.ufp.streaming.rec.models.ArtistContent;
import edu.ufp.streaming.rec.enums.ArtistRole;
import edu.ufp.streaming.rec.models.Content;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
/**
 * Gere todas as relações de participação {@link ArtistContent} entre
 * as entidades {@link Artist} (Artista) e {@link Content} (Conteúdo) na plataforma.
 * @author  Diogo Vicente
 */
public class ArtistContentManager {

    /** ST Primária: "artistId:contentId:role" → ArtistContent. */
    private final ST<String, ArtistContent> participationST;

    /** Índice: artistId → lista de ArtistContent. */
    private final ST<String, List<ArtistContent>> byArtistIndex;

    /** Índice: contentId → lista de ArtistContent (elenco/equipa de um conteúdo). */
    private final ST<String, List<ArtistContent>> byContentIndex;

    /** BST Ordenada: data de participação como dia da época (Long) → lista de ArtistContent. */
    private final RedBlackBST<Long, List<ArtistContent>> byDateBST;

    /**
     * Constrói um ArtistContentManager vazio.
     */
    public ArtistContentManager() {
        this.participationST = new ST<>();
        this.byArtistIndex   = new ST<>();
        this.byContentIndex  = new ST<>();
        this.byDateBST       = new RedBlackBST<>();
    }

    /**
     * Regista a participação de um artista num item de conteúdo.
     * Também atualiza a filmografia interna do artista via {@link Artist#addParticipation(ArtistContent)}.
     *
     * @param artist  o {@link Artist} participante; não deve ser {@code null}
     * @param content o item de {@link Content}; não deve ser {@code null}
     * @param role    a função ({@link ArtistRole}) que o artista desempenhou
     * @param date    a data da participação
     * @return o {@link ArtistContent} criado, ou {@code null} se já existir ou for inválido
     */
    public ArtistContent addParticipation(Artist artist, Content content,
                                          ArtistRole role, LocalDate date) {
        if (artist == null || content == null || role == null || date == null) return null;

        String key = compositeKey(artist.getId(), content.getId(), role);
        if (participationST.contains(key)) return null;

        ArtistContent ac = new ArtistContent(artist, content, role, date);
        participationST.put(key, ac);
        indexByArtist(ac);
        indexByContent(ac);
        indexByDate(ac);

        // Keep edu.pt.lp2.edu.ufp.streaming.rec.models.Artist's internal filmography in sync
        artist.addParticipation(ac);

        return ac;
    }

    /**
     * Remove um registo de participação específico.
     *
     * @param artistId  o ID do artista
     * @param contentId o ID do conteúdo
     * @param role      a função que o artista desempenhou nesse conteúdo
     * @return o {@link ArtistContent} removido, ou {@code null} se não for encontrado
     */
    public ArtistContent removeParticipation(String artistId, String contentId, ArtistRole role) {
        String key = compositeKey(artistId, contentId, role);
        if (!participationST.contains(key)) return null;

        ArtistContent ac = participationST.get(key);
        participationST.delete(key);
        removeFromArtistIndex(ac);
        removeFromContentIndex(ac);
        removeFromDateIndex(ac);
        return ac;
    }

    /**
     * Remove TODOS os registos de participação de um determinado artista.
     * Deve ser chamado quando um artista é apagado (consistência R4).
     *
     * @param artistId o ID do artista a ser removido
     */
    public void removeAllByArtist(String artistId) {
        List<ArtistContent> list = byArtistIndex.get(artistId);
        if (list == null) return;

        for (ArtistContent ac : new ArrayList<>(list)) {
            String key = compositeKey(ac.getArtist().getId(), ac.getContent().getId(), ac.getRole());
            participationST.delete(key);
            removeFromContentIndex(ac);
            removeFromDateIndex(ac);
        }
        byArtistIndex.delete(artistId);
    }

    /**
     * Remove TODOS os registos de participação de um determinado conteúdo.
     * Deve ser chamado quando um conteúdo é apagado (consistência R4).
     *
     * @param contentId o ID do conteúdo a ser removido
     */
    public void removeAllByContent(String contentId) {
        List<ArtistContent> list = byContentIndex.get(contentId);
        if (list == null) return;

        for (ArtistContent ac : new ArrayList<>(list)) {
            String key = compositeKey(ac.getArtist().getId(), contentId, ac.getRole());
            participationST.delete(key);
            removeFromArtistIndex(ac);
            removeFromDateIndex(ac);
        }
        byContentIndex.delete(contentId);
    }

    /**
     * Retorna a filmografia de um artista (todos os conteúdos em que participou).
     *
     * @param artistId o ID do artista
     * @return lista de registos {@link ArtistContent}; vazia se nenhuns
     */
    public List<ArtistContent> getFilmography(String artistId) {
        List<ArtistContent> list = byArtistIndex.get(artistId);
        return list != null ? new ArrayList<>(list) : new ArrayList<>();
    }

    /**
     * Retorna todos os artistas que participaram num conteúdo (elenco + equipa técnica).
     *
     * @param contentId o ID do conteúdo
     * @return lista de registos {@link ArtistContent}; vazia se nenhuns
     */
    public List<ArtistContent> getCastAndCrew(String contentId) {
        List<ArtistContent> list = byContentIndex.get(contentId);
        return list != null ? new ArrayList<>(list) : new ArrayList<>();
    }

    /**
     * Retorna todas as participações de um artista filtradas por função.
     *
     * @param artistId o ID do artista
     * @param role     a {@link ArtistRole} pela qual filtrar
     * @return lista de registos {@link ArtistContent} correspondentes
     */
    public List<ArtistContent> getFilmographyByRole(String artistId, ArtistRole role) {
        List<ArtistContent> result = new ArrayList<>();
        List<ArtistContent> list = byArtistIndex.get(artistId);
        if (list == null) return result;
        for (ArtistContent ac : list) {
            if (ac.getRole() == role) result.add(ac);
        }
        return result;
    }

    /**
     * Retorna todas as participações de um artista dentro de um intervalo de datas.
     *
     * @param artistId o ID do artista
     * @param from     início do intervalo (inclusive)
     * @param to       fim do intervalo (inclusive)
     * @return lista de registos {@link ArtistContent} correspondentes
     */
    public List<ArtistContent> getFilmographyByDateRange(String artistId,
                                                         LocalDate from, LocalDate to) {
        List<ArtistContent> result = new ArrayList<>();
        // CORREÇÃO: from.toEpochDay() e to.toEpochDay()
        for (Long d : byDateBST.keys(from.toEpochDay(), to.toEpochDay())) {
            List<ArtistContent> bucket = byDateBST.get(d);
            if (bucket == null) continue;
            for (ArtistContent ac : bucket) {
                if (ac.getArtist().getId().equals(artistId)) result.add(ac);
            }
        }
        return result;
    }

    /**
     * Retorna todas as participações de todos os artistas num intervalo de datas.
     *
     * @param from início do intervalo (inclusive)
     * @param to   fim do intervalo (inclusive)
     * @return lista de todos os registos {@link ArtistContent} no intervalo
     */
    public List<ArtistContent> getAllByDateRange(LocalDate from, LocalDate to) {
        List<ArtistContent> result = new ArrayList<>();
        // CORREÇÃO: from.toEpochDay() e to.toEpochDay()
        for (Long d : byDateBST.keys(from.toEpochDay(), to.toEpochDay())) {
            List<ArtistContent> bucket = byDateBST.get(d);
            if (bucket != null) result.addAll(bucket);
        }
        return result;
    }

    /**
     * Retorna todos os realizadores de um determinado conteúdo.
     *
     * @param contentId o ID do conteúdo
     * @return lista de objetos {@link Artist} com a função DIRECTOR nesse conteúdo
     */
    public List<Artist> getDirectors(String contentId) {
        List<Artist> result = new ArrayList<>();
        List<ArtistContent> list = byContentIndex.get(contentId);
        if (list == null) return result;
        for (ArtistContent ac : list) {
            if (ac.getRole() == ArtistRole.DIRECTOR) result.add(ac.getArtist());
        }
        return result;
    }

    /**
     * Retorna todos os atores de um determinado conteúdo.
     *
     * @param contentId o ID do conteúdo
     * @return lista de objetos {@link Artist} com a função ACTOR nesse conteúdo
     */
    public List<Artist> getActors(String contentId) {
        List<Artist> result = new ArrayList<>();
        List<ArtistContent> list = byContentIndex.get(contentId);
        if (list == null) return result;
        for (ArtistContent ac : list) {
            if (ac.getRole() == ArtistRole.ACTOR) result.add(ac.getArtist());
        }
        return result;
    }

    /**
     * Verifica se um artista tem alguma participação num determinado conteúdo.
     *
     * @param artistId  o ID do artista
     * @param contentId o ID do conteúdo
     * @return {@code true} se existir pelo menos uma participação
     */
    public boolean hasParticipation(String artistId, String contentId) {
        List<ArtistContent> list = byArtistIndex.get(artistId);
        if (list == null) return false;
        for (ArtistContent ac : list) {
            if (ac.getContent().getId().equals(contentId)) return true;
        }
        return false;
    }

    /**
     * Retorna o número total de registos de participação.
     *
     * @return contagem total
     */
    public int size() {
        return participationST.size();
    }

    /**
     * Retorna todos os registos de participação como uma lista simples.
     *
     * @return lista de todos os objetos {@link ArtistContent}
     */
    public List<ArtistContent> listAll() {
        List<ArtistContent> result = new ArrayList<>();
        for (String key : participationST.keys()) result.add(participationST.get(key));
        return result;
    }

    // --- Métodos Auxiliares de Indexação ---
    
    private String compositeKey(String artistId, String contentId, ArtistRole role) {
        return artistId + ":" + contentId + ":" + role.name();
    }

    private void indexByArtist(ArtistContent ac) {
        String key = ac.getArtist().getId();
        List<ArtistContent> list = byArtistIndex.get(key);
        if (list == null) { list = new ArrayList<>(); byArtistIndex.put(key, list); }
        list.add(ac);
    }

    private void indexByContent(ArtistContent ac) {
        String key = ac.getContent().getId();
        List<ArtistContent> list = byContentIndex.get(key);
        if (list == null) { list = new ArrayList<>(); byContentIndex.put(key, list); }
        list.add(ac);
    }

    private void indexByDate(ArtistContent ac) {
        Long dateKey = ac.getDate().toEpochDay();
        List<ArtistContent> bucket = byDateBST.get(dateKey);
        if (bucket == null) { bucket = new ArrayList<>(); byDateBST.put(dateKey, bucket); }
        bucket.add(ac);
    }

    private void removeFromArtistIndex(ArtistContent ac) {
        String key = ac.getArtist().getId();
        List<ArtistContent> list = byArtistIndex.get(key);
        if (list != null) { list.remove(ac); if (list.isEmpty()) byArtistIndex.delete(key); }
    }

    private void removeFromContentIndex(ArtistContent ac) {
        String key = ac.getContent().getId();
        List<ArtistContent> list = byContentIndex.get(key);
        if (list != null) { list.remove(ac); if (list.isEmpty()) byContentIndex.delete(key); }
    }

    private void removeFromDateIndex(ArtistContent ac) {
        Long dateKey = ac.getDate().toEpochDay();
        List<ArtistContent> bucket = byDateBST.get(dateKey);
        if (bucket != null) { bucket.remove(ac); if (bucket.isEmpty()) byDateBST.delete(dateKey); }
    }
}