package edu.ufp.streaming.rec.gui;
/*
   Importações necessárias para UI (JavaFX), Modelos e Gestores.
   Notar a biblioteca 'algs4' para as estruturas de dados.
*/
import edu.ufp.streaming.rec.enums.ArtistRole;
import edu.ufp.streaming.rec.managers.AppStateSerializer;
import edu.ufp.streaming.rec.managers.ContentFileManager;
import edu.ufp.streaming.rec.managers.ContentSerializer;
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
 * @author Diogo Vicente
 */
public class AdminDashboardFX {

    // VARIÁVEIS DE ESTILO (CSS)
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

    // ESTILOS REUTILIZÁVEIS - Evita repetição de código CSS
    private static final String FIELD =
            "-fx-background-color:" + N_INPUT + ";-fx-text-fill:" + N_TEXT + ";" +
                    "-fx-prompt-text-fill:" + N_MUTED + ";-fx-border-color:" + N_BORDER + ";" +
                    "-fx-border-radius:4;-fx-background-radius:4;-fx-padding:10 12;-fx-font-size:13px;";// Campos de texto

    private static final String BTN_R =
            "-fx-background-color:" + N_RED + ";-fx-text-fill:white;" +
                    "-fx-font-weight:bold;-fx-background-radius:4;-fx-padding:9 18;-fx-cursor:hand;"; // Botões principais

    private static final String BTN_S =
            "-fx-background-color:" + N_CARD2 + ";-fx-text-fill:" + N_TEXT + ";" +
                    "-fx-border-color:" + N_BORDER + ";-fx-border-radius:4;-fx-background-radius:4;" +
                    "-fx-padding:9 14;-fx-cursor:hand;";

    private static final String BTN_DANGER =
            "-fx-background-color:transparent;-fx-text-fill:#FF5252;" +
                    "-fx-border-color:#FF5252;-fx-border-radius:4;-fx-background-radius:4;" +
                    "-fx-padding:6 12;-fx-cursor:hand;-fx-font-size:11px;";

    private final StreamingDatabase db; // Referência à "Base de Dados" (Motor central)
    private final User adminUser;   // O utilizador que está logado
    private double xOff, yOff; // Para permitir arrastar a janela sem bordas

    private final Label snackLabel = new Label(); // Notificações (ex: "Sucesso")
    private javafx.animation.PauseTransition snackTimer;

    // REQUISITO R2/R3: Callbacks para atualizar a interface quando os dados mudam nas STs ou BSTs
    private Runnable refreshUsersTab;
    private Runnable refreshContentsTab;
    private Runnable refreshArtistsTab;
    private Runnable refreshGenresTab;

    /**
     * CONSTRUTOR: Recebe a instância da BD e o utilizador atual.
     * Demonstra Injeção de Dependência.
     */
    public AdminDashboardFX(StreamingDatabase db, User adminUser) {
        this.db        = db;
        this.adminUser = adminUser;
    }
    /**
     * FUNÇÃO: start()
     * O que faz: Configura o "Stage" (Janela), aplica o CSS global e monta o layout principal.
     */
    public void start(Stage oldStage) {
        // Validação de segurança: apenas administradores entram
        if (adminUser == null) return;
        if (!adminUser.isAdmin()) {
            new StreamingDashboardFX(db, adminUser).start(oldStage);
            return;
        }
        oldStage.close();
        Stage stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);
        BorderPane root = new BorderPane(); // Organiza Top, Center e Bottom
        root.setStyle("-fx-background-color:" + N_BG + ";-fx-background-radius:10;");

        root.setTop(buildNavBar(stage));

        // TabPane: Separação por entidades (Utilizadores, Conteúdos, Artistas, Géneros)
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
        root.setBottom(buildStatusBar()); // Barra de estado inferior

        // Lógica para arrastar a janela (como não tem bordas do Windows)
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
                        ".scroll-bar .thumb{-fx-background-color:" + N_BORDER + ";-fx-background-radius:4;}" +
                        ".combo-box .list-cell{-fx-text-fill:" + N_TEXT + ";-fx-background-color:" + N_CARD + ";}" +
                        ".combo-box-popup .list-cell{-fx-text-fill:" + N_TEXT + ";-fx-background-color:#1A1A1A;}" +
                        ".combo-box-popup .list-cell:hover{-fx-background-color:#3A3A3A;}" +
                        ".combo-box .arrow-button{-fx-background-color:" + N_CARD + ";}" +
                        ".combo-box .arrow{-fx-background-color:" + N_TEXT + ";}";

        Scene scene = new Scene(root, 1280, 820);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add("data:text/css," +
                java.net.URLEncoder.encode(css, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20"));

        stage.setScene(scene);
        stage.centerOnScreen();

        // REQUISITO R11: Grava o estado ao fechar (Persistência)
        stage.setOnCloseRequest(e -> AppStateSerializer.save(db));
        stage.show();
    }
    /**
     * FUNÇÃO: buildNavBar()
     * O que faz: Constrói a barra superior com o logótipo, o nome do Admin e botão de Sair.
     */
    private HBox buildNavBar(Stage stage) {
        HBox bar = new HBox(24);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 24, 0, 24));
        bar.setPrefHeight(64);
        bar.setStyle("-fx-background-color:" + N_BG + ";-fx-border-color:" + N_BORDER + ";-fx-border-width:0 0 1 0;");

        Label logo = new Label("STREAMING APP");
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
        btnX.setOnAction(e -> {
            AppStateSerializer.save(db);
            System.exit(0);
        });

        bar.getChildren().addAll(logo, adminBadge, spacer, avatar, userName, btnLogout, btnX);
        return bar;
    }

    private VBox buildStatusBar() {
        HBox info = new HBox();
        info.setPadding(new Insets(6, 24, 0, 24));
        Label l = new Label("Painel de Administração  |  Bem-vindo, " + adminUser.getName());
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
    /**
     * FUNÇÃO: snack()
     * O que faz: Sistema de notificações visual (tipo Toast).
     * Útil para dar feedback ao Admin sem usar janelas de Alert chatas.
     */
    private void snack(String msg, boolean ok) {
        snackLabel.setText(msg);
        snackLabel.setStyle("-fx-text-fill:" + (ok ? N_GREEN : "#FF5252") + ";-fx-font-size:12px;-fx-font-weight:bold;");
        snackLabel.setVisible(true);
        if (snackTimer != null) snackTimer.stop();
        // Esconde após 3 segundos
        snackTimer = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
        snackTimer.setOnFinished(e -> snackLabel.setVisible(false));
        snackTimer.play();
    }

    private void refreshAllData() {
        if (refreshUsersTab != null) refreshUsersTab.run();
        if (refreshContentsTab != null) refreshContentsTab.run();
        if (refreshArtistsTab != null) refreshArtistsTab.run();
        if (refreshGenresTab != null) refreshGenresTab.run();
    }

    /**
     * FUNÇÃO: buildUsersTab()
     * REQUISITO R2a / R3a: Gestão de Utilizadores.
     * O que faz:
     *  - Listagem (TableView) ligada à ST de Utilizadores.
     *  - Pesquisa: Chama o método searchByNameSubstring() que usa RedBlackBST.
     *  - Criação: cria objeto User e insere na BD.
     *  - Remoção em Cascata (R4): Ao remover aqui, o Manager limpa ligações no Grafo.
     */
    private Tab buildUsersTab() {
        Tab tab = new Tab("👥  Utilizadores");
        BorderPane pane = new BorderPane();
        pane.setStyle("-fx-background-color:" + N_BG + ";");
        pane.setPadding(new Insets(20));

        TableView<User> table = new TableView<>();
        table.setStyle("-fx-background-color:" + N_CARD + ";");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Ligamos diretamente as colunas da tabela aos getters da classe ‘User’ através de Lambdas.
        TableColumn<User,String> cId    = col("ID",       d -> d.getValue().getId());
        TableColumn<User,String> cName  = col("Nome",     d -> d.getValue().getName());
        TableColumn<User,String> cEmail = col("Email",    d -> d.getValue().getEmail());
        TableColumn<User,String> cReg   = col("Região",   d -> d.getValue().getRegion());
        TableColumn<User,String> cDate  = col("Registo",  d -> d.getValue().getRegisterDate().toString());
        TableColumn<User,String> cAdmin = col("Admin",    d -> d.getValue().isAdmin() ? "✔ SIM" : "—");

        table.getColumns().addAll(cId, cName, cEmail, cReg, cDate, cAdmin);

        Runnable refresh = () -> table.getItems().setAll(db.users().listAll());
        refreshUsersTab = refresh;
        refresh.run();

        VBox sidebar = new VBox(16);
        sidebar.setPrefWidth(320);
        sidebar.setPadding(new Insets(0, 0, 0, 20));

        VBox searchCard = card("Pesquisar");
        TextField fSearch = field("Nome ou ID...");
        Button bSearch = btn("Pesquisar", BTN_R), bAll = btn("Todos", BTN_S);
        bSearch.setOnAction(e -> {
            String q = fSearch.getText().trim();
            if (q.isEmpty()) { refresh.run(); return; }
            table.getItems().setAll(db.users().searchByNameSubstring(q));
        });
        bAll.setOnAction(e -> { fSearch.clear(); refresh.run(); });
        searchCard.getChildren().add(row(fSearch, bSearch, bAll));

        VBox createCard = card("Criar Utilizador");
        TextField cUName   = field("Nome completo");
        TextField cUEmail  = field("Email");
        TextField cURegion = field("Região (PT)");
        PasswordField cUPwd = pwd("Password");
        CheckBox cUAdmin   = new CheckBox("Administrador");
        cUAdmin.setStyle("-fx-text-fill:" + N_TEXT + ";-fx-font-size:13px;");
        Button bCreate = btn("Criar", BTN_R);
        bCreate.setMaxWidth(Double.MAX_VALUE);
        bCreate.setOnAction(e -> {
            String id = "usr_" + java.util.UUID.randomUUID().toString().substring(0, 8);
            String nome = cUName.getText().trim();
            String email = cUEmail.getText().trim();
            String regiao = cURegion.getText().trim();
            String pwd = cUPwd.getText();

            if (nome.isEmpty() || pwd.isEmpty()) { snack("Nome e Password são obrigatórios", false); return; }

            User novo = new User(id, nome, email, regiao.isEmpty() ? "PT" : regiao.toUpperCase(), LocalDate.now(), pwd);
            novo.setAdmin(cUAdmin.isSelected());
            db.addUser(novo); // Insere no UserManager e no Grafo simultaneamente
            // Gravação imediata do estado para evitar perda de dados em caso de crash
            AppStateSerializer.save(db);
            refresh.run();
            cUName.clear(); cUEmail.clear(); cURegion.clear(); cUPwd.clear(); cUAdmin.setSelected(false);
            snack("Utilizador '" + nome + "' criado com sucesso", true);
        });
        createCard.getChildren().addAll(cUName, cUEmail, cURegion, cUPwd, cUAdmin, bCreate);

        VBox actionsCard = card("Ações sobre selecionado");
        Button bEditName   = btn("Editar Nome",    BTN_S);
        Button bEditEmail  = btn("Editar Email",   BTN_S);
        Button bEditRegion = btn("Editar Região",  BTN_S);
        Button bToggleAdmin = btn("Promover/Revogar Admin", BTN_R);
        Button bRemove     = btn("Remover Utilizador", BTN_DANGER);

        bEditName.setMaxWidth(Double.MAX_VALUE);
        bEditEmail.setMaxWidth(Double.MAX_VALUE);
        bEditRegion.setMaxWidth(Double.MAX_VALUE);
        bToggleAdmin.setMaxWidth(Double.MAX_VALUE);
        bRemove.setMaxWidth(Double.MAX_VALUE);

        // Edição e validação do retorno da base de dados.
        bEditName.setOnAction(e -> {
            User sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { snack("Seleciona um utilizador da lista primeiro", false); return; }
            String nv = askInput("Novo nome:", sel.getName());
            if (nv == null || nv.trim().isEmpty()) return;
            db.users().editName(sel.getId(), nv.trim());
            AppStateSerializer.save(db); refresh.run();
            snack("Nome atualizado com sucesso", true);
        });
        bEditEmail.setOnAction(e -> {
            User sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { snack("Seleciona um utilizador da lista primeiro", false); return; }
            String nv = askInput("Novo email:", sel.getEmail());
            if (nv == null || nv.trim().isEmpty()) return;
            db.users().editEmail(sel.getId(), nv.trim());
            AppStateSerializer.save(db); refresh.run();
            snack("Email atualizado com sucesso", true);
        });
        bEditRegion.setOnAction(e -> {
            User sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { snack("Seleciona um utilizador da lista primeiro", false); return; }
            String nv = askInput("Nova região:", sel.getRegion());
            if (nv == null || nv.trim().isEmpty()) return;
            db.users().editRegion(sel.getId(), nv.trim().toUpperCase());
            AppStateSerializer.save(db); refresh.run();
            snack("Região atualizada com sucesso", true);
        });
        bToggleAdmin.setOnAction(e -> {
            User sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { snack("Seleciona um utilizador da lista primeiro", false); return; }
            // Proteção para o Admin não revogar os seus próprios privilégios por engano
            if (sel.getId().equals(adminUser.getId())) { snack("Não podes alterar a tua própria conta", false); return; }
            sel.setAdmin(!sel.isAdmin());
            AppStateSerializer.save(db); refresh.run();
            snack(sel.isAdmin() ? sel.getName() + " é agora Admin" : sel.getName() + " já não é Admin", true);
        });
        bRemove.setOnAction(e -> {
            User sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { snack("Seleciona um utilizador da lista primeiro", false); return; }
            if (sel.getId().equals(adminUser.getId())) { snack("Não podes remover a tua própria conta", false); return; }
            if (!confirm("Remover utilizador '" + sel.getName() + "'?")) return;

            // 🎓 PONTO DE DEFESA: Requisito R4 (Remoção em Cascata).
            // O db.removeUser não apaga só o User da ST, avisa o Grafo e limpa as interações/follows!
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
    /**
     * FUNÇÃO: buildContentsTab()
     * REQUISITO R2b / R1: Gestão de Conteúdos (Filmes, Séries, Docs).
     * O que faz:
     *  - Usa 'instanceof' para distinguir os tipos de conteúdos na tabela.
     *  - Permite Gerir Episódios (apenas se for Série) e Adicionar Elenco (R6).
     *  - REQUISITO R10: Botões para Importar/Exportar ficheiros TXT (CSV) e Binário.
     */
    private Tab buildContentsTab() {
        Tab tab = new Tab("🎬  Conteúdos");
        BorderPane pane = new BorderPane();
        pane.setStyle("-fx-background-color:" + N_BG + ";");
        pane.setPadding(new Insets(20));

        javafx.scene.control.TableView<Content> table = new javafx.scene.control.TableView<>();
        table.setStyle("-fx-background-color:" + N_CARD + ";");
        table.setColumnResizePolicy(javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY);

        // 🎓 PONTO DE DEFESA: Operador 'instanceof'. Demonstra o tratamento correto de
        // Polimorfismo / Herança (Requisito R1). Apresenta a string correta dependendo da sub-classe.
        javafx.scene.control.TableColumn<Content,String> cId    = col("ID",       d -> d.getValue().getId());
        javafx.scene.control.TableColumn<Content,String> cTitle = col("Título",   d -> d.getValue().getTitle());
        javafx.scene.control.TableColumn<Content,String> cType  = col("Tipo",     d -> d.getValue() instanceof Movie ? "Filme" : d.getValue() instanceof Series ? "Série" : "Doc");
        javafx.scene.control.TableColumn<Content,String> cGenre = col("Género",   d -> d.getValue().getGenre().getName());
        javafx.scene.control.TableColumn<Content,String> cYear  = col("Ano",      d -> String.valueOf(d.getValue().getReleaseDate().getYear()));
        javafx.scene.control.TableColumn<Content,String> cDur   = col("Dur.(min)",d -> String.valueOf(d.getValue().getDuration()));
        javafx.scene.control.TableColumn<Content,String> cRat   = col("Rating",   d -> String.format("%.1f", d.getValue().getRating()));
        javafx.scene.control.TableColumn<Content,String> cReg   = col("Região",   d -> d.getValue().getRegion());

        // Exemplo de Cast (Cast Explícito para Movie) para invocar o getDirector()
        javafx.scene.control.TableColumn<Content,String> cDir   = col("Realizador", d -> (d.getValue() instanceof Movie && ((Movie) d.getValue()).getDirector() != null) ? ((Movie) d.getValue()).getDirector().getName() : "—");

        table.getColumns().addAll(cId, cTitle, cType, cGenre, cYear, cDur, cRat, cReg, cDir);

        Runnable refresh = () -> table.getItems().setAll(db.contents().listAll());
        refreshContentsTab = refresh;
        refresh.run();

        VBox sidebar = new VBox(16);
        sidebar.setPrefWidth(340);
        sidebar.setPadding(new Insets(0, 0, 0, 20));

        VBox searchCard = card("Pesquisar");
        TextField fSearch = field("Título...");
        Button bS = btn("Pesquisar", BTN_R), bA = btn("Todos", BTN_S);
        bS.setOnAction(e -> table.getItems().setAll(db.contents().searchByTitleSubstring(fSearch.getText().trim())));
        bA.setOnAction(e -> { fSearch.clear(); refresh.run(); });
        searchCard.getChildren().add(row(fSearch, bS, bA));

        VBox createCard = card("Criar Conteúdo");
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
        TextField fCDur   = field("Duração (minutos)");
        TextField cCReg   = field("Região (PT)");

        Button bCreate = btn("Criar", BTN_R);
        bCreate.setMaxWidth(Double.MAX_VALUE);
        bCreate.setOnAction(e -> {
            String id = "cnt_" + java.util.UUID.randomUUID().toString().substring(0, 8);
            String title = cCTitle.getText().trim();
            String region = cCReg.getText().trim();
            Genre genre = cCGenre.getValue();

            if (title.isEmpty() || genre == null) { snack("Título e Género são obrigatórios", false); return; }

            LocalDate date;
            try { date = LocalDate.parse(cCDate.getText().trim()); }
            catch (DateTimeParseException ex) { snack("Data inválida (usa AAAA-MM-DD)", false); return; }
            int dur;
            try { dur = Integer.parseInt(fCDur.getText().trim()); if (dur <= 0) throw new NumberFormatException(); }
            catch (NumberFormatException ex) { snack("Duração inválida", false); return; }

            // Criação polimórfica utilizando a nova estrutura 'Switch Expression' do Java.
            Content novo = switch (cCType.getValue()) {
                case "Série"        -> new Series(id, title, genre, date, dur, region.isEmpty() ? "PT" : region.toUpperCase(), 1);
                case "Documentário" -> new Documentary(id, title, genre, date, dur, region.isEmpty() ? "PT" : region.toUpperCase(), "");
                default             -> new Movie(id, title, genre, date, dur, region.isEmpty() ? "PT" : region.toUpperCase(), null);
            };
            db.addContent(novo);
            AppStateSerializer.save(db); refresh.run();
            cCTitle.clear(); cCDate.clear(); fCDur.clear(); cCReg.clear();
            snack("Conteúdo '" + title + "' criado com sucesso", true);
        });
        createCard.getChildren().addAll(cCTitle, cCType, cCGenre, cCDate, fCDur, cCReg, bCreate);

        // Requisito R10 (Import/Export de Ficheiros Texto e Binário)
        VBox syncCard = card("Sincronização de Dados");
        Button bImportTxt = btn("Importar TXT", BTN_S);
        Button bExportTxt = btn("Exportar TXT", BTN_S);
        Button bExportBin = btn("Exportar Binário", BTN_S);
        Button bImportBin = btn("Importar Binário", BTN_S);

        bImportTxt.setMaxWidth(Double.MAX_VALUE);
        bExportTxt.setMaxWidth(Double.MAX_VALUE);
        bExportBin.setMaxWidth(Double.MAX_VALUE);
        bImportBin.setMaxWidth(Double.MAX_VALUE);

        bImportTxt.setOnAction(e -> {
            if (!adminUser.isAdmin()) { snack("Apenas administradores podem importar", false); return; }
            TextInputDialog dialog = new TextInputDialog("conteudos_compativeis.txt");
            dialog.setTitle("Importar Conteúdos");
            dialog.setHeaderText("Indica o caminho do ficheiro TXT");
            dialog.setGraphic(null);
            dialog.showAndWait().map(String::trim).filter(path -> !path.isEmpty()).ifPresent(path -> {
                ContentFileManager.importContents(db.contents(), db.genres(), path);
                AppStateSerializer.save(db); refreshAllData();
                snack("Conteúdos TXT importados com sucesso", true);
            });
        });

        bExportTxt.setOnAction(e -> {
            ContentFileManager.exportGenres(db.genres(), "generos_exportados.txt");
            ContentFileManager.exportContents(db.contents(), "conteudos_exportados.txt");
            snack("Dados exportados para TXT com sucesso!", true);
        });

        bExportBin.setOnAction(e -> {
            ContentSerializer.exportGenres(db.genres(), "generos.bin");
            ContentSerializer.exportContents(db.contents(), "conteudos.bin");
            snack("Dados exportados para BINÁRIO com sucesso!", true);
        });

        bImportBin.setOnAction(e -> {
            if (!adminUser.isAdmin()) { snack("Apenas administradores podem importar", false); return; }
            ContentSerializer.importGenres(db.genres(), "generos.bin");
            ContentSerializer.importContents(db.contents(), "conteudos.bin");
            AppStateSerializer.save(db);
            refreshAllData();
            snack("Dados importados do BINÁRIO com sucesso!", true);
        });

        syncCard.getChildren().addAll(bImportTxt, bExportTxt, bExportBin, bImportBin);

        VBox actCard = card("Ações sobre selecionado");
        Button bEditTitle  = btn("Editar Título",    BTN_S);
        Button bEditDur    = btn("Editar Duração",   BTN_S);
        Button bEditRegion = btn("Editar Região",    BTN_S);
        Button bEditSeasons     = btn("Editar Temporadas", BTN_S);
        Button bManageEpisodes  = btn("Gerir Episódios",   BTN_S);
        Button bAddCast = btn("Adicionar Elenco/Equipa", BTN_S);
        Button bRemove     = btn("Remover Conteúdo", BTN_DANGER);
        for (Button b : List.of(bEditTitle, bEditDur, bEditRegion, bAddCast, bRemove)) b.setMaxWidth(Double.MAX_VALUE);

        bEditTitle.setOnAction(e -> {
            Content sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { snack("Seleciona um conteúdo da lista primeiro", false); return; }
            String nv = askInput("Novo título:", sel.getTitle());
            if (nv == null || nv.trim().isEmpty()) return;
            db.contents().editTitle(sel.getId(), nv.trim());
            AppStateSerializer.save(db); refresh.run();
            snack("Título atualizado", true);
        });
        bEditDur.setOnAction(e -> {
            Content sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { snack("Seleciona um conteúdo da lista primeiro", false); return; }
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
            if (sel == null) { snack("Seleciona um conteúdo da lista primeiro", false); return; }
            String nv = askInput("Nova região:", sel.getRegion());
            if (nv == null || nv.trim().isEmpty()) return;
            sel.setRegion(nv.trim().toUpperCase());
            AppStateSerializer.save(db); refresh.run();
            snack("Região atualizada", true);
        });

        // Validação de Casts (Type Checking).
        // Garante que não invocamos a gestão de episódios num Filme ou Documentário.
        bManageEpisodes.setOnAction(e -> {
            Content sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { snack("Seleciona um conteúdo da lista primeiro", false); return; }

            // Pattern Matching no instanceof (Introduzido no Java 16+)
            if (!(sel instanceof Series serie)) {
                snack("Apenas séries possuem episódios", false);
                return;
            }

            Stage popup = new Stage();
            popup.initStyle(StageStyle.TRANSPARENT);
            popup.initOwner(table.getScene().getWindow());

            Label lblHeader = new Label("Gerir Episódios de: " + serie.getTitle());
            lblHeader.setStyle("-fx-text-fill: #F5A623; -fx-font-size: 14px; -fx-font-weight: bold;");

            ListView<String> listEpisodes = new ListView<>();
            listEpisodes.setPrefHeight(150);
            listEpisodes.setStyle("-fx-background-color: " + N_INPUT + "; -fx-control-inner-background: " + N_INPUT + ";");

            if (serie.getEpisodes() != null) {
                listEpisodes.getItems().setAll(serie.getEpisodes());
            }

            listEpisodes.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); setStyle(""); }
                    else { setText(item); setStyle("-fx-text-fill: white; -fx-background-color: " + N_INPUT + ";"); }
                }
            });

            TextField fEpisodeName = new TextField();
            fEpisodeName.setPromptText("Nome do Episódio (ex: S01E01 - Piloto)");
            fEpisodeName.setStyle(
                    "-fx-background-color: #E0E0E0; -fx-text-fill: #000000; -fx-prompt-text-fill: #666666;" +
                            "-fx-border-color: " + N_BORDER + "; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 8 10;"
            );

            Button btnAdd = new Button("Adicionar Episódio");
            btnAdd.setStyle("-fx-background-color: " + N_RED+ "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
            btnAdd.setMaxWidth(Double.MAX_VALUE);

            btnAdd.setOnAction(ev -> {
                String epName = fEpisodeName.getText().trim();
                if (epName.isEmpty()) { snack("Digita um nome para o episódio", false); return; }

                serie.addEpisode(epName);
                listEpisodes.getItems().setAll(serie.getEpisodes());
                fEpisodeName.clear();
                AppStateSerializer.save(db);
                snack("Episódio adicionado", true);
            });

            Button btnRemoveEp = new Button("Remover selecionado");
            btnRemoveEp.setStyle("-fx-background-color: transparent; -fx-text-fill: #FF5252; -fx-border-color: #FF5252; -fx-border-radius: 4; -fx-cursor: hand;");
            btnRemoveEp.setMaxWidth(Double.MAX_VALUE);

            btnRemoveEp.setOnAction(ev -> {
                String selectedEp = listEpisodes.getSelectionModel().getSelectedItem();
                if (selectedEp == null) { snack("Seleciona um episódio da lista para o remover", false); return; }

                serie.removeEpisode(selectedEp);
                listEpisodes.getItems().setAll(serie.getEpisodes());
                AppStateSerializer.save(db);
                snack("Episódio removido", true);
            });

            Button btnClose = new Button("Fechar");
            btnClose.setStyle("-fx-background-color: " + N_CARD2 + "; -fx-text-fill: " + N_TEXT + "; -fx-cursor: hand;");
            btnClose.setMaxWidth(Double.MAX_VALUE);
            btnClose.setOnAction(ev -> popup.close());

            VBox boxLayout = new VBox(10, lblHeader, new Separator(), listEpisodes, btnRemoveEp, new Separator(), fEpisodeName, btnAdd, btnClose);
            boxLayout.setPadding(new Insets(15));
            boxLayout.setStyle(
                    "-fx-background-color: " + N_BG + "; -fx-border-color: " + N_GOLD + ";" +
                            "-fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;"
            );

            Scene popupScene = new Scene(boxLayout, 360, 420);
            popupScene.setFill(Color.TRANSPARENT);
            popup.setScene(popupScene);
            popup.centerOnScreen();
            popup.show();
        });


        bEditSeasons.setOnAction(e -> {
            Content sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { snack("Seleciona um conteúdo da lista primeiro", false); return; }

            if (!(sel instanceof Series serie)) {
                snack("Apenas séries possuem o campo de temporadas", false);
                return;
            }

            String nv = askInput("Novo número de temporadas:", String.valueOf(serie.getSeasons()));
            if (nv == null) return;

            try {
                int novasTemporadas = Integer.parseInt(nv.trim());
                if (novasTemporadas <= 0) {
                    snack("O número de temporadas deve ser maior que zero", false);
                    return;
                }

                serie.setSeasons(novasTemporadas);
                AppStateSerializer.save(db);
                refresh.run();
                snack("Número de temporadas atualizado com sucesso!", true);

            } catch (NumberFormatException ex) {
                snack("Valor numérico inválido", false);
            }
        });

        // Associação Artista-Conteúdo (Requisito R6).
        // Aqui criamos as arestas de relacionamento entre um Artista e um Conteúdo,
        // gerindo os papéis de enum (ArtistRole) atribuídos.
        bAddCast.setOnAction(e -> {
            Content sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { snack("Seleciona um conteúdo da lista primeiro", false); return; }

            Stage popup = new Stage();
            popup.initStyle(StageStyle.TRANSPARENT);
            popup.initOwner(table.getScene().getWindow());

            Label lblHeader = new Label("Associa um artista a: " + sel.getTitle());
            lblHeader.setStyle("-fx-text-fill: #F5A623; -fx-font-size: 14px; -fx-font-weight: bold;");

            Label lblId = new Label("ID do Artista:");
            lblId.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 12px; -fx-font-weight: bold;");

            TextField fArtistId = new TextField();
            fArtistId.setPromptText("ID do Artista (ex: a1)");
            fArtistId.setStyle(
                    "-fx-background-color: #E0E0E0;" +
                            "-fx-text-fill: #000000;" +
                            "-fx-prompt-text-fill: #666666;" +
                            "-fx-border-color: " + N_BORDER + ";" +
                            "-fx-border-radius: 4;" +
                            "-fx-background-radius: 4;" +
                            "-fx-padding: 10 12;" +
                            "-fx-font-size: 13px;"
            );

            Label lblRole = new Label("Função no Conteúdo:");
            lblRole.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 12px; -fx-font-weight: bold;");

            ComboBox<ArtistRole> comboRole = new ComboBox<>();
            comboRole.getItems().addAll(ArtistRole.values());
            comboRole.setValue(ArtistRole.ACTOR);
            comboRole.setMaxWidth(Double.MAX_VALUE);
            comboRole.setStyle(
                    "-fx-background-color: #E0E0E0;" +
                            "-fx-text-fill: #000000;" +
                            "-fx-border-color: " + N_BORDER + ";" +
                            "-fx-border-radius: 4;" +
                            "-fx-background-radius: 4;" +
                            "-fx-padding: 5;"
            );

            comboRole.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(ArtistRole item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.toString());
                        setStyle("-fx-text-fill: #000000; -fx-background-color: #E0E0E0;");
                    }
                }
            });

            Button btnConfirm = new Button("Confirmar");
            btnConfirm.setStyle("-fx-background-color: " + N_RED + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-cursor: hand;");
            btnConfirm.setMaxWidth(Double.MAX_VALUE);

            Button btnCancelCustom = new Button("Cancelar");
            btnCancelCustom.setStyle("-fx-background-color: " + N_INPUT + "; -fx-text-fill: " + N_TEXT + "; -fx-padding: 8 16; -fx-cursor: hand;");
            btnCancelCustom.setMaxWidth(Double.MAX_VALUE);

            btnCancelCustom.setOnAction(ev -> popup.close());

            btnConfirm.setOnAction(ev -> {
                String idArt = fArtistId.getText().trim();
                ArtistRole papel = comboRole.getValue();

                if (idArt.isEmpty()) { snack("O ID do artista é obrigatório", false); return; }

                Artist artista = db.artists().get(idArt);
                if (artista == null) { snack("Artista com o ID '" + idArt + "' não existe", false); return; }

                if (sel instanceof Movie && papel == ArtistRole.DIRECTOR) {
                    ((Movie) sel).setDirector(artista);
                }

                db.participations().addParticipation(artista, sel, papel, LocalDate.now());
                AppStateSerializer.save(db);
                refresh.run();
                popup.close();
                snack(artista.getName() + " adicionado como " + papel + " com sucesso!", true);
            });

            VBox boxLayout = new VBox(12, lblHeader, new Separator(), lblId, fArtistId, lblRole, comboRole, new Region(), btnConfirm, btnCancelCustom);
            boxLayout.setPadding(new Insets(20));
            boxLayout.setStyle(
                    "-fx-background-color: " + N_BG + ";" +
                            "-fx-border-color: " + N_GOLD + ";" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 8;" +
                            "-fx-background-radius: 8;"
            );

            Scene popupScene = new Scene(boxLayout, 340, 320);
            popupScene.setFill(Color.TRANSPARENT);
            popup.setScene(popupScene);
            popup.centerOnScreen();
            popup.show();
        });
        bRemove.setOnAction(e -> {
            Content sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { snack("Seleciona um conteúdo da lista primeiro", false); return; }
            if (!confirm("Remover '" + sel.getTitle() + "'?")) return;
            db.removeContent(sel.getId());
            AppStateSerializer.save(db); refresh.run();
            snack("Conteúdo removido", true);
        });
        actCard.getChildren().addAll(bEditTitle, bEditDur, bEditRegion, bAddCast, bRemove,bManageEpisodes, bEditSeasons);

        sidebar.getChildren().addAll(searchCard, createCard, syncCard, actCard);
        pane.setCenter(table);
        pane.setRight(scroll(sidebar));
        tab.setContent(pane);
        return tab;
    }
    /**
     * FUNÇÃO: buildArtistsTab()
     * REQUISITO R2a / R3a: Gestão de Artistas.
     * O que faz: CRUD completo para os artistas.
     * Defesa: Mostra como as Enums (ArtistRole) são usadas no ComboBox.
     */
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
        refreshArtistsTab = refresh;
        refresh.run();

        VBox sidebar = new VBox(16);
        sidebar.setPrefWidth(320);
        sidebar.setPadding(new Insets(0, 0, 0, 20));

        VBox searchCard = card("Pesquisar");
        TextField fSearch = field("Nome...");
        Button bS = btn("Pesquisar", BTN_R), bA = btn("Todos", BTN_S);
        bS.setOnAction(e -> table.getItems().setAll(db.artists().searchByNameSubstring(fSearch.getText().trim())));
        bA.setOnAction(e -> { fSearch.clear(); refresh.run(); });
        searchCard.getChildren().add(row(fSearch, bS, bA));

        VBox createCard = card("Criar Artista");
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
        TextField cADate = field("Data nascimento. (AAAA-MM-DD)");

        Button bCreate = btn("Criar", BTN_R);
        bCreate.setMaxWidth(Double.MAX_VALUE);
        bCreate.setOnAction(e -> {
            String id = "art_" + java.util.UUID.randomUUID().toString().substring(0, 8);
            String nome = cAName.getText().trim();
            String nat = cANat.getText().trim();

            if (nome.isEmpty()) { snack("O Nome é obrigatório", false); return; }

            LocalDate date;
            try { date = LocalDate.parse(cADate.getText().trim()); }
            catch (DateTimeParseException ex) { snack("Data inválida (usa AAAA-MM-DD)", false); return; }
            Artist novo = new Artist(id, nome, nat.isEmpty() ? "PT" : nat, cAGen.getValue(), date, cARole.getValue());
            db.addArtist(novo);
            AppStateSerializer.save(db); refresh.run();
            cAName.clear(); cANat.clear(); cADate.clear();
            snack("Artista '" + nome + "' criado com sucesso", true);
        });
        createCard.getChildren().addAll(cAName, cANat, cAGen, cARole, cADate, bCreate);

        VBox actCard = card("Ações sobre selecionado");
        Button bEditName = btn("Editar Nome",        BTN_S);
        Button bEditNat  = btn("Editar Nacionalidade", BTN_S);
        Button bRemove   = btn("Remover Artista",    BTN_DANGER);
        for (Button b : List.of(bEditName, bEditNat, bRemove)) b.setMaxWidth(Double.MAX_VALUE);

        bEditName.setOnAction(e -> {
            Artist sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { snack("Seleciona um artista da lista primeiro", false); return; }
            String nv = askInput("Novo nome:", sel.getName());
            if (nv == null || nv.trim().isEmpty()) return;
            db.artists().editName(sel.getId(), nv.trim());
            AppStateSerializer.save(db); refresh.run();
            snack("Nome atualizado", true);
        });
        bEditNat.setOnAction(e -> {
            Artist sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { snack("Seleciona um artista da lista primeiro", false); return; }
            String nv = askInput("Nova nacionalidade:", sel.getNationality());
            if (nv == null || nv.trim().isEmpty()) return;
            boolean ok = db.artists().editNationality(sel.getId(), nv.trim());
            if (ok){
                AppStateSerializer.save(db);
                refresh.run();
                snack("Nacionalidade atualizada", true);
            }else {
                snack("Erro ao atualizar a nacionalidade", false);
            }
        });
        bRemove.setOnAction(e -> {
            Artist sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { snack("Seleciona um artista da lista primeiro", false); return; }
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
    /**
     * FUNÇÃO: buildGenresTab()
     * REQUISITO R2b: Gestão de Géneros.
     * O que faz: Permite criar géneros e mostra quantos conteúdos os utilizam.
     * Validação R4: Impede remover géneros que tenham filmes associados (através de try-catch).
     */
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
        refreshGenresTab = refresh;
        refresh.run();

        VBox sidebar = new VBox(16);
        sidebar.setPrefWidth(300);
        sidebar.setPadding(new Insets(0, 0, 0, 20));

        VBox createCard = card("Criar Género");
        TextField cGName = field("Nome do género");
        Button bCreate = btn("Criar", BTN_R);
        bCreate.setMaxWidth(Double.MAX_VALUE);
        bCreate.setOnAction(e -> {
            String id = "gen_" + java.util.UUID.randomUUID().toString().substring(0, 8);
            String nome = cGName.getText().trim();

            if (nome.isEmpty()) { snack("O Nome do género é obrigatório", false); return; }

            db.addGenre(new Genre(id, nome));
            AppStateSerializer.save(db); refresh.run();
            cGName.clear();
            snack("Género '" + nome + "' criado com sucesso", true);
        });
        createCard.getChildren().addAll(cGName, bCreate);

        VBox actCard = card("Ações sobre selecionado");
        Button bRemove = btn("Remover Género", BTN_DANGER);
        bRemove.setMaxWidth(Double.MAX_VALUE);
        bRemove.setOnAction(e -> {
            // Validação na remoção.
            // O try-catch captura a 'IllegalStateException' lançada pelo manager caso
            // o género não possa ser apagado (por exemplo, se já estiver em uso num Filme).
            Genre sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { snack("Seleciona um género da lista primeiro", false); return; }
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

    /**
     * FUNÇÃO: col()
     * O que faz: Atalho para criar colunas de tabelas rapidamente usando Lambdas.
     */
    private <T> javafx.scene.control.TableColumn<T,String> col(String header,
                                                               java.util.function.Function<javafx.scene.control.TableColumn.CellDataFeatures<T,String>,String> fn) {
        javafx.scene.control.TableColumn<T,String> c = new javafx.scene.control.TableColumn<>(header);
        c.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(fn.apply(d)));
        return c;
    }
    /**
     * FUNÇÃO: card()
     * O que faz: Cria um contentor visual (VBox) padronizado, como se fosse um "cartão",
     * com um título em dourado.
     * Usado para agrupar logicamente áreas da interface
     */
    private VBox card(String title) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color:" + N_CARD + ";-fx-background-radius:8;");
        Label lbl = new Label(title.toUpperCase());
        lbl.setStyle("-fx-text-fill:" + N_GOLD + ";-fx-font-size:11px;-fx-font-weight:bold;");
        box.getChildren().add(lbl);
        return box;
    }
    /**
     * FUNÇÃO: field()
     * O que faz: "Fábrica" de campos de texto normais. Instancia um TextField,
     * insere a marca de água (prompt) e aplica a formatação padrão.
     */
    private TextField field(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setStyle(FIELD);
        f.setMaxWidth(Double.MAX_VALUE);
        return f;
    }
    /**
     * FUNÇÃO: pwd()
     * O que faz: Exatamente o mesmo que o field(), mas devolve um PasswordField
     * (onde os caracteres digitados aparecem ocultos, como "••••••").
     */
    private PasswordField pwd(String prompt) {
        PasswordField f = new PasswordField();
        f.setPromptText(prompt);
        f.setStyle(FIELD);
        f.setMaxWidth(Double.MAX_VALUE);
        return f;
    }
    /**
     * FUNÇÃO: btn()
     * O que faz: Construtor rápido para Botões. Recebe o texto e a String com o estilo CSS.
     */
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
    /**
     * FUNÇÃO: initials()
     * O que faz: Pega no nome "Diogo Vicente" e devolve "DV".
     * Usado para o design dos avatares redondos.
     */
    private String initials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[0].charAt(0) + parts[parts.length-1].charAt(0)).toUpperCase();
    }
    /**
     * FUNÇÃO: askInput() / confirm()
     * O que faz: Abre caixas de diálogo (Dialogs) para pedir texto ou confirmação.
     */
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