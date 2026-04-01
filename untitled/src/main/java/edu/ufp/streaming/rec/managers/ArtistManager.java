package edu.ufp.streaming.rec.managers;

import edu.princeton.cs.algs4.ST;
import edu.princeton.cs.algs4.RedBlackBST;
import edu.ufp.streaming.rec.models.Artist;
import edu.ufp.streaming.rec.enums.ArtistRole;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Gerenciador de Artistas: organiza artistas por ID, Nome e Data de Nascimento.
 * @author  Diogo Vicente
 */
public class ArtistManager {

    /** Tabela de Símbolos Primária: artistId → Artist (busca média O(1)). */
    private final ST<String, Artist> artistST;

    /** BST Ordenada: data de nascimento como dia da época (Long) → lista de Artistas nascidos nessa data. */
    private final RedBlackBST<Long, List<Artist>> byBirthDateBST;

    /** BST Ordenada: nome em minúsculas → lista de Artistas com esse nome. */
    private final RedBlackBST<String, List<Artist>> byNameBST;

    /**
     * Constrói um ArtistManager vazio.
     */
    public ArtistManager() {
        this.artistST       = new ST<>();
        this.byBirthDateBST = new RedBlackBST<>();
        this.byNameBST      = new RedBlackBST<>();
    }

    /**
     * Insere um novo artista em todas as estruturas.
     *
     * @param artist o {@link Artist} a inserir; não deve ser {@code null}
     * @return {@code true} se inserido; {@code false} se for {@code null} ou se o ID já existir
     */
    public boolean insert(Artist artist) {
        if (artist == null || artistST.contains(artist.getId())) return false;

        artistST.put(artist.getId(), artist);
        indexByBirthDate(artist);
        indexByName(artist);
        return true;
    }

    /**
     * Remove um artista de todas as estruturas pelo ID.
     *
     * @param id o ID do artista a remover
     * @return o {@link Artist} removido, ou {@code null} se não for encontrado
     */
    public Artist remove(String id) {
        if (!artistST.contains(id)) return null;

        Artist a = artistST.get(id);
        artistST.delete(id);
        removeFromBirthDateIndex(a);
        removeFromNameIndex(a);
        return a;
    }

    /**
     * Edita o nome de um artista existente e re-indexa a BST de nomes.
     *
     * @param id      o ID do artista
     * @param newName o novo nome
     * @return {@code true} se for bem-sucedido; {@code false} se o artista não for encontrado
     */
    public boolean editName(String id, String newName) {
        Artist a = artistST.get(id);
        if (a == null) return false;

        removeFromNameIndex(a);
        a.setName(newName);
        indexByName(a);
        return true;
    }

    /**
     * Edita a nacionalidade de um artista existente.
     *
     * @param id             o ID do artista
     * @param newNationality a nova nacionalidade
     * @return {@code true} se for bem-sucedido; {@code false} se o artista não for encontrado
     */
    public boolean editNationality(String id, String newNationality) {
        Artist a = artistST.get(id);
        if (a == null) return false;
        a.setNationality(newNationality);
        return true;
    }

    /**
     * Retorna o artista com o ID fornecido.
     *
     * @param id o ID do artista
     * @return o {@link Artist}, ou {@code null} se não for encontrado
     */
    public Artist get(String id) {
        return artistST.get(id);
    }

    /**
     * Retorna {@code true} se um artista com o ID fornecido existir.
     *
     * @param id o ID do artista
     * @return {@code true} se presente
     */
    public boolean contains(String id) {
        return artistST.contains(id);
    }

    /**
     * Retorna o número total de artistas.
     *
     * @return número de artistas
     */
    public int size() {
        return artistST.size();
    }

    /**
     * Retorna todos os artistas como uma lista não ordenada.
     *
     * @return lista de todos os objetos {@link Artist}
     */
    public List<Artist> listAll() {
        List<Artist> result = new ArrayList<>();
        for (String key : artistST.keys()) result.add(artistST.get(key));
        return result;
    }

    /**
     * Retorna todos os artistas nascidos em uma data exata.
     *
     * @param date a data de nascimento para pesquisa
     * @return lista de artistas correspondentes (pode estar vazia)
     */
    public List<Artist> searchByBirthDate(LocalDate date) {
        List<Artist> list = byBirthDateBST.get(date.toEpochDay());
        return list != null ? new ArrayList<>(list) : new ArrayList<>();
    }

    /**
     * Retorna todos os artistas nascidos dentro de um intervalo de datas [de, até].
     * Útil para consultas de "idade entre X e Y".
     *
     * @param from início do intervalo (inclusive)
     * @param to   fim do intervalo (inclusive)
     * @return lista de artistas cuja data de nascimento cai no intervalo
     */
    public List<Artist> searchByBirthDateRange(LocalDate from, LocalDate to) {
        List<Artist> result = new ArrayList<>();
        for (Long d : byBirthDateBST.keys(from.toEpochDay(), to.toEpochDay())) {
            List<Artist> bucket = byBirthDateBST.get(d);
            if (bucket != null) result.addAll(bucket);
        }
        return result;
    }

    /**
     * Retorna todos os artistas cujo nome contém a substring fornecida (insensível a maiúsculas).
     *
     * @param substring a substring a procurar
     * @return lista de artistas correspondentes
     */
    public List<Artist> searchByNameSubstring(String substring) {
        String lower = substring.toLowerCase();
        List<Artist> result = new ArrayList<>();
        for (String key : byNameBST.keys()) {
            if (key.contains(lower)) result.addAll(byNameBST.get(key));
        }
        return result;
    }

    /**
     * Retorna todos os artistas de uma determinada nacionalidade (insensível a maiúsculas).
     *
     * @param nationality a nacionalidade pela qual filtrar
     * @return lista de artistas correspondentes
     */
    public List<Artist> searchByNationality(String nationality) {
        List<Artist> result = new ArrayList<>();
        for (String key : artistST.keys()) {
            Artist a = artistST.get(key);
            if (a.getNationality().equalsIgnoreCase(nationality)) result.add(a);
        }
        return result;
    }

    /**
     * Retorna todos os artistas de um determinado género (ex: "M", "F").
     *
     * @param gender a string do género pela qual filtrar
     * @return lista de artistas correspondentes
     */
    public List<Artist> searchByGender(String gender) {
        List<Artist> result = new ArrayList<>();
        for (String key : artistST.keys()) {
            Artist a = artistST.get(key);
            if (a.getGender().equalsIgnoreCase(gender)) result.add(a);
        }
        return result;
    }

    /**
     * Retorna todos os artistas de uma nacionalidade nascidos num intervalo de datas.
     *
     * @param nationality a nacionalidade pela qual filtrar
     * @param from        início do intervalo (inclusive)
     * @param to          fim do intervalo (inclusive)
     * @return lista de artistas correspondentes
     */
    public List<Artist> searchByNationalityAndBirthDateRange(String nationality,
                                                             LocalDate from,
                                                             LocalDate to) {
        List<Artist> result = new ArrayList<>();
        for (Long d : byBirthDateBST.keys(from.toEpochDay(), to.toEpochDay())) {
            List<Artist> bucket = byBirthDateBST.get(d);
            if (bucket == null) continue;
            for (Artist a : bucket) {
                if (a.getNationality().equalsIgnoreCase(nationality)) result.add(a);
            }
        }
        return result;
    }

    /**
     * Retorna artistas que coincidam com substring de nome, nacionalidade e género.
     *
     * @param substring   substring para procurar no nome
     * @param nationality nacionalidade para filtrar
     * @param gender      género para filtrar
     * @return lista de artistas correspondentes
     */
    public List<Artist> searchByNameSubstringNationalityAndGender(String substring,
                                                                  String nationality,
                                                                  String gender) {
        String lower = substring.toLowerCase();
        List<Artist> result = new ArrayList<>();
        for (String key : byNameBST.keys()) {
            if (!key.contains(lower)) continue;
            for (Artist a : byNameBST.get(key)) {
                if (a.getNationality().equalsIgnoreCase(nationality)
                        && a.getGender().equalsIgnoreCase(gender)) {
                    result.add(a);
                }
            }
        }
        return result;
    }

    /**
     * Retorna todos os artistas com uma função específica (ex: ACTOR, DIRECTOR).
     *
     * @param role o {@link ArtistRole} pelo qual filtrar
     * @return lista de artistas correspondentes
     */
    public List<Artist> searchByRole(ArtistRole role) {
        List<Artist> result = new ArrayList<>();
        for (String key : artistST.keys()) {
            Artist a = artistST.get(key);
            if (a.getRole() == role) result.add(a);
        }
        return result;
    }

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