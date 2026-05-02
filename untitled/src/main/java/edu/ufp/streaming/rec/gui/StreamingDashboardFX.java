package edu.ufp.streaming.rec.gui;

import edu.ufp.streaming.rec.enums.ArtistRole;
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
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class StreamingDashboardFX {

    private final StreamingDatabase db;
    private final User loggedUser;

    public StreamingDashboardFX(StreamingDatabase db, User loggedUser) {
        this.db = db;
        this.loggedUser = loggedUser;
    }

    public void start(Stage stage) {
        BorderPane root = new BorderPane();

        // -------------------------------------------------------------------
        // MENU BAR (R10 / R11)
        // -------------------------------------------------------------------
        MenuBar menuBar = new MenuBar();
        Menu menuFicheiro = new Menu("Ficheiro");

        MenuItem exportTxt = new MenuItem("Exportar Conteúdos TXT (R10)");
        exportTxt.setOnAction(e -> {
            ContentFileManager.exportGenres(db.genres(), "genres.txt");
            ContentFileManager.exportContents(db.contents(), "contents.txt");
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Dados exportados para genres.txt e contents.txt");
        });

        MenuItem importTxt = new MenuItem("Importar Conteúdos TXT (R10)");
        importTxt.setOnAction(e -> {
            ContentFileManager.importGenres(db.genres(), "genres.txt");
            ContentFileManager.importContents(db.contents(), db.genres(), "contents.txt");
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Dados importados de txt!");
        });

        MenuItem exportBin = new MenuItem("Exportar Binário (R11)");
        exportBin.setOnAction(e -> {
            ContentSerializer.exportGenres(db.genres(), "genres.bin");
            ContentSerializer.exportContents(db.contents(), "contents.bin");
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Dados serializados para binário!");
        });

        MenuItem importBin = new MenuItem("Importar Binário (R11)");
        importBin.setOnAction(e -> {
            ContentSerializer.importGenres(db.genres(), "genres.bin");
            ContentSerializer.importContents(db.contents(), "contents.bin");
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Dados deserializados de binário!");
        });

        menuFicheiro.getItems().addAll(exportTxt, importTxt, new SeparatorMenuItem(), exportBin, importBin);
        menuBar.getMenus().add(menuFicheiro);
        root.setTop(menuBar);

        // -------------------------------------------------------------------
        // TABS
        // -------------------------------------------------------------------
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabPane.getTabs().add(buildUsersTab());
        tabPane.getTabs().add(buildContentsTab());
        tabPane.getTabs().add(buildArtistsTab());
        tabPane.getTabs().add(buildGraphTab());

        root.setCenter(tabPane);

        // Barra de estado inferior
        Label statusLabel = new Label("Plataforma de Streaming | LP2/AED2 | Sessão: " + loggedUser.getName());
        statusLabel.setPadding(new Insets(5));
        statusLabel.setStyle("-fx-background-color: #e0e0e0;");
        root.setBottom(statusLabel);

        Scene scene = new Scene(root, 1100, 750);

        stage.close();
        Stage mainStage = new Stage();
        mainStage.setTitle("Plataforma de Streaming — LP2/AED2");
        mainStage.setScene(scene);
        mainStage.show();
    }

    // =======================================================================
    // 1. TAB UTILIZADORES
    // =======================================================================
    private Tab buildUsersTab() {
        Tab tab = new Tab("👤 Utilizadores");
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(10));

        TableView<User> table = new TableView<>();
        TableColumn<User, String> cId   = new TableColumn<>("ID");
        TableColumn<User, String> cName = new TableColumn<>("Nome");
        TableColumn<User, String> cMail = new TableColumn<>("Email");
        TableColumn<User, String> cReg  = new TableColumn<>("Região");
        TableColumn<User, String> cDate = new TableColumn<>("Data Registo");
        cId.setCellValueFactory(d   -> new SimpleStringProperty(d.getValue().getId()));
        cName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        cMail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        cReg.setCellValueFactory(d  -> new SimpleStringProperty(d.getValue().getRegion()));
        cDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRegisterDate().toString()));
        table.getColumns().addAll(cId, cName, cMail, cReg, cDate);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Runnable refreshTable = () -> table.setItems(FXCollections.observableArrayList(db.users().listAll()));
        refreshTable.run();

        // --- PESQUISA ---
        VBox searchBox = createTitledBox("Pesquisar por Nome");
        HBox sRow = new HBox(10); sRow.setAlignment(Pos.CENTER_LEFT);
        TextField fSearch = new TextField(); fSearch.setPromptText("Nome...");
        Button btnSearch = new Button("Pesquisar");
        Button btnAll    = new Button("Todos");
        btnSearch.setOnAction(e -> table.setItems(FXCollections.observableArrayList(
                db.users().searchByNameSubstring(fSearch.getText().trim()))));
        btnAll.setOnAction(e -> refreshTable.run());
        sRow.getChildren().addAll(fSearch, btnSearch, btnAll);
        searchBox.getChildren().add(sRow);

        // --- FORMULÁRIO INSERIR ---
        VBox formBox = createTitledBox("Inserir Utilizador");
        GridPane form = new GridPane(); form.setHgap(10); form.setVgap(10);
        TextField     fId    = new TextField();
        TextField     fNome  = new TextField();
        TextField     fEmail = new TextField();
        TextField     fRegio = new TextField("PT");
        PasswordField fPwd   = new PasswordField();
        PasswordField fConf  = new PasswordField();
        form.addRow(0, new Label("ID:"),       fId,    new Label("Nome:"),       fNome);
        form.addRow(1, new Label("Email:"),    fEmail, new Label("Região:"),     fRegio);
        form.addRow(2, new Label("Password:"), fPwd,   new Label("Confirmar:"),  fConf);

        Button btnAdd    = new Button("Adicionar");
        Button btnRemove = new Button("Remover Selecionado");

        btnAdd.setOnAction(e -> {
            if (fId.getText().isEmpty() || fNome.getText().isEmpty() || fPwd.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erro", "ID, Nome e Password são obrigatórios."); return;
            }
            if (!fPwd.getText().equals(fConf.getText())) {
                showAlert(Alert.AlertType.ERROR, "Erro", "As passwords não coincidem."); return;
            }
            // Verificar email duplicado
            if (!fEmail.getText().isEmpty()) {
                for (User u : db.users().listAll()) {
                    if (u.getEmail().equalsIgnoreCase(fEmail.getText().trim())) {
                        showAlert(Alert.AlertType.ERROR, "Email Duplicado",
                                "O email já está registado por '" + u.getName() + "'."); return;
                    }
                }
            }
            User u = new User(fId.getText().trim(), fNome.getText().trim(),
                    fEmail.getText().trim(), fRegio.getText().trim(), LocalDate.now(), fPwd.getText());
            if (db.addUser(u)) {
                UserPersistenceManager.save(db); // ← PERSISTÊNCIA
                refreshTable.run();
                fId.clear(); fNome.clear(); fEmail.clear(); fPwd.clear(); fConf.clear();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erro", "ID já existe.");
            }
        });

        btnRemove.setOnAction(e -> {
            User u = table.getSelectionModel().getSelectedItem();
            if (u != null) {
                db.removeUser(u.getId());
                UserPersistenceManager.save(db); // ← PERSISTÊNCIA
                refreshTable.run();
            }
        });
        formBox.getChildren().addAll(form, new HBox(10, btnAdd, btnRemove));

        // --- EDIÇÃO ---
        VBox editBox = createTitledBox("Ações no Utilizador Selecionado");

        // Linha 1: editar campos
        HBox editRow1 = new HBox(10); editRow1.setAlignment(Pos.CENTER_LEFT);
        Button btnEditEmail = new Button("Editar Email");
        btnEditEmail.setOnAction(e -> {
            User u = table.getSelectionModel().getSelectedItem();
            if (u == null) return;
            String novo = askInput("Novo Email:", u.getEmail());
            if (novo == null || novo.trim().isEmpty()) return;
            // Verificar duplicado (excluindo o próprio)
            for (User other : db.users().listAll()) {
                if (!other.getId().equals(u.getId()) && other.getEmail().equalsIgnoreCase(novo.trim())) {
                    showAlert(Alert.AlertType.ERROR, "Email Duplicado",
                            "Email já registado por '" + other.getName() + "'."); return;
                }
            }
            if (db.users().editEmail(u.getId(), novo.trim())) {
                UserPersistenceManager.save(db);
                refreshTable.run();
            }
        });

        Button btnEditReg = new Button("Editar Região");
        btnEditReg.setOnAction(e -> {
            User u = table.getSelectionModel().getSelectedItem();
            if (u == null) return;
            String nova = askInput("Nova Região:", u.getRegion());
            if (nova != null && !nova.trim().isEmpty()) {
                db.users().editRegion(u.getId(), nova.trim());
                UserPersistenceManager.save(db);
                refreshTable.run();
            }
        });

        Button btnAltPwd = new Button("Alterar Password");
        btnAltPwd.setOnAction(e -> {
            User u = table.getSelectionModel().getSelectedItem();
            if (u == null) return;
            // Só o próprio utilizador pode alterar a sua password
            if (!u.getId().equals(loggedUser.getId())) {
                showAlert(Alert.AlertType.WARNING, "Sem permissão",
                        "Só podes alterar a tua própria password."); return;
            }
            // Diálogo com dois campos de password
            PasswordField novaPwd    = new PasswordField(); novaPwd.setPromptText("Nova password");
            PasswordField confirmPwd = new PasswordField(); confirmPwd.setPromptText("Confirmar");
            VBox content = new VBox(8,
                    new Label("Nova password:"), novaPwd,
                    new Label("Confirmar:"),     confirmPwd);
            content.setPadding(new Insets(10));

            Dialog<ButtonType> dlg = new Dialog<>();
            dlg.setTitle("Alterar Password");
            dlg.getDialogPane().setContent(content);
            dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            Optional<ButtonType> res = dlg.showAndWait();
            if (res.isEmpty() || res.get() != ButtonType.OK) return;

            String nova    = novaPwd.getText();
            String confirm = confirmPwd.getText();
            if (nova.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erro", "A password não pode estar vazia."); return;
            }
            if (!nova.equals(confirm)) {
                showAlert(Alert.AlertType.ERROR, "Erro", "As passwords não coincidem."); return;
            }
            db.changePassword(u.getId(), nova);
            UserPersistenceManager.save(db);
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Password alterada com sucesso!");
        });

        editRow1.getChildren().addAll(btnEditEmail, btnEditReg, new Separator(), btnAltPwd);

        // Linha 2: follows
        HBox editRow2 = new HBox(10); editRow2.setAlignment(Pos.CENTER_LEFT);
        TextField fFollowId = new TextField(); fFollowId.setPromptText("ID a seguir/deixar"); fFollowId.setPrefWidth(140);

        Button btnFol = new Button("Seguir");
        btnFol.setOnAction(e -> {
            User u = table.getSelectionModel().getSelectedItem();
            String target = fFollowId.getText().trim();
            if (u == null || target.isEmpty()) return;
            if (db.addFollow(u.getId(), target) != null)
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", u.getId() + " passou a seguir " + target + " ✓");
            else
                showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível criar o follow.");
        });

        Button btnUnfol = new Button("Deixar de Seguir");
        btnUnfol.setOnAction(e -> {
            User u = table.getSelectionModel().getSelectedItem();
            String target = fFollowId.getText().trim();
            if (u == null || target.isEmpty()) return;
            if (db.follows().unfollow(u.getId(), target) != null)
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", u.getId() + " deixou de seguir " + target + " ✓");
            else
                showAlert(Alert.AlertType.ERROR, "Erro", "Relação de follow não encontrada.");
        });

        Button btnVerSeg = new Button("Ver Seguidores");
        btnVerSeg.setOnAction(e -> {
            User u = table.getSelectionModel().getSelectedItem();
            if (u == null) return;
            List<User> segs = db.follows().getFollowers(u.getId());
            if (segs.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Seguidores", u.getId() + " não tem seguidores.");
            } else {
                StringBuilder sb = new StringBuilder("Seguidores de " + u.getId() + ":\n");
                segs.forEach(s -> sb.append("  • ").append(s.getName()).append(" (").append(s.getId()).append(")\n"));
                showAlert(Alert.AlertType.INFORMATION, "Seguidores de " + u.getId(), sb.toString());
            }
        });

        Button btnVerSeguindo = new Button("Ver a Seguir");
        btnVerSeguindo.setOnAction(e -> {
            User u = table.getSelectionModel().getSelectedItem();
            if (u == null) return;
            List<User> seguindo = db.follows().getFollowing(u.getId());
            if (seguindo.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "A Seguir", u.getId() + " não segue ninguém.");
            } else {
                StringBuilder sb = new StringBuilder(u.getId() + " segue:\n");
                seguindo.forEach(s -> sb.append("  • ").append(s.getName()).append(" (").append(s.getId()).append(")\n"));
                showAlert(Alert.AlertType.INFORMATION, "A Seguir — " + u.getId(), sb.toString());
            }
        });

        editRow2.getChildren().addAll(
                new Label("ID Alvo:"), fFollowId, btnFol, btnUnfol,
                new Separator(), btnVerSeg, btnVerSeguindo);
        editBox.getChildren().addAll(editRow1, editRow2);

        VBox controls = new VBox(10);
        controls.getChildren().addAll(searchBox, formBox, editBox);

        pane.setCenter(table);
        ScrollPane scroll = new ScrollPane(controls); scroll.setFitToWidth(true);
        pane.setBottom(scroll);
        tab.setContent(pane);
        return tab;
    }

    // =======================================================================
    // 2. TAB CONTEÚDOS
    // =======================================================================
    private Tab buildContentsTab() {
        Tab tab = new Tab("🎬 Conteúdos");
        BorderPane pane = new BorderPane(); pane.setPadding(new Insets(10));

        TableView<Content> table = new TableView<>();
        TableColumn<Content, String> cId   = new TableColumn<>("ID");
        TableColumn<Content, String> cTipo = new TableColumn<>("Tipo");
        TableColumn<Content, String> cTit  = new TableColumn<>("Título");
        TableColumn<Content, String> cGen  = new TableColumn<>("Género");
        TableColumn<Content, String> cDate = new TableColumn<>("Data Lançamento");
        TableColumn<Content, String> cDur  = new TableColumn<>("Duração");
        TableColumn<Content, String> cRat  = new TableColumn<>("Rating");
        cId.setCellValueFactory(d   -> new SimpleStringProperty(d.getValue().getId()));
        cTipo.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue() instanceof Movie ? "Filme" : d.getValue() instanceof Series ? "Série" : "Documentário"));
        cTit.setCellValueFactory(d  -> new SimpleStringProperty(d.getValue().getTitle()));
        cGen.setCellValueFactory(d  -> new SimpleStringProperty(d.getValue().getGenre().getName()));
        cDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getReleaseDate().toString()));
        cDur.setCellValueFactory(d  -> new SimpleStringProperty(String.valueOf(d.getValue().getDuration())));
        cRat.setCellValueFactory(d  -> new SimpleStringProperty(String.format("%.1f", d.getValue().getRating())));
        table.getColumns().addAll(cId, cTipo, cTit, cGen, cDate, cDur, cRat);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Runnable refreshTable = () -> table.setItems(FXCollections.observableArrayList(db.contents().listAll()));
        refreshTable.run();

        // --- PESQUISA ---
        VBox searchBox = createTitledBox("Pesquisar Conteúdos");
        HBox sRow1 = new HBox(10); sRow1.setAlignment(Pos.CENTER_LEFT);
        TextField fSearchTit = new TextField(); fSearchTit.setPromptText("Título...");
        Button btnSearchTit = new Button("Por Título");
        btnSearchTit.setOnAction(e -> table.setItems(FXCollections.observableArrayList(
                db.contents().searchByTitleSubstring(fSearchTit.getText().trim()))));
        sRow1.getChildren().addAll(new Label("Título:"), fSearchTit, btnSearchTit);

        HBox sRow2 = new HBox(10); sRow2.setAlignment(Pos.CENTER_LEFT);
        ComboBox<String> cbFilterTipo = new ComboBox<>(
                FXCollections.observableArrayList("-- Todos --", "Filme", "Série", "Documentário"));
        cbFilterTipo.setValue("-- Todos --");
        Button btnSearchTipo = new Button("Por Tipo");
        Button btnAllCont    = new Button("Ver Todos");
        btnSearchTipo.setOnAction(e -> {
            String sel = cbFilterTipo.getValue();
            if (sel.startsWith("--")) { refreshTable.run(); return; }
            List<Content> filtrados = db.contents().listAll().stream().filter(c ->
                    (sel.equals("Filme")        && c instanceof Movie)
                            || (sel.equals("Série")      && c instanceof Series)
                            || (sel.equals("Documentário") && c instanceof Documentary)
            ).toList();
            table.setItems(FXCollections.observableArrayList(filtrados));
        });
        btnAllCont.setOnAction(e -> refreshTable.run());
        sRow2.getChildren().addAll(new Label("Tipo:"), cbFilterTipo, btnSearchTipo, btnAllCont);
        searchBox.getChildren().addAll(sRow1, sRow2);

        // --- FORMULÁRIO ---
        VBox formBox = createTitledBox("Inserir Conteúdo");
        GridPane form = new GridPane(); form.setHgap(10); form.setVgap(10);
        ComboBox<String> cbTipo = new ComboBox<>(
                FXCollections.observableArrayList("Filme", "Série", "Documentário")); cbTipo.setValue("Filme");
        TextField fId    = new TextField(); TextField fTit  = new TextField();
        TextField fGenId = new TextField(); TextField fData = new TextField("2024-01-01");
        TextField fDur   = new TextField("120"); TextField fReg = new TextField("PT");
        form.addRow(0, new Label("Tipo:"),            cbTipo, new Label("ID:"),       fId);
        form.addRow(1, new Label("Título:"),           fTit,   new Label("ID Género:"), fGenId);
        form.addRow(2, new Label("Data (yyyy-MM-dd):"), fData, new Label("Duração(m):"), fDur);
        form.add(new Label("Região:"), 0, 3); form.add(fReg, 1, 3);

        Button btnAdd    = new Button("Adicionar");
        Button btnRemove = new Button("Remover Selecionado");
        btnAdd.setOnAction(e -> {
            try {
                Genre g = db.genres().get(fGenId.getText().trim());
                if (g == null) { showAlert(Alert.AlertType.ERROR, "Erro", "Género não existe!"); return; }
                LocalDate data = LocalDate.parse(fData.getText().trim());
                int dur = Integer.parseInt(fDur.getText().trim());
                Content c = switch (cbTipo.getValue()) {
                    case "Série"        -> new Series(fId.getText().trim(), fTit.getText(), g, data, dur, fReg.getText(), 1);
                    case "Documentário" -> new Documentary(fId.getText().trim(), fTit.getText(), g, data, dur, fReg.getText(), "", "");
                    default             -> new Movie(fId.getText().trim(), fTit.getText(), g, data, dur, fReg.getText(), null);
                };
                if (db.addContent(c)) { refreshTable.run(); fId.clear(); fTit.clear(); }
                else showAlert(Alert.AlertType.ERROR, "Erro", "ID já existe.");
            } catch (Exception ex) { showAlert(Alert.AlertType.ERROR, "Erro", "Verifica as datas ou números."); }
        });
        btnRemove.setOnAction(e -> {
            Content c = table.getSelectionModel().getSelectedItem();
            if (c != null) { db.removeContent(c.getId()); refreshTable.run(); }
        });
        formBox.getChildren().addAll(form, new HBox(10, btnAdd, btnRemove));

        VBox controls = new VBox(10); controls.getChildren().addAll(searchBox, formBox);
        pane.setCenter(table);
        ScrollPane scroll = new ScrollPane(controls); scroll.setFitToWidth(true);
        pane.setBottom(scroll);
        tab.setContent(pane);
        return tab;
    }

    // =======================================================================
    // 3. TAB ARTISTAS
    // =======================================================================
    private Tab buildArtistsTab() {
        Tab tab = new Tab("🎭 Artistas");
        BorderPane pane = new BorderPane(); pane.setPadding(new Insets(10));

        TableView<Artist> table = new TableView<>();
        TableColumn<Artist, String> cId      = new TableColumn<>("ID");
        TableColumn<Artist, String> cNome    = new TableColumn<>("Nome");
        TableColumn<Artist, String> cNac     = new TableColumn<>("Nacionalidade");
        TableColumn<Artist, String> cGenT    = new TableColumn<>("Género");
        TableColumn<Artist, String> cDataN   = new TableColumn<>("Data Nasc.");
        TableColumn<Artist, String> cRole    = new TableColumn<>("Papel");
        cId.setCellValueFactory(d    -> new SimpleStringProperty(d.getValue().getId()));
        cNome.setCellValueFactory(d  -> new SimpleStringProperty(d.getValue().getName()));
        cNac.setCellValueFactory(d   -> new SimpleStringProperty(d.getValue().getNationality()));
        cGenT.setCellValueFactory(d  -> new SimpleStringProperty(d.getValue().getGender()));
        cDataN.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBirthDate().toString()));
        cRole.setCellValueFactory(d  -> new SimpleStringProperty(d.getValue().getRole().toString()));
        table.getColumns().addAll(cId, cNome, cNac, cGenT, cDataN, cRole);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Runnable refreshTable = () -> table.setItems(FXCollections.observableArrayList(db.artists().listAll()));
        refreshTable.run();

        // --- PESQUISA ---
        VBox searchBox = createTitledBox("Pesquisar Artistas");
        HBox sRow1 = new HBox(10); sRow1.setAlignment(Pos.CENTER_LEFT);
        TextField fSearchNome = new TextField(); fSearchNome.setPromptText("Nome...");
        Button btnSearchNome  = new Button("Por Nome");
        ComboBox<ArtistRole> cbRoleFilter = new ComboBox<>(FXCollections.observableArrayList(ArtistRole.values()));
        Button btnSearchRole  = new Button("Por Papel");
        btnSearchNome.setOnAction(e -> table.setItems(FXCollections.observableArrayList(
                db.artists().searchByNameSubstring(fSearchNome.getText().trim()))));
        btnSearchRole.setOnAction(e -> {
            if (cbRoleFilter.getValue() != null)
                table.setItems(FXCollections.observableArrayList(db.artists().searchByRole(cbRoleFilter.getValue())));
        });
        sRow1.getChildren().addAll(new Label("Nome:"), fSearchNome, btnSearchNome,
                new Label("Papel:"), cbRoleFilter, btnSearchRole);

        HBox sRow2 = new HBox(10); sRow2.setAlignment(Pos.CENTER_LEFT);
        TextField fSearchData = new TextField(); fSearchData.setPromptText("yyyy-MM-dd");
        Button btnSearchData  = new Button("Por Data Nasc.");
        Button btnAllArt      = new Button("Ver Todos");
        btnSearchData.setOnAction(e -> {
            try { table.setItems(FXCollections.observableArrayList(
                    db.artists().searchByBirthDate(LocalDate.parse(fSearchData.getText().trim())))); }
            catch (Exception ex) { showAlert(Alert.AlertType.ERROR, "Erro", "Formato de data inválido."); }
        });
        btnAllArt.setOnAction(e -> refreshTable.run());
        sRow2.getChildren().addAll(new Label("Data Nasc:"), fSearchData, btnSearchData, btnAllArt);
        searchBox.getChildren().addAll(sRow1, sRow2);

        // --- FORMULÁRIO ---
        VBox formBox = createTitledBox("Inserir / Editar Artista");
        GridPane form = new GridPane(); form.setHgap(10); form.setVgap(10);
        TextField fId   = new TextField(); TextField fNome = new TextField();
        TextField fNac  = new TextField("PT"); TextField fGen = new TextField("M");
        TextField fData = new TextField("1980-01-01");
        ComboBox<ArtistRole> cbRole = new ComboBox<>(FXCollections.observableArrayList(ArtistRole.values()));
        cbRole.setValue(ArtistRole.ACTOR);
        form.addRow(0, new Label("ID:"),             fId,    new Label("Nome:"),           fNome);
        form.addRow(1, new Label("Nacionalidade:"),  fNac,   new Label("Género (M/F):"),    fGen);
        form.addRow(2, new Label("Data Nasc:"),       fData,  new Label("Papel:"),           cbRole);

        Button btnAdd     = new Button("Adicionar");
        Button btnRemove  = new Button("Remover Selecionado");
        Button btnEditNac = new Button("Editar Nacionalidade");

        btnAdd.setOnAction(e -> {
            try {
                Artist a = new Artist(fId.getText().trim(), fNome.getText().trim(),
                        fNac.getText().trim(), fGen.getText().trim(),
                        LocalDate.parse(fData.getText().trim()), cbRole.getValue());
                if (db.addArtist(a)) { refreshTable.run(); fId.clear(); fNome.clear(); }
                else showAlert(Alert.AlertType.ERROR, "Erro", "ID de Artista já existe.");
            } catch (Exception ex) { showAlert(Alert.AlertType.ERROR, "Erro", "Verifica a data!"); }
        });
        btnRemove.setOnAction(e -> {
            Artist a = table.getSelectionModel().getSelectedItem();
            if (a != null) { db.removeArtist(a.getId()); refreshTable.run(); }
        });
        btnEditNac.setOnAction(e -> {
            Artist a = table.getSelectionModel().getSelectedItem();
            if (a == null) return;
            String nova = askInput("Nova Nacionalidade:", a.getNationality());
            if (nova != null && !nova.trim().isEmpty()) {
                db.artists().editNationality(a.getId(), nova);
                refreshTable.run();
            }
        });
        formBox.getChildren().addAll(form, new HBox(10, btnAdd, btnRemove, btnEditNac));

        VBox controls = new VBox(10); controls.getChildren().addAll(searchBox, formBox);
        pane.setCenter(table);
        ScrollPane scroll = new ScrollPane(controls); scroll.setFitToWidth(true);
        pane.setBottom(scroll);

        // ADICIONADO AQUI
        tab.setContent(pane);

        return tab;
    }

    // =======================================================================
    // 4. TAB GRAFO / R8
    // =======================================================================
    private Tab buildGraphTab() {
        Tab tab = new Tab("🔗 Grafo / R8");
        BorderPane pane = new BorderPane(); pane.setPadding(new Insets(10));

        TextArea output = new TextArea();
        output.setEditable(false);
        output.setStyle("-fx-font-family: monospace; -fx-font-size: 14px;");

        VBox controls = new VBox(15); controls.setPadding(new Insets(10, 10, 10, 0));
        controls.setPrefWidth(350);

        // R8a
        VBox r8aBox = createTitledBox("R8a — Caminho mais curto entre utilizadores");
        TextField fOrig = new TextField(); fOrig.setPromptText("ID Origem");
        TextField fDest = new TextField(); fDest.setPromptText("ID Destino");
        Button btnCaminho = new Button("Calcular");
        btnCaminho.setOnAction(e -> {
            String o = fOrig.getText().trim(), d = fDest.getText().trim();
            List<String> path = db.getGraph().caminhoMaisCurtoBetweenUsers(o, d);
            double peso = db.getGraph().pesoCaminhoMaisCurto(o, d);
            if (path.isEmpty()) output.setText("[R8a] Sem caminho de " + o + " para " + d);
            else output.setText(String.format("[R8a] Caminho %s -> %s: %s\n      Peso total: %.2f", o, d, path, peso));
        });
        r8aBox.getChildren().addAll(
                new HBox(5, new Label("Origem:"), fOrig),
                new HBox(5, new Label("Destino:"), fDest),
                btnCaminho);

        // R8c
        VBox r8cBox = createTitledBox("R8c — Grafo é fortemente conexo?");
        Button btnConexo = new Button("Verificar");
        btnConexo.setOnAction(e -> {
            boolean conexo = db.getGraph().isGrafoUtilizadoresConexo();
            output.setText("[R8c] O grafo de utilizadores é fortemente conexo: " + (conexo ? "SIM ✓" : "NÃO ✗"));
        });
        r8cBox.getChildren().add(btnConexo);

        // R8g
        VBox r8gBox = createTitledBox("R8g — Seguidores que viram um conteúdo (2024)");
        TextField fUId = new TextField(); fUId.setPromptText("User ID");
        TextField fCId = new TextField(); fCId.setPromptText("Content ID");
        Button btnG = new Button("Pesquisar");
        btnG.setOnAction(e -> {
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
        r8gBox.getChildren().addAll(
                new HBox(5, new Label("User ID:"), fUId),
                new HBox(5, new Label("Content ID:"), fCId),
                btnG);

        // Info grafo
        Button btnInfo = new Button("ℹ️ Info do Grafo");
        btnInfo.setOnAction(e -> output.setText(String.format("[GRAFO] Vértices: %d | Arestas: %d",
                db.getGraph().totalVertices(), db.getGraph().totalArestas())));

        controls.getChildren().addAll(r8aBox, r8cBox, r8gBox, btnInfo);
        pane.setLeft(controls);
        pane.setCenter(output);

        // ADICIONADO AQUI
        tab.setContent(pane);

        return tab;
    }

    // =======================================================================
    // HELPERS GRÁFICOS
    // =======================================================================

    private VBox createTitledBox(String title) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15, 10, 10, 10));
        box.setStyle("-fx-border-color: #bbbbbb; -fx-border-radius: 5;");
        Label lblTitle = new Label(" " + title + " ");
        lblTitle.setStyle("-fx-background-color: #f4f4f4; -fx-font-weight: bold; -fx-translate-y: -23;");
        VBox.setMargin(lblTitle, new Insets(0, 0, -20, 10));
        box.getChildren().add(lblTitle);
        return box;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(msg);
        alert.showAndWait();
    }

    private String askInput(String header, String defaultVal) {
        TextInputDialog dialog = new TextInputDialog(defaultVal);
        dialog.setTitle("Editar"); dialog.setHeaderText(header);
        Optional<String> res = dialog.showAndWait();
        return res.orElse(null);
    }
}