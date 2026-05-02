package edu.ufp.streaming.rec.managers;

import edu.ufp.streaming.rec.models.User;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Gere a persistência de utilizadores num ficheiro JSON simples.
 * Não usa bibliotecas externas — serialização manual.
 *
 * Ficheiro: users_data.json (criado na pasta de execução)
 */
public class UserPersistenceManager {

    private static final String FILE = "users_data.json";

    // -------------------------------------------------------------------------
    // GUARDAR
    // -------------------------------------------------------------------------

    /**
     * Grava todos os utilizadores da base de dados no ficheiro JSON.
     * Chama isto depois de qualquer addUser / removeUser / changePassword.
     */
    public static void save(StreamingDatabase db) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            pw.println("[");
            List<User> all = db.users().listAll();
            for (int i = 0; i < all.size(); i++) {
                User u = all.get(i);
                pw.println("  {");
                pw.println("    \"id\": "           + jsonStr(u.getId())             + ",");
                pw.println("    \"name\": "         + jsonStr(u.getName())           + ",");
                pw.println("    \"email\": "        + jsonStr(u.getEmail())          + ",");
                pw.println("    \"region\": "       + jsonStr(u.getRegion())         + ",");
                pw.println("    \"registerDate\": " + jsonStr(u.getRegisterDate().toString()) + ",");
                // getPasswordHash devolve o hash/plaintext guardado internamente
                pw.println("    \"passwordHash\": " + jsonStr(u.getPasswordHash())   );
                pw.print("  }");
                if (i < all.size() - 1) pw.print(",");
                pw.println();
            }
            pw.println("]");
        } catch (IOException ex) {
            System.err.println("[UserPersistenceManager] Erro ao guardar: " + ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // CARREGAR
    // -------------------------------------------------------------------------

    /**
     * Lê o ficheiro JSON e adiciona à base de dados os utilizadores que ainda
     * não existem (evita duplicados com os dados de exemplo).
     *
     * @return número de utilizadores carregados do ficheiro
     */
    public static int load(StreamingDatabase db) {
        Path path = Paths.get(FILE);
        if (!Files.exists(path)) return 0;

        int count = 0;
        try {
            String content = Files.readString(path);
            // Parser manual de JSON muito simples: extrai cada bloco { ... }
            List<String> blocks = extractBlocks(content);
            for (String block : blocks) {
                String id           = extractField(block, "id");
                String name         = extractField(block, "name");
                String email        = extractField(block, "email");
                String region       = extractField(block, "region");
                String dateStr      = extractField(block, "registerDate");
                String passwordHash = extractField(block, "passwordHash");

                if (id == null || name == null) continue;
                if (db.users().contains(id)) continue; // já existe (ex: dados de exemplo)

                LocalDate date = (dateStr != null) ? LocalDate.parse(dateStr) : LocalDate.now();
                String emailFinal  = (email  != null) ? email  : "";
                String regionFinal = (region != null) ? region : "PT";

                // Cria utilizador e define o hash directamente (sem re-hash)
                User u = new User(id, name, emailFinal, regionFinal, date, null);
                u.setPasswordHash(passwordHash);
                db.addUser(u);
                count++;
            }
        } catch (Exception ex) {
            System.err.println("[UserPersistenceManager] Erro ao carregar: " + ex.getMessage());
        }
        return count;
    }

    // -------------------------------------------------------------------------
    // Helpers de parsing JSON manual
    // -------------------------------------------------------------------------

    private static List<String> extractBlocks(String json) {
        List<String> blocks = new ArrayList<>();
        int depth = 0, start = -1;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') { if (depth == 0) start = i; depth++; }
            else if (c == '}') { depth--; if (depth == 0 && start != -1) blocks.add(json.substring(start, i + 1)); }
        }
        return blocks;
    }

    /** Extrai o valor de "key": "value" de um bloco JSON simples. */
    private static String extractField(String block, String key) {
        String search = "\"" + key + "\"";
        int idx = block.indexOf(search);
        if (idx < 0) return null;
        int colon = block.indexOf(':', idx + search.length());
        if (colon < 0) return null;
        int q1 = block.indexOf('"', colon + 1);
        if (q1 < 0) return null;
        int q2 = q1 + 1;
        while (q2 < block.length()) {
            if (block.charAt(q2) == '"' && block.charAt(q2 - 1) != '\\') break;
            q2++;
        }
        return block.substring(q1 + 1, q2).replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static String jsonStr(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}