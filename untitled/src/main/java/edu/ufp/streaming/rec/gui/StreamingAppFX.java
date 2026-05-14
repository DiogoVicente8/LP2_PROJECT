package edu.ufp.streaming.rec.gui;

import edu.ufp.streaming.rec.enums.ArtistRole;
import edu.ufp.streaming.rec.enums.InterationType;
import edu.ufp.streaming.rec.managers.AppStateSerializer;
import edu.ufp.streaming.rec.managers.StreamingDatabase;
import edu.ufp.streaming.rec.models.*;
import javafx.application.Application;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class StreamingAppFX extends Application {

    private StreamingDatabase db;

    @Override
    public void start(Stage primaryStage) {
        db = buildSampleDB();

        edu.ufp.streaming.rec.managers.SeedData.populate(db);
        // Carrega estado completo (utilizadores, conteúdos, follows, interações).
        // Dados de exemplo com o mesmo ID não são duplicados.
        AppStateSerializer.load(db);

        // Garantir que o utilizador admin mantém privilégios mesmo após desserialização
        // (ficheiros .dat gravados antes do campo 'admin' existir não o restauram)
        User adminUser = db.users().get("admin");
        if (adminUser != null) adminUser.setAdmin(true);

        // Guarda estado ao fechar a janela principal
        primaryStage.setOnCloseRequest(e -> AppStateSerializer.save(db));

        new LoginScreenFX(db, primaryStage, loggedUser -> {
            if (loggedUser.isAdmin()) {
                new AdminDashboardFX(db, loggedUser).start(primaryStage);
            } else {
                new StreamingDashboardFX(db, loggedUser).start(primaryStage);
            }
        });
    }

    /** Ponto de entrada JavaFX. */
    public static void main(String[] args) {
        launch(args);
    }

    public static StreamingDatabase buildSampleDB() {
        StreamingDatabase sdb = new StreamingDatabase();

        Genre gAcao  = new Genre("g1", "Acao");
        Genre gDrama = new Genre("g2", "Drama");
        Genre gSci   = new Genre("g3", "Sci-Fi");
        sdb.addGenre(gAcao); sdb.addGenre(gDrama); sdb.addGenre(gSci);

        User u1 = new User("u1", "Alice Silva", "alice@mail.com", "PT", LocalDate.of(2020, 1, 10), "alice123");
        User u2 = new User("u2", "Bruno Costa", "bruno@mail.com", "PT", LocalDate.of(2020, 3, 15), "bruno123");
        User admin = new User("admin", "Administrador", "admin@streaming.com", "PT", LocalDate.of(2020, 1, 1), "admin123");
        admin.setAdmin(true);
        sdb.addUser(u1); sdb.addUser(u2); sdb.addUser(admin);

        sdb.addFollow("u1", "u2");

        return sdb;
    }
}