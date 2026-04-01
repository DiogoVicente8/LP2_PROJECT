package edu.ufp.streaming.rec.managers;

import edu.ufp.streaming.rec.models.Content;
import edu.ufp.streaming.rec.models.Documentary;
import edu.ufp.streaming.rec.models.Genre;
import edu.ufp.streaming.rec.models.Movie;
import edu.ufp.streaming.rec.models.Series;

import java.io.*;
import java.time.LocalDate;

/**
 * Classe responsável pela importação e exportação de dados em ficheiros de texto.
 * Suporta géneros e conteúdos (filmes, séries, documentários).
 *
 * @author Pedro
 */
public class ContentFileManager {

    /**
     * Exporta todos os géneros para um ficheiro de texto.
     *
     * @param gm       gestor de géneros
     * @param filePath caminho do ficheiro de destino
     */
    public static void exportGenres(GenreManager gm, String filePath) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            for (Genre g : gm.listAll()) {
                pw.println(g.getId() + ";" + g.getName());
            }
            System.out.println("Géneros exportados para " + filePath);
        } catch (IOException e) {
            System.err.println("Erro ao exportar géneros: " + e.getMessage());
        }
    }

    /**
     * Importa géneros de um ficheiro de texto para o gestor.
     * Formato esperado por linha: {@code id;nome}
     *
     * @param gm       gestor de géneros
     * @param filePath caminho do ficheiro de origem
     */
    public static void importGenres(GenreManager gm, String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 2) {
                    gm.insert(new Genre(parts[0].trim(), parts[1].trim()));
                }
            }
            System.out.println("Géneros importados de " + filePath);
        } catch (IOException e) {
            System.err.println("Erro ao importar géneros: " + e.getMessage());
        }
    }

    /**
     * Exporta todos os conteúdos para um ficheiro de texto.
     * Formato: {@code tipo;id;titulo;genreId;data;duracao;regiao;[campos extra]}
     *
     * @param cm       gestor de conteúdos
     * @param filePath caminho do ficheiro de destino
     */
    public static void exportContents(ContentManager cm, String filePath) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            for (Content c : cm.listAll()) {
                if (c instanceof Movie) {
                    pw.println("MOVIE;" + c.getId() + ";" + c.getTitle() + ";"
                            + c.getGenre().getId() + ";" + c.getReleaseDate() + ";"
                            + c.getDuration() + ";" + c.getRegion());
                } else if (c instanceof Series s) {
                    pw.println("SERIES;" + c.getId() + ";" + c.getTitle() + ";"
                            + c.getGenre().getId() + ";" + c.getReleaseDate() + ";"
                            + c.getDuration() + ";" + c.getRegion() + ";" + s.getSeasons());
                } else if (c instanceof Documentary d) {
                    pw.println("DOCUMENTARY;" + c.getId() + ";" + c.getTitle() + ";"
                            + c.getGenre().getId() + ";" + c.getReleaseDate() + ";"
                            + c.getDuration() + ";" + c.getRegion() + ";"
                            + d.getTopic() + ";" + d.getNarrator());
                }
            }
            System.out.println("Conteúdos exportados para " + filePath);
        } catch (IOException e) {
            System.err.println("Erro ao exportar conteúdos: " + e.getMessage());
        }
    }

    /**
     * Importa conteúdos de um ficheiro de texto para o gestor.
     * Os géneros devem já estar carregados no GenreManager.
     *
     * @param cm       gestor de conteúdos
     * @param gm       gestor de géneros (para resolver o género pelo ID)
     * @param filePath caminho do ficheiro de origem
     */
    public static void importContents(ContentManager cm, GenreManager gm, String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(";");
                if (p.length < 7) continue;
                String type = p[0].trim();
                String id = p[1].trim();
                String title = p[2].trim();
                Genre genre = gm.get(p[3].trim());
                LocalDate date = LocalDate.parse(p[4].trim());
                int duration = Integer.parseInt(p[5].trim());
                String region = p[6].trim();

                if (genre == null) continue;

                switch (type) {
                    case "MOVIE" ->
                            cm.insert(new Movie(id, title, genre, date, duration, region, null));
                    case "SERIES" -> {
                        int seasons = p.length > 7 ? Integer.parseInt(p[7].trim()) : 1;
                        cm.insert(new Series(id, title, genre, date, duration, region, seasons));
                    }
                    case "DOCUMENTARY" -> {
                        String topic = p.length > 7 ? p[7].trim() : "";
                        String narrator = p.length > 8 ? p[8].trim() : "";
                        cm.insert(new Documentary(id, title, genre, date, duration, region, topic, narrator));
                    }
                    default -> System.err.println("Tipo desconhecido: " + type);
                }
            }
            System.out.println("Conteúdos importados de " + filePath);
        } catch (IOException e) {
            System.err.println("Erro ao importar conteúdos: " + e.getMessage());
        }
    }
}