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

        // Carrega estado completo (utilizadores, conteúdos, follows, interações).
        // Dados de exemplo com o mesmo ID não são duplicados.
        AppStateSerializer.load(db);

        // Guarda estado ao fechar a janela principal
        primaryStage.setOnCloseRequest(e -> AppStateSerializer.save(db));

        new LoginScreenFX(db, primaryStage, loggedUser ->
                new StreamingDashboardFX(db, loggedUser).start(primaryStage)
        );
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
        sdb.addUser(u1); sdb.addUser(u2);

        Movie m1 = new Movie("c1", "Inception", gAcao, LocalDate.of(2010, 7, 16), 148, "PT", null);
        sdb.addContent(m1);

        Artist a1 = new Artist("a1", "Christopher Nolan", "GB", "M", LocalDate.of(1970, 7, 30), ArtistRole.DIRECTOR);
        sdb.addArtist(a1);
        sdb.addParticipation("a1", "c1", ArtistRole.DIRECTOR, LocalDate.of(2010, 7, 16));

        sdb.addFollow("u1", "u2");
        sdb.addInteraction(new Interation(u1, m1, LocalDateTime.of(2024, 1, 10, 20, 0),
                0, 0.9, InterationType.WATCH, "i1"));

        return sdb;
    }
}