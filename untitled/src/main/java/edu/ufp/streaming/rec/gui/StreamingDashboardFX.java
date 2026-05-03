package edu.ufp.streaming.rec.gui;

import edu.ufp.streaming.rec.enums.ArtistRole;
import edu.ufp.streaming.rec.enums.InterationType;
import edu.ufp.streaming.rec.managers.AppStateSerializer;
import edu.ufp.streaming.rec.managers.ContentFileManager;
import edu.ufp.streaming.rec.managers.ContentSerializer;
import edu.ufp.streaming.rec.managers.StreamingDatabase;
import edu.ufp.streaming.rec.managers.UserPersistenceManager;
import edu.ufp.streaming.rec.models.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class StreamingDashboardFX {

    // ── Paleta ────────────────────────────────────────────────────────────
    private static final String C_BG      = "#0A0A12";
    private static final String C_SURFACE = "#1C1C2A";
    private static final String C_BORDER  = "#323246";
    private static final String C_ACCENT  = "#E53935";
    private static final String C_TEXT    = "#F0F0F5";
    private static final String C_MUTED   = "#8C8CA0";
    private static final String C_SUCCESS = "#2ECC71";

    // ── Estilos reutilizáveis ─────────────────────────────────────────────
    private static final String FIELD =
            "-fx-background-color:" + C_SURFACE + ";" +
                    "-fx-text-fill:" + C_TEXT + ";" +
                    "-fx-border-color:" + C_BORDER + ";" +
                    "-fx-border-radius:6;-fx-background-radius:6;-fx-padding:8 10;";

    private static final String BTN_P =
            "-fx-background-color:" + C_ACCENT + ";-fx-text-fill:white;" +
                    "-fx-font-weight:bold;-fx-background-radius:8;-fx-padding:7 16;-fx-cursor:hand;";

    private static final String BTN_S =
            "-fx-background-color:" + C_SURFACE + ";-fx-text-fill:" + C_TEXT + ";" +
                    "-fx-border-color:" + C_BORDER + ";-fx-border-radius:8;-fx-background-radius:8;" +
                    "-fx-padding:7 14;-fx-cursor:hand;";

    private static final String BTN_G =
            "-fx-background-color:transparent;-fx-text-fill:" + C_ACCENT + ";" +
                    "-fx-border-color:" + C_ACCENT + ";-fx-border-radius:8;-fx-background-radius:8;" +
                    "-fx-padding:7 14;-fx-cursor:hand;";

    private final StreamingDatabase db;
    private final User loggedUser;
    private double xOff = 0, yOff = 0;

    private final Label snackLabel = new Label();
    private javafx.animation.PauseTransition snackTimer;

    public StreamingDashboardFX(StreamingDatabase db, User loggedUser) {
        this.db = db;
        this.loggedUser = loggedUser;
    }

    public void start(Stage oldStage) {
        oldStage.close();
        Stage stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);

        BorderPane root = new BorderPane();
        root.setStyle(
                "-fx-background-color:" + C_BG + ";" +
                        "-fx-background-radius:16;-fx-border-radius:16;" +
                        "-fx-border-color:" + C_ACCENT + ";-fx-border-width:2 0 0 0;"
        );
        root.setTop(buildTopBar(stage));

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle("-fx-background-color:" + C_BG + ";");
        tabs.getTabs().addAll(
                buildUsersTab(),
                buildContentsTab(),
                buildArtistsTab(),
                buildGraphTab(),
                buildMinhaContaTab()
        );

        root.setCenter(tabs);
        root.setBottom(buildStatusBar());

        root.setOnMousePressed(ev -> { xOff = ev.getSceneX(); yOff = ev.getSceneY(); });
        root.setOnMouseDragged(ev -> { stage.setX(ev.getScreenX() - xOff); stage.setY(ev.getScreenY() - yOff); });

        String css =
                ".tab-pane .tab-header-area .tab-header-background{-fx-background-color:" + C_BG + ";}" +
                        ".tab-pane .tab{-fx-background-color:" + C_SURFACE + ";-fx-background-radius:8 8 0 0;-fx-padding:4 14;}" +
                        ".tab-pane .tab:selected{-fx-background-color:" + C_ACCENT + ";}" +
                        ".tab .tab-label{-fx-text-fill:" + C_MUTED + ";-fx-font-size:13px;}" +
                        ".tab:selected .tab-label{-fx-text-fill:white;-fx-font-weight:bold;}" +
                        ".tab-pane .tab-content-area{-fx-background-color:" + C_BG + ";}" +
                        ".table-view{-fx-background-color:" + C_SURFACE + ";-fx-border-color:" + C_BORDER + ";-fx-border-radius:8;-fx-background-radius:8;}" +
                        ".table-view .column-header-background{-fx-background-color:" + C_BG + ";}" +
                        ".table-view .column-header,.table-view .filler{-fx-background-color:" + C_BG + ";-fx-border-color:" + C_BORDER + ";}" +
                        ".table-view .column-header .label{-fx-text-fill:" + C_MUTED + ";-fx-font-weight:bold;}" +
                        ".table-row-cell{-fx-background-color:" + C_SURFACE + ";-fx-border-color:" + C_BORDER + ";}" +
                        ".table-row-cell:odd{-fx-background-color:#161622;}" +
                        ".table-row-cell:selected{-fx-background-color:#3a1515;}" +
                        ".table-cell{-fx-text-fill:" + C_TEXT + ";}" +
                        ".scroll-pane{-fx-background-color:" + C_BG + ";-fx-background:" + C_BG + ";}" +
                        ".scroll-pane > .viewport{-fx-background-color:" + C_BG + ";}" +
                        ".split-pane{-fx-background-color:" + C_BG + ";}";

        Scene scene = new Scene(root, 1180, 800);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add("data:text/css," +
                java.net.URLEncoder.encode(css, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20"));

        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    // ── Top Bar ───────────────────────────────────────────────────────────
    private HBox buildTopBar(Stage stage) {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(14, 20, 14, 20));
        bar.setStyle("-fx-background-color:" + C_BG + ";");

        Label logo = new Label("▶ StreamingApp");
        logo.setStyle("-fx-text-fill:" + C_TEXT + ";-fx-font-size:20px;-fx-font-weight:bold;");
        Label pipe = new Label("  |");
        pipe.setStyle("-fx-text-fill:" + C_ACCENT + ";-fx-font-size:20px;");
        Label sess = new Label("Sessão: " + loggedUser.getName());
        sess.setStyle("-fx-text-fill:" + C_MUTED + ";-fx-font-size:13px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Menu mFich = new Menu("  ⚙  Ficheiro  ");
        MenuItem expTxt = new MenuItem("Exportar TXT (R10)");
        MenuItem impTxt = new MenuItem("Importar TXT (R10)");
        MenuItem expBin = new MenuItem("Exportar Binário (R11)");
        MenuItem impBin = new MenuItem("Importar Binário (R11)");
        expTxt.setOnAction(e -> { ContentFileManager.exportGenres(db.genres(), "genres.txt"); ContentFileManager.exportContents(db.contents(), "contents.txt"); snack("✓ Dados exportados para TXT", true); });
        impTxt.setOnAction(e -> { ContentFileManager.importGenres(db.genres(), "genres.txt"); ContentFileManager.importContents(db.contents(), db.genres(), "contents.txt"); snack("✓ Dados importados de TXT", true); });
        expBin.setOnAction(e -> { ContentSerializer.exportGenres(db.genres(), "genres.bin"); ContentSerializer.exportContents(db.contents(), "contents.bin"); snack("✓ Dados serializados em binário", true); });
        impBin.setOnAction(e -> { ContentSerializer.importGenres(db.genres(), "genres.bin"); ContentSerializer.importContents(db.contents(), "contents.bin"); snack("✓ Dados importados de binário", true); });
        mFich.getItems().addAll(expTxt, impTxt, new SeparatorMenuItem(), expBin, impBin);

        MenuBar menuBar = new MenuBar(mFich);
        menuBar.setStyle("-fx-background-color:transparent;-fx-padding:0;");

        Button btnLogout = new Button("⏏  Sair da Conta");
        btnLogout.setStyle(
                "-fx-background-color:transparent;-fx-text-fill:" + C_MUTED + ";" +
                        "-fx-border-color:" + C_BORDER + ";-fx-border-radius:8;-fx-background-radius:8;" +
                        "-fx-padding:6 12;-fx-font-size:12px;-fx-cursor:hand;"
        );
        btnLogout.setOnMouseEntered(e -> btnLogout.setStyle(
                "-fx-background-color:" + C_SURFACE + ";-fx-text-fill:" + C_TEXT + ";" +
                        "-fx-border-color:" + C_ACCENT + ";-fx-border-radius:8;-fx-background-radius:8;" +
                        "-fx-padding:6 12;-fx-font-size:12px;-fx-cursor:hand;"
        ));
        btnLogout.setOnMouseExited(e -> btnLogout.setStyle(
                "-fx-background-color:transparent;-fx-text-fill:" + C_MUTED + ";" +
                        "-fx-border-color:" + C_BORDER + ";-fx-border-radius:8;-fx-background-radius:8;" +
                        "-fx-padding:6 12;-fx-font-size:12px;-fx-cursor:hand;"
        ));
        btnLogout.setOnAction(e -> {
            AppStateSerializer.save(db);
            stage.close();
            Stage loginStage = new Stage();
            new LoginScreenFX(db, loginStage, loggedUser2 ->
                    new StreamingDashboardFX(db, loggedUser2).start(loginStage)
            );
        });

        Button btnX = new Button("✕");
        btnX.setStyle("-fx-background-color:transparent;-fx-text-fill:" + C_MUTED + ";-fx-font-size:16px;-fx-cursor:hand;");
        btnX.setOnAction(e -> System.exit(0));

        bar.getChildren().addAll(logo, pipe, sess, spacer, menuBar, btnLogout, btnX);
        return bar;
    }

    // ── Status Bar + Snackbar ─────────────────────────────────────────────
    private VBox buildStatusBar() {
        HBox infoBar = new HBox();
        infoBar.setPadding(new Insets(7, 20, 0, 20));
        Label l = new Label("LP2 / AED2  •  UFP  •  " + loggedUser.getId() + " @ " + loggedUser.getRegion());
        l.setStyle("-fx-text-fill:" + C_MUTED + ";-fx-font-size:11px;");
        infoBar.getChildren().add(l);

        snackLabel.setVisible(false);
        snackLabel.setStyle("-fx-text-fill:" + C_TEXT + ";-fx-font-size:12px;-fx-font-weight:bold;-fx-padding:0 20;");
        HBox snackBar = new HBox(snackLabel);
        snackBar.setAlignment(Pos.CENTER);
        snackBar.setPadding(new Insets(4, 20, 8, 20));
        snackBar.setStyle("-fx-background-color:transparent;");

        VBox statusBox = new VBox(infoBar, snackBar);
        statusBox.setStyle("-fx-background-color:" + C_SURFACE + ";-fx-background-radius:0 0 14 14;");
        return statusBox;
    }

    private void snack(String msg, boolean success) {
        snackLabel.setText(msg);
        snackLabel.setStyle(
                "-fx-text-fill:" + (success ? C_SUCCESS : "#FF5252") + ";" +
                        "-fx-font-size:12px;-fx-font-weight:bold;-fx-padding:0 20;"
        );
        snackLabel.setVisible(true);
        if (snackTimer != null) snackTimer.stop();
        snackTimer = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
        snackTimer.setOnFinished(e -> snackLabel.setVisible(false));
        snackTimer.play();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 1. TAB UTILIZADORES — estilo social / Spotify
    // ═══════════════════════════════════════════════════════════════════════
    private Tab buildUsersTab() {
        Tab tab = new Tab("👤  Utilizadores");
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(16));
        pane.setStyle("-fx-background-color:" + C_BG + ";");

        // ── Lista de cards ─────────────────────────────────────────────────
        VBox userList = new VBox(10);
        userList.setPadding(new Insets(4, 0, 4, 0));

        ScrollPane listScroll = new ScrollPane(userList);
        listScroll.setFitToWidth(true);
        listScroll.setStyle(
                "-fx-background-color:" + C_BG + ";" +
                        "-fx-background:" + C_BG + ";" +
                        "-fx-border-color:transparent;"
        );

        final User[] selectedUser = {null};

        // Runnable[] para poder ser referenciado dentro de si mesmo e passado ao buildUserCard
        Runnable[] refreshList = {null};
        refreshList[0] = () -> {
            userList.getChildren().clear();
            for (User u : db.users().listAll())
                userList.getChildren().add(buildUserCard(u, selectedUser, refreshList));
        };
        refreshList[0].run();

        // ── Sidebar ────────────────────────────────────────────────────────
        VBox sidebar = new VBox(14);
        sidebar.setPrefWidth(340);
        sidebar.setPadding(new Insets(0, 0, 0, 14));

        // Pesquisa
        VBox sCard = card("🔍  Pesquisar por Nome");
        TextField fSearch = field("Nome...");
        Button bSearch = btn("Pesquisar", BTN_P), bAll = btn("Todos", BTN_S);
        bSearch.setOnAction(e -> {
            userList.getChildren().clear();
            for (User u : db.users().searchByNameSubstring(fSearch.getText().trim()))
                userList.getChildren().add(buildUserCard(u, selectedUser, refreshList));
        });
        bAll.setOnAction(e -> refreshList[0].run());
        HBox sr = new HBox(8, fSearch, bSearch, bAll);
        sr.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(fSearch, Priority.ALWAYS);
        sCard.getChildren().add(sr);

        // Inserir
        VBox iCard = card("➕  Inserir Utilizador");
        GridPane form = grid();
        TextField fId    = field("ID"),
                fNome  = field("Nome"),
                fEmail = field("Email"),
                fReg   = field("PT");
        PasswordField fPwd  = pwd("Password"),
                fConf = pwd("Confirmar");
        form.addRow(0, lbl("ID:"),       fId,    lbl("Nome:"),      fNome);
        form.addRow(1, lbl("Email:"),    fEmail, lbl("Região:"),    fReg);
        form.addRow(2, lbl("Password:"), fPwd,   lbl("Confirmar:"), fConf);

        Button bAdd = btn("Adicionar", BTN_P);
        Button bRem = btn("Remover Sel.", BTN_G);

        bAdd.setOnAction(e -> {
            if (fId.getText().isEmpty() || fNome.getText().isEmpty() || fPwd.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erro", "ID, Nome e Password são obrigatórios.");
                return;
            }
            if (!fPwd.getText().equals(fConf.getText())) {
                showAlert(Alert.AlertType.ERROR, "Erro", "As passwords não coincidem.");
                return;
            }
            if (!fEmail.getText().isEmpty()) {
                for (User u : db.users().listAll()) {
                    if (u.getEmail().equalsIgnoreCase(fEmail.getText().trim())) {
                        showAlert(Alert.AlertType.ERROR, "Email Duplicado",
                                "Já registado por '" + u.getName() + "'.");
                        return;
                    }
                }
            }
            User u = new User(fId.getText().trim(), fNome.getText().trim(),
                    fEmail.getText().trim(), fReg.getText().trim(),
                    LocalDate.now(), fPwd.getText());
            if (db.addUser(u)) {
                UserPersistenceManager.save(db);
                refreshList[0].run();
                fId.clear(); fNome.clear(); fEmail.clear();
                fPwd.clear(); fConf.clear();
                snack("✓ Utilizador adicionado com sucesso", true);
            } else {
                showAlert(Alert.AlertType.ERROR, "Erro", "ID já existe.");
            }
        });

        bRem.setOnAction(e -> {
            if (selectedUser[0] == null) {
                showAlert(Alert.AlertType.ERROR, "Erro", "Seleciona primeiro um utilizador.");
                return;
            }
            db.removeUser(selectedUser[0].getId());
            UserPersistenceManager.save(db);
            selectedUser[0] = null;
            refreshList[0].run();
            snack("✓ Utilizador removido", true);
        });

        iCard.getChildren().addAll(form, row(bAdd, bRem));

        // Seguir / Deixar
        VBox eCard = card("🔗  Ações no Utilizador Selecionado");
        TextField fFolId = field("ID a seguir / deixar de seguir");
        Button bFol   = btn("➕  Seguir",     BTN_P);
        Button bUnfol = btn("✖  Deixar",      BTN_G);
        Button bSeg   = btn("👥  Seguidores", BTN_S);
        Button bSeg2  = btn("➜  A Seguir",    BTN_S);

        bFol.setOnAction(e -> {
            if (selectedUser[0] == null) { snack("✗ Seleciona um utilizador primeiro", false); return; }
            String t = fFolId.getText().trim();
            if (t.isEmpty()) { snack("✗ Introduz o ID alvo", false); return; }
            if (db.addFollow(selectedUser[0].getId(), t) != null) {
                refreshList[0].run();
                snack("✓ " + selectedUser[0].getId() + " passou a seguir " + t, true);
            } else {
                snack("✗ Não foi possível criar o follow", false);
            }
        });

        bUnfol.setOnAction(e -> {
            if (selectedUser[0] == null) { snack("✗ Seleciona um utilizador primeiro", false); return; }
            String t = fFolId.getText().trim();
            if (t.isEmpty()) { snack("✗ Introduz o ID alvo", false); return; }
            if (db.follows().unfollow(selectedUser[0].getId(), t) != null) {
                refreshList[0].run();
                snack("✓ Deixou de seguir " + t, true);
            } else {
                snack("✗ Relação não encontrada", false);
            }
        });

        bSeg.setOnAction(e -> {
            if (selectedUser[0] == null) return;
            List<User> l = db.follows().getFollowers(selectedUser[0].getId());
            if (l.isEmpty()) showAlert(Alert.AlertType.INFORMATION, "Seguidores",
                    selectedUser[0].getId() + " não tem seguidores.");
            else {
                StringBuilder sb = new StringBuilder();
                l.forEach(s -> sb.append("• ").append(s.getName())
                        .append(" (").append(s.getId()).append(")\n"));
                showAlert(Alert.AlertType.INFORMATION,
                        "Seguidores de " + selectedUser[0].getId(), sb.toString());
            }
        });

        bSeg2.setOnAction(e -> {
            if (selectedUser[0] == null) return;
            List<User> l = db.follows().getFollowing(selectedUser[0].getId());
            if (l.isEmpty()) showAlert(Alert.AlertType.INFORMATION, "A Seguir",
                    selectedUser[0].getId() + " não segue ninguém.");
            else {
                StringBuilder sb = new StringBuilder();
                l.forEach(s -> sb.append("• ").append(s.getName())
                        .append(" (").append(s.getId()).append(")\n"));
                showAlert(Alert.AlertType.INFORMATION,
                        selectedUser[0].getId() + " segue:", sb.toString());
            }
        });

        eCard.getChildren().addAll(
                lbl("Seleciona um card e introduz o ID alvo:"),
                fFolId,
                row(bFol, bUnfol),
                row(bSeg, bSeg2)
        );

        sidebar.getChildren().addAll(sCard, iCard, eCard);
        pane.setCenter(listScroll);
        pane.setRight(scroll(sidebar));
        tab.setContent(pane);
        return tab;
    }

    // ── Card individual de utilizador ─────────────────────────────────────
    private javafx.scene.Node buildUserCard(User u, User[] selectedUser, Runnable[] refreshList) {
        HBox card = new HBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(14, 18, 14, 18));
        card.setStyle(
                "-fx-background-color:" + C_SURFACE + ";" +
                        "-fx-border-color:" + C_BORDER + ";" +
                        "-fx-border-radius:12;-fx-background-radius:12;-fx-cursor:hand;"
        );

        // Avatar circular
        Label avatar = new Label(initials(u.getName()));
        avatar.setStyle(
                "-fx-background-color:" + avatarColor(u.getId()) + ";" +
                        "-fx-text-fill:white;-fx-font-size:16px;-fx-font-weight:bold;" +
                        "-fx-min-width:50;-fx-min-height:50;-fx-max-width:50;-fx-max-height:50;" +
                        "-fx-alignment:center;-fx-background-radius:25;"
        );

        // Info central
        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label lName = new Label(u.getName());
        lName.setStyle("-fx-text-fill:" + C_TEXT + ";-fx-font-size:15px;-fx-font-weight:bold;");

        HBox meta = new HBox(8);
        meta.setAlignment(Pos.CENTER_LEFT);
        meta.getChildren().addAll(
                badge(u.getId(),             "#1E1E30", C_MUTED),
                badge(u.getEmail(),          "#1E1E30", C_MUTED),
                badge("🌍 " + u.getRegion(), "#1a1a28", C_ACCENT)
        );

        // Stats sempre frescos
        int followers = db.follows().getFollowers(u.getId()).size();
        int following = db.follows().getFollowing(u.getId()).size();
        int watched   = (int) u.getInteractions().stream()
                .filter(i -> i.getType() == InterationType.WATCH).count();

        HBox statsRow = new HBox(14);
        statsRow.setAlignment(Pos.CENTER_LEFT);
        statsRow.getChildren().addAll(
                miniStat("👥", followers, "seguidores"),
                miniStat("➜",  following, "a seguir"),
                miniStat("▶",  watched,   "vistos")
        );

        info.getChildren().addAll(lName, meta, statsRow);

        // Botão Seguir / A seguir
        boolean isMe    = u.getId().equals(loggedUser.getId());
        boolean jaSegue = db.follows().getFollowing(loggedUser.getId())
                .stream().anyMatch(x -> x.getId().equals(u.getId()));

        Button bFollow = new Button(isMe ? "— Eu —" : jaSegue ? "✓ A seguir" : "+ Seguir");
        bFollow.setStyle(followBtnStyle(isMe, jaSegue));

        if (!isMe) {
            bFollow.setOnAction(e -> {
                boolean segueAgora = db.follows().getFollowing(loggedUser.getId())
                        .stream().anyMatch(x -> x.getId().equals(u.getId()));
                if (segueAgora) {
                    db.follows().unfollow(loggedUser.getId(), u.getId());
                    snack("✓ Deixaste de seguir " + u.getName(), true);
                } else {
                    db.addFollow(loggedUser.getId(), u.getId());
                    snack("✓ Passaste a seguir " + u.getName(), true);
                }
                refreshList[0].run(); // re-render completo — stats e botão atualizados
            });
        }

        // Hover + seleção
        card.setOnMouseClicked(e -> {
            selectedUser[0] = u;
            card.setStyle(
                    "-fx-background-color:#1e1030;" +
                            "-fx-border-color:" + C_ACCENT + ";-fx-border-width:2;" +
                            "-fx-border-radius:12;-fx-background-radius:12;-fx-cursor:hand;"
            );
        });
        card.setOnMouseEntered(e -> {
            if (selectedUser[0] == null || !selectedUser[0].getId().equals(u.getId()))
                card.setStyle(
                        "-fx-background-color:#22222e;" +
                                "-fx-border-color:" + C_ACCENT + ";" +
                                "-fx-border-radius:12;-fx-background-radius:12;-fx-cursor:hand;"
                );
        });
        card.setOnMouseExited(e -> {
            if (selectedUser[0] == null || !selectedUser[0].getId().equals(u.getId()))
                card.setStyle(
                        "-fx-background-color:" + C_SURFACE + ";" +
                                "-fx-border-color:" + C_BORDER + ";" +
                                "-fx-border-radius:12;-fx-background-radius:12;-fx-cursor:hand;"
                );
        });

        card.getChildren().addAll(avatar, info, bFollow);
        return card;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 2. TAB CONTEÚDOS
    // ═══════════════════════════════════════════════════════════════════════
    private Tab buildContentsTab() {
        Tab tab = new Tab("🎬  Conteúdos");
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(16));
        pane.setStyle("-fx-background-color:" + C_BG + ";");

        TableView<Content> table = new TableView<>();
        table.getColumns().addAll(
                col("ID",              d -> d.getValue().getId()),
                col("Tipo",            d -> d.getValue() instanceof Movie ? "Filme" : d.getValue() instanceof Series ? "Série" : "Documentário"),
                col("Título",          d -> d.getValue().getTitle()),
                col("Género",          d -> d.getValue().getGenre().getName()),
                col("Data Lançamento", d -> d.getValue().getReleaseDate().toString()),
                col("Duração",         d -> String.valueOf(d.getValue().getDuration())),
                col("Rating",          d -> String.format("%.1f", d.getValue().getRating()))
        );
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        Runnable refresh = () -> table.setItems(FXCollections.observableArrayList(db.contents().listAll()));
        refresh.run();

        VBox sidebar = new VBox(14);
        sidebar.setPrefWidth(340);
        sidebar.setPadding(new Insets(0, 0, 0, 14));

        VBox sCard = card("🔍  Pesquisar Conteúdos");
        TextField fTit = field("Título...");
        Button bSTit = btn("Por Título", BTN_P);
        bSTit.setOnAction(e -> table.setItems(
                FXCollections.observableArrayList(db.contents().searchByTitleSubstring(fTit.getText().trim()))));
        HBox sr1 = new HBox(8, fTit, bSTit);
        sr1.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(fTit, Priority.ALWAYS);
        ComboBox<String> cbT = new ComboBox<>(FXCollections.observableArrayList("-- Todos --", "Filme", "Série", "Documentário"));
        cbT.setValue("-- Todos --");
        cbT.setStyle(FIELD);
        Button bST = btn("Por Tipo", BTN_S), bAll = btn("Todos", BTN_S);
        bST.setOnAction(e -> {
            String s = cbT.getValue();
            if (s.startsWith("--")) { refresh.run(); return; }
            table.setItems(FXCollections.observableArrayList(db.contents().listAll().stream()
                    .filter(c -> (s.equals("Filme") && c instanceof Movie) ||
                            (s.equals("Série") && c instanceof Series) ||
                            (s.equals("Documentário") && c instanceof Documentary))
                    .toList()));
        });
        bAll.setOnAction(e -> refresh.run());
        sCard.getChildren().addAll(sr1, new HBox(8, cbT, bST, bAll));

        VBox iCard = card("➕  Inserir Conteúdo");
        GridPane form = grid();
        ComboBox<String> cbType = new ComboBox<>(FXCollections.observableArrayList("Filme", "Série", "Documentário"));
        cbType.setValue("Filme");
        cbType.setStyle(FIELD);
        TextField fId = field("ID"), fTitF = field("Título"), fGId = field("ID Género"),
                fData = field("2024-01-01"), fDur = field("120"), fReg = field("PT");
        form.addRow(0, lbl("Tipo:"), cbType, lbl("ID:"), fId);
        form.addRow(1, lbl("Título:"), fTitF, lbl("ID Género:"), fGId);
        form.addRow(2, lbl("Data:"), fData, lbl("Duração(m):"), fDur);
        form.addRow(3, lbl("Região:"), fReg);
        Button bAdd = btn("Adicionar", BTN_P), bRem = btn("Remover Sel.", BTN_G);
        bAdd.setOnAction(e -> {
            try {
                Genre g = db.genres().get(fGId.getText().trim());
                if (g == null) { showAlert(Alert.AlertType.ERROR, "Erro", "Género não existe!"); return; }
                LocalDate data = LocalDate.parse(fData.getText().trim());
                int dur = Integer.parseInt(fDur.getText().trim());
                Content c = switch (cbType.getValue()) {
                    case "Série"        -> new Series(fId.getText().trim(), fTitF.getText(), g, data, dur, fReg.getText(), 1);
                    case "Documentário" -> new Documentary(fId.getText().trim(), fTitF.getText(), g, data, dur, fReg.getText(), "", "");
                    default             -> new Movie(fId.getText().trim(), fTitF.getText(), g, data, dur, fReg.getText(), null);
                };
                if (db.addContent(c)) { refresh.run(); fId.clear(); fTitF.clear(); }
                else showAlert(Alert.AlertType.ERROR, "Erro", "ID já existe.");
            } catch (Exception ex) { showAlert(Alert.AlertType.ERROR, "Erro", "Verifica datas/números."); }
        });
        bRem.setOnAction(e -> {
            Content c = table.getSelectionModel().getSelectedItem();
            if (c != null) { db.removeContent(c.getId()); refresh.run(); }
        });
        iCard.getChildren().addAll(form, row(bAdd, bRem));

        // Rating por estrelas
        VBox rCard = card("⭐  Avaliar Conteúdo Selecionado");
        final int[] currentRating = {0};
        Button[] stars = new Button[5];
        HBox starRow = new HBox(4);
        starRow.setAlignment(Pos.CENTER_LEFT);

        Runnable paintStars = () -> {
            for (int i = 0; i < 5; i++) {
                stars[i].setStyle(
                        "-fx-background-color:transparent;-fx-font-size:22px;-fx-cursor:hand;" +
                                "-fx-text-fill:" + (i < currentRating[0] ? "#FFD700" : C_BORDER) + ";"
                );
            }
        };

        for (int i = 0; i < 5; i++) {
            final int val = i + 1;
            stars[i] = new Button("★");
            stars[i].setStyle("-fx-background-color:transparent;-fx-font-size:22px;-fx-cursor:hand;-fx-text-fill:" + C_BORDER + ";");
            stars[i].setOnMouseEntered(e -> {
                for (int j = 0; j < 5; j++)
                    stars[j].setStyle("-fx-background-color:transparent;-fx-font-size:22px;-fx-cursor:hand;-fx-text-fill:" + (j < val ? "#FFD700" : C_BORDER) + ";");
            });
            stars[i].setOnMouseExited(e -> paintStars.run());
            stars[i].setOnAction(e -> { currentRating[0] = val; paintStars.run(); });
            starRow.getChildren().add(stars[i]);
        }

        Label ratingValLabel = new Label("Seleciona um conteúdo e uma nota");
        ratingValLabel.setStyle("-fx-text-fill:" + C_MUTED + ";-fx-font-size:11px;");

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                int existing = (int) Math.round(sel.getRating());
                currentRating[0] = existing;
                paintStars.run();
                ratingValLabel.setText("Rating atual de \"" + sel.getTitle() + "\": " +
                        (existing == 0 ? "sem avaliação" : existing + "/5 ⭐"));
                ratingValLabel.setStyle("-fx-text-fill:" + C_MUTED + ";-fx-font-size:11px;");
            } else {
                currentRating[0] = 0;
                paintStars.run();
                ratingValLabel.setText("Seleciona um conteúdo e uma nota");
            }
        });

        Button bRate = btn("Submeter Avaliação", BTN_P);
        bRate.setMaxWidth(Double.MAX_VALUE);
        bRate.setOnAction(e -> {
            Content c = table.getSelectionModel().getSelectedItem();
            if (c == null) { showAlert(Alert.AlertType.ERROR, "Erro", "Seleciona primeiro um conteúdo na tabela."); return; }
            if (currentRating[0] == 0) { showAlert(Alert.AlertType.ERROR, "Erro", "Escolhe uma nota de 1 a 5 estrelas."); return; }
            String iId = "i_" + loggedUser.getId() + "_" + c.getId() + "_" + System.currentTimeMillis();
            Interation inter = new Interation(loggedUser, c, LocalDateTime.now(), currentRating[0], 0.0, InterationType.RATE, iId);
            db.addInteraction(inter);
            double soma = 0; int count = 0;
            for (User u : db.users().listAll()) {
                for (Interation it : u.getInteractions()) {
                    if (it.getType() == InterationType.RATE && it.getContent().getId().equals(c.getId())) {
                        soma += it.getRating(); count++;
                    }
                }
            }
            if (count > 0) c.setRating(soma / count);
            refresh.run();
            Content updated = db.contents().get(c.getId());
            if (updated != null) table.getSelectionModel().select(updated);
            ratingValLabel.setText("Avaliação submetida: " + currentRating[0] + "/5 ⭐  (média: " + String.format("%.1f", c.getRating()) + ")");
            ratingValLabel.setStyle("-fx-text-fill:" + C_SUCCESS + ";-fx-font-size:11px;");
        });

        rCard.getChildren().addAll(starRow, ratingValLabel, bRate);
        sidebar.getChildren().addAll(sCard, iCard, rCard);
        pane.setCenter(table);
        pane.setRight(scroll(sidebar));
        tab.setContent(pane);
        return tab;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 3. TAB ARTISTAS
    // ═══════════════════════════════════════════════════════════════════════
    private Tab buildArtistsTab() {
        Tab tab = new Tab("🎭  Artistas");
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(16));
        pane.setStyle("-fx-background-color:" + C_BG + ";");

        TableView<Artist> table = new TableView<>();
        table.getColumns().addAll(
                col("ID",            d -> d.getValue().getId()),
                col("Nome",          d -> d.getValue().getName()),
                col("Nacionalidade", d -> d.getValue().getNationality()),
                col("Género",        d -> d.getValue().getGender()),
                col("Data Nasc.",    d -> d.getValue().getBirthDate().toString()),
                col("Papel",         d -> d.getValue().getRole().toString())
        );
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        Runnable refresh = () -> table.setItems(FXCollections.observableArrayList(db.artists().listAll()));
        refresh.run();

        VBox sidebar = new VBox(14);
        sidebar.setPrefWidth(340);
        sidebar.setPadding(new Insets(0, 0, 0, 14));

        VBox sCard = card("🔍  Pesquisar Artistas");
        TextField fNome = field("Nome...");
        Button bSN = btn("Por Nome", BTN_P);
        bSN.setOnAction(e -> table.setItems(
                FXCollections.observableArrayList(db.artists().searchByNameSubstring(fNome.getText().trim()))));
        HBox sr1 = new HBox(8, fNome, bSN);
        sr1.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(fNome, Priority.ALWAYS);
        ComboBox<ArtistRole> cbRF = new ComboBox<>(FXCollections.observableArrayList(ArtistRole.values()));
        cbRF.setStyle(FIELD);
        Button bSR = btn("Por Papel", BTN_S), bAll = btn("Todos", BTN_S);
        TextField fData = field("yyyy-MM-dd");
        Button bSD = btn("Por Data Nasc.", BTN_S);
        bSR.setOnAction(e -> { if (cbRF.getValue() != null) table.setItems(FXCollections.observableArrayList(db.artists().searchByRole(cbRF.getValue()))); });
        bSD.setOnAction(e -> {
            try { table.setItems(FXCollections.observableArrayList(db.artists().searchByBirthDate(LocalDate.parse(fData.getText().trim())))); }
            catch (Exception ex) { showAlert(Alert.AlertType.ERROR, "Erro", "Formato de data inválido."); }
        });
        bAll.setOnAction(e -> refresh.run());
        sCard.getChildren().addAll(sr1, new HBox(8, cbRF, bSR), new HBox(8, fData, bSD, bAll));

        VBox iCard = card("➕  Inserir / Editar Artista");
        GridPane form = grid();
        TextField fId = field("ID"), fNomeA = field("Nome"), fNac = field("PT"),
                fGen = field("M"), fDataN = field("1980-01-01");
        ComboBox<ArtistRole> cbRole = new ComboBox<>(FXCollections.observableArrayList(ArtistRole.values()));
        cbRole.setValue(ArtistRole.ACTOR);
        cbRole.setStyle(FIELD);
        form.addRow(0, lbl("ID:"), fId, lbl("Nome:"), fNomeA);
        form.addRow(1, lbl("Nac.:"), fNac, lbl("Género:"), fGen);
        form.addRow(2, lbl("Data Nasc.:"), fDataN, lbl("Papel:"), cbRole);
        Button bAdd = btn("Adicionar", BTN_P), bRem = btn("Remover", BTN_G), bEN = btn("Editar Nac.", BTN_S);
        bAdd.setOnAction(e -> {
            try {
                Artist a = new Artist(fId.getText().trim(), fNomeA.getText().trim(), fNac.getText().trim(),
                        fGen.getText().trim(), LocalDate.parse(fDataN.getText().trim()), cbRole.getValue());
                if (db.addArtist(a)) { refresh.run(); fId.clear(); fNomeA.clear(); }
                else showAlert(Alert.AlertType.ERROR, "Erro", "ID de Artista já existe.");
            } catch (Exception ex) { showAlert(Alert.AlertType.ERROR, "Erro", "Verifica a data!"); }
        });
        bRem.setOnAction(e -> {
            Artist a = table.getSelectionModel().getSelectedItem();
            if (a != null) { db.removeArtist(a.getId()); refresh.run(); }
        });
        bEN.setOnAction(e -> {
            Artist a = table.getSelectionModel().getSelectedItem();
            if (a == null) return;
            String nova = askInput("Nova Nacionalidade:", a.getNationality());
            if (nova != null && !nova.trim().isEmpty()) { db.artists().editNationality(a.getId(), nova); refresh.run(); }
        });
        iCard.getChildren().addAll(form, row(bAdd, bRem, bEN));

        sidebar.getChildren().addAll(sCard, iCard);
        pane.setCenter(table);
        pane.setRight(scroll(sidebar));
        tab.setContent(pane);
        return tab;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 4. TAB GRAFO / R8
    // ═══════════════════════════════════════════════════════════════════════
    private Tab buildGraphTab() {
        Tab tab = new Tab("🔗  Grafo / R8");
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(16));
        pane.setStyle("-fx-background-color:" + C_BG + ";");

        TextArea output = new TextArea();
        output.setEditable(false);
        output.setStyle(
                "-fx-font-family:monospace;-fx-font-size:13px;" +
                        "-fx-background-color:" + C_SURFACE + ";-fx-text-fill:" + C_SUCCESS + ";" +
                        "-fx-border-color:" + C_BORDER + ";-fx-border-radius:8;-fx-background-radius:8;"
        );
        output.setText("// Resultados aparecem aqui...");

        VBox sidebar = new VBox(14);
        sidebar.setPrefWidth(340);
        sidebar.setPadding(new Insets(0, 0, 0, 14));

        VBox r8a = card("R8a — Caminho mais curto");
        TextField fOrig = field("ID Origem"), fDest = field("ID Destino");
        Button bCam = btn("Calcular", BTN_P);
        bCam.setOnAction(e -> {
            String o = fOrig.getText().trim(), d = fDest.getText().trim();
            List<String> path = db.getGraph().caminhoMaisCurtoBetweenUsers(o, d);
            double peso = db.getGraph().pesoCaminhoMaisCurto(o, d);
            if (path.isEmpty()) output.setText("[R8a] Sem caminho de " + o + " para " + d);
            else output.setText(String.format("[R8a] Caminho %s → %s:\n  %s\n  Peso total: %.2f", o, d, path, peso));
        });
        r8a.getChildren().addAll(new HBox(8, lbl("Origem:"), fOrig), new HBox(8, lbl("Destino:"), fDest), bCam);

        VBox r8c = card("R8c — Conectividade forte");
        Button bConexo = btn("Verificar Grafo", BTN_P);
        bConexo.setOnAction(e -> {
            boolean ok = db.getGraph().isGrafoUtilizadoresConexo();
            output.setText("[R8c] Grafo fortemente conexo: " + (ok ? "SIM ✓" : "NÃO ✗"));
        });
        r8c.getChildren().add(bConexo);

        VBox r8g = card("R8g — Seguidores que viram conteúdo (2024)");
        TextField fUId = field("User ID"), fCId = field("Content ID");
        Button bG = btn("Pesquisar", BTN_P);
        bG.setOnAction(e -> {
            String u = fUId.getText().trim(), c = fCId.getText().trim();
            List<User> list = db.getGraph().seguidoresQueViramConteudo(u, c,
                    LocalDateTime.of(2024, 1, 1, 0, 0), LocalDateTime.of(2024, 12, 31, 23, 59),
                    db.follows(), db.users());
            if (list.isEmpty()) output.setText("[R8g] Nenhum seguidor de " + u + " viu " + c + " em 2024.");
            else {
                StringBuilder sb = new StringBuilder("[R8g] Seguidores de " + u + " que viram " + c + ":\n");
                list.forEach(usr -> sb.append("  • ").append(usr.getName()).append("\n"));
                output.setText(sb.toString());
            }
        });
        r8g.getChildren().addAll(new HBox(8, lbl("User ID:"), fUId), new HBox(8, lbl("Content ID:"), fCId), bG);

        Button bInfo = btn("ℹ  Info do Grafo", BTN_S);
        bInfo.setOnAction(e -> output.setText(String.format("[GRAFO] Vértices: %d | Arestas: %d",
                db.getGraph().totalVertices(), db.getGraph().totalArestas())));

        sidebar.getChildren().addAll(r8a, r8c, r8g, bInfo);
        pane.setCenter(output);
        pane.setRight(scroll(sidebar));
        tab.setContent(pane);
        return tab;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 5. TAB MINHA CONTA
    // ═══════════════════════════════════════════════════════════════════════
    private Tab buildMinhaContaTab() {
        Tab tab = new Tab("🙋  Minha Conta");
        ScrollPane rootScroll = new ScrollPane();
        rootScroll.setFitToWidth(true);
        rootScroll.setStyle("-fx-background-color:" + C_BG + ";-fx-background:" + C_BG + ";");

        VBox main = new VBox(18);
        main.setPadding(new Insets(20));
        main.setStyle("-fx-background-color:" + C_BG + ";");

        // Hero
        HBox hero = new HBox(20);
        hero.setAlignment(Pos.CENTER_LEFT);
        hero.setPadding(new Insets(20));
        hero.setStyle(
                "-fx-background-color:" + C_SURFACE + ";" +
                        "-fx-border-color:" + C_ACCENT + ";-fx-border-width:0 0 0 4;" +
                        "-fx-border-radius:10;-fx-background-radius:10;"
        );

        Label avatar = new Label(initials(loggedUser.getName()));
        avatar.setStyle(
                "-fx-background-color:" + C_ACCENT + ";-fx-text-fill:white;" +
                        "-fx-font-size:28px;-fx-font-weight:bold;" +
                        "-fx-min-width:70;-fx-min-height:70;-fx-max-width:70;-fx-max-height:70;" +
                        "-fx-alignment:center;-fx-background-radius:35;"
        );

        VBox heroInfo = new VBox(6);
        Label heroName = new Label(loggedUser.getName());
        heroName.setStyle("-fx-text-fill:" + C_TEXT + ";-fx-font-size:22px;-fx-font-weight:bold;");
        Label heroSub = new Label(loggedUser.getId() + "  •  " + loggedUser.getEmail() + "  •  " + loggedUser.getRegion());
        heroSub.setStyle("-fx-text-fill:" + C_MUTED + ";-fx-font-size:13px;");
        Label heroDate = new Label("Membro desde " + loggedUser.getRegisterDate());
        heroDate.setStyle("-fx-text-fill:" + C_MUTED + ";-fx-font-size:12px;");

        Runnable refreshHero = () -> {
            User u = db.users().get(loggedUser.getId());
            if (u == null) return;
            heroName.setText(u.getName());
            heroSub.setText(u.getId() + "  •  " + u.getEmail() + "  •  " + u.getRegion());
            avatar.setText(initials(u.getName()));
        };

        int totalWatched  = (int) loggedUser.getInteractions().stream().filter(i -> i.getType() == InterationType.WATCH).count();
        int totalRated    = (int) loggedUser.getInteractions().stream().filter(i -> i.getType() == InterationType.RATE).count();
        int totalFollowing = db.follows().getFollowing(loggedUser.getId()).size();
        int totalFollowers = db.follows().getFollowers(loggedUser.getId()).size();

        HBox stats = new HBox(20);
        stats.setAlignment(Pos.CENTER_LEFT);
        stats.setPadding(new Insets(10, 0, 0, 0));
        stats.getChildren().addAll(
                statChip("▶", totalWatched,   "vistos"),
                statChip("⭐", totalRated,    "avaliados"),
                statChip("➜", totalFollowing, "a seguir"),
                statChip("👥", totalFollowers, "seguidores")
        );

        heroInfo.getChildren().addAll(heroName, heroSub, heroDate, stats);

        VBox editProfile = new VBox(8);
        editProfile.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(editProfile, Priority.ALWAYS);

        Button bEditNome  = btn("✏  Nome",      BTN_S);
        Button bEditEmail = btn("✉  Email",     BTN_S);
        Button bEditReg   = btn("🌍  Região",   BTN_S);
        Button bEditPwd   = btn("🔒  Password", BTN_G);
        bEditNome.setMaxWidth(Double.MAX_VALUE);
        bEditEmail.setMaxWidth(Double.MAX_VALUE);
        bEditReg.setMaxWidth(Double.MAX_VALUE);
        bEditPwd.setMaxWidth(Double.MAX_VALUE);

        bEditNome.setOnAction(e -> {
            String novo = askInput("Novo nome:", loggedUser.getName());
            if (novo == null || novo.trim().isEmpty()) return;
            if (db.users().editName(loggedUser.getId(), novo.trim())) {
                refreshHero.run(); AppStateSerializer.save(db); snack("✓ Nome atualizado", true);
            }
        });
        bEditEmail.setOnAction(e -> {
            String novo = askInput("Novo email:", loggedUser.getEmail());
            if (novo == null || novo.trim().isEmpty()) return;
            for (User u : db.users().listAll()) {
                if (!u.getId().equals(loggedUser.getId()) && u.getEmail().equalsIgnoreCase(novo.trim())) {
                    snack("✗ Email já está em uso por " + u.getName(), false); return;
                }
            }
            if (db.users().editEmail(loggedUser.getId(), novo.trim())) {
                refreshHero.run(); AppStateSerializer.save(db); snack("✓ Email atualizado", true);
            }
        });
        bEditReg.setOnAction(e -> {
            String nova = askInput("Nova região:", loggedUser.getRegion());
            if (nova == null || nova.trim().isEmpty()) return;
            if (db.users().editRegion(loggedUser.getId(), nova.trim())) {
                refreshHero.run(); AppStateSerializer.save(db); snack("✓ Região atualizada", true);
            }
        });
        bEditPwd.setOnAction(e -> {
            PasswordField fAtual = new PasswordField(); fAtual.setPromptText("Password atual"); fAtual.setStyle(FIELD);
            PasswordField fNova  = new PasswordField(); fNova.setPromptText("Nova password");   fNova.setStyle(FIELD);
            PasswordField fConf  = new PasswordField(); fConf.setPromptText("Confirmar");        fConf.setStyle(FIELD);
            VBox content = new VBox(8, lbl("Password atual:"), fAtual, lbl("Nova password:"), fNova, lbl("Confirmar:"), fConf);
            content.setPadding(new Insets(10));
            Dialog<ButtonType> dlg = new Dialog<>();
            dlg.setTitle("Alterar Password");
            dlg.getDialogPane().setContent(content);
            dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            if (dlg.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
            if (db.authenticate(loggedUser.getId(), fAtual.getText()) == null) { snack("✗ Password atual incorreta", false); return; }
            if (fNova.getText().isEmpty()) { snack("✗ Nova password vazia", false); return; }
            if (!fNova.getText().equals(fConf.getText())) { snack("✗ Passwords não coincidem", false); return; }
            db.changePassword(loggedUser.getId(), fNova.getText());
            AppStateSerializer.save(db);
            snack("✓ Password alterada com sucesso", true);
        });

        editProfile.getChildren().addAll(bEditNome, bEditEmail, bEditReg, bEditPwd);
        hero.getChildren().addAll(avatar, heroInfo, editProfile);

        // Linha 1: Interações + Recomendações
        HBox row1 = new HBox(14);
        row1.setPrefHeight(260);

        VBox interCard = card("🎬  Histórico de Interações");
        VBox.setVgrow(interCard, Priority.ALWAYS);
        HBox.setHgrow(interCard, Priority.ALWAYS);
        TableView<Interation> tInter = new TableView<>();
        tInter.setStyle("-fx-background-color:" + C_SURFACE + ";");
        tInter.getColumns().addAll(
                col("Conteúdo",  d -> d.getValue().getContent().getTitle()),
                col("Tipo",      d -> d.getValue().getType().toString()),
                col("Rating",    d -> d.getValue().getType() == InterationType.RATE ? String.format("%.0f ⭐", d.getValue().getRating()) : "—"),
                col("Progresso", d -> d.getValue().getType() == InterationType.WATCH ? String.format("%.0f%%", d.getValue().getProgress() * 100) : "—"),
                col("Data",      d -> d.getValue().getWatchDate().toLocalDate().toString())
        );
        tInter.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tInter.setItems(FXCollections.observableArrayList(loggedUser.getInteractions()));
        tInter.setPlaceholder(new Label("Sem interações registadas."));
        VBox.setVgrow(tInter, Priority.ALWAYS);
        interCard.getChildren().add(tInter);

        VBox recomCard = card("💡  Recomendações (R8d)");
        recomCard.setPrefWidth(270);
        List<Content> recomendacoes = db.getGraph().recomendarConteudosPorProximidade(
                loggedUser.getId(), db.follows(), db.users());
        if (recomendacoes.isEmpty()) {
            Label empty = new Label("Segue outros utilizadores\npara receber recomendações.");
            empty.setStyle("-fx-text-fill:" + C_MUTED + ";-fx-font-size:12px;");
            recomCard.getChildren().add(empty);
        } else {
            for (Content c : recomendacoes) {
                HBox chip = new HBox(8);
                chip.setAlignment(Pos.CENTER_LEFT);
                chip.setPadding(new Insets(8, 10, 8, 10));
                chip.setStyle("-fx-background-color:#14141E;-fx-border-color:" + C_BORDER + ";-fx-border-radius:6;-fx-background-radius:6;");
                String tipo = c instanceof Movie ? "🎬" : c instanceof Series ? "📺" : "🎥";
                Label lTipo = new Label(tipo);
                VBox lInfo = new VBox(2);
                Label lTit = new Label(c.getTitle());
                lTit.setStyle("-fx-text-fill:" + C_TEXT + ";-fx-font-size:12px;-fx-font-weight:bold;");
                Label lGen = new Label(c.getGenre().getName() + "  •  " + String.format("%.1f ⭐", c.getRating()));
                lGen.setStyle("-fx-text-fill:" + C_MUTED + ";-fx-font-size:11px;");
                lInfo.getChildren().addAll(lTit, lGen);
                chip.getChildren().addAll(lTipo, lInfo);
                recomCard.getChildren().add(chip);
            }
        }
        row1.getChildren().addAll(interCard, recomCard);

        // Linha 2: Follows + Preferências
        HBox row2 = new HBox(14);
        VBox followCard = card("➜  A Seguir");
        HBox.setHgrow(followCard, Priority.ALWAYS);
        List<User> seguindo = db.follows().getFollowing(loggedUser.getId());
        if (seguindo.isEmpty()) {
            Label empty = new Label("Ainda não segues ninguém.");
            empty.setStyle("-fx-text-fill:" + C_MUTED + ";-fx-font-size:12px;");
            followCard.getChildren().add(empty);
        } else {
            for (User u : seguindo) followCard.getChildren().add(userChip(u));
        }

        VBox followersCard = card("👥  Seguidores");
        HBox.setHgrow(followersCard, Priority.ALWAYS);
        List<User> seguidores = db.follows().getFollowers(loggedUser.getId());
        if (seguidores.isEmpty()) {
            Label empty = new Label("Ainda não tens seguidores.");
            empty.setStyle("-fx-text-fill:" + C_MUTED + ";-fx-font-size:12px;");
            followersCard.getChildren().add(empty);
        } else {
            for (User u : seguidores) followersCard.getChildren().add(userChip(u));
        }

        VBox prefCard = card("🎭  Géneros Preferidos");
        prefCard.setPrefWidth(220);
        List<Genre> prefs = loggedUser.getPreferences();
        if (prefs.isEmpty()) {
            Label empty = new Label("Sem géneros preferidos\nregistados.");
            empty.setStyle("-fx-text-fill:" + C_MUTED + ";-fx-font-size:12px;");
            prefCard.getChildren().add(empty);
        } else {
            FlowPane fp = new FlowPane(8, 8);
            for (Genre g : prefs) {
                Label chip = new Label("  " + g.getName() + "  ");
                chip.setStyle(
                        "-fx-background-color:#2a1111;-fx-text-fill:" + C_ACCENT + ";" +
                                "-fx-border-color:" + C_ACCENT + ";-fx-border-radius:20;-fx-background-radius:20;" +
                                "-fx-font-size:11px;-fx-padding:4 8;"
                );
                fp.getChildren().add(chip);
            }
            prefCard.getChildren().add(fp);
        }
        row2.getChildren().addAll(followCard, followersCard, prefCard);

        // Linha 3: Registar interação
        VBox watchCard = card("➕  Registar Interação");
        GridPane wForm = grid();
        ComboBox<String> cbContentId = new ComboBox<>();
        cbContentId.setStyle(FIELD + "-fx-pref-width:200px;");
        cbContentId.setPromptText("Seleciona conteúdo...");
        db.contents().listAll().forEach(c -> cbContentId.getItems().add(c.getId() + " — " + c.getTitle()));
        ComboBox<InterationType> cbType = new ComboBox<>(
                FXCollections.observableArrayList(InterationType.WATCH, InterationType.BOOKMARK, InterationType.SKIP));
        cbType.setValue(InterationType.WATCH);
        cbType.setStyle(FIELD);
        TextField fProgress = field("0.0 – 1.0 (progresso)");
        fProgress.setText("1.0");
        wForm.addRow(0, lbl("Conteúdo:"), cbContentId, lbl("Tipo:"), cbType);
        wForm.addRow(1, lbl("Progresso:"), fProgress);
        Button bWatch = btn("Registar", BTN_P);
        bWatch.setOnAction(e -> {
            String sel = cbContentId.getValue();
            if (sel == null) { showAlert(Alert.AlertType.ERROR, "Erro", "Seleciona um conteúdo."); return; }
            String contentId = sel.split(" — ")[0].trim();
            Content c = db.contents().get(contentId);
            if (c == null) { showAlert(Alert.AlertType.ERROR, "Erro", "Conteúdo não encontrado."); return; }
            double progress = 1.0;
            try { progress = Double.parseDouble(fProgress.getText().trim()); } catch (Exception ignored) {}
            progress = Math.max(0.0, Math.min(1.0, progress));
            String iId = "i_" + loggedUser.getId() + "_" + contentId + "_" + System.currentTimeMillis();
            Interation inter = new Interation(loggedUser, c, LocalDateTime.now(), 0.0, progress, cbType.getValue(), iId);
            db.addInteraction(inter);
            tInter.setItems(FXCollections.observableArrayList(loggedUser.getInteractions()));
            snack("✓ " + cbType.getValue() + " registado para \"" + c.getTitle() + "\"", true);
        });
        watchCard.getChildren().addAll(wForm, row(bWatch));

        main.getChildren().addAll(hero, row1, row2, watchCard);
        rootScroll.setContent(main);
        tab.setContent(rootScroll);
        return tab;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // HELPERS VISUAIS
    // ═══════════════════════════════════════════════════════════════════════

    private String initials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    private String avatarColor(String id) {
        String[] palette = {
                "#E53935", "#8E24AA", "#1E88E5", "#00897B",
                "#F4511E", "#6D4C41", "#039BE5", "#43A047"
        };
        return palette[Math.abs(id.hashCode()) % palette.length];
    }

    private String followBtnStyle(boolean isMe, boolean jaSegue) {
        if (isMe)
            return "-fx-background-color:#2a2a3a;-fx-text-fill:" + C_MUTED + ";" +
                    "-fx-border-radius:20;-fx-background-radius:20;-fx-padding:6 14;-fx-font-size:12px;";
        if (jaSegue)
            return "-fx-background-color:#1a3a1a;-fx-text-fill:" + C_SUCCESS + ";" +
                    "-fx-border-color:" + C_SUCCESS + ";" +
                    "-fx-border-radius:20;-fx-background-radius:20;-fx-padding:6 14;" +
                    "-fx-font-size:12px;-fx-cursor:hand;";
        return "-fx-background-color:" + C_ACCENT + ";-fx-text-fill:white;" +
                "-fx-border-radius:20;-fx-background-radius:20;-fx-padding:6 14;" +
                "-fx-font-size:12px;-fx-font-weight:bold;-fx-cursor:hand;";
    }

    private Label badge(String text, String bg, String fg) {
        Label l = new Label(text);
        l.setStyle(
                "-fx-background-color:" + bg + ";-fx-text-fill:" + fg + ";" +
                        "-fx-font-size:11px;-fx-padding:2 8;-fx-background-radius:20;"
        );
        return l;
    }

    private HBox miniStat(String icon, int value, String label) {
        Label lv = new Label(icon + " " + value);
        lv.setStyle("-fx-text-fill:" + C_TEXT + ";-fx-font-size:12px;-fx-font-weight:bold;");
        Label ll = new Label(label);
        ll.setStyle("-fx-text-fill:" + C_MUTED + ";-fx-font-size:10px;");
        HBox h = new HBox(4, lv, ll);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    private VBox statChip(String icon, int value, String label) {
        VBox v = new VBox(2);
        v.setAlignment(Pos.CENTER);
        v.setPadding(new Insets(8, 14, 8, 14));
        v.setStyle("-fx-background-color:#14141E;-fx-border-color:" + C_BORDER + ";-fx-border-radius:8;-fx-background-radius:8;");
        Label lIcon = new Label(icon + " " + value);
        lIcon.setStyle("-fx-text-fill:" + C_TEXT + ";-fx-font-size:16px;-fx-font-weight:bold;");
        Label lLbl  = new Label(label);
        lLbl.setStyle("-fx-text-fill:" + C_MUTED + ";-fx-font-size:10px;");
        v.getChildren().addAll(lIcon, lLbl);
        return v;
    }

    private HBox userChip(User u) {
        HBox chip = new HBox(10);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setPadding(new Insets(8, 12, 8, 12));
        chip.setStyle(
                "-fx-background-color:#14141E;" +
                        "-fx-border-color:" + C_BORDER + ";-fx-border-radius:8;-fx-background-radius:8;-fx-cursor:hand;"
        );
        Label av = new Label(initials(u.getName()));
        av.setStyle(
                "-fx-background-color:" + C_BORDER + ";-fx-text-fill:" + C_TEXT + ";" +
                        "-fx-font-size:11px;-fx-font-weight:bold;" +
                        "-fx-min-width:30;-fx-min-height:30;-fx-max-width:30;-fx-max-height:30;" +
                        "-fx-alignment:center;-fx-background-radius:15;"
        );
        VBox info = new VBox(2);
        Label lName = new Label(u.getName());
        lName.setStyle("-fx-text-fill:" + C_TEXT + ";-fx-font-size:12px;-fx-font-weight:bold;");
        Label lId = new Label(u.getId() + "  •  " + u.getRegion());
        lId.setStyle("-fx-text-fill:" + C_MUTED + ";-fx-font-size:10px;");
        info.getChildren().addAll(lName, lId);
        chip.getChildren().addAll(av, info);
        return chip;
    }

    // ── Helpers base ──────────────────────────────────────────────────────
    private VBox card(String title) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(14));
        box.setStyle("-fx-background-color:" + C_SURFACE + ";-fx-border-color:" + C_BORDER + ";-fx-border-radius:10;-fx-background-radius:10;");
        Label lbl = new Label(title);
        lbl.setStyle("-fx-text-fill:" + C_ACCENT + ";-fx-font-weight:bold;-fx-font-size:12px;");
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color:" + C_BORDER + ";");
        box.getChildren().addAll(lbl, sep);
        return box;
    }

    private <T> TableColumn<T, String> col(String h, java.util.function.Function<TableColumn.CellDataFeatures<T, String>, String> fn) {
        TableColumn<T, String> c = new TableColumn<>(h);
        c.setCellValueFactory(d -> new SimpleStringProperty(fn.apply(d)));
        return c;
    }

    private TextField    field(String p) { TextField f = new TextField(); f.setPromptText(p); f.setStyle(FIELD); return f; }
    private PasswordField  pwd(String p) { PasswordField f = new PasswordField(); f.setPromptText(p); f.setStyle(FIELD); return f; }
    private Button         btn(String t, String s) { Button b = new Button(t); b.setStyle(s); return b; }
    private Label          lbl(String t) { Label l = new Label(t); l.setStyle("-fx-text-fill:" + C_MUTED + ";-fx-font-size:12px;"); return l; }
    private GridPane      grid() { GridPane g = new GridPane(); g.setHgap(10); g.setVgap(10); return g; }
    private HBox           row(javafx.scene.Node... nodes) { HBox h = new HBox(8, nodes); h.setAlignment(Pos.CENTER_LEFT); return h; }
    private ScrollPane    scroll(javafx.scene.Node n) { ScrollPane s = new ScrollPane(n); s.setFitToWidth(true); s.setStyle("-fx-background-color:" + C_BG + ";-fx-background:" + C_BG + ";"); return s; }
    private void    showAlert(Alert.AlertType t, String title, String msg) { Alert a = new Alert(t); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait(); }
    private String  askInput(String header, String def) { TextInputDialog d = new TextInputDialog(def); d.setTitle("Editar"); d.setHeaderText(header); return d.showAndWait().orElse(null); }
}