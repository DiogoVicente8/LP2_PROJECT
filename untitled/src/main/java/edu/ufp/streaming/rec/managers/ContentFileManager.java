package edu.ufp.streaming.rec.managers;

import edu.ufp.streaming.rec.models.Content;
import edu.ufp.streaming.rec.models.Documentary;
import edu.ufp.streaming.rec.models.Genre;
import edu.ufp.streaming.rec.models.Movie;
import edu.ufp.streaming.rec.models.Series;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Classe responsável pela importação e exportação de dados em ficheiros de texto (formato CSV).
 * Suporta géneros e conteúdos com tratamento de polimorfismo (filmes, séries, documentários).
 * Parsing Seguro para evitar NumberFormatException.
 *
 * @author Pedro
 */
public class ContentFileManager {

    // -------------------------------------------------------------------------
    // Géneros
    // -------------------------------------------------------------------------

    public static void exportGenres(GenreManager gm, String filePath) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            for (Genre g : gm.listAll()) {
                pw.println(g.getId() + ";" + g.getName());
            }
            System.out.println("[ContentFileManager] Géneros exportados para " + filePath);
        } catch (IOException e) {
            System.err.println("[ContentFileManager] Erro ao exportar géneros: " + e.getMessage());
        }
    }

    public static void importGenres(GenreManager gm, String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length >= 2) {
                    gm.insert(new Genre(parts[0].trim(), parts[1].trim()));
                }
            }
            System.out.println("[ContentFileManager] Géneros importados de " + filePath);
        } catch (IOException e) {
            System.err.println("[ContentFileManager] Erro ao importar géneros: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Conteúdos
    // -------------------------------------------------------------------------

    public static void exportContents(ContentManager cm, String filePath) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {

            for (Content c : cm.listAll()) {
                if (c instanceof Movie m) {
                    pw.println("MOVIE;" + m.getId() + ";" + m.getTitle() + ";"
                            + m.getGenre().getId() + ";" + m.getReleaseDate() + ";"
                            + m.getDuration() + ";" + m.getRegion() + ";" + m.getRating());

                } else if (c instanceof Series s) {
                    pw.println("SERIES;" + s.getId() + ";" + s.getTitle() + ";"
                            + s.getGenre().getId() + ";" + s.getReleaseDate() + ";"
                            + s.getDuration() + ";" + s.getRegion() + ";"
                            + s.getSeasons() + ";" + s.getRating());

                } else if (c instanceof Documentary d) {
                    // CORREÇÃO: Removido o ";" + ";" extra que corrompia o parsing do Documentário
                    pw.println("DOCUMENTARY;" + d.getId() + ";" + d.getTitle() + ";"
                            + d.getGenre().getId() + ";" + d.getReleaseDate() + ";"
                            + d.getDuration() + ";" + d.getRegion() + ";"
                            + d.getTopic() + ";" + d.getRating());
                }
            }
            System.out.println("[ContentFileManager] Conteúdos exportados para " + filePath);

        } catch (IOException e) {
            System.err.println("[ContentFileManager] Erro ao exportar conteúdos: " + e.getMessage());
        }
    }

    public static void importContents(ContentManager cm, GenreManager gm, String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String line;
            while ((line = br.readLine()) != null) {

                String[] p = line.split(";", -1);

                // CORREÇÃO: O número mínimo de colunas é 7 (Tipo até Região)
                if (p.length < 7) continue;

                String type   = p[0].trim();
                String id     = p[1].trim();
                String title  = p[2].trim();
                Genre genre   = gm.get(p[3].trim());
                String region = p[6].trim();

                if (genre == null || id.isEmpty()) continue;

                LocalDate date;
                try {
                    date = LocalDate.parse(p[4].trim());
                } catch (DateTimeParseException e) {
                    continue;
                }

                int duration = parseIntSafe(p[5]);

                switch (type) {
                    case "MOVIE" -> {
                        double rating = p.length > 7 ? parseDoubleSafe(p[7]) : 0.0;
                        Movie mv = new Movie(id, title, genre, date, duration, region, null);
                        mv.setRating(rating);
                        cm.insert(mv);
                    }
                    case "SERIES" -> {
                        int seasons   = p.length > 7 ? parseIntSafe(p[7]) : 1;
                        double rating = p.length > 8 ? parseDoubleSafe(p[8]) : 0.0;
                        Series s = new Series(id, title, genre, date, duration, region, seasons);
                        s.setRating(rating);
                        cm.insert(s);
                    }
                    case "DOCUMENTARY" -> {
                        String topic  = p.length > 7 ? p[7].trim() : "";
                        // Em Documentary, o 'rating' é sempre a última coluna garantidamente
                        double rating = parseDoubleSafe(p[p.length - 1]);

                        Documentary d = new Documentary(id, title, genre, date, duration, region, topic);
                        d.setRating(rating);
                        cm.insert(d);
                    }
                    default -> System.err.println("[ContentFileManager] Tipo desconhecido: " + type);
                }
            }
            System.out.println("[ContentFileManager] Conteúdos importados de " + filePath);

        } catch (IOException e) {
            System.err.println("[ContentFileManager] Erro na leitura: " + e.getMessage());
        }
    }

    // =========================================================================
    // Helpers de Parsing Seguro (Evita NumberFormatException e Crashes)
    // =========================================================================

    private static int parseIntSafe(String s) {
        if (s == null || s.trim().isEmpty()) return 1;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private static double parseDoubleSafe(String s) {
        if (s == null || s.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}