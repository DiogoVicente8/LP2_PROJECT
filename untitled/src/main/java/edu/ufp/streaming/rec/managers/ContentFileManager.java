package edu.ufp.streaming.rec.managers;

import edu.ufp.streaming.rec.models.Content;
import edu.ufp.streaming.rec.models.Documentary;
import edu.ufp.streaming.rec.models.Genre;
import edu.ufp.streaming.rec.models.Movie;
import edu.ufp.streaming.rec.models.Series;

import java.io.*;
import java.time.LocalDate;

/**
 * Classe responsável pela importação e exportação de dados em ficheiros de texto (formato CSV).
 * Suporta géneros e conteúdos com tratamento de polimorfismo (filmes, séries, documentários).
 *
 * @author Pedro
 */
public class ContentFileManager {

    // -------------------------------------------------------------------------
    // Géneros
    // -------------------------------------------------------------------------

    /**
     * Exporta todos os géneros para um ficheiro de texto.
     */
    public static void exportGenres(GenreManager gm, String filePath) {
        // try-with-resources garante o fecho automático do ficheiro
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {

            for (Genre g : gm.listAll()) {
                pw.println(g.getId() + ";" + g.getName());
            }
            System.out.println("[ContentFileManager] Géneros exportados para " + filePath);

        } catch (IOException e) {
            System.err.println("[ContentFileManager] Erro ao exportar géneros: " + e.getMessage());
        }
    }

    /**
     * Importa géneros de um ficheiro de texto para o gestor.
     * Formato esperado por linha: id;nome
     */
    public static void importGenres(GenreManager gm, String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");

                // Validação de segurança para evitar crash caso a linha esteja mal formatada
                if (parts.length == 2) {
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

    /**
     * Exporta todos os conteúdos para um ficheiro de texto, respeitando o polimorfismo.
     * Formato base: tipo;id;titulo;genreId;data;duracao;regiao;[campos extra];rating
     */
    public static void exportContents(ContentManager cm, String filePath) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {

            for (Content c : cm.listAll()) {
                // Pattern Matching do Java moderno (instanceof cria logo a variável casted)
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
                    pw.println("DOCUMENTARY;" + d.getId() + ";" + d.getTitle() + ";"
                            + d.getGenre().getId() + ";" + d.getReleaseDate() + ";"
                            + d.getDuration() + ";" + d.getRegion() + ";"
                            + d.getTopic() + ";" + d.getNarrator() + ";" + d.getRating());
                }
            }
            System.out.println("[ContentFileManager] Conteúdos exportados para " + filePath);

        } catch (IOException e) {
            System.err.println("[ContentFileManager] Erro ao exportar conteúdos: " + e.getMessage());
        }
    }

    /**
     * Importa conteúdos de um ficheiro de texto para o gestor e recria o polimorfismo.
     * Requer que os géneros já estejam carregados em memória.
     */
    public static void importContents(ContentManager cm, GenreManager gm, String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String line;
            while ((line = br.readLine()) != null) {

                // O limitador -1 previne que o Java ignore colunas vazias no final da linha
                String[] p = line.split(";", -1);
                if (p.length < 8) continue;

                String type     = p[0].trim();
                String id       = p[1].trim();
                String title    = p[2].trim();
                Genre genre     = gm.get(p[3].trim());
                LocalDate date  = LocalDate.parse(p[4].trim());
                int duration    = Integer.parseInt(p[5].trim());
                String region   = p[6].trim();

                // Integridade referencial: Se o género não existir na BD, ignoramos a linha
                if (genre == null) continue;

                // Reconstrução de objetos usando Switch Expressions (Java 14+)
                switch (type) {
                    case "MOVIE" -> {
                        double rating = p.length > 7 && !p[7].isBlank() ? Double.parseDouble(p[7].trim()) : 0.0;
                        Movie mv = new Movie(id, title, genre, date, duration, region, null);
                        mv.setRating(rating);
                        cm.insert(mv);
                    }
                    case "SERIES" -> {
                        int seasons   = p.length > 7 && !p[7].isBlank() ? Integer.parseInt(p[7].trim()) : 1;
                        double rating = p.length > 8 && !p[8].isBlank() ? Double.parseDouble(p[8].trim()) : 0.0;
                        Series s = new Series(id, title, genre, date, duration, region, seasons);
                        s.setRating(rating);
                        cm.insert(s);
                    }
                    case "DOCUMENTARY" -> {
                        String topic    = p.length > 7 ? p[7].trim() : "";
                        String narrator = p.length > 8 ? p[8].trim() : "";
                        double rating   = p.length > 9 && !p[9].isBlank() ? Double.parseDouble(p[9].trim()) : 0.0;
                        Documentary d = new Documentary(id, title, genre, date, duration, region, topic, narrator);
                        d.setRating(rating);
                        cm.insert(d);
                    }
                    default -> System.err.println("[ContentFileManager] Tipo de conteúdo desconhecido: " + type);
                }
            }
            System.out.println("[ContentFileManager] Conteúdos importados de " + filePath);

        } catch (IOException | NumberFormatException e) {
            System.err.println("[ContentFileManager] Erro na leitura/conversão de dados do ficheiro: " + e.getMessage());
        }
    }
}