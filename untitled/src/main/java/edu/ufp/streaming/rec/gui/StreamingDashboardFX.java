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
import javafx.collections.ObservableList;
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

public class StreamingDashboardFX {

    // ── Netflix palette ───────────────────────────────────────────────────
    private static final String N_BG     = "#141414";
    private static final String N_CARD   = "#1F1F1F";
    private static final String N_CARD2  = "#2A2A2A";
    private static final String N_RED    = "#E50914";
    private static final String N_TEXT   = "#FFFFFF";
    private static final String N_MUTED  = "#A0A0A0";
    private static final String N_INPUT  = "#333333";
    private static final String N_BORDER = "#404040";
    private static final String N_GREEN  = "#46D369";

    // ── Estilos ───────────────────────────────────────────────────────────
    private static final String FIELD =
            "-fx-background-color:" + N_INPUT + ";-fx-text-fill:" + N_TEXT + ";" +
                    "-fx-prompt-text-fill:" + N_MUTED + ";-fx-border-color:" + N_BORDER + ";" +
                    "-fx-border-radius:4;-fx-background-radius:4;-fx-padding:10 12;-fx-font-size:13px;";

    private static final String BTN_R =
            "-fx-background-color:" + N_RED + ";-fx-text-fill:white;" +
                    "-fx-font-weight:bold;-fx-background-radius:4;-fx-padding:9 18;-fx-cursor:hand;";

    private static final String BTN_S =
            "-fx-background-color:" + N_CARD2 + ";-fx-text-fill:" + N_TEXT + ";" +
                    "-fx-border-color:" + N_BORDER + ";-fx-border-radius:4;-fx-background-radius:4;" +
                    "-fx-padding:9 14;-fx-cursor:hand;";

    private static final String BTN_G =
            "-fx-background-color:transparent;-fx-text-fill:" + N_MUTED + ";" +
                    "-fx-border-color:" + N_BORDER + ";-fx-border-radius:4;-fx-background-radius:4;" +
                    "-fx-padding:9 14;-fx-cursor:hand;";

    private final StreamingDatabase db;
    private final User loggedUser;
    private double xOff, yOff;

    private final Label snackLabel = new Label();
    private javafx.animation.PauseTransition snackTimer;
    private Runnable refreshStats;

    // ── Lista partilhada para o histórico de interações ───────────────────
    // FIX: ObservableList partilhada entre a tab Conteúdos e Minha Conta.
    // Sempre que addInteractionAndRefresh() é chamado, a tabela atualiza automaticamente.
    private final ObservableList<Interation> interactionsList = FXCollections.observableArrayList();

    public StreamingDashboardFX(StreamingDatabase db, User loggedUser) {
        this.db = db;
        this.loggedUser = loggedUser;
        // Inicializa com as interações existentes do utilizador
        interactionsList.setAll(loggedUser.getInteractions());
    }

    /**
     * FIX: Método centralizado para adicionar interação e sincronizar a lista observable.
     * Chama db.addInteraction() e depois atualiza o interactionsList para que o TableView
     * na tab "Minha Conta" reflita a mudança imediatamente, sem precisar de refresh manual.
     */
    private void addInteractionAndRefresh(Interation interaction) {
        db.addInteraction(interaction);
        interactionsList.setAll(loggedUser.getInteractions());
        AppStateSerializer.save(db);
        if (refreshStats != null) refreshStats.run();
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
                buildGraphTab(),
                buildMinhaContaTab()
        );

        root.setCenter(tabs);
        root.setBottom(buildStatusBar());

        root.setOnMousePressed(ev -> { xOff = ev.getSceneX(); yOff = ev.getSceneY(); });
        root.setOnMouseDragged(ev -> { stage.setX(ev.getScreenX()-xOff); stage.setY(ev.getScreenY()-yOff); });

        // CSS Netflix — tabs flat, tabelas escuras
        String css =
                ".tab-pane .tab-header-area .tab-header-background{-fx-background-color:" + N_BG + ";}" +
                        ".tab-pane .tab{-fx-background-color:" + N_BG + ";-fx-padding:10 20;-fx-background-radius:0;}" +
                        ".tab-pane .tab:selected{-fx-background-color:" + N_BG + ";-fx-border-color:" + N_RED + ";-fx-border-width:0 0 3 0;}" +
                        ".tab .tab-label{-fx-text-fill:" + N_MUTED + ";-fx-font-size:13px;-fx-font-weight:bold;}" +
                        ".tab:selected .tab-label{-fx-text-fill:" + N_TEXT + ";}" +
                        ".tab:hover .tab-label{-fx-text-fill:" + N_TEXT + ";}" +
                        ".tab-pane .tab-content-area{-fx-background-color:" + N_BG + ";}" +
                        ".table-view{-fx-background-color:" + N_CARD + ";-fx-border-color:" + N_BORDER + ";}" +
                        ".table-view .column-header-background{-fx-background-color:" + N_BG + ";}" +
                        ".table-view .column-header,.table-view .filler{-fx-background-color:" + N_BG + ";-fx-border-color:" + N_BORDER + ";}" +
                        ".table-view .column-header .label{-fx-text-fill:" + N_MUTED + ";-fx-font-weight:bold;-fx-font-size:11px;}" +
                        ".table-row-cell{-fx-background-color:" + N_CARD + ";-fx-border-color:" + N_BORDER + ";}" +
                        ".table-row-cell:odd{-fx-background-color:#191919;}" +
                        ".table-row-cell:selected{-fx-background-color:#2a0a0a;}" +
                        ".table-row-cell:hover{-fx-background-color:#252525;}" +
                        ".table-cell{-fx-text-fill:" + N_TEXT + ";-fx-font-size:13px;}" +
                        ".scroll-pane{-fx-background-color:" + N_BG + ";-fx-background:" + N_BG + ";}" +
                        ".scroll-pane > .viewport{-fx-background-color:" + N_BG + ";}" +
                        ".scroll-bar{-fx-background-color:" + N_BG + ";}" +
                        ".scroll-bar .thumb{-fx-background-color:" + N_BORDER + ";-fx-background-radius:4;}" +
                        ".combo-box{-fx-background-color:" + N_INPUT + ";-fx-border-color:" + N_BORDER + ";}" +
                        ".combo-box .list-cell{-fx-text-fill:" + N_TEXT + ";-fx-background-color:" + N_INPUT + ";}" +
                        ".combo-box-popup .list-view{-fx-background-color:" + N_CARD + ";}" +
                        ".combo-box-popup .list-cell:hover{-fx-background-color:" + N_CARD2 + ";}";

        Scene scene = new Scene(root, 1280, 820);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add("data:text/css," +
                java.net.URLEncoder.encode(css, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20"));

        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // NAVBAR
    // ═══════════════════════════════════════════════════════════════════════
    private HBox buildNavBar(Stage stage) {
        HBox bar = new HBox(32);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 24, 0, 24));
        bar.setPrefHeight(64);
        bar.setStyle("-fx-background-color:" + N_BG + ";-fx-border-color:" + N_BORDER + ";-fx-border-width:0 0 1 0;");

        Label logo = new Label("STREAMINGAPP");
        logo.setStyle(
                "-fx-text-fill:" + N_RED + ";" +
                        "-fx-font-size:22px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-font-family:'Georgia';"
        );

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Menu mFich = new Menu("Ficheiro");
        MenuItem expTxt = new MenuItem("Exportar TXT (R10)");
        MenuItem impTxt = new MenuItem("Importar TXT (R10)");
        MenuItem expBin = new MenuItem("Exportar Binário (R11)");
        MenuItem impBin = new MenuItem("Importar Binário (R11)");
        expTxt.setOnAction(e -> { ContentFileManager.exportGenres(db.genres(),"genres.txt"); ContentFileManager.exportContents(db.contents(),"contents.txt"); snack("✓ Exportado para TXT",true); });
        impTxt.setOnAction(e -> { ContentFileManager.importGenres(db.genres(),"genres.txt"); ContentFileManager.importContents(db.contents(),db.genres(),"contents.txt"); snack("✓ Importado de TXT",true); });
        expBin.setOnAction(e -> { ContentSerializer.exportGenres(db.genres(),"genres.bin"); ContentSerializer.exportContents(db.contents(),"contents.bin"); snack("✓ Serializado",true); });
        impBin.setOnAction(e -> { ContentSerializer.importGenres(db.genres(),"genres.bin"); ContentSerializer.importContents(db.contents(),"contents.bin"); snack("✓ Importado de binário",true); });
        mFich.getItems().addAll(expTxt, impTxt, new SeparatorMenuItem(), expBin, impBin);
        MenuBar menuBar = new MenuBar(mFich);
        menuBar.setStyle("-fx-background-color:transparent;-fx-padding:0;");

        Label avatar = new Label(initials(loggedUser.getName()));
        avatar.setStyle(
                "-fx-background-color:" + N_RED + ";-fx-text-fill:white;" +
                        "-fx-font-size:13px;-fx-font-weight:bold;" +
                        "-fx-min-width:34;-fx-min-height:34;-fx-max-width:34;-fx-max-height:34;" +
                        "-fx-alignment:center;-fx-background-radius:4;"
        );
        Label userName = new Label(loggedUser.getName());
        userName.setStyle("-fx-text-fill:" + N_TEXT + ";-fx-font-size:13px;");

        Button btnLogout = new Button("Sair");
        btnLogout.setStyle(
                "-fx-background-color:transparent;-fx-text-fill:" + N_MUTED + ";" +
                        "-fx-font-size:13px;-fx-cursor:hand;-fx-underline:false;"
        );
        btnLogout.setOnMouseEntered(e -> btnLogout.setStyle(
                "-fx-background-color:transparent;-fx-text-fill:" + N_TEXT + ";" +
                        "-fx-font-size:13px;-fx-cursor:hand;"
        ));
        btnLogout.setOnMouseExited(e -> btnLogout.setStyle(
                "-fx-background-color:transparent;-fx-text-fill:" + N_MUTED + ";" +
                        "-fx-font-size:13px;-fx-cursor:hand;"
        ));
        btnLogout.setOnAction(e -> {
            AppStateSerializer.save(db);
            stage.close();
            Stage ls = new Stage();
            new LoginScreenFX(db, ls, u -> new StreamingDashboardFX(db, u).start(ls));
        });

        Button btnX = new Button("✕");
        btnX.setStyle("-fx-background-color:transparent;-fx-text-fill:"+N_MUTED+";-fx-font-size:14px;-fx-cursor:hand;");
        btnX.setOnAction(e -> System.exit(0));

        bar.getChildren().addAll(logo, spacer, menuBar, avatar, userName, btnLogout, btnX);
        return bar;
    }

    // ── Status bar + Snackbar ─────────────────────────────────────────────
    private VBox buildStatusBar() {
        HBox info = new HBox();
        info.setPadding(new Insets(6, 24, 0, 24));
        Label l = new Label("LP2 / AED2  •  UFP  •  " + loggedUser.getId() + "  •  " + loggedUser.getRegion());
        l.setStyle("-fx-text-fill:" + N_MUTED + ";-fx-font-size:11px;");
        info.getChildren().add(l);

        snackLabel.setVisible(false);
        snackLabel.setStyle("-fx-font-size:12px;-fx-font-weight:bold;");
        HBox snackRow = new HBox(snackLabel);
        snackRow.setAlignment(Pos.CENTER);
        snackRow.setPadding(new Insets(3, 24, 6, 24));

        VBox bar = new VBox(info, snackRow);
        bar.setStyle("-fx-background-color:#0D0D0D;-fx-border-color:" + N_BORDER + ";-fx-border-width:1 0 0 0;");
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

    // ═══════════════════════════════════════════════════════════════════════
    // 1. TAB UTILIZADORES
    // ═══════════════════════════════════════════════════════════════════════
    private Tab buildUsersTab() {
        Tab tab = new Tab("Utilizadores");
        BorderPane pane = new BorderPane();
        pane.setStyle("-fx-background-color:" + N_BG + ";");
        pane.setPadding(new Insets(20));

        VBox userList = new VBox(8);
        userList.setPadding(new Insets(0, 0, 20, 0));
        ScrollPane listScroll = new ScrollPane(userList);
        listScroll.setFitToWidth(true);
        listScroll.setStyle("-fx-background-color:" + N_BG + ";-fx-background:" + N_BG + ";");

        final User[] sel = {null};
        Runnable[] rl = {null};
        rl[0] = () -> {
            userList.getChildren().clear();
            for (User u : db.users().listAll())
                userList.getChildren().add(buildUserCard(u, sel, rl));
        };
        rl[0].run();

        VBox sidebar = new VBox(16);
        sidebar.setPrefWidth(340);
        sidebar.setPadding(new Insets(0, 0, 0, 20));

        VBox sCard = nCard("Pesquisar");
        TextField fSearch = field("Nome...");
        Button bS = btn("Pesquisar", BTN_R), bA = btn("Todos", BTN_S);
        bS.setOnAction(e -> { userList.getChildren().clear(); for (User u : db.users().searchByNameSubstring(fSearch.getText().trim())) userList.getChildren().add(buildUserCard(u,sel,rl)); });
        bA.setOnAction(e -> rl[0].run());
        HBox sr = new HBox(8, fSearch, bS, bA); sr.setAlignment(Pos.CENTER_LEFT); HBox.setHgrow(fSearch, Priority.ALWAYS);
        sCard.getChildren().add(sr);

        sidebar.getChildren().addAll(sCard);
        pane.setCenter(listScroll);
        pane.setRight(scroll(sidebar));
        tab.setContent(pane);
        return tab;
    }

    private javafx.scene.Node buildUserCard(User u, User[] sel, Runnable[] rl) {
        HBox card = new HBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16, 20, 16, 20));

        Runnable styleNormal   = () -> card.setStyle("-fx-background-color:" + N_CARD + ";-fx-background-radius:6;-fx-cursor:hand;");
        Runnable styleHover    = () -> card.setStyle("-fx-background-color:" + N_CARD2 + ";-fx-background-radius:6;-fx-cursor:hand;");
        Runnable styleSelected = () -> card.setStyle("-fx-background-color:#2a0a0a;-fx-border-color:"+N_RED+";-fx-border-width:0 0 0 3;-fx-background-radius:6;-fx-cursor:hand;");
        styleNormal.run();

        Label av = new Label(initials(u.getName()));
        av.setStyle(
                "-fx-background-color:" + avatarColor(u.getId()) + ";-fx-text-fill:white;" +
                        "-fx-font-size:16px;-fx-font-weight:bold;" +
                        "-fx-min-width:48;-fx-min-height:48;-fx-max-width:48;-fx-max-height:48;" +
                        "-fx-alignment:center;-fx-background-radius:4;"
        );

        VBox info = new VBox(4); HBox.setHgrow(info, Priority.ALWAYS);
        Label lName = new Label(u.getName());
        lName.setStyle("-fx-text-fill:"+N_TEXT+";-fx-font-size:15px;-fx-font-weight:bold;");

        HBox meta = new HBox(12);
        meta.setAlignment(Pos.CENTER_LEFT);
        Label lId    = nbadge(u.getId());
        Label lEmail = new Label(u.getEmail().isEmpty() ? "sem email" : u.getEmail());
        lEmail.setStyle("-fx-text-fill:"+N_MUTED+";-fx-font-size:12px;");
        Label lReg   = new Label("🌍 " + u.getRegion());
        lReg.setStyle("-fx-text-fill:"+N_MUTED+";-fx-font-size:12px;");
        meta.getChildren().addAll(lId, lEmail, lReg);

        int followers = db.follows().getFollowers(u.getId()).size();
        int following = db.follows().getFollowing(u.getId()).size();
        int watched   = (int) u.getInteractions().stream().filter(i -> i.getType() == InterationType.WATCH).count();

        HBox statsRow = new HBox(16);
        statsRow.setAlignment(Pos.CENTER_LEFT);
        statsRow.getChildren().addAll(
                mstat("👥", followers, "seguidores"),
                mstat("➜",  following, "seguindo"),
                mstat("▶",  watched,   "vistos")
        );

        info.getChildren().addAll(lName, meta, statsRow);

        boolean isMe    = u.getId().equals(loggedUser.getId());
        boolean jaSegue = db.follows().getFollowing(loggedUser.getId()).stream().anyMatch(x->x.getId().equals(u.getId()));
        Button bFollow  = new Button(isMe ? "—" : jaSegue ? "✓ A seguir" : "+ Seguir");
        bFollow.setStyle(followStyle(isMe, jaSegue));

        if (!isMe) {
            bFollow.setOnAction(e -> {
                boolean segueAgora = db.follows().getFollowing(loggedUser.getId()).stream().anyMatch(x->x.getId().equals(u.getId()));
                if (segueAgora) { db.follows().unfollow(loggedUser.getId(),u.getId()); snack("✓ Deixaste de seguir "+u.getName(),true); }
                else { db.addFollow(loggedUser.getId(),u.getId()); snack("✓ Passaste a seguir "+u.getName(),true); }
                if (refreshStats != null) refreshStats.run();
                rl[0].run();
            });
        }

        card.setOnMouseClicked(e -> { sel[0]=u; styleSelected.run(); });
        card.setOnMouseEntered(e -> { if(sel[0]==null||!sel[0].getId().equals(u.getId())) styleHover.run(); });
        card.setOnMouseExited(e -> { if(sel[0]==null||!sel[0].getId().equals(u.getId())) styleNormal.run(); });

        card.getChildren().addAll(av, info, bFollow);
        return card;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 2. TAB CONTEÚDOS
    // ═══════════════════════════════════════════════════════════════════════
    private Tab buildContentsTab() {
        Tab tab = new Tab("Conteúdos");
        BorderPane pane = new BorderPane();
        pane.setStyle("-fx-background-color:" + N_BG + ";");
        pane.setPadding(new Insets(20));

        TextField fSearch = field("Pesquisar título...");
        ToggleGroup tgType = new ToggleGroup();
        ToggleButton tbAll = typeToggle("Todos",  tgType, true);
        ToggleButton tbMov = typeToggle("Filmes", tgType, false);
        ToggleButton tbSer = typeToggle("Séries", tgType, false);
        ToggleButton tbDoc = typeToggle("Docs",   tgType, false);

        HBox topBar = new HBox(10, fSearch, tbAll, tbMov, tbSer, tbDoc);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 0, 16, 0));
        HBox.setHgrow(fSearch, Priority.ALWAYS);

        FlowPane grid = new FlowPane(14, 14);
        grid.setPadding(new Insets(4));
        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color:" + N_BG + ";-fx-background:" + N_BG + ";");

        Runnable[] rl = {null};
        rl[0] = () -> {
            String q = fSearch.getText().trim().toLowerCase();
            String typeFilter = ((ToggleButton) tgType.getSelectedToggle()).getText();
            grid.getChildren().clear();
            db.contents().listAll().stream()
                    .filter(c -> c.getTitle().toLowerCase().contains(q))
                    .filter(c -> {
                        if ("Todos".equals(typeFilter))  return true;
                        if ("Filmes".equals(typeFilter)) return c instanceof Movie;
                        if ("Séries".equals(typeFilter)) return c instanceof Series;
                        if ("Docs".equals(typeFilter))   return c instanceof Documentary;
                        return true;
                    })
                    .forEach(c -> grid.getChildren().add(buildContentCard(c, rl)));
        };
        rl[0].run();

        fSearch.textProperty().addListener((o, old, nv) -> rl[0].run());
        tgType.selectedToggleProperty().addListener((o, old, nv) -> { if (nv != null) rl[0].run(); });

        VBox main = new VBox(0, topBar, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        main.setStyle("-fx-background-color:" + N_BG + ";");

        pane.setCenter(main);
        tab.setContent(pane);
        return tab;
    }

    private javafx.scene.Node buildContentCard(Content c, Runnable[] rl) {
        String typeLabel = c instanceof Movie ? "FILME" : c instanceof Series ? "SÉRIE" : "DOC";
        String typeColor = c instanceof Movie ? N_RED   : c instanceof Series ? "#185FA5" : "#3B6D11";
        String icon      = c instanceof Movie ? "🎬"   : c instanceof Series ? "📺"      : "🎥";
        String thumbBg   = c instanceof Movie ? "#1a0505" : c instanceof Series ? "#05051a" : "#051a05";

        VBox card = new VBox(0);
        card.setPrefWidth(210);
        card.setMaxWidth(210);
        card.setStyle(
                "-fx-background-color:" + N_CARD + ";-fx-background-radius:8;" +
                        "-fx-border-color:" + N_BORDER + ";-fx-border-radius:8;-fx-cursor:default;"
        );

        // ── Thumbnail ─────────────────────────────────────────────────────────
        StackPane thumb = new StackPane();
        thumb.setPrefHeight(120);
        thumb.setStyle("-fx-background-color:" + thumbBg + ";");

        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size:38px;");

        Label typeBadge = new Label(typeLabel);
        typeBadge.setStyle(
                "-fx-background-color:" + typeColor + ";-fx-text-fill:white;" +
                        "-fx-font-size:9px;-fx-font-weight:bold;-fx-padding:3 8;-fx-background-radius:4;"
        );
        StackPane.setAlignment(typeBadge, Pos.TOP_LEFT);
        StackPane.setMargin(typeBadge, new Insets(8));

        Label ratingBadge = new Label(String.format("%.1f ★", c.getRating()));
        ratingBadge.setStyle(
                "-fx-background-color:#0d0d0d;-fx-text-fill:" + N_RED + ";" +
                        "-fx-font-size:10px;-fx-font-weight:bold;-fx-padding:3 7;-fx-background-radius:4;"
        );
        StackPane.setAlignment(ratingBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(ratingBadge, new Insets(8));

        thumb.getChildren().addAll(iconLbl, typeBadge, ratingBadge);

        // ── Body ──────────────────────────────────────────────────────────────
        VBox body = new VBox(8);
        body.setPadding(new Insets(12, 14, 14, 14));

        Label title = new Label(c.getTitle());
        title.setStyle("-fx-text-fill:" + N_TEXT + ";-fx-font-size:13px;-fx-font-weight:bold;");
        title.setMaxWidth(182);

        Label meta = new Label(c.getGenre().getName() + "  •  " + c.getReleaseDate().getYear());
        meta.setStyle("-fx-text-fill:" + N_MUTED + ";-fx-font-size:11px;");

        // ── Estrelas ──────────────────────────────────────────────────────────
        final int[] starVal = {(int) Math.round(c.getRating())};
        Button[] stars = new Button[5];
        HBox starRow = new HBox(2);
        starRow.setAlignment(Pos.CENTER_LEFT);

        Runnable paintStars = () -> {
            for (int i = 0; i < 5; i++)
                stars[i].setStyle(
                        "-fx-background-color:transparent;-fx-font-size:16px;-fx-cursor:hand;" +
                                "-fx-text-fill:" + (i < starVal[0] ? N_RED : N_BORDER) + ";-fx-padding:0;"
                );
        };

        for (int i = 0; i < 5; i++) {
            final int v = i + 1;
            stars[i] = new Button("★");
            stars[i].setOnMouseEntered(e -> {
                for (int j = 0; j < 5; j++)
                    stars[j].setStyle(
                            "-fx-background-color:transparent;-fx-font-size:16px;-fx-cursor:hand;" +
                                    "-fx-text-fill:" + (j < v ? N_RED : N_BORDER) + ";-fx-padding:0;"
                    );
            });
            stars[i].setOnMouseExited(e -> paintStars.run());
            stars[i].setOnAction(e -> {
                starVal[0] = v;
                paintStars.run();
                String iId = "i_" + loggedUser.getId() + "_" + c.getId() + "_" + System.currentTimeMillis();
                // FIX: usar addInteractionAndRefresh em vez de db.addInteraction direto
                addInteractionAndRefresh(new Interation(loggedUser, c, LocalDateTime.now(), v, 0.0, InterationType.RATE, iId));
                double soma = 0; int cnt = 0;
                for (User u : db.users().listAll())
                    for (Interation it : u.getInteractions())
                        if (it.getType() == InterationType.RATE && it.getContent().getId().equals(c.getId())) {
                            soma += it.getRating(); cnt++;
                        }
                if (cnt > 0) c.setRating(soma / cnt);
                ratingBadge.setText(String.format("%.1f ★", c.getRating()));
                snack("✓ Avaliado: " + v + "/5 ★", true);
            });
            starRow.getChildren().add(stars[i]);
        }
        paintStars.run();

        // ── Botões de ação ────────────────────────────────────────────────────
        Button bBk = actionBtn("+ SAVE",  "#2A2A2A",     N_MUTED, N_BORDER);
        Button bWt = actionBtn("WATCHED", N_RED,          "white",  N_RED);
        Button bSk = actionBtn("SKIP",    "transparent",  N_MUTED, N_BORDER);

        bBk.setOnAction(e -> {
            boolean on = "✓ SAVED".equals(bBk.getText());
            if (!on) {
                String iId = "i_" + loggedUser.getId() + "_" + c.getId() + "_" + System.currentTimeMillis();
                // FIX: usar addInteractionAndRefresh — atualiza o histórico imediatamente
                addInteractionAndRefresh(new Interation(loggedUser, c, LocalDateTime.now(), 0, 0.0, InterationType.BOOKMARK, iId));
                bBk.setText("✓ SAVED");
                bBk.setStyle(actionStyle("#1a2a1a", N_GREEN, N_GREEN));
                snack("✓ \"" + c.getTitle() + "\" guardado", true);
            } else {
                bBk.setText("+ SAVE");
                bBk.setStyle(actionStyle("#2A2A2A", N_MUTED, N_BORDER));
            }
        });

        bWt.setOnAction(e -> {
            boolean on = "✓ VISTO".equals(bWt.getText());
            if (!on) {
                String iId = "i_" + loggedUser.getId() + "_" + c.getId() + "_" + System.currentTimeMillis();
                // FIX: usar addInteractionAndRefresh — atualiza o histórico imediatamente
                addInteractionAndRefresh(new Interation(loggedUser, c, LocalDateTime.now(), 0, 1.0, InterationType.WATCH, iId));
                bWt.setText("✓ VISTO");
                bWt.setStyle(actionStyle("#831010", "white", "#831010"));
                bSk.setText("SKIP");
                bSk.setStyle(actionStyle("transparent", N_MUTED, N_BORDER));
                snack("✓ \"" + c.getTitle() + "\" marcado como visto", true);
            } else {
                bWt.setText("WATCHED");
                bWt.setStyle(actionStyle(N_RED, "white", N_RED));
            }
        });

        bSk.setOnAction(e -> {
            boolean on = "✓ SKIP".equals(bSk.getText());
            if (!on) {
                String iId = "i_" + loggedUser.getId() + "_" + c.getId() + "_" + System.currentTimeMillis();
                // FIX: usar addInteractionAndRefresh — atualiza o histórico imediatamente
                addInteractionAndRefresh(new Interation(loggedUser, c, LocalDateTime.now(), 0, 0.0, InterationType.SKIP, iId));
                bSk.setText("✓ SKIP");
                bSk.setStyle(actionStyle("#1a1a2a", "#8888FF", "#555555"));
                bWt.setText("WATCHED");
                bWt.setStyle(actionStyle(N_RED, "white", N_RED));
                snack("\"" + c.getTitle() + "\" marcado para skip", true);
            } else {
                bSk.setText("SKIP");
                bSk.setStyle(actionStyle("transparent", N_MUTED, N_BORDER));
            }
        });

        HBox actions = new HBox(6, bBk, bWt, bSk);

        body.getChildren().addAll(title, meta, starRow, actions);
        card.getChildren().addAll(thumb, body);

        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color:" + N_CARD2 + ";-fx-background-radius:8;" +
                        "-fx-border-color:#555;-fx-border-radius:8;-fx-cursor:default;"
        ));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color:" + N_CARD + ";-fx-background-radius:8;" +
                        "-fx-border-color:" + N_BORDER + ";-fx-border-radius:8;-fx-cursor:default;"
        ));

        return card;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private Button actionBtn(String text, String bg, String fg, String border) {
        Button b = new Button(text);
        b.setStyle(actionStyle(bg, fg, border));
        b.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(b, Priority.ALWAYS);
        return b;
    }

    private String actionStyle(String bg, String fg, String border) {
        return "-fx-background-color:" + bg + ";-fx-text-fill:" + fg + ";" +
                "-fx-border-color:" + border + ";-fx-border-radius:20;-fx-background-radius:20;" +
                "-fx-font-size:9px;-fx-font-weight:bold;-fx-padding:5 4;-fx-cursor:hand;";
    }

    private ToggleButton typeToggle(String text, ToggleGroup tg, boolean selected) {
        ToggleButton tb = new ToggleButton(text);
        tb.setToggleGroup(tg);
        tb.setSelected(selected);
        tb.setStyle(selected ? BTN_R : BTN_S);
        tb.selectedProperty().addListener((o, old, nv) -> tb.setStyle(nv ? BTN_R : BTN_S));
        return tb;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 3. TAB ARTISTAS
    // ═══════════════════════════════════════════════════════════════════════
    private Tab buildArtistsTab() {
        Tab tab = new Tab("Artistas");
        BorderPane pane = new BorderPane();
        pane.setStyle("-fx-background-color:" + N_BG + ";");
        pane.setPadding(new Insets(20));

        TextField fSearch = field("Pesquisar nome...");
        ToggleGroup tgRole = new ToggleGroup();
        ToggleButton tbAll = typeToggle("Todos",    tgRole, true);
        ToggleButton tbAct = typeToggle("Actor",    tgRole, false);
        ToggleButton tbDir = typeToggle("Director", tgRole, false);
        ToggleButton tbPro = typeToggle("Producer", tgRole, false);

        HBox topBar = new HBox(10, fSearch, tbAll, tbAct, tbDir, tbPro);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 0, 16, 0));
        HBox.setHgrow(fSearch, Priority.ALWAYS);

        FlowPane grid = new FlowPane(14, 14);
        grid.setPadding(new Insets(4));
        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color:" + N_BG + ";-fx-background:" + N_BG + ";");

        Runnable[] rl = {null};
        rl[0] = () -> {
            String q = fSearch.getText().trim().toLowerCase();
            String roleFilter = ((ToggleButton) tgRole.getSelectedToggle()).getText();
            grid.getChildren().clear();
            db.artists().listAll().stream()
                    .filter(a -> a.getName().toLowerCase().contains(q))
                    .filter(a -> "Todos".equals(roleFilter) || a.getRole().toString().equalsIgnoreCase(roleFilter))
                    .forEach(a -> grid.getChildren().add(buildArtistCard(a)));
        };
        rl[0].run();

        fSearch.textProperty().addListener((o, old, nv) -> rl[0].run());
        tgRole.selectedToggleProperty().addListener((o, old, nv) -> { if (nv != null) rl[0].run(); });

        VBox main = new VBox(0, topBar, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        main.setStyle("-fx-background-color:" + N_BG + ";");

        pane.setCenter(main);
        tab.setContent(pane);
        return tab;
    }

    private javafx.scene.Node buildArtistCard(Artist a) {
        String roleColor = switch (a.getRole().toString()) {
            case "DIRECTOR" -> "#185FA5";
            case "PRODUCER" -> "#3B6D11";
            default         -> N_RED;
        };
        String roleBg = switch (a.getRole().toString()) {
            case "DIRECTOR" -> "#05051a";
            case "PRODUCER" -> "#051a05";
            default         -> "#1a0505";
        };

        VBox card = new VBox(0);
        card.setPrefWidth(210);
        card.setMaxWidth(210);
        card.setStyle(
                "-fx-background-color:" + N_CARD + ";-fx-background-radius:8;" +
                        "-fx-border-color:" + N_BORDER + ";-fx-border-radius:8;-fx-cursor:default;"
        );

        StackPane thumb = new StackPane();
        thumb.setPrefHeight(110);
        thumb.setStyle("-fx-background-color:" + roleBg + ";");

        Label av = new Label(initials(a.getName()));
        av.setStyle(
                "-fx-background-color:" + roleColor + ";-fx-text-fill:white;" +
                        "-fx-font-size:26px;-fx-font-weight:bold;" +
                        "-fx-min-width:64;-fx-min-height:64;-fx-max-width:64;-fx-max-height:64;" +
                        "-fx-alignment:center;-fx-background-radius:6;"
        );

        Label roleBadge = new Label(a.getRole().toString());
        roleBadge.setStyle(
                "-fx-background-color:" + roleColor + ";-fx-text-fill:white;" +
                        "-fx-font-size:9px;-fx-font-weight:bold;-fx-padding:3 8;-fx-background-radius:4;"
        );
        StackPane.setAlignment(roleBadge, Pos.TOP_LEFT);
        StackPane.setMargin(roleBadge, new Insets(8));

        String genderIcon = "M".equalsIgnoreCase(a.getGender()) ? "♂" : "♀";
        Label genderLbl = new Label(genderIcon);
        genderLbl.setStyle("-fx-text-fill:" + N_MUTED + ";-fx-font-size:14px;");
        StackPane.setAlignment(genderLbl, Pos.TOP_RIGHT);
        StackPane.setMargin(genderLbl, new Insets(10));

        thumb.getChildren().addAll(av, roleBadge, genderLbl);

        VBox body = new VBox(6);
        body.setPadding(new Insets(12, 14, 14, 14));

        Label name = new Label(a.getName());
        name.setStyle("-fx-text-fill:" + N_TEXT + ";-fx-font-size:13px;-fx-font-weight:bold;");
        name.setMaxWidth(182);

        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        Label natLbl = new Label("🌍 " + a.getNationality());
        natLbl.setStyle("-fx-text-fill:" + N_MUTED + ";-fx-font-size:11px;");
        Label dateLbl = new Label("🎂 " + a.getBirthDate().toString());
        dateLbl.setStyle("-fx-text-fill:" + N_MUTED + ";-fx-font-size:11px;");
        metaRow.getChildren().addAll(natLbl, dateLbl);

        body.getChildren().addAll(name, metaRow);
        card.getChildren().addAll(thumb, body);

        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color:" + N_CARD2 + ";-fx-background-radius:8;" +
                        "-fx-border-color:#555;-fx-border-radius:8;-fx-cursor:default;"
        ));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color:" + N_CARD + ";-fx-background-radius:8;" +
                        "-fx-border-color:" + N_BORDER + ";-fx-border-radius:8;-fx-cursor:default;"
        ));

        return card;
    }

    private javafx.scene.Node buildArtistCard(Artist a, Runnable[] rl) {
        String roleColor = switch (a.getRole().toString()) {
            case "DIRECTOR" -> "#185FA5";
            case "PRODUCER" -> "#3B6D11";
            default         -> N_RED;
        };
        String roleBg = switch (a.getRole().toString()) {
            case "DIRECTOR" -> "#05051a";
            case "PRODUCER" -> "#051a05";
            default         -> "#1a0505";
        };

        VBox card = new VBox(0);
        card.setPrefWidth(210);
        card.setMaxWidth(210);
        card.setStyle(
                "-fx-background-color:" + N_CARD + ";-fx-background-radius:8;" +
                        "-fx-border-color:" + N_BORDER + ";-fx-border-radius:8;-fx-cursor:default;"
        );

        StackPane thumb = new StackPane();
        thumb.setPrefHeight(110);
        thumb.setStyle("-fx-background-color:" + roleBg + ";");

        Label av = new Label(initials(a.getName()));
        av.setStyle(
                "-fx-background-color:" + roleColor + ";-fx-text-fill:white;" +
                        "-fx-font-size:26px;-fx-font-weight:bold;" +
                        "-fx-min-width:64;-fx-min-height:64;-fx-max-width:64;-fx-max-height:64;" +
                        "-fx-alignment:center;-fx-background-radius:6;"
        );

        Label roleBadge = new Label(a.getRole().toString());
        roleBadge.setStyle(
                "-fx-background-color:" + roleColor + ";-fx-text-fill:white;" +
                        "-fx-font-size:9px;-fx-font-weight:bold;-fx-padding:3 8;-fx-background-radius:4;"
        );
        StackPane.setAlignment(roleBadge, Pos.TOP_LEFT);
        StackPane.setMargin(roleBadge, new Insets(8));

        String genderIcon = "M".equalsIgnoreCase(a.getGender()) ? "♂" : "♀";
        Label genderLbl = new Label(genderIcon);
        genderLbl.setStyle("-fx-text-fill:" + N_MUTED + ";-fx-font-size:14px;");
        StackPane.setAlignment(genderLbl, Pos.TOP_RIGHT);
        StackPane.setMargin(genderLbl, new Insets(10));

        thumb.getChildren().addAll(av, roleBadge, genderLbl);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color:" + N_BORDER + ";-fx-padding:0;");

        Button bEN  = actionBtn("✏ Nac.",   "#2A2A2A",    N_MUTED,  N_BORDER);
        Button bRem = actionBtn("Remover",  "transparent", "#FF5252", "#FF5252");

        bEN.setOnAction(e -> {
            String nv = askInput("Nova Nacionalidade:", a.getNationality());
            if (nv != null && !nv.trim().isEmpty()) {
                db.artists().editNationality(a.getId(), nv.trim());
                rl[0].run();
                snack("✓ Nacionalidade atualizada", true);
            }
        });
        bRem.setOnAction(e -> {
            db.removeArtist(a.getId());
            rl[0].run();
            snack("✓ Artista removido", true);
        });

        HBox actions = new HBox(6, bEN, bRem);

        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color:" + N_CARD2 + ";-fx-background-radius:8;" +
                        "-fx-border-color:#555;-fx-border-radius:8;-fx-cursor:default;"
        ));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color:" + N_CARD + ";-fx-background-radius:8;" +
                        "-fx-border-color:" + N_BORDER + ";-fx-border-radius:8;-fx-cursor:default;"
        ));

        return card;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 4. TAB GRAFO / R8
    // ═══════════════════════════════════════════════════════════════════════
    private Tab buildGraphTab() {
        Tab tab = new Tab("Grafo / R8");
        BorderPane pane = new BorderPane();
        pane.setStyle("-fx-background-color:"+N_BG+";"); pane.setPadding(new Insets(20));

        TextArea output = new TextArea("// Resultados aparecem aqui...");
        output.setEditable(false);
        output.setStyle("-fx-font-family:monospace;-fx-font-size:13px;-fx-background-color:"+N_CARD+";-fx-text-fill:"+N_GREEN+";-fx-border-color:"+N_BORDER+";");

        VBox sidebar = new VBox(16); sidebar.setPrefWidth(340); sidebar.setPadding(new Insets(0,0,0,20));

        VBox r8a = nCard("R8a — Caminho mais curto");
        TextField fO=field("ID Origem"), fDest=field("ID Destino");
        Button bC=btn("Calcular",BTN_R);
        bC.setOnAction(e->{String o=fO.getText().trim(),d=fDest.getText().trim();List<String> p=db.getGraph().caminhoMaisCurtoBetweenUsers(o,d);double w=db.getGraph().pesoCaminhoMaisCurto(o,d);if(p.isEmpty())output.setText("[R8a] Sem caminho de "+o+" para "+d);else output.setText(String.format("[R8a] %s → %s:\n  %s\n  Peso: %.2f",o,d,p,w));});
        r8a.getChildren().addAll(new HBox(8,lbl("Origem:"),fO), new HBox(8,lbl("Destino:"),fDest), bC);

        VBox r8c = nCard("R8c — Conectividade forte");
        Button bConexo=btn("Verificar Grafo",BTN_R);
        bConexo.setOnAction(e->output.setText("[R8c] Fortemente conexo: "+(db.getGraph().isGrafoUtilizadoresConexo()?"SIM ✓":"NÃO ✗")));
        r8c.getChildren().add(bConexo);

        VBox r8g = nCard("R8g — Seguidores que viram conteúdo (2024)");
        TextField fUId=field("User ID"), fCId=field("Content ID");
        Button bG=btn("Pesquisar",BTN_R);
        bG.setOnAction(e->{String u=fUId.getText().trim(),c=fCId.getText().trim();List<User> l=db.getGraph().seguidoresQueViramConteudo(u,c,LocalDateTime.of(2024,1,1,0,0),LocalDateTime.of(2024,12,31,23,59),db.follows(),db.users());if(l.isEmpty())output.setText("[R8g] Nenhum resultado.");else{StringBuilder sb=new StringBuilder("[R8g] Seguidores de "+u+" que viram "+c+":\n");l.forEach(x->sb.append("  • ").append(x.getName()).append("\n"));output.setText(sb.toString());}});
        r8g.getChildren().addAll(new HBox(8,lbl("User ID:"),fUId), new HBox(8,lbl("Content ID:"),fCId), bG);

        Button bInfo=btn("Info do Grafo",BTN_S);
        bInfo.setOnAction(e->output.setText(String.format("[GRAFO] Vértices: %d | Arestas: %d",db.getGraph().totalVertices(),db.getGraph().totalArestas())));

        sidebar.getChildren().addAll(r8a, r8c, r8g, bInfo);
        pane.setCenter(output); pane.setRight(scroll(sidebar));
        tab.setContent(pane); return tab;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 5. TAB MINHA CONTA
    // ═══════════════════════════════════════════════════════════════════════
    private Tab buildMinhaContaTab() {
        Tab tab = new Tab("Minha Conta");
        ScrollPane rootScroll = new ScrollPane();
        rootScroll.setFitToWidth(true);
        rootScroll.setStyle("-fx-background-color:"+N_BG+";-fx-background:"+N_BG+";");

        VBox main = new VBox(20);
        main.setPadding(new Insets(24));
        main.setStyle("-fx-background-color:"+N_BG+";");

        // ── Hero ─────────────────────────────────────────────────────────
        HBox hero = new HBox(24);
        hero.setAlignment(Pos.CENTER_LEFT);
        hero.setPadding(new Insets(28));
        hero.setStyle("-fx-background-color:"+N_CARD+";-fx-background-radius:8;");

        Label avatar = new Label(initials(loggedUser.getName()));
        avatar.setStyle(
                "-fx-background-color:"+N_RED+";-fx-text-fill:white;" +
                        "-fx-font-size:28px;-fx-font-weight:bold;" +
                        "-fx-min-width:76;-fx-min-height:76;-fx-max-width:76;-fx-max-height:76;" +
                        "-fx-alignment:center;-fx-background-radius:6;"
        );

        VBox heroInfo = new VBox(6); HBox.setHgrow(heroInfo, Priority.ALWAYS);
        Label heroName = new Label(loggedUser.getName());
        heroName.setStyle("-fx-text-fill:"+N_TEXT+";-fx-font-size:24px;-fx-font-weight:bold;");
        Label heroSub  = new Label(loggedUser.getId()+"  •  "+loggedUser.getEmail()+"  •  "+loggedUser.getRegion());
        heroSub.setStyle("-fx-text-fill:"+N_MUTED+";-fx-font-size:13px;");
        Label heroDate = new Label("Membro desde "+loggedUser.getRegisterDate());
        heroDate.setStyle("-fx-text-fill:"+N_MUTED+";-fx-font-size:12px;");

        Runnable refreshHero = () -> {
            User u = db.users().get(loggedUser.getId()); if(u==null)return;
            heroName.setText(u.getName()); avatar.setText(initials(u.getName()));
            heroSub.setText(u.getId()+"  •  "+u.getEmail()+"  •  "+u.getRegion());
        };

        VBox chipVistos     = statChip("▶", 0, "vistos");
        VBox chipAvaliados  = statChip("★", 0, "avaliados");
        VBox chipSeguindo   = statChip("➜", 0, "seguindo");
        VBox chipSeguidores = statChip("👥", 0, "seguidores");

        HBox stats = new HBox(16);
        stats.setPadding(new Insets(10,0,0,0));
        stats.getChildren().addAll(chipVistos, chipAvaliados, chipSeguindo, chipSeguidores);

        refreshStats = () -> {
            int tw  = (int) loggedUser.getInteractions().stream().filter(i->i.getType()==InterationType.WATCH).count();
            int tr  = (int) loggedUser.getInteractions().stream().filter(i->i.getType()==InterationType.RATE).count();
            int tfo = db.follows().getFollowing(loggedUser.getId()).size();
            int tfi = db.follows().getFollowers(loggedUser.getId()).size();
            ((Label) chipVistos.getChildren().get(0)).setText("▶ " + tw);
            ((Label) chipAvaliados.getChildren().get(0)).setText("★ " + tr);
            ((Label) chipSeguindo.getChildren().get(0)).setText("➜ " + tfo);
            ((Label) chipSeguidores.getChildren().get(0)).setText("👥 " + tfi);
        };
        refreshStats.run();

        heroInfo.getChildren().addAll(heroName, heroSub, heroDate, stats);

        VBox editBtns = new VBox(8);
        editBtns.setAlignment(Pos.CENTER_RIGHT);
        Button bN=btn("✏  Nome",BTN_S), bE=btn("✉  Email",BTN_S), bR=btn("🌍  Região",BTN_S), bP=btn("🔒  Password",BTN_G);
        bN.setMaxWidth(160); bE.setMaxWidth(160); bR.setMaxWidth(160); bP.setMaxWidth(160);
        bN.setOnAction(e->{ String nv=askInput("Novo nome:",loggedUser.getName());if(nv==null||nv.trim().isEmpty())return;if(db.users().editName(loggedUser.getId(),nv.trim())){refreshHero.run();AppStateSerializer.save(db);snack("✓ Nome atualizado",true);} });
        bE.setOnAction(e->{ String nv=askInput("Novo email:",loggedUser.getEmail());if(nv==null||nv.trim().isEmpty())return;for(User u:db.users().listAll())if(!u.getId().equals(loggedUser.getId())&&u.getEmail().equalsIgnoreCase(nv.trim())){snack("✗ Email em uso",false);return;}if(db.users().editEmail(loggedUser.getId(),nv.trim())){refreshHero.run();AppStateSerializer.save(db);snack("✓ Email atualizado",true);} });
        bR.setOnAction(e->{ String nv=askInput("Nova região:",loggedUser.getRegion());if(nv==null||nv.trim().isEmpty())return;if(db.users().editRegion(loggedUser.getId(),nv.trim())){refreshHero.run();AppStateSerializer.save(db);snack("✓ Região atualizada",true);} });
        bP.setOnAction(e->{
            PasswordField pa=new PasswordField();pa.setPromptText("Password atual");pa.setStyle(FIELD);
            PasswordField pn=new PasswordField();pn.setPromptText("Nova password");pn.setStyle(FIELD);
            PasswordField pc=new PasswordField();pc.setPromptText("Confirmar");pc.setStyle(FIELD);
            VBox vc=new VBox(8,new Label("Atual:"),pa,new Label("Nova:"),pn,new Label("Confirmar:"),pc);vc.setPadding(new Insets(10));
            Dialog<ButtonType> dlg=new Dialog<>();dlg.setTitle("Alterar Password");dlg.getDialogPane().setContent(vc);dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK,ButtonType.CANCEL);
            if(dlg.showAndWait().orElse(ButtonType.CANCEL)!=ButtonType.OK)return;
            if(db.authenticate(loggedUser.getId(),pa.getText())==null){snack("✗ Password atual incorreta",false);return;}
            if(pn.getText().isEmpty()){snack("✗ Password vazia",false);return;}
            if(!pn.getText().equals(pc.getText())){snack("✗ Não coincidem",false);return;}
            db.changePassword(loggedUser.getId(),pn.getText());AppStateSerializer.save(db);snack("✓ Password alterada",true);
        });
        editBtns.getChildren().addAll(bN, bE, bR, bP);
        hero.getChildren().addAll(avatar, heroInfo, editBtns);

        // ── Linha 1: Interações + Recomendações ──────────────────────────
        HBox row1 = new HBox(16); row1.setPrefHeight(260);

        VBox interCard = nCard("Histórico de Interações");
        VBox.setVgrow(interCard,Priority.ALWAYS); HBox.setHgrow(interCard,Priority.ALWAYS);
        TableView<Interation> tI = new TableView<>();
        tI.setStyle("-fx-background-color:"+N_CARD+";");
        tI.getColumns().addAll(
                col("Conteúdo", d->d.getValue().getContent().getTitle()),
                col("Tipo",     d->d.getValue().getType().toString()),
                col("Rating",   d->d.getValue().getType()==InterationType.RATE?String.format("%.0f ★",d.getValue().getRating()):"—"),
                col("Progresso",d->d.getValue().getType()==InterationType.WATCH?String.format("%.0f%%",d.getValue().getProgress()*100):"—"),
                col("Data",     d->d.getValue().getWatchDate().toLocalDate().toString())
        );
        tI.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // FIX: ligar o TableView à interactionsList partilhada.
        // A partir daqui, sempre que addInteractionAndRefresh() for chamado
        // (na tab Conteúdos ou aqui no formulário), a tabela atualiza sozinha.
        tI.setItems(interactionsList);

        tI.setPlaceholder(new Label("Sem interações."));
        VBox.setVgrow(tI,Priority.ALWAYS);
        interCard.getChildren().add(tI);

        VBox recomCard = nCard("Recomendações (R8d)");
        recomCard.setPrefWidth(260);
        List<Content> recs = db.getGraph().recomendarConteudosPorProximidade(loggedUser.getId(),db.follows(),db.users());
        if (recs.isEmpty()) {
            Label el=new Label("Segue utilizadores\npara receber recomendações.");
            el.setStyle("-fx-text-fill:"+N_MUTED+";-fx-font-size:12px;");
            recomCard.getChildren().add(el);
        } else {
            for (Content c : recs) {
                HBox chip = new HBox(10); chip.setAlignment(Pos.CENTER_LEFT); chip.setPadding(new Insets(10,12,10,12));
                chip.setStyle("-fx-background-color:#0d0d0d;-fx-background-radius:6;");
                String tp = c instanceof Movie?"🎬":c instanceof Series?"📺":"🎥";
                Label ti = new Label(tp);
                VBox li = new VBox(2);
                Label lt = new Label(c.getTitle()); lt.setStyle("-fx-text-fill:"+N_TEXT+";-fx-font-size:13px;-fx-font-weight:bold;");
                Label lg = new Label(c.getGenre().getName()+"  •  "+String.format("%.1f ★",c.getRating())); lg.setStyle("-fx-text-fill:"+N_MUTED+";-fx-font-size:11px;");
                li.getChildren().addAll(lt,lg); chip.getChildren().addAll(ti,li);
                recomCard.getChildren().add(chip);
            }
        }
        row1.getChildren().addAll(interCard, recomCard);

        // ── Linha 2: Follows + Preferências ──────────────────────────────
        HBox row2 = new HBox(16);
        VBox fCard=nCard("A Seguir"); HBox.setHgrow(fCard,Priority.ALWAYS);
        List<User> segu=db.follows().getFollowing(loggedUser.getId());
        if(segu.isEmpty()){Label el=new Label("Não segues ninguém.");el.setStyle("-fx-text-fill:"+N_MUTED+";-fx-font-size:12px;");fCard.getChildren().add(el);}
        else for(User u:segu) fCard.getChildren().add(userChip(u));

        VBox fCard2=nCard("Seguidores"); HBox.setHgrow(fCard2,Priority.ALWAYS);
        List<User> segs=db.follows().getFollowers(loggedUser.getId());
        if(segs.isEmpty()){Label el=new Label("Sem seguidores ainda.");el.setStyle("-fx-text-fill:"+N_MUTED+";-fx-font-size:12px;");fCard2.getChildren().add(el);}
        else for(User u:segs) fCard2.getChildren().add(userChip(u));

        VBox prefCard=nCard("Géneros Preferidos"); prefCard.setPrefWidth(200);
        List<Genre> prefs=loggedUser.getPreferences();
        if(prefs.isEmpty()){Label el=new Label("Sem géneros preferidos.");el.setStyle("-fx-text-fill:"+N_MUTED+";-fx-font-size:12px;");prefCard.getChildren().add(el);}
        else{ FlowPane fp=new FlowPane(8,8); for(Genre g:prefs){Label chip=new Label("  "+g.getName()+"  ");chip.setStyle("-fx-background-color:#2a0505;-fx-text-fill:"+N_RED+";-fx-border-color:"+N_RED+";-fx-border-radius:20;-fx-background-radius:20;-fx-font-size:11px;-fx-padding:4 8;");fp.getChildren().add(chip);} prefCard.getChildren().add(fp); }
        row2.getChildren().addAll(fCard, fCard2, prefCard);



        main.getChildren().addAll(hero, row1, row2);
        rootScroll.setContent(main);
        tab.setContent(rootScroll);
        return tab;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // HELPERS VISUAIS
    // ═══════════════════════════════════════════════════════════════════════
    private String initials(String n) {
        if(n==null||n.isEmpty())return"?";
        String[] p=n.trim().split("\\s+");
        if(p.length==1)return p[0].substring(0,Math.min(2,p[0].length())).toUpperCase();
        return(""+p[0].charAt(0)+p[p.length-1].charAt(0)).toUpperCase();
    }

    private String avatarColor(String id) {
        String[] pal={"#E50914","#831010","#C62828","#B71C1C","#8B0000","#D32F2F","#960505","#6D0000"};
        return pal[Math.abs(id.hashCode())%pal.length];
    }

    private String followStyle(boolean isMe, boolean jaSegue) {
        if(isMe) return "-fx-background-color:transparent;-fx-text-fill:"+N_MUTED+";-fx-font-size:12px;-fx-padding:6 14;";
        if(jaSegue) return "-fx-background-color:#1a1a1a;-fx-text-fill:"+N_GREEN+";-fx-border-color:"+N_GREEN+";-fx-border-radius:20;-fx-background-radius:20;-fx-padding:6 14;-fx-font-size:12px;-fx-cursor:hand;";
        return "-fx-background-color:"+N_RED+";-fx-text-fill:white;-fx-border-radius:20;-fx-background-radius:20;-fx-padding:6 14;-fx-font-size:12px;-fx-font-weight:bold;-fx-cursor:hand;";
    }

    private Label nbadge(String t) {
        Label l=new Label(t);
        l.setStyle("-fx-background-color:#0d0d0d;-fx-text-fill:"+N_MUTED+";-fx-font-size:11px;-fx-padding:2 8;-fx-background-radius:4;");
        return l;
    }

    private HBox mstat(String icon, int v, String label) {
        Label lv=new Label(icon+" "+v); lv.setStyle("-fx-text-fill:"+N_TEXT+";-fx-font-size:12px;-fx-font-weight:bold;");
        Label ll=new Label(label); ll.setStyle("-fx-text-fill:"+N_MUTED+";-fx-font-size:11px;");
        HBox h=new HBox(4,lv,ll); h.setAlignment(Pos.CENTER_LEFT); return h;
    }

    private VBox statChip(String icon, int v, String label) {
        VBox box=new VBox(2); box.setAlignment(Pos.CENTER); box.setPadding(new Insets(10,16,10,16));
        box.setStyle("-fx-background-color:#0d0d0d;-fx-background-radius:6;");
        Label li=new Label(icon+" "+v); li.setStyle("-fx-text-fill:"+N_TEXT+";-fx-font-size:16px;-fx-font-weight:bold;");
        Label ll=new Label(label); ll.setStyle("-fx-text-fill:"+N_MUTED+";-fx-font-size:10px;");
        box.getChildren().addAll(li,ll); return box;
    }

    private HBox userChip(User u) {
        HBox chip=new HBox(10); chip.setAlignment(Pos.CENTER_LEFT); chip.setPadding(new Insets(10,14,10,14));
        chip.setStyle("-fx-background-color:#0d0d0d;-fx-background-radius:6;-fx-cursor:hand;");
        Label av=new Label(initials(u.getName()));
        av.setStyle("-fx-background-color:"+avatarColor(u.getId())+";-fx-text-fill:white;-fx-font-size:11px;-fx-font-weight:bold;-fx-min-width:32;-fx-min-height:32;-fx-max-width:32;-fx-max-height:32;-fx-alignment:center;-fx-background-radius:4;");
        VBox info=new VBox(2);
        Label ln=new Label(u.getName()); ln.setStyle("-fx-text-fill:"+N_TEXT+";-fx-font-size:13px;-fx-font-weight:bold;");
        Label li=new Label(u.getId()+" • "+u.getRegion()); li.setStyle("-fx-text-fill:"+N_MUTED+";-fx-font-size:11px;");
        info.getChildren().addAll(ln,li); chip.getChildren().addAll(av,info); return chip;
    }

    private VBox nCard(String title) {
        VBox box=new VBox(12); box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color:"+N_CARD+";-fx-background-radius:8;");
        Label lbl=new Label(title.toUpperCase());
        lbl.setStyle("-fx-text-fill:"+N_MUTED+";-fx-font-size:11px;-fx-font-weight:bold;");
        box.getChildren().add(lbl); return box;
    }

    private <T> TableColumn<T,String> col(String h, java.util.function.Function<TableColumn.CellDataFeatures<T,String>,String> fn) {
        TableColumn<T,String> c=new TableColumn<>(h);
        c.setCellValueFactory(d->new SimpleStringProperty(fn.apply(d))); return c;
    }

    private TextField    field(String p)          { TextField f=new TextField(); f.setPromptText(p); f.setStyle(FIELD); return f; }
    private PasswordField  pwd(String p)          { PasswordField f=new PasswordField(); f.setPromptText(p); f.setStyle(FIELD); return f; }
    private Button         btn(String t,String s) { Button b=new Button(t); b.setStyle(s); return b; }
    private Label          lbl(String t)          { Label l=new Label(t); l.setStyle("-fx-text-fill:"+N_MUTED+";-fx-font-size:12px;"); return l; }
    private GridPane      grid()                  { GridPane g=new GridPane(); g.setHgap(10); g.setVgap(10); return g; }
    private HBox           row(javafx.scene.Node... n) { HBox h=new HBox(8,n); h.setAlignment(Pos.CENTER_LEFT); return h; }
    private ScrollPane    scroll(javafx.scene.Node n)  { ScrollPane s=new ScrollPane(n); s.setFitToWidth(true); s.setStyle("-fx-background-color:"+N_BG+";-fx-background:"+N_BG+";"); return s; }
    private void    showAlert(Alert.AlertType t, String title, String msg) { Alert a=new Alert(t); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait(); }
    private String  askInput(String h, String d)  { TextInputDialog td=new TextInputDialog(d); td.setTitle("Editar"); td.setHeaderText(h); return td.showAndWait().orElse(null); }
}