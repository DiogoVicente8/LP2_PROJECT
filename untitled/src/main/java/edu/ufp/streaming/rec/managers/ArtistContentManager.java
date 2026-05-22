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
import java.util.stream.Collectors;

/**
 * Gere todas as relações de participação {@link ArtistContent} entre
 * as entidades {@link Artist} (Artista) e {@link Content} (Conteúdo) na plataforma.
 * * @author Diogo Vicente
 */
public class ArtistContentManager {

    private final ST<String, ArtistContent> participationST;
    private final ST<String, List<ArtistContent>> byArtistIndex;
    private final ST<String, List<ArtistContent>> byContentIndex;
    private final RedBlackBST<Long, List<ArtistContent>> byDateBST;

    public ArtistContentManager() {
        this.participationST = new ST<>();
        this.byArtistIndex   = new ST<>();
        this.byContentIndex  = new ST<>();
        this.byDateBST       = new RedBlackBST<>();
    }

    // -------------------------------------------------------------------------
    // Inserção e Remoção
    // -------------------------------------------------------------------------

    public ArtistContent addParticipation(Artist artist, Content content, ArtistRole role, LocalDate date) {
        if (artist == null || content == null || role == null || date == null) return null;

        String key = compositeKey(artist.getId(), content.getId(), role);
        if (participationST.contains(key)) return null;

        ArtistContent ac = new ArtistContent(artist, content, role, date);
        participationST.put(key, ac);
        indexByArtist(ac);
        indexByContent(ac);
        indexByDate(ac);

        artist.addParticipation(ac);
        return ac;
    }

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

    public void removeAllByArtist(String artistId) {
        List<ArtistContent> list = byArtistIndex.get(artistId);
        if (list == null) return;

        // Usa-se 'new ArrayList<>(list)' para fazer uma cópia segura e evitar
        for (ArtistContent ac : new ArrayList<>(list)) {
            String key = compositeKey(ac.getArtist().getId(), ac.getContent().getId(), ac.getRole());
            participationST.delete(key);
            removeFromContentIndex(ac);
            removeFromDateIndex(ac);
        }
        byArtistIndex.delete(artistId);
    }

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

    // -------------------------------------------------------------------------
    // Consultas Rápidas via Índices
    // -------------------------------------------------------------------------

    public List<ArtistContent> getFilmography(String artistId) {
        List<ArtistContent> list = byArtistIndex.get(artistId);
        return list != null ? new ArrayList<>(list) : new ArrayList<>();
    }

    public List<ArtistContent> getCastAndCrew(String contentId) {
        List<ArtistContent> list = byContentIndex.get(contentId);
        return list != null ? new ArrayList<>(list) : new ArrayList<>();
    }

    /**
     * Retorna todas as participações de um artista filtradas por função (ex: só como Realizador).
     */
    public List<ArtistContent> getFilmographyByRole(String artistId, ArtistRole role) {
        List<ArtistContent> list = byArtistIndex.get(artistId);
        if (list == null) return new ArrayList<>();

        // Filtra a filmografia instantaneamente mantendo apenas a função desejada.
        return list.stream()
                .filter(ac -> ac.getRole() == role)
                .collect(Collectors.toList());
    }

    /**
     * Retorna todas as participações de um artista dentro de um intervalo de datas.
     */
    public List<ArtistContent> getFilmographyByDateRange(String artistId, LocalDate from, LocalDate to) {
        List<ArtistContent> result = new ArrayList<>();

        // Iteramos apenas as datas da Árvore Binária que pertencem a este intervalo
        for (Long dataNaArvore : byDateBST.keys(from.toEpochDay(), to.toEpochDay())) {
            List<ArtistContent> participacoesDestaData = byDateBST.get(dataNaArvore);

            // SE há participações nesta data, filtra pelo artista
            if (participacoesDestaData != null) {
                for (ArtistContent ac : participacoesDestaData) {
                    if (ac.getArtist().getId().equals(artistId)) {
                        result.add(ac);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Retorna todas as participações de todos os artistas num intervalo de datas.
     */
    public List<ArtistContent> getAllByDateRange(LocalDate from, LocalDate to) {
        List<ArtistContent> result = new ArrayList<>();

        for (Long dataNaArvore : byDateBST.keys(from.toEpochDay(), to.toEpochDay())) {
            List<ArtistContent> participacoesDestaData = byDateBST.get(dataNaArvore);

            if (participacoesDestaData != null) {
                result.addAll(participacoesDestaData);
            }
        }
        return result;
    }

    /**
     * Retorna todos os realizadores de um determinado conteúdo.
     */
    public List<Artist> getDirectors(String contentId) {
        List<ArtistContent> list = byContentIndex.get(contentId);
        if (list == null) return new ArrayList<>();

        List<Artist> result = new ArrayList<>();
        for (ArtistContent ac : list) {
            // Se a função for Realizador (DIRECTOR), guarda a pessoa (Artist)
            if (ac.getRole() == ArtistRole.DIRECTOR) {
                result.add(ac.getArtist());
            }
        }
        return result;
    }

    /**
     * Retorna todos os atores de um determinado conteúdo.
     */
    public List<Artist> getActors(String contentId) {
        List<ArtistContent> list = byContentIndex.get(contentId);
        if (list == null) return new ArrayList<>();

        List<Artist> result = new ArrayList<>();
        for (ArtistContent ac : list) {
            // Se a função for Ator (ACTOR), guarda a pessoa (Artist)
            if (ac.getRole() == ArtistRole.ACTOR) {
                result.add(ac.getArtist());
            }
        }
        return result;
    }

    /**
     * Verifica se um artista tem alguma participação num determinado conteúdo.
     */
    public boolean hasParticipation(String artistId, String contentId) {
        List<ArtistContent> list = byArtistIndex.get(artistId);
        if (list == null) return false;

        // "Alguma destas participações pertence ao ID de conteúdo procurado?"
        return list.stream()
                .anyMatch(ac -> ac.getContent().getId().equals(contentId));
    }

    public int size() {
        return participationST.size();
    }

    public List<ArtistContent> listAll() {
        List<ArtistContent> result = new ArrayList<>();
        for (String key : participationST.keys()) {
            result.add(participationST.get(key));
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Métodos Auxiliares de Indexação
    // -------------------------------------------------------------------------

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