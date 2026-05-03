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
 * Formato do ficheiro (sequencial):
 *   [GÉNEROS] int count → (id, name)*
 *   [CONTEÚDOS] int count → (type, id, title, genreId, date, dur, region, extra)*
 *   [ARTISTAS] int count → (id, name, nat, gender, date, role)*
 *   [UTILIZADORES] int count → (id, name, email, region, date, pwdHash)*
 *   [FOLLOWS] int count → (followerId, followedId, date)*
 *   [INTERAÇÕES] int count → (userId, contentId, date, rating, progress, type, id)*
 *
 * Uso:
 *   AppStateSerializer.save(db);          // gravar
 *   AppStateSerializer.load(db);          // carregar (sem duplicar dados existentes)
 */
public class AppStateSerializer {

    public static final String FILE = "app_state.dat";

    // ── GUARDAR ──────────────────────────────────────────────────────────

    public static void save(StreamingDatabase db) {
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(FILE)))) {

            // Géneros
            List<Genre> genres = db.genres().listAll();
            out.writeInt(genres.size());
            for (Genre g : genres) {
                writeStr(out, g.getId());
                writeStr(out, g.getName());
            }

            // Conteúdos
            List<Content> contents = db.contents().listAll();
            out.writeInt(contents.size());
            for (Content c : contents) {
                String type = c instanceof Movie ? "M" : c instanceof Series ? "S" : "D";
                writeStr(out, type);
                writeStr(out, c.getId());
                writeStr(out, c.getTitle());
                writeStr(out, c.getGenre().getId());
                writeStr(out, c.getReleaseDate().toString());
                out.writeInt(c.getDuration());
                writeStr(out, c.getRegion() != null ? c.getRegion() : "");
                out.writeDouble(c.getRating());
                // campo extra por tipo
                if (c instanceof Series s) out.writeInt(s.getSeasons());
                else if (c instanceof Documentary d) {
                    writeStr(out, d.getTopic() != null ? d.getTopic() : "");
                    writeStr(out, d.getNarrator() != null ? d.getNarrator() : "");
                } else {
                    writeStr(out, ""); // Movie sem extra relevante
                }
            }

            // Artistas
            List<Artist> artists = db.artists().listAll();
            out.writeInt(artists.size());
            for (Artist a : artists) {
                writeStr(out, a.getId());
                writeStr(out, a.getName());
                writeStr(out, a.getNationality() != null ? a.getNationality() : "");
                writeStr(out, a.getGender() != null ? a.getGender() : "");
                writeStr(out, a.getBirthDate().toString());
                writeStr(out, a.getRole().toString());
            }

            // Utilizadores
            List<User> users = db.users().listAll();
            out.writeInt(users.size());
            for (User u : users) {
                writeStr(out, u.getId());
                writeStr(out, u.getName());
                writeStr(out, u.getEmail() != null ? u.getEmail() : "");
                writeStr(out, u.getRegion() != null ? u.getRegion() : "");
                writeStr(out, u.getRegisterDate().toString());
                writeStr(out, u.getPasswordHash() != null ? u.getPasswordHash() : "");
            }

            // Follows (recolhidos iterando pelos utilizadores)
            List<UserFollow> follows = new java.util.ArrayList<>();
            for (User u : users) {
                for (User followed : db.follows().getFollowing(u.getId())) {
                    // Reconstituir um UserFollow temporário para obter a data
                    follows.add(new UserFollow(u, followed));
                }
            }
            out.writeInt(follows.size());
            for (UserFollow f : follows) {
                writeStr(out, f.getFollower().getId());
                writeStr(out, f.getFollowed().getId());
                writeStr(out, f.getDate().toString());
            }

            // Interações
            int totalInter = 0;
            for (User u : users) totalInter += u.getInteractions().size();
            out.writeInt(totalInter);
            for (User u : users) {
                for (Interation i : u.getInteractions()) {
                    writeStr(out, u.getId());
                    writeStr(out, i.getContent().getId());
                    writeStr(out, i.getWatchDate().toString());
                    out.writeDouble(i.getRating());
                    out.writeDouble(i.getProgress());
                    writeStr(out, i.getType().toString());
                    writeStr(out, i.getId() != null ? i.getId() : "");
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

        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(new FileInputStream(FILE)))) {

            // Géneros
            int gCount = in.readInt();
            for (int i = 0; i < gCount; i++) {
                String id   = readStr(in);
                String name = readStr(in);
                if (db.genres().get(id) == null)
                    db.addGenre(new Genre(id, name));
            }

            // Conteúdos
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

                Genre g = db.genres().get(genreId);
                if (g == null) { skipExtra(in, type); continue; }

                Content c = null;
                if ("S".equals(type)) {
                    int seasons = in.readInt();
                    c = new Series(id, title, g, date, dur, region, seasons);
                } else if ("D".equals(type)) {
                    String topic = readStr(in);
                    String nar = readStr(in);
                    c = new Documentary(id, title, g, date, dur, region, topic, nar);
                } else {
                    readStr(in); // consume extra
                    c = new Movie(id, title, g, date, dur, region, null);
                }
                c.setRating(rating);
                if (db.contents().get(id) == null) db.addContent(c);
            }

            // Artistas
            int aCount = in.readInt();
            for (int i = 0; i < aCount; i++) {
                String id    = readStr(in);
                String name  = readStr(in);
                String nat   = readStr(in);
                String gen   = readStr(in);
                LocalDate bd = LocalDate.parse(readStr(in));
                String role  = readStr(in);
                if (!db.artists().contains(id)) {
                    Artist a = new Artist(id, name, nat, gen, bd,
                            edu.ufp.streaming.rec.enums.ArtistRole.valueOf(role));
                    db.addArtist(a);
                }
            }

            // Utilizadores
            int uCount = in.readInt();
            for (int i = 0; i < uCount; i++) {
                String id      = readStr(in);
                String name    = readStr(in);
                String email   = readStr(in);
                String region  = readStr(in);
                LocalDate date = LocalDate.parse(readStr(in));
                String hash    = readStr(in);
                if (!db.users().contains(id)) {
                    User u = new User(id, name, email, region, date, null);
                    u.setPasswordHash(hash);
                    db.addUser(u);
                }
            }

            // Follows
            int fCount = in.readInt();
            for (int i = 0; i < fCount; i++) {
                String followerId  = readStr(in);
                String followedId  = readStr(in);
                readStr(in); // date — não usada na reconstituição
                User follower = db.users().get(followerId);
                User followed = db.users().get(followedId);
                if (follower != null && followed != null
                        && !db.follows().isFollowing(followerId, followedId)) {
                    db.addFollow(followerId, followedId);
                }
            }

            // Interações
            int iCount = in.readInt();
            for (int i = 0; i < iCount; i++) {
                String userId    = readStr(in);
                String contentId = readStr(in);
                LocalDateTime dt = LocalDateTime.parse(readStr(in));
                double rating    = in.readDouble();
                double progress  = in.readDouble();
                String typeStr   = readStr(in);
                String iId       = readStr(in);

                User u    = db.users().get(userId);
                Content c = db.contents().get(contentId);
                if (u == null || c == null) continue;

                // Evitar duplicados pelo ID
                boolean exists = u.getInteractions().stream()
                        .anyMatch(it -> iId.equals(it.getId()));
                if (!exists) {
                    edu.ufp.streaming.rec.enums.InterationType type =
                            edu.ufp.streaming.rec.enums.InterationType.valueOf(typeStr);
                    Interation inter = new Interation(u, c, dt, rating, progress, type, iId);
                    db.addInteraction(inter);
                }
            }

            System.out.println("[AppStateSerializer] Estado carregado de " + FILE);
        } catch (Exception ex) {
            System.err.println("[AppStateSerializer] Erro ao carregar: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private static void writeStr(DataOutputStream out, String s) throws IOException {
        byte[] bytes = (s != null ? s : "").getBytes(java.nio.charset.StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    private static String readStr(DataInputStream in) throws IOException {
        int len = in.readInt();
        if (len < 0 || len > 10_000) return "";
        byte[] bytes = new byte[len];
        in.readFully(bytes);
        return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
    }

    /** Consome os bytes extra de um tipo de conteúdo não reconhecido. */
    private static void skipExtra(DataInputStream in, String type) throws IOException {
        if ("S".equals(type)) in.readInt();
        else if ("D".equals(type)) { readStr(in); readStr(in); }
        else readStr(in);
    }
}