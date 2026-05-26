package edu.ufp.streaming.rec.managers;

import edu.princeton.cs.algs4.ST;
import edu.princeton.cs.algs4.RedBlackBST;
import edu.ufp.streaming.rec.models.Artist;
import edu.ufp.streaming.rec.enums.ArtistRole;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gerenciador de Artistas: organiza artistas por 'ID', Nome e Data de Nascimento.
 * @author Diogo Vicente
 */
public class ArtistManager {

    /** Tabela de Símbolos Primária: artistId → Artist (busca média O(1)). */
    private final ST<String, Artist> artistST;

    /** BST Ordenada: data de nascimento como dia da época (Long) → lista de Artistas nascidos nessa data. */
    private final RedBlackBST<Long, List<Artist>> byBirthDateBST;

    /** BST Ordenada: nome em minúsculas → lista de Artistas com esse nome. */
    private final RedBlackBST<String, List<Artist>> byNameBST;

    public ArtistManager() {
        this.artistST       = new ST<>();
        this.byBirthDateBST = new RedBlackBST<>();
        this.byNameBST      = new RedBlackBST<>();
    }

    // -------------------------------------------------------------------------
    // CRUD e Edições
    // -------------------------------------------------------------------------

    public boolean insert(Artist artist) {
        if (artist == null || artistST.contains(artist.getId())) return false;

        artistST.put(artist.getId(), artist);
        indexByBirthDate(artist);
        indexByName(artist);
        return true;
    }

    public Artist remove(String id) {
        if (!artistST.contains(id)) return null;

        Artist a = artistST.get(id);
        artistST.delete(id);
        removeFromBirthDateIndex(a);
        removeFromNameIndex(a);
        return a;
    }

    public boolean editName(String id, String newName) {
        Artist a = artistST.get(id);
        if (a == null) return false;

        removeFromNameIndex(a);
        a.setName(newName);
        indexByName(a);
        return true;
    }

    public boolean editNationality(String id, String newNationality) {
        Artist a = artistST.get(id);
        if (a == null) return false;
        a.setNationality(newNationality);
        return true;
    }

    // -------------------------------------------------------------------------
    // Consultas Básicas
    // -------------------------------------------------------------------------

    public Artist get(String id) {
        return artistST.get(id);
    }

    public boolean contains(String id) {
        return artistST.contains(id);
    }

    public int size() {
        return artistST.size();
    }

    public List<Artist> listAll() {
        List<Artist> result = new ArrayList<>();
        for (String artistId : artistST.keys()) {
            result.add(artistST.get(artistId));
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Consultas Avançadas
    // -------------------------------------------------------------------------

    public List<Artist> searchByBirthDateRange(LocalDate from, LocalDate to) {
        List<Artist> result = new ArrayList<>();

        //Iterar apenas a secção da árvore correspondente ao intervalo de datas
        for (Long dataNaArvore : byBirthDateBST.keys(from.toEpochDay(), to.toEpochDay())) {
            List<Artist> artistasDestaData = byBirthDateBST.get(dataNaArvore);
            if (artistasDestaData != null) {
                result.addAll(artistasDestaData);
            }
        }
        return result;
    }

    public List<Artist> searchByNameSubstring(String substring) {
        String lower = substring.toLowerCase();
        List<Artist> result = new ArrayList<>();

        for (String nomeNaArvore : byNameBST.keys()) {
            // Se o nome contiver a pesquisa, adiciona à lista
            if (nomeNaArvore.contains(lower)) {
                result.addAll(byNameBST.get(nomeNaArvore));
            }
        }
        return result;
    }

    public List<Artist> searchByNationality(String nationality) {
        // Filtra a lista completa de forma declarativa e concisa.
        return listAll().stream()
                .filter(a -> nationality.equalsIgnoreCase(a.getNationality()))
                .collect(Collectors.toList());
    }

    public List<Artist> searchByGender(String gender) {
        return listAll().stream()
                .filter(a -> gender.equalsIgnoreCase(a.getGender()))
                .collect(Collectors.toList());
    }

    public List<Artist> searchByRole(ArtistRole role) {
        return listAll().stream()
                .filter(a -> a.getRole() == role)
                .collect(Collectors.toList());
    }

    public List<Artist> searchByNationalityAndBirthDateRange(String nationality, LocalDate from, LocalDate to) {
        List<Artist> result = new ArrayList<>();

        for (Long dataNaArvore : byBirthDateBST.keys(from.toEpochDay(), to.toEpochDay())) {
            List<Artist> artistasDestaData = byBirthDateBST.get(dataNaArvore);

            if (artistasDestaData != null) {
                for (Artist a : artistasDestaData) {
                    if (nationality.equalsIgnoreCase(a.getNationality())) {
                        result.add(a);
                    }
                }
            }
        }
        return result;
    }

    public List<Artist> searchByNameSubstringNationalityAndGender(String substring, String nationality, String gender) {
        String lower = substring.toLowerCase();
        List<Artist> result = new ArrayList<>();

        for (String nomeNaArvore : byNameBST.keys()) {
            if (nomeNaArvore.contains(lower)) {
                for (Artist a : byNameBST.get(nomeNaArvore)) {
                    if (nationality.equalsIgnoreCase(a.getNationality()) && gender.equalsIgnoreCase(a.getGender())) {
                        result.add(a);
                    }
                }
            }
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Métodos Internos de Indexação
    // -------------------------------------------------------------------------

    private void indexByBirthDate(Artist artist) {
        Long date = artist.getBirthDate().toEpochDay();
        List<Artist> bucket = byBirthDateBST.get(date);
        if (bucket == null) {
            bucket = new ArrayList<>();
            byBirthDateBST.put(date, bucket);
        }
        bucket.add(artist);
    }

    private void removeFromBirthDateIndex(Artist artist) {
        Long date = artist.getBirthDate().toEpochDay();
        List<Artist> bucket = byBirthDateBST.get(date);
        if (bucket != null) {
            bucket.remove(artist);
            if (bucket.isEmpty()) byBirthDateBST.delete(date);
        }
    }

    private void indexByName(Artist artist) {
        String key = artist.getName().toLowerCase();
        List<Artist> bucket = byNameBST.get(key);
        if (bucket == null) {
            bucket = new ArrayList<>();
            byNameBST.put(key, bucket);
        }
        bucket.add(artist);
    }

    private void removeFromNameIndex(Artist artist) {
        String key = artist.getName().toLowerCase();
        List<Artist> bucket = byNameBST.get(key);
        if (bucket != null) {
            bucket.remove(artist);
            if (bucket.isEmpty()) byNameBST.delete(key);
        }
    }
}