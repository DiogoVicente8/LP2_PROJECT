package edu.ufp.streaming.rec.managers;

import edu.ufp.streaming.rec.models.*;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Persistência completa do estado da aplicação.
 *
 * Guarda e carrega utilizadores, artistas, géneros, conteúdos,
 * follows e interações num único ficheiro binário (app_state.dat).
 *
 * @author Diogo Vicente
 */
public class AppStateSerializer {

    public static final String FILE = "app_state.dat";

    // ── GUARDAR ──────────────────────────────────────────────────────────

    public static void save(StreamingDatabase db) {
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(FILE)))) {

            // 1. Géneros
            List<Genre> genres = db.getGenreManager().listAll();
            out.writeInt(genres.size());
            for (Genre g : genres) {
                writeStr(out, g.getId());
                writeStr(out, g.getName());
            }

            // 2. Conteúdos (Polimorfismo)
            List<Content> contents = db.getContentManager().listAll();
            out.writeInt(contents.size());

            for (Content c : contents) {
                String type = "M"; // Default para Filme
                if (c instanceof Series) type = "S";
                else if (c instanceof Documentary) type = "D";

                writeStr(out, type);
                writeStr(out, c.getId());
                writeStr(out, c.getTitle());
                writeStr(out, c.getGenre() != null ? c.getGenre().getId() : "g_unknown");
                writeStr(out, c.getReleaseDate() != null ? c.getReleaseDate().toString() : LocalDate.now().toString());
                out.writeInt(c.getDuration());
                writeStr(out, c.getRegion());
                out.writeDouble(c.getRating());

                // Pattern Matching do Java Moderno (Aviso resolvido)
                if (c instanceof Series s) {
                    out.writeInt(s.getSeasons());
                } else if (c instanceof Documentary d) {
                    writeStr(out, d.getTopic());
                } else {
                    writeStr(out, "");
                }
            }

            // 3. Artistas
            List<Artist> artists = db.getArtistManager().listAll();
            out.writeInt(artists.size());
            for (Artist a : artists) {
                writeStr(out, a.getId());
                writeStr(out, a.getName());
                writeStr(out, a.getNationality());
                writeStr(out, a.getGender());
                writeStr(out, a.getBirthDate() != null ? a.getBirthDate().toString() : LocalDate.now().toString());
                writeStr(out, a.getRole() != null ? a.getRole().toString() : "ACTOR");
            }

            // 4. Utilizadores
            List<User> users = db.getUserManager().listAll();
            out.writeInt(users.size());
            for (User u : users) {
                writeStr(out, u.getId());
                writeStr(out, u.getName());
                writeStr(out, u.getEmail());
                writeStr(out, u.getRegion());
                writeStr(out, u.getRegisterDate() != null ? u.getRegisterDate().toString() : LocalDate.now().toString());
                writeStr(out, u.getPasswordHash());
                out.writeBoolean(u.isAdmin());
            }

            // 5. Follows
            List<UserFollow> follows = db.getFollowManager().listAll();
            out.writeInt(follows.size());
            for (UserFollow f : follows) {
                writeStr(out, f.getFollower().getId());
                writeStr(out, f.getFollowed().getId());
                writeStr(out, f.getDate() != null ? f.getDate().toString() : LocalDateTime.now().toString());
            }

            // 6. Interações
            int totalInter = 0;
            for (User u : users) {
                totalInter += u.getInteractions().size();
            }
            out.writeInt(totalInter);

            for (User u : users) {
                for (Interation i : u.getInteractions()) {
                    writeStr(out, u.getId());
                    writeStr(out, i.getContent().getId());
                    writeStr(out, i.getWatchDate() != null ? i.getWatchDate().toString() : LocalDateTime.now().toString());
                    out.writeDouble(i.getRating());
                    out.writeDouble(i.getProgress());
                    writeStr(out, i.getType() != null ? i.getType().toString() : "WATCH");
                    writeStr(out, i.getId());
                }
            }

            System.out.println("[AppStateSerializer] Estado guardado em " + FILE);
        } catch (IOException ex) {
            System.err.println("[AppStateSerializer] Erro ao guardar: " + ex.getMessage());
        }
    }

    // ── CARREGAR ─────────────────────────────────────────────────────────

    public static void load(StreamingDatabase db) {
        Path path = Paths.get(FILE);
        if (!Files.exists(path)) return;

        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(FILE)))) {

            // 1. Géneros
            int gCount = in.readInt();
            for (int i = 0; i < gCount; i++) {
                String id   = readStr(in);
                String name = readStr(in);
                if (db.getGenreManager().get(id) == null) {
                    db.addGenre(new Genre(id, name));
                }
            }

            // 2. Conteúdos
            int cCount = in.readInt();
            for (int i = 0; i < cCount; i++) {
                String type    = readStr(in);
                String id      = readStr(in);
                String title   = readStr(in);
                String genreId = readStr(in);
                LocalDate date = LocalDate.parse(readStr(in));
                int dur        = in.readInt();
                String region  = readStr(in);
                double rating  = in.readDouble();

                Genre g = db.getGenreManager().get(genreId);
                if (g == null) {
                    skipExtra(in, type);
                    continue;
                }

                Content c; // Aviso null redundante resolvido
                if (type.equals("S")) {
                    int seasons = in.readInt();
                    c = new Series(id, title, g, date, dur, region, seasons);
                } else if (type.equals("D")) {
                    String topic = readStr(in);
                    c = new Documentary(id, title, g, date, dur, region, topic);
                } else {
                    readStr(in); // Consumir byte vazio do Filme
                    c = new Movie(id, title, g, date, dur, region, null);
                }

                c.setRating(rating);
                if (db.getContentManager().get(id) == null) {
                    db.addContent(c);
                }
            }

            // 3. Artistas
            int aCount = in.readInt();
            for (int i = 0; i < aCount; i++) {
                String id    = readStr(in);
                String name  = readStr(in);
                String nat   = readStr(in);
                String gen   = readStr(in);
                LocalDate bd = LocalDate.parse(readStr(in));
                String role  = readStr(in);

                if (!db.getArtistManager().contains(id)) {
                    Artist a = new Artist(id, name, nat, gen, bd, edu.ufp.streaming.rec.enums.ArtistRole.valueOf(role));
                    db.addArtist(a);
                }
            }

            // 4. Utilizadores
            int uCount = in.readInt();
            for (int i = 0; i < uCount; i++) {
                String id      = readStr(in);
                String name    = readStr(in);
                String email   = readStr(in);
                String region  = readStr(in);
                LocalDate date = LocalDate.parse(readStr(in));
                String hash    = readStr(in);
                boolean isAdmin = in.readBoolean();

                if (!db.getUserManager().contains(id)) {
                    User u = new User(id, name, email, region, date, null);
                    u.setPasswordHash(hash);
                    u.setAdmin(isAdmin);
                    db.addUser(u);
                }
            }

            // 5. Follows
            int fCount = in.readInt();
            for (int i = 0; i < fCount; i++) {
                String followerId  = readStr(in);
                String followedId  = readStr(in);
                LocalDateTime followDate = LocalDateTime.parse(readStr(in));

                User follower = db.getUserManager().get(followerId);
                User followed = db.getUserManager().get(followedId);

                if (follower != null && followed != null && !db.getFollowManager().isFollowing(followerId, followedId)) {
                    db.addFollowWithDate(followerId, followedId, followDate);
                }
            }

            // 6. Interações
            int iCount = in.readInt();
            for (int i = 0; i < iCount; i++) {
                String userId    = readStr(in);
                String contentId = readStr(in);
                LocalDateTime dt = LocalDateTime.parse(readStr(in));
                double rating    = in.readDouble();
                double progress  = in.readDouble();
                String typeStr   = readStr(in);
                String iId       = readStr(in);

                User u = db.getUserManager().get(userId);
                Content c = db.getContentManager().get(contentId);

                if (u == null || c == null) continue;

                // Evitar carregar interações duplicadas
                boolean jaExiste = u.getInteractions().stream().anyMatch(it -> iId.equals(it.getId()));

                if (!jaExiste) {
                    edu.ufp.streaming.rec.enums.InterationType type = edu.ufp.streaming.rec.enums.InterationType.valueOf(typeStr);
                    Interation inter = new Interation(u, c, dt, rating, progress, type, iId);
                    db.addInteraction(inter);
                }
            }

            System.out.println("[AppStateSerializer] Estado carregado com sucesso!");

        } catch (EOFException eof) {
            System.err.println("[AppStateSerializer] O ficheiro app_state.dat esta corrompido ou incompleto. Apague-o e reinicie a aplicacao.");
        } catch (Exception ex) {
            System.err.println("[AppStateSerializer] Erro ao carregar: " + ex.getMessage());
        }
    }

    // ── Helpers ─────────────────────────

    private static void writeStr(DataOutputStream out, String s) throws IOException {
        String safeString = (s != null) ? s : "";
        byte[] bytes = safeString.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    private static String readStr(DataInputStream in) throws IOException {
        int len = in.readInt();
        if (len < 0 || len > 100_000)
            throw new IOException("Comprimento de string inválido: " + len + " — ficheiro possivelmente corrompido");
        byte[] bytes = new byte[len];
        in.readFully(bytes);
        return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
    }

    private static void skipExtra(DataInputStream in, String type) throws IOException {
        if ("S".equals(type)) in.readInt();
        else if ("D".equals(type)) { readStr(in); readStr(in); }
        else readStr(in);
    }
}