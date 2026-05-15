package edu.ufp.streaming.rec.gui;

import edu.ufp.streaming.rec.enums.ArtistRole;
import edu.ufp.streaming.rec.managers.AppStateSerializer;
import edu.ufp.streaming.rec.managers.StreamingDatabase;
import edu.ufp.streaming.rec.models.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Painel de administração da plataforma de streaming.
 * Só acessível a utilizadores com {@code isAdmin() == true}.
 *
 * Funcionalidades:
 *  - Gerir Utilizadores (listar, criar, editar, remover, promover a admin)
 *  - Gerir Conteúdos (listar, criar, editar título/rating/duração, remover)
 *  - Gerir Artistas (listar, criar, editar, remover)
 *  - Gerir Géneros (listar, criar, remover)
 *
 * @author Diogo Vicente
 */
public class AdminDashboardFX {

    // ── Paleta ────────────────────────────────────────────────────────────────
    private static final String N_BG     = "#0d0d0d";
    private static final String N_CARD   = "#1A1A1A";
    private static final String N_CARD2  = "#252525";
    private static final String N_RED    = "#E50914";
    private static final String N_GOLD   = "#F5A623";
    private static final String N_TEXT   = "#FFFFFF";
    private static final String N_MUTED  = "#888888";
    private static final String N_INPUT  = "#2A2A2A";
    private static final String N_BORDER = "#3A3A3A";
    private static final String N_GREEN  = "#46D369";
    private static final String N_BLUE   = "#185FA5";

    // ── Estilos reutilizáveis ─────────────────────────────────────────────────
    private static final String FIELD =
            "-fx-background-color:" + N_INPUT + ";-fx-text-fill:" + N_TEXT + ";" +
                    "-fx-prompt-text-fill:" + N_MUTED + ";-fx-border-color:" + N_BORDER + ";" +
                    "-fx-border-radius:4;-fx-background-radius:4;-fx-padding:10 12;-fx-font-size:13px;";

    private static final String BTN_R =
            "-fx-background-color:" + N_RED + ";-fx-text-fill:white;" +
                    "-fx-font-weight:bold;-fx-background-radius:4;-fx-padding:9 18;-fx-cursor:hand;";

    private static final String BTN_G =
            "-fx-background-color:" + N_RED + ";-fx-text-fill:white;" +
                    "-fx-font-weight:bold;-fx-background-radius:4;-fx-padding:9 18;-fx-cursor:hand;";

    private static final String BTN_S =
            "-fx-background-color:" + N_CARD2 + ";-fx-text-fill:" + N_TEXT + ";" +
                    "-fx-border-color:" + N_BORDER + ";-fx-border-radius:4;-fx-background-radius:4;" +
                    "-fx-padding:9 14;-fx-cursor:hand;";

    private static final String BTN_DANGER =
            "-fx-background-color:transparent;-fx-text-fill:#FF5252;" +
                    "-fx-border-color:#FF5252;-fx-border-radius:4;-fx-background-radius:4;" +
                    "-fx-padding:6 12;-fx-cursor:hand;-fx-font-size:11px;";

    private static final String BTN_SMALL =
            "-fx-background-color:" + N_CARD2 + ";-fx-text-fill:" + N_TEXT + ";" +
                    "-fx-border-color:" + N_BORDER + ";-fx-border-radius:4;-fx-background-radius:4;" +
                    "-fx-padding:6 12;-fx-cursor:hand;-fx-font-size:11px;";

    private final StreamingDatabase db;
    private final User adminUser;
    private double xOff, yOff;

    private final Label snackLabel = new Label();
    private javafx.animation.PauseTransition snackTimer;

    public AdminDashboardFX(StreamingDatabase db, User adminUser) {
        this.db        = db;
        this.adminUser = adminUser;
    }

    public void start(Stage oldStage) {
        oldStage.close();
        Stage stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + N_BG + ";-fx-background-radius:10;");

        root.setTop(buildNavBar(stage));

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle("-fx-background-color:" + N_BG + ";");
        tabs.getTabs().addAll(
                buildUsersTab(),
                buildContentsTab(),
                buildArtistsTab(),
                buildGenresTab()
        );

        root.setCenter(tabs);
        root.setBottom(buildStatusBar());

        root.setOnMousePressed(ev -> { xOff = ev.getSceneX(); yOff = ev.getSceneY(); });
        root.setOnMouseDragged(ev -> { stage.setX(ev.getScreenX()-xOff); stage.setY(ev.getScreenY()-yOff); });

        String css =
                ".tab-pane .tab-header-area .tab-header-background{-fx-background-color:" + N_BG + ";}" +
                        ".tab-pane .tab{-fx-background-color:" + N_BG + ";-fx-padding:10 20;-fx-background-radius:0;}" +
                        ".tab-pane .tab:selected{-fx-background-color:" + N_BG + ";-fx-border-color:" + N_GOLD + ";-fx-border-width:0 0 3 0;}" +
                        ".tab .tab-label{-fx-text-fill:" + N_MUTED + ";-fx-font-size:13px;-fx-font-weight:bold;}" +
                        ".tab:selected .tab-label{-fx-text-fill:" + N_TEXT + ";}" +
                        ".tab:hover .tab-label{-fx-text-fill:" + N_TEXT + ";}" +
                        ".tab-pane .tab-content-area{-fx-background-color:" + N_BG + ";}" +
                        ".table-view{-fx-background-color:" + N_CARD + ";-fx-border-color:" + N_BORDER + ";}" +
                        ".table-view .column-header-background{-fx-background-color:" + N_BG + ";}" +
                        ".table-view .column-header,.table-view .filler{-fx-background-color:" + N_BG + ";-fx-border-color:" + N_BORDER + ";}" +
                        ".table-view .column-header .label{-fx-text-fill:" + N_MUTED + ";-fx-font-weight:bold;-fx-font-size:11px;}" +
                        ".table-row-cell{-fx-background-color:" + N_CARD + ";-fx-border-color:" + N_BORDER + ";}" +
                        ".table-row-cell:odd{-fx-background-color:#141414;}" +
                        ".table-row-cell:selected{-fx-background-color:#2a2200;}" +
                        ".table-row-cell:hover{-fx-background-color:#1e1e1e;}" +
                        ".table-cell{-fx-text-fill:" + N_TEXT + ";-fx-font-size:13px;}" +
                        ".scroll-pane{-fx-background-color:" + N_BG + ";-fx-background:" + N_BG + ";}" +
                        ".scroll-pane > .viewport{-fx-background-color:" + N_BG + ";}" +
                        ".scroll-bar{-fx-background-color:" + N_BG + ";}" +
                        ".scroll-bar .thumb{-fx-background-color:" + N_BORDER + ";-fx-background-radius:4;}";

        Scene scene = new Scene(root, 1280, 820);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add("data:text/css," +
                java.net.URLEncoder.encode(css, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20"));

        stage.setScene(scene);
        stage.centerOnScreen();
        stage.setOnCloseRequest(e -> AppStateSerializer.save(db));
        stage.show();
    }

    // =========================================================================
    // NAVBAR
    // =========================================================================

    private HBox buildNavBar(Stage stage) {
        HBox bar = new HBox(24);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 24, 0, 24));
        bar.setPrefHeight(64);
        bar.setStyle("-fx-background-color:" + N_BG + ";-fx-border-color:" + N_BORDER + ";-fx-border-width:0 0 1 0;");

        Label logo = new Label("STREAMINGAPP");
        logo.setStyle("-fx-text-fill:" + N_RED + ";-fx-font-size:22px;-fx-font-weight:bold;-fx-font-family:'Georgia';");

        Label adminBadge = new Label("⚙  ADMIN");
        adminBadge.setStyle("-fx-background-color:" + N_GOLD + ";-fx-text-fill:#111;-fx-font-size:11px;-fx-font-weight:bold;-fx-padding:4 10;-fx-background-radius:20;");

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Label avatar = new Label(initials(adminUser.getName()));
        avatar.setStyle("-fx-background-color:" + N_GOLD + ";-fx-text-fill:#111;-fx-font-size:13px;-fx-font-weight:bold;-fx-min-width:34;-fx-min-height:34;-fx-max-width:34;-fx-max-height:34;-fx-alignment:center;-fx-background-radius:4;");
        Label userName = new Label(adminUser.getName());
        userName.setStyle("-fx-text-fill:" + N_TEXT + ";-fx-font-size:13px;");

        Button btnLogout = new Button("Sair");
        btnLogout.setStyle("-fx-background-color:transparent;-fx-text-fill:" + N_MUTED + ";-fx-font-size:13px;-fx-cursor:hand;");
        btnLogout.setOnAction(e -> {
            AppStateSerializer.save(db);
            stage.close();
            Stage ls = new Stage();
            new LoginScreenFX(db, ls, u -> {
                if (u.isAdmin()) new AdminDashboardFX(db, u).start(ls);
                else             new StreamingDashboardFX(db, u).start(ls);
            });
        });

        Button btnX = new Button("X");
        btnX.setStyle("-fx-background-color:transparent;-fx-text-fill:" + N_MUTED + ";-fx-font-size:14px;-fx-cursor:hand;");
        btnX.setOnAction(e -> System.exit(0));

        bar.getChildren().addAll(logo, adminBadge, spacer, avatar, userName, btnLogout, btnX);
        return bar;
    }

    // =========================================================================
    // STATUS BAR
    // =========================================================================

    private VBox buildStatusBar() {
        HBox info = new HBox();
        info.setPadding(new Insets(6, 24, 0, 24));
        Label l = new Label("PAINEL DE ADMINISTRAÇÃO | LP2 / AED2 | UFP | " + adminUser.getId());
        l.setStyle("-fx-text-fill:" + N_GOLD + ";-fx-font-size:11px;-fx-font-weight:bold;");
        info.getChildren().add(l);

        snackLabel.setVisible(false);
        snackLabel.setStyle("-fx-font-size:12px;-fx-font-weight:bold;");
        HBox snackRow = new HBox(snackLabel);
        snackRow.setAlignment(Pos.CENTER);
        snackRow.setPadding(new Insets(3, 24, 6, 24));

        VBox bar = new VBox(info, snackRow);
        bar.setStyle("-fx-background-color:#050505;-fx-border-color:" + N_BORDER + ";-fx-border-width:1 0 0 0;");
        return bar;
    }

    private void snack(String msg, boolean ok) {
        snackLabel.setText(msg);
        snackLabel.setStyle("-fx-text-fill:" + (ok ? N_GREEN : "#FF5252") + ";-fx-font-size:12px;-fx-font-weight:bold;");
        snackLabel.setVisible(true);
        if (snackTimer != null) snackTimer.stop();
        snackTimer = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
        snackTimer.setOnFinished(e -> snackLabel.setVisible(false));
        snackTimer.play();
    }

    // =========================================================================
    // ABA: UTILIZADORES
    // =========================================================================

    private Tab buildUsersTab() {
        Tab tab = new Tab("👥  Utilizadores");
        BorderPane pane = new BorderPane();
        pane.setStyle("-fx-background-color:" + N_BG + ";");
        pane.setPadding(new Insets(20));

        // Tabela
        javafx.scene.control.TableView<User> table = new javafx.scene.control.TableView<>();
        table.setStyle("-fx-background-color:" + N_CARD + ";");
        table.setColumnResizePolicy(javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY);

        javafx.scene.control.TableColumn<User,String> cId    = col("ID",       d -> d.getValue().getId());
        javafx.scene.control.TableColumn<User,String> cName  = col("Nome",     d -> d.getValue().getName());
        javafx.scene.control.TableColumn<User,String> cEmail = col("Email",    d -> d.getValue().getEmail());
        javafx.scene.control.TableColumn<User,String> cReg   = col("Região",   d -> d.getValue().getRegion());
        javafx.scene.control.TableColumn<User,String> cDate  = col("Registo",  d -> d.getValue().getRegisterDate().toString());
        javafx.scene.control.TableColumn<User,String> cAdmin = col("Admin",    d -> d.getValue().isAdmin() ? "✔ SIM" : "—");

        table.getColumns().addAll(cId, cName, cEmail, cReg, cDate, cAdmin);

        Runnable refresh = () -> {
            table.getItems().setAll(db.users().listAll());
        };
        refresh.run();

        // Sidebar
        VBox sidebar = new VBox(16);
        sidebar.setPrefWidth(320);
        sidebar.setPadding(new Insets(0, 0, 0, 20));

        // Pesquisa
        VBox searchCard = card("Pesquisar");
        TextField fSearch = field("Nome ou ID...");
        Button bSearch = btn("Pesquisar", BTN_G), bAll = btn("Todos", BTN_S);
        bSearch.setOnAction(e -> {
            String q = fSearch.getText().trim();
            if (q.isEmpty()) { refresh.run(); return; }
            table.getItems().setAll(db.users().searchByNameSubstring(q));
        });
        bAll.setOnAction(e -> { fSearch.clear(); refresh.run(); });
        searchCard.getChildren().add(row(fSearch, bSearch, bAll));

        // Criar utilizador
        VBox createCard = card("Criar Utilizador");
        TextField cUId     = field("ID (ex: u10)");
        TextField cUName   = field("Nome completo");
        TextField cUEmail  = field("Email");
        TextField cURegion = field("Região (PT)");
        PasswordField cUPwd = pwd("Password");
        CheckBox cUAdmin   = new CheckBox("Administrador");
        cUAdmin.setStyle("-fx-text-fill:" + N_TEXT + ";-fx-font-size:13px;");
        Button bCreate = btn("Criar", BTN_G);
        bCreate.setMaxWidth(Double.MAX_VALUE);
        bCreate.setOnAction(e -> {
            String id = cUId.getText().trim(), nome = cUName.getText().trim();
            String email = cUEmail.getText().trim(), regiao = cURegion.getText().trim();
            String pwd = cUPwd.getText();
            if (id.isEmpty() || nome.isEmpty() || pwd.isEmpty()) { snack("ID, Nome e Password são obrigatórios", false); return; }
            if (db.users().contains(id)) { snack("ID '" + id + "' já existe", false); return; }
            User novo = new User(id, nome, email, regiao.isEmpty() ? "PT" : regiao.toUpperCase(), LocalDate.now(), pwd);
            novo.setAdmin(cUAdmin.isSelected());
            db.addUser(novo);
            AppStateSerializer.save(db);
            refresh.run();
            cUId.clear(); cUName.clear(); cUEmail.clear(); cURegion.clear(); cUPwd.clear(); cUAdmin.setSelected(false);
            snack("Utilizador '" + id + "' criado", true);
        });
        createCard.getChildren().addAll(cUId, cUName, cUEmail, cURegion, cUPwd, cUAdmin, bCreate);

        // Ações sobre selecionado
        VBox actionsCard = card("Ações sobre Selecionado");
        Button bEditName   = btn("Editar Nome",    BTN_S);
        Button bEditEmail  = btn("Editar Email",   BTN_S);
        Button bEditRegion = btn("Editar Região",  BTN_S);
        Button bToggleAdmin = btn("Promover/Revogar Admin", BTN_G);
        Button bRemove     = btn("Remover Utilizador", BTN_DANGER);

        bEditName.setMaxWidth(Double.MAX_VALUE);
        bEditEmail.setMaxWidth(Double.MAX_VALUE);
        bEditRegion.setMaxWidth(Double.MAX_VALUE);
        bToggleAdmin.setMaxWidth(Double.MAX_VALUE);
        bRemove.setMaxWidth(Double.MAX_VALUE);

        bEditName.setOnAction(e -> {
            User sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { snack("Seleciona um utilizador", false); return; }
            String nv = askInput("Novo nome:", sel.getName());
            if (nv == null || nv.trim().isEmpty()) return;
            db.users().editName(sel.getId(), nv.trim());
            AppStateSerializer.save(db); refresh.run();
            snack("Nome atualizado", true);
        });
        bEditEmail.setOnAction(e -> {
            User sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { snack("Seleciona um utilizador", false); return; }
            String nv = askInput("Novo email:", sel.getEmail());
            if (nv == null || nv.trim().isEmpty()) return;
            db.users().editEmail(sel.getId(), nv.trim());
            AppStateSerializer.save(db); refresh.run();
            snack("Email atualizado", true);
        });
        bEditRegion.setOnAction(e -> {
            User sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { snack("Seleciona um utilizador", false); return; }
            String nv = askInput("Nova região:", sel.getRegion());
            if (nv == null || nv.trim().isEmpty()) return;
            db.users().editRegion(sel.getId(), nv.trim().toUpperCase());
            AppStateSerializer.save(db); refresh.run();
            snack("Região atualizada", true);
        });
        bToggleAdmin.setOnAction(e -> {
            User sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { snack("Seleciona um utilizador", false); return; }
            if (sel.getId().equals(adminUser.getId())) { snack("Não podes alterar a tua própria conta", false); return; }
            sel.setAdmin(!sel.isAdmin());
            AppStateSerializer.save(db); refresh.run();
            snack(sel.isAdmin() ? sel.getName() + " é agora Admin" : sel.getName() + " já não é Admin", true);
        });
        bRemove.setOnAction(e -> {
            User sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { snack("Seleciona um utilizador", false); return; }
            if (sel.getId().equals(adminUser.getId())) { snack("Não podes remover a tua própria conta", false); return; }
            if (!confirm("Remover utilizador '" + sel.getName() + "'?")) return;
            db.removeUser(sel.getId());
            AppStateSerializer.save(db); refresh.run();
            snack("Utilizador removido", true);
        });

        actionsCard.getChildren().addAll(bEditName, bEditEmail, bEditRegion, bToggleAdmin, bRemove);
        sidebar.getChildren().addAll(searchCard, createCard, actionsCard);

        pane.setCenter(table);
        pane.setRight(scroll(sidebar));
        tab.setContent(pane);
        return tab;
    }

    // =========================================================================
    // ABA: CONTEÚDOS
    // =========================================================================

    private Tab buildContentsTab() {
        Tab tab = new Tab("🎬  Conteúdos");
        BorderPane pane = new BorderPane();
        pane.setStyle("-fx-background-color:" + N_BG + ";");
        pane.setPadding(new Insets(20));

        javafx.scene.control.TableView<Content> table = new javafx.scene.control.TableView<>();
        table.setStyle("-fx-background-color:" + N_CARD + ";");
        table.setColumnResizePolicy(javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY);

        javafx.scene.control.TableColumn<Content,String> cId    = col("ID",       d -> d.getValue().getId());
        javafx.scene.control.TableColumn<Content,String> cTitle = col("Título",   d -> d.getValue().getTitle());
        javafx.scene.control.TableColumn<Content,String> cType  = col("Tipo",     d -> d.getValue() instanceof Movie ? "Filme" : d.getValue() instanceof Series ? "Série" : "Doc");
        javafx.scene.control.TableColumn<Content,String> cGenre = col("Género",   d -> d.getValue().getGenre().getName());
        javafx.scene.control.TableColumn<Content,String> cYear  = col("Ano",      d -> String.valueOf(d.getValue().getReleaseDate().getYear()));
        javafx.scene.control.TableColumn<Content,String> cDur   = col("Dur.(min)",d -> String.valueOf(d.getValue().getDuration()));
        javafx.scene.control.TableColumn<Content,String> cRat   = col("Rating",   d -> String.format("%.1f", d.getValue().getRating()));
        javafx.scene.control.TableColumn<Content,String> cReg   = col("Região",   d -> d.getValue().getRegion());

        table.getColumns().addAll(cId, cTitle, cType, cGenre, cYear, cDur, cRat, cReg);

        Runnable refresh = () -> table.getItems().setAll(db.contents().listAll());
        refresh.run();

        // Sidebar
        VBox sidebar = new VBox(16);
        sidebar.setPrefWidth(340);
        sidebar.setPadding(new Insets(0, 0, 0, 20));

        // Pesquisa
        VBox searchCard = card("Pesquisar");
        TextField fSearch = field("Título...");
        Button bS = btn("Pesquisar", BTN_G), bA = btn("Todos", BTN_S);
        bS.setOnAction(e -> table.getItems().setAll(db.contents().searchByTitleSubstring(fSearch.getText().trim())));
        bA.setOnAction(e -> { fSearch.clear(); refresh.run(); });
        searchCard.getChildren().add(row(fSearch, bS, bA));

        // Criar conteúdo
        VBox createCard = card("Criar Conteúdo");
        TextField cCId    = field("ID (ex: c10)");
        TextField cCTitle = field("Título");
        ComboBox<String> cCType = new ComboBox<>();
        cCType.getItems().addAll("Filme", "Série", "Documentário");
        cCType.setValue("Filme");
        cCType.setStyle("-fx-background-color:" + N_INPUT + ";-fx-text-fill:" + N_TEXT + ";-fx-border-color:" + N_BORDER + ";-fx-border-radius:4;");
        cCType.setMaxWidth(Double.MAX_VALUE);
        ComboBox<Genre> cCGenre = new ComboBox<>();
        cCGenre.getItems().setAll(db.genres().listAll());
        cCGenre.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Genre g) { return g == null ? "" : g.getName(); }
            public Genre fromString(String s) { return null; }
        });
        cCGenre.setMaxWidth(Double.MAX_VALUE);
        cCGenre.setStyle("-fx-background-color:" + N_INPUT + ";-fx-text-fill:" + N_TEXT + ";-fx-border-color:" + N_BORDER + ";-fx-border-radius:4;");
        TextField cCDate  = field("Data lançamento (AAAA-MM-DD)");
        TextField cCDur   = field("Duração (minutos)");
        TextField cCReg   = field("Região (PT)");

        Button bCreate = btn("Criar", BTN_G);
        bCreate.setMaxWidth(Double.MAX_VALUE);
        bCreate.setOnAction(e -> {
            String id = cCId.getText().trim(), title = cCTitle.getText().trim();
            String region = cCReg.getText().trim();
            Genre genre = cCGenre.getValue();
            if (id.isEmpty() || title.isEmpty() || genre == null) { snack("ID, Título e Género são obrigatórios", false); return; }
            if (db.contents().get(id) != null) { snack("ID '" + id + "' já existe", false); return; }
            LocalDate date;
            try { date = LocalDate.parse(cCDate.getText().trim()); }
            catch (DateTimeParseException ex) { snack("Data inválida (usa AAAA-MM-DD)", false); return; }
            int dur;
            try { dur = Integer.parseInt(cCDur.getText().trim()); if (dur <= 0) throw new NumberFormatException(); }
            catch (NumberFormatException ex) { snack("Duração inválida", false); return; }
            Content novo = switch (cCType.getValue()) {
                case "Série"        -> new Series(id, title, genre, date, dur, region.isEmpty() ? "PT" : region.toUpperCase(), 1);
                case "Documentário" -> new Documentary(id, title, genre, date, dur, region.isEmpty() ? "PT" : region.toUpperCase(), "", "");
                default             -> new Movie(id, title, genre, date, dur, region.isEmpty() ? "PT" : region.toUpperCase(), null);
            };
            db.addContent(novo);
            AppStateSerializer.save(db); refresh.run();
            cCId.clear(); cCTitle.clear(); cCDate.clear(); cCDur.clear(); cCReg.clear();
            snack("Conteúdo '" + title + "' criado", true);
        });
        createCard.getChildren().addAll(cCId, cCTitle, cCType, cCGenre, cCDate, cCDur, cCReg, bCreate);

        // Ações
        VBox actCard = card("Ações sobre Selecionado");
        Button bEditTitle  = btn("Editar Título",    BTN_S);
        Button bEditDur    = btn("Editar Duração",   BTN_S);
        Button bEditRegion = btn("Editar Região",    BTN_S);
        Button bRemove     = btn("Remover Conteúdo", BTN_DANGER);
        for (Button b : List.of(bEditTitle, bEditDur, bEditRegion, bRemove)) b.setMaxWidth(Double.MAX_VALUE);

        bEditTitle.setOnAction(e -> {
            Content sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { snack("Seleciona um conteúdo", false); return; }
            String nv = askInput("Novo título:", sel.getTitle());
            if (nv == null || nv.trim().isEmpty()) return;
            db.contents().editTitle(sel.getId(), nv.trim());
            AppStateSerializer.save(db); refresh.run();
            snack("Título atualizado", true);
        });
        bEditDur.setOnAction(e -> {
            Content sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { snack("Seleciona um conteúdo", false); return; }
            String nv = askInput("Nova duração (min):", String.valueOf(sel.getDuration()));
            if (nv == null) return;
            try {
                int d = Integer.parseInt(nv.trim());
                sel.setDuration(d);
                AppStateSerializer.save(db); refresh.run();
                snack("Duração atualizada", true);
            } catch (Exception ex) { snack("Valor inválido", false); }
        });
        bEditRegion.setOnAction(e -> {
            Content sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { snack("Seleciona um conteúdo", false); return; }
            String nv = askInput("Nova região:", sel.getRegion());
            if (nv == null || nv.trim().isEmpty()) return;
            sel.setRegion(nv.trim().toUpperCase());
            AppStateSerializer.save(db); refresh.run();
            snack("Região atualizada", true);
        });
        bRemove.setOnAction(e -> {
            Content sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { snack("Seleciona um conteúdo", false); return; }
            if (!confirm("Remover '" + sel.getTitle() + "'?")) return;
            db.removeContent(sel.getId());
            AppStateSerializer.save(db); refresh.run();
            snack("Conteúdo removido", true);
        });
        actCard.getChildren().addAll(bEditTitle, bEditDur, bEditRegion, bRemove);

        sidebar.getChildren().addAll(searchCard, createCard, actCard);
        pane.setCenter(table);
        pane.setRight(scroll(sidebar));
        tab.setContent(pane);
        return tab;
    }

    // =========================================================================
    // ABA: ARTISTAS
    // =========================================================================

    private Tab buildArtistsTab() {
        Tab tab = new Tab("🎭  Artistas");
        BorderPane pane = new BorderPane();
        pane.setStyle("-fx-background-color:" + N_BG + ";");
        pane.setPadding(new Insets(20));

        javafx.scene.control.TableView<Artist> table = new javafx.scene.control.TableView<>();
        table.setStyle("-fx-background-color:" + N_CARD + ";");
        table.setColumnResizePolicy(javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY);

        javafx.scene.control.TableColumn<Artist,String> cId   = col("ID",          d -> d.getValue().getId());
        javafx.scene.control.TableColumn<Artist,String> cName = col("Nome",         d -> d.getValue().getName());
        javafx.scene.control.TableColumn<Artist,String> cRole = col("Função",       d -> d.getValue().getRole().toString());
        javafx.scene.control.TableColumn<Artist,String> cNat  = col("Nacionalidade",d -> d.getValue().getNationality());
        javafx.scene.control.TableColumn<Artist,String> cGen  = col("Género",       d -> d.getValue().getGender());
        javafx.scene.control.TableColumn<Artist,String> cDate = col("Nascimento",   d -> d.getValue().getBirthDate().toString());

        table.getColumns().addAll(cId, cName, cRole, cNat, cGen, cDate);

        Runnable refresh = () -> table.getItems().setAll(db.artists().listAll());
        refresh.run();

        // Sidebar
        VBox sidebar = new VBox(16);
        sidebar.setPrefWidth(320);
        sidebar.setPadding(new Insets(0, 0, 0, 20));

        VBox searchCard = card("Pesquisar");
        TextField fSearch = field("Nome...");
        Button bS = btn("Pesquisar", BTN_G), bA = btn("Todos", BTN_S);
        bS.setOnAction(e -> table.getItems().setAll(db.artists().searchByNameSubstring(fSearch.getText().trim())));
        bA.setOnAction(e -> { fSearch.clear(); refresh.run(); });
        searchCard.getChildren().add(row(fSearch, bS, bA));

        VBox createCard = card("Criar Artista");
        TextField cAId   = field("ID (ex: a10)");
        TextField cAName = field("Nome completo");
        TextField cANat  = field("Nacionalidade");
        ComboBox<String> cAGen = new ComboBox<>();
        cAGen.getItems().addAll("M", "F");
        cAGen.setValue("M");
        cAGen.setStyle("-fx-background-color:" + N_INPUT + ";-fx-text-fill:" + N_TEXT + ";-fx-border-color:" + N_BORDER + ";-fx-border-radius:4;");
        cAGen.setMaxWidth(Double.MAX_VALUE);
        ComboBox<ArtistRole> cARole = new ComboBox<>();
        cARole.getItems().addAll(ArtistRole.values());
        cARole.setValue(ArtistRole.ACTOR);
        cARole.setStyle("-fx-background-color:" + N_INPUT + ";-fx-text-fill:" + N_TEXT + ";-fx-border-color:" + N_BORDER + ";-fx-border-radius:4;");
        cARole.setMaxWidth(Double.MAX_VALUE);
        TextField cADate = field("Data nasc. (AAAA-MM-DD)");

        Button bCreate = btn("Criar", BTN_G);
        bCreate.setMaxWidth(Double.MAX_VALUE);
        bCreate.setOnAction(e -> {
            String id = cAId.getText().trim(), nome = cAName.getText().trim(), nat = cANat.getText().trim();
            if (id.isEmpty() || nome.isEmpty()) { snack("ID e Nome são obrigatórios", false); return; }
            if (db.artists().contains(id)) { snack("ID '" + id + "' já existe", false); return; }
            LocalDate date;
            try { date = LocalDate.parse(cADate.getText().trim()); }
            catch (DateTimeParseException ex) { snack("Data inválida (usa AAAA-MM-DD)", false); return; }
            Artist novo = new Artist(id, nome, nat.isEmpty() ? "PT" : nat, cAGen.getValue(), date, cARole.getValue());
            db.addArtist(novo);
            AppStateSerializer.save(db); refresh.run();
            cAId.clear(); cAName.clear(); cANat.clear(); cADate.clear();
            snack("Artista '" + nome + "' criado", true);
        });
        createCard.getChildren().addAll(cAId, cAName, cANat, cAGen, cARole, cADate, bCreate);

        VBox actCard = card("Ações sobre Selecionado");
        Button bEditName = btn("Editar Nome",        BTN_S);
        Button bEditNat  = btn("Editar Nacionalidade", BTN_S);
        Button bRemove   = btn("Remover Artista",    BTN_DANGER);
        for (Button b : List.of(bEditName, bEditNat, bRemove)) b.setMaxWidth(Double.MAX_VALUE);

        bEditName.setOnAction(e -> {
            Artist sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { snack("Seleciona um artista", false); return; }
            String nv = askInput("Novo nome:", sel.getName());
            if (nv == null || nv.trim().isEmpty()) return;
            db.artists().editName(sel.getId(), nv.trim());
            AppStateSerializer.save(db); refresh.run();
            snack("Nome atualizado", true);
        });
        bEditNat.setOnAction(e -> {
            Artist sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { snack("Seleciona um artista", false); return; }
            String nv = askInput("Nova nacionalidade:", sel.getNationality());
            if (nv == null || nv.trim().isEmpty()) return;
            db.artists().editNationality(sel.getId(), nv.trim());
            AppStateSerializer.save(db); refresh.run();
            snack("Nacionalidade atualizada", true);
        });
        bRemove.setOnAction(e -> {
            Artist sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { snack("Seleciona um artista", false); return; }
            if (!confirm("Remover artista '" + sel.getName() + "'?")) return;
            db.removeArtist(sel.getId());
            AppStateSerializer.save(db); refresh.run();
            snack("Artista removido", true);
        });
        actCard.getChildren().addAll(bEditName, bEditNat, bRemove);

        sidebar.getChildren().addAll(searchCard, createCard, actCard);
        pane.setCenter(table);
        pane.setRight(scroll(sidebar));
        tab.setContent(pane);
        return tab;
    }

    // =========================================================================
    // ABA: GÉNEROS
    // =========================================================================

    private Tab buildGenresTab() {
        Tab tab = new Tab("🏷  Géneros");
        BorderPane pane = new BorderPane();
        pane.setStyle("-fx-background-color:" + N_BG + ";");
        pane.setPadding(new Insets(20));

        javafx.scene.control.TableView<Genre> table = new javafx.scene.control.TableView<>();
        table.setStyle("-fx-background-color:" + N_CARD + ";");
        table.setColumnResizePolicy(javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY);

        javafx.scene.control.TableColumn<Genre,String> cId   = col("ID",   d -> d.getValue().getId());
        javafx.scene.control.TableColumn<Genre,String> cName = col("Nome", d -> d.getValue().getName());
        javafx.scene.control.TableColumn<Genre,String> cUse  = col("Conteúdos que usam",
                d -> String.valueOf(db.contents().searchByGenre(d.getValue().getId()).size()));
        table.getColumns().addAll(cId, cName, cUse);

        Runnable refresh = () -> table.getItems().setAll(db.genres().listAll());
        refresh.run();

        VBox sidebar = new VBox(16);
        sidebar.setPrefWidth(300);
        sidebar.setPadding(new Insets(0, 0, 0, 20));

        VBox createCard = card("Criar Género");
        TextField cGId   = field("ID (ex: g10)");
        TextField cGName = field("Nome do género");
        Button bCreate = btn("Criar", BTN_G);
        bCreate.setMaxWidth(Double.MAX_VALUE);
        bCreate.setOnAction(e -> {
            String id = cGId.getText().trim(), nome = cGName.getText().trim();
            if (id.isEmpty() || nome.isEmpty()) { snack("ID e Nome são obrigatórios", false); return; }
            if (db.genres().get(id) != null) { snack("ID '" + id + "' já existe", false); return; }
            db.addGenre(new Genre(id, nome));
            AppStateSerializer.save(db); refresh.run();
            cGId.clear(); cGName.clear();
            snack("Género '" + nome + "' criado", true);
        });
        createCard.getChildren().addAll(cGId, cGName, bCreate);

        VBox actCard = card("Ações sobre Selecionado");
        Button bRemove = btn("Remover Género", BTN_DANGER);
        bRemove.setMaxWidth(Double.MAX_VALUE);
        bRemove.setOnAction(e -> {
            Genre sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { snack("Seleciona um género", false); return; }
            if (!confirm("Remover género '" + sel.getName() + "'?")) return;
            try {
                db.removeGenre(sel.getId());
                AppStateSerializer.save(db); refresh.run();
                snack("Género removido", true);
            } catch (IllegalStateException ex) {
                snack(ex.getMessage(), false);
            }
        });
        actCard.getChildren().add(bRemove);

        sidebar.getChildren().addAll(createCard, actCard);
        pane.setCenter(table);
        pane.setRight(scroll(sidebar));
        tab.setContent(pane);
        return tab;
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private <T> javafx.scene.control.TableColumn<T,String> col(String header,
                                                               java.util.function.Function<javafx.scene.control.TableColumn.CellDataFeatures<T,String>,String> fn) {
        javafx.scene.control.TableColumn<T,String> c = new javafx.scene.control.TableColumn<>(header);
        c.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(fn.apply(d)));
        return c;
    }

    private VBox card(String title) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color:" + N_CARD + ";-fx-background-radius:8;");
        Label lbl = new Label(title.toUpperCase());
        lbl.setStyle("-fx-text-fill:" + N_GOLD + ";-fx-font-size:11px;-fx-font-weight:bold;");
        box.getChildren().add(lbl);
        return box;
    }

    private TextField field(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setStyle(FIELD);
        f.setMaxWidth(Double.MAX_VALUE);
        return f;
    }

    private PasswordField pwd(String prompt) {
        PasswordField f = new PasswordField();
        f.setPromptText(prompt);
        f.setStyle(FIELD);
        f.setMaxWidth(Double.MAX_VALUE);
        return f;
    }

    private Button btn(String text, String style) {
        Button b = new Button(text);
        b.setStyle(style);
        return b;
    }

    private HBox row(javafx.scene.Node... nodes) {
        HBox h = new HBox(8, nodes);
        h.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(nodes[0], Priority.ALWAYS);
        return h;
    }

    private ScrollPane scroll(javafx.scene.Node node) {
        ScrollPane s = new ScrollPane(node);
        s.setFitToWidth(true);
        s.setStyle("-fx-background-color:" + N_BG + ";-fx-background:" + N_BG + ";");
        return s;
    }

    private String initials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[0].charAt(0) + parts[parts.length-1].charAt(0)).toUpperCase();
    }

    private String askInput(String header, String defaultVal) {
        TextInputDialog td = new TextInputDialog(defaultVal);
        td.setTitle("Editar");
        td.setHeaderText(header);
        td.setGraphic(null);
        return td.showAndWait().orElse(null);
    }

    private boolean confirm(String message) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Confirmar");
        a.setHeaderText(null);
        a.setContentText(message);
        return a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}