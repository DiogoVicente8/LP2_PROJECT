package edu.ufp.streaming.rec.gui;

import edu.ufp.streaming.rec.managers.AppStateSerializer;
import edu.ufp.streaming.rec.managers.StreamingDatabase;
import javafx.application.Application;
import javafx.stage.Stage;

public class StreamingAppFX extends Application {

    private StreamingDatabase db;

    @Override
    public void start(Stage primaryStage) {
        db = buildSampleDB();

        // Carregar estado completo se o ficheiro existir, senão criar dados de exemplo
        java.nio.file.Path path = java.nio.file.Paths.get(AppStateSerializer.FILE);
        if (!java.nio.file.Files.exists(path)) {
            edu.ufp.streaming.rec.managers.SeedData.populate(db);
        }
        // Carrega estado completo (utilizadores, conteúdos, follows, interações).
        // Dados de exemplo com o mesmo 'ID' não são duplicados.
        AppStateSerializer.load(db);

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
        return new StreamingDatabase();
    }
}