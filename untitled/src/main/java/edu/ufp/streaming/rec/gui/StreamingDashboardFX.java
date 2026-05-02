package edu.ufp.streaming.rec.gui;

import edu.ufp.streaming.rec.enums.ArtistRole;
import edu.ufp.streaming.rec.enums.InterationType;
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

    // ── Paleta (igual ao LoginScreenFX) ──────────────────────────────────
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

        // ══════════════════════════════════════════════════════════════
        // AQUI: buildMyAccountTab() adicionado no final da lista de tabs
        // ══════════════════════════════════════════════════════════════
        tabs.getTabs().addAll(
                buildUsersTab(),
                buildContentsTab(),
                buildArtistsTab(),
                buildGraphTab(),
                buildMyAccountTab()   // ← NOVA TAB
        );

        root.setCenter(tabs);
        root.setBottom(buildStatusBar());

        root.setOnMousePressed(ev -> { xOff = ev.getSceneX(); yOff = ev.getSceneY(); });
        root.setOnMouseDragged(ev -> { stage.setX(ev.getScreenX()-xOff); stage.setY(ev.getScreenY()-yOff); });

        // CSS para as tabs
        String css = "" +
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
        scene.getStylesheets().add("data:text/css," + java.net.URLEncoder.encode(css, java.nio.charset.StandardCharsets.UTF_8).replace("+","%20"));

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

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Menu mFich = new Menu("  ⚙  Ficheiro  ");
        MenuItem expTxt = new MenuItem("Exportar TXT (R10)");
        MenuItem impTxt = new MenuItem("Importar TXT (R10)");
        MenuItem expBin = new MenuItem("Exportar Binário (R11)");
        MenuItem impBin = new MenuItem("Importar Binário (R11)");
        expTxt.setOnAction(e -> { ContentFileManager.exportGenres(db.genres(),"genres.txt"); ContentFileManager.exportContents(db.contents(),"contents.txt"); showAlert(Alert.AlertType.INFORMATION,"Exportado","Dados exportados para TXT."); });
        impTxt.setOnAction(e -> { ContentFileManager.importGenres(db.genres(),"genres.txt"); ContentFileManager.importContents(db.contents(),db.genres(),"contents.txt"); showAlert(Alert.AlertType.INFORMATION,"Importado","Dados importados de TXT!"); });
        expBin.setOnAction(e -> { ContentSerializer.exportGenres(db.genres(),"genres.bin"); ContentSerializer.exportContents(db.contents(),"contents.bin"); showAlert(Alert.AlertType.INFORMATION,"Serializado","Dados serializados!"); });
        impBin.setOnAction(e -> { ContentSerializer.importGenres(db.genres(),"genres.bin"); ContentSerializer.importContents(db.contents(),"contents.bin"); showAlert(Alert.AlertType.INFORMATION,"Deserializado","Dados importados de binário!"); });
        mFich.getItems().addAll(expTxt, impTxt, new SeparatorMenuItem(), expBin, impBin);
        MenuBar menuBar = new MenuBar(mFich);
        menuBar.setStyle("-fx-background-color:transparent;-fx-padding:0;");

        Button btnLogout = new Button("⏏  Sair da Conta");
        btnLogout.setStyle(
                "-fx-background-color:transparent;" +
                        "-fx-text-fill:" + C_MUTED + ";" +
                        "-fx-border-color:" + C_BORDER + ";" +
                        "-fx-border-radius:8;-fx-background-radius:8;" +
                        "-fx-padding:6 12;-fx-font-size:12px;-fx-cursor:hand;"
        );
        btnLogout.setOnMouseEntered(e -> btnLogout.setStyle(
                "-fx-background-color:" + C_SURFACE + ";" +
                        "-fx-text-fill:" + C_TEXT + ";" +
                        "-fx-border-color:" + C_ACCENT + ";" +
                        "-fx-border-radius:8;-fx-background-radius:8;" +
                        "-fx-padding:6 12;-fx-font-size:12px;-fx-cursor:hand;"
        ));
        btnLogout.setOnMouseExited(e -> btnLogout.setStyle(
                "-fx-background-color:transparent;" +
                        "-fx-text-fill:" + C_MUTED + ";" +
                        "-fx-border-color:" + C_BORDER + ";" +
                        "-fx-border-radius:8;-fx-background-radius:8;" +
                        "-fx-padding:6 12;-fx-font-size:12px;-fx-cursor:hand;"
        ));
        btnLogout.setOnAction(e -> {
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

    // ── Status Bar ────────────────────────────────────────────────────────
    private HBox buildStatusBar() {
        HBox bar = new HBox();
        bar.setPadding(new Insets(8, 20, 10, 20));
        bar.setStyle("-fx-background-color:" + C_SURFACE + ";-fx-background-radius:0 0 14 14;");
        Label l = new Label("LP2 / AED2  •  UFP  •  " + loggedUser.getId() + " @ " + loggedUser.getRegion());
        l.setStyle("-fx-text-fill:" + C_MUTED + ";-fx-font-size:11px;");
        bar.getChildren().add(l);
        return bar;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 1. TAB UTILIZADORES
    // ═══════════════════════════════════════════════════════════════════════
    private Tab buildUsersTab() {
        Tab tab = new Tab("👤  Utilizadores");
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(16));
        pane.setStyle("-fx-background-color:" + C_BG + ";");

        TableView<User> table = new TableView<>();
        table.getColumns().addAll(
                col("ID",           d -> d.getValue().getId()),
                col("Nome",         d -> d.getValue().getName()),
                col("Email",        d -> d.getValue().getEmail()),
                col("Região",       d -> d.getValue().getRegion()),
                col("Data Registo", d -> d.getValue().getRegisterDate().toString())
        );
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        Runnable refresh = () -> table.setItems(FXCollections.observableArrayList(db.users().listAll()));
        refresh.run();

        VBox sidebar = new VBox(14);
        sidebar.setPrefWidth(340); sidebar.setPadding(new Insets(0, 0, 0, 14));

        // Pesquisa
        VBox sCard = card("🔍  Pesquisar por Nome");
        TextField fSearch = field("Nome...");
        Button bSearch = btn("Pesquisar", BTN_P), bAll = btn("Todos", BTN_S);
        bSearch.setOnAction(e -> table.setItems(FXCollections.observableArrayList(db.users().searchByNameSubstring(fSearch.getText().trim()))));
        bAll.setOnAction(e -> refresh.run());
        HBox sr = new HBox(8, fSearch, bSearch, bAll); sr.setAlignment(Pos.CENTER_LEFT); HBox.setHgrow(fSearch, Priority.ALWAYS);
        sCard.getChildren().add(sr);

        // Inserir
        VBox iCard = card("➕  Inserir Utilizador");
        GridPane form = grid();
        TextField fId=field("ID"),fNome=field("Nome"),fEmail=field("Email"),fReg=field("PT");
        PasswordField fPwd=pwd("Password"),fConf=pwd("Confirmar");
        form.addRow(0,lbl("ID:"),fId,lbl("Nome:"),fNome);
        form.addRow(1,lbl("Email:"),fEmail,lbl("Região:"),fReg);
        form.addRow(2,lbl("Password:"),fPwd,lbl("Confirmar:"),fConf);
        Button bAdd=btn("Adicionar",BTN_P), bRem=btn("Remover Sel.",BTN_G);
        bAdd.setOnAction(e -> {
            if(fId.getText().isEmpty()||fNome.getText().isEmpty()||fPwd.getText().isEmpty()){showAlert(Alert.AlertType.ERROR,"Erro","ID, Nome e Password são obrigatórios.");return;}
            if(!fPwd.getText().equals(fConf.getText())){showAlert(Alert.AlertType.ERROR,"Erro","As passwords não coincidem.");return;}
            if(!fEmail.getText().isEmpty()){for(User u:db.users().listAll()){if(u.getEmail().equalsIgnoreCase(fEmail.getText().trim())){showAlert(Alert.AlertType.ERROR,"Email Duplicado","Já registado por '"+u.getName()+"'.");return;}}}
            User u=new User(fId.getText().trim(),fNome.getText().trim(),fEmail.getText().trim(),fReg.getText().trim(),LocalDate.now(),fPwd.getText());
            if(db.addUser(u)){UserPersistenceManager.save(db);refresh.run();fId.clear();fNome.clear();fEmail.clear();fPwd.clear();fConf.clear();}
            else showAlert(Alert.AlertType.ERROR,"Erro","ID já existe.");
        });
        bRem.setOnAction(e -> {User u=table.getSelectionModel().getSelectedItem();if(u!=null){db.removeUser(u.getId());UserPersistenceManager.save(db);refresh.run();}});
        iCard.getChildren().addAll(form, row(bAdd,bRem));

        // Editar / Follows
        VBox eCard = card("✏️  Ações no Utilizador Selecionado");
        Button bEEmail=btn("Editar Email",BTN_S), bEReg=btn("Editar Região",BTN_S), bAltPwd=btn("Alterar Password",BTN_S);
        bEEmail.setOnAction(e -> {
            User u=table.getSelectionModel().getSelectedItem();if(u==null)return;
            String novo=askInput("Novo Email:",u.getEmail());if(novo==null||novo.trim().isEmpty())return;
            for(User o:db.users().listAll()){if(!o.getId().equals(u.getId())&&o.getEmail().equalsIgnoreCase(novo.trim())){showAlert(Alert.AlertType.ERROR,"Duplicado","Já registado por '"+o.getName()+"'.");return;}}
            if(db.users().editEmail(u.getId(),novo.trim())){UserPersistenceManager.save(db);refresh.run();}
        });
        bEReg.setOnAction(e -> {
            User u=table.getSelectionModel().getSelectedItem();if(u==null)return;
            String nova=askInput("Nova Região:",u.getRegion());
            if(nova!=null&&!nova.trim().isEmpty()){db.users().editRegion(u.getId(),nova.trim());UserPersistenceManager.save(db);refresh.run();}
        });
        bAltPwd.setOnAction(e -> {
            User u=table.getSelectionModel().getSelectedItem();if(u==null)return;
            if(!u.getId().equals(loggedUser.getId())){showAlert(Alert.AlertType.WARNING,"Sem permissão","Só podes alterar a tua própria password.");return;}
            PasswordField np=new PasswordField();np.setPromptText("Nova password");
            PasswordField cp=new PasswordField();cp.setPromptText("Confirmar");
            VBox vc=new VBox(8,new Label("Nova password:"),np,new Label("Confirmar:"),cp);vc.setPadding(new Insets(10));
            Dialog<ButtonType> dlg=new Dialog<>();dlg.setTitle("Alterar Password");dlg.getDialogPane().setContent(vc);dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK,ButtonType.CANCEL);
            Optional<ButtonType> res=dlg.showAndWait();if(res.isEmpty()||res.get()!=ButtonType.OK)return;
            if(np.getText().isEmpty()){showAlert(Alert.AlertType.ERROR,"Erro","Password vazia.");return;}
            if(!np.getText().equals(cp.getText())){showAlert(Alert.AlertType.ERROR,"Erro","Não coincidem.");return;}
            db.changePassword(u.getId(),np.getText());UserPersistenceManager.save(db);showAlert(Alert.AlertType.INFORMATION,"Sucesso","Password alterada!");
        });
        TextField fFolId=field("ID a seguir/deixar");
        Button bFol=btn("Seguir",BTN_P),bUnfol=btn("Deixar",BTN_G),bSeg=btn("Seguidores",BTN_S),bSeg2=btn("A Seguir",BTN_S);
        bFol.setOnAction(e -> {User u=table.getSelectionModel().getSelectedItem();String t=fFolId.getText().trim();if(u==null||t.isEmpty())return;if(db.addFollow(u.getId(),t)!=null)showAlert(Alert.AlertType.INFORMATION,"Sucesso",u.getId()+" passou a seguir "+t+" ✓");else showAlert(Alert.AlertType.ERROR,"Erro","Não foi possível.");});
        bUnfol.setOnAction(e -> {User u=table.getSelectionModel().getSelectedItem();String t=fFolId.getText().trim();if(u==null||t.isEmpty())return;if(db.follows().unfollow(u.getId(),t)!=null)showAlert(Alert.AlertType.INFORMATION,"Sucesso","Deixou de seguir "+t+" ✓");else showAlert(Alert.AlertType.ERROR,"Erro","Relação não encontrada.");});
        bSeg.setOnAction(e -> {User u=table.getSelectionModel().getSelectedItem();if(u==null)return;List<User> l=db.follows().getFollowers(u.getId());if(l.isEmpty())showAlert(Alert.AlertType.INFORMATION,"Seguidores",u.getId()+" não tem seguidores.");else{StringBuilder sb=new StringBuilder();l.forEach(s->sb.append("• ").append(s.getName()).append(" (").append(s.getId()).append(")\n"));showAlert(Alert.AlertType.INFORMATION,"Seguidores de "+u.getId(),sb.toString());}});
        bSeg2.setOnAction(e -> {User u=table.getSelectionModel().getSelectedItem();if(u==null)return;List<User> l=db.follows().getFollowing(u.getId());if(l.isEmpty())showAlert(Alert.AlertType.INFORMATION,"A Seguir",u.getId()+" não segue ninguém.");else{StringBuilder sb=new StringBuilder();l.forEach(s->sb.append("• ").append(s.getName()).append(" (").append(s.getId()).append(")\n"));showAlert(Alert.AlertType.INFORMATION,u.getId()+" segue:",sb.toString());}});
        eCard.getChildren().addAll(row(bEEmail,bEReg,bAltPwd),new HBox(8,lbl("ID Alvo:"),fFolId),row(bFol,bUnfol,bSeg,bSeg2));

        sidebar.getChildren().addAll(sCard, iCard, eCard);
        ScrollPane scroll = scroll(sidebar);

        pane.setCenter(table); pane.setRight(scroll);
        tab.setContent(pane); return tab;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 2. TAB CONTEÚDOS
    // ═══════════════════════════════════════════════════════════════════════
    private Tab buildContentsTab() {
        Tab tab = new Tab("🎬  Conteúdos");
        BorderPane pane = new BorderPane(); pane.setPadding(new Insets(16)); pane.setStyle("-fx-background-color:"+C_BG+";");

        TableView<Content> table = new TableView<>();
        table.getColumns().addAll(
                col("ID",              d->d.getValue().getId()),
                col("Tipo",            d->d.getValue() instanceof Movie?"Filme":d.getValue() instanceof Series?"Série":"Documentário"),
                col("Título",          d->d.getValue().getTitle()),
                col("Género",          d->d.getValue().getGenre().getName()),
                col("Data Lançamento", d->d.getValue().getReleaseDate().toString()),
                col("Duração",         d->String.valueOf(d.getValue().getDuration())),
                col("Rating",          d->String.format("%.1f",d.getValue().getRating()))
        );
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        Runnable refresh = () -> table.setItems(FXCollections.observableArrayList(db.contents().listAll()));
        refresh.run();

        VBox sidebar = new VBox(14); sidebar.setPrefWidth(340); sidebar.setPadding(new Insets(0,0,0,14));

        VBox sCard = card("🔍  Pesquisar Conteúdos");
        TextField fTit=field("Título...");
        Button bSTit=btn("Por Título",BTN_P); bSTit.setOnAction(e->table.setItems(FXCollections.observableArrayList(db.contents().searchByTitleSubstring(fTit.getText().trim()))));
        HBox sr1=new HBox(8,fTit,bSTit);sr1.setAlignment(Pos.CENTER_LEFT);HBox.setHgrow(fTit,Priority.ALWAYS);
        ComboBox<String> cbT=new ComboBox<>(FXCollections.observableArrayList("-- Todos --","Filme","Série","Documentário"));cbT.setValue("-- Todos --");cbT.setStyle(FIELD);
        Button bST=btn("Por Tipo",BTN_S),bAll=btn("Todos",BTN_S);
        bST.setOnAction(e->{String s=cbT.getValue();if(s.startsWith("--")){refresh.run();return;}table.setItems(FXCollections.observableArrayList(db.contents().listAll().stream().filter(c->(s.equals("Filme")&&c instanceof Movie)||(s.equals("Série")&&c instanceof Series)||(s.equals("Documentário")&&c instanceof Documentary)).toList()));});
        bAll.setOnAction(e->refresh.run());
        sCard.getChildren().addAll(sr1,new HBox(8,cbT,bST,bAll));

        VBox iCard = card("➕  Inserir Conteúdo");
        GridPane form=grid();
        ComboBox<String> cbType=new ComboBox<>(FXCollections.observableArrayList("Filme","Série","Documentário"));cbType.setValue("Filme");cbType.setStyle(FIELD);
        TextField fId=field("ID"),fTitF=field("Título"),fGId=field("ID Género"),fData=field("2024-01-01"),fDur=field("120"),fReg=field("PT");
        form.addRow(0,lbl("Tipo:"),cbType,lbl("ID:"),fId);
        form.addRow(1,lbl("Título:"),fTitF,lbl("ID Género:"),fGId);
        form.addRow(2,lbl("Data:"),fData,lbl("Duração(m):"),fDur);
        form.addRow(3,lbl("Região:"),fReg);
        Button bAdd=btn("Adicionar",BTN_P),bRem=btn("Remover Sel.",BTN_G);
        bAdd.setOnAction(e->{try{Genre g=db.genres().get(fGId.getText().trim());if(g==null){showAlert(Alert.AlertType.ERROR,"Erro","Género não existe!");return;}LocalDate data=LocalDate.parse(fData.getText().trim());int dur=Integer.parseInt(fDur.getText().trim());Content c=switch(cbType.getValue()){case"Série"->new Series(fId.getText().trim(),fTitF.getText(),g,data,dur,fReg.getText(),1);case"Documentário"->new Documentary(fId.getText().trim(),fTitF.getText(),g,data,dur,fReg.getText(),"","");default->new Movie(fId.getText().trim(),fTitF.getText(),g,data,dur,fReg.getText(),null);};if(db.addContent(c)){refresh.run();fId.clear();fTitF.clear();}else showAlert(Alert.AlertType.ERROR,"Erro","ID já existe.");}catch(Exception ex){showAlert(Alert.AlertType.ERROR,"Erro","Verifica datas/números.");}});
        bRem.setOnAction(e->{Content c=table.getSelectionModel().getSelectedItem();if(c!=null){db.removeContent(c.getId());refresh.run();}});
        iCard.getChildren().addAll(form,row(bAdd,bRem));

        // ── Rating ───────────────────────────────────────────────────────
        VBox rCard = card("⭐  Avaliar Conteúdo Selecionado");

        final int[] currentRating = {0};
        Button[] stars = new Button[5];
        HBox starRow = new HBox(4);
        starRow.setAlignment(Pos.CENTER_LEFT);

        Runnable paintStars = () -> {
            for (int i = 0; i < 5; i++) {
                boolean filled = i < currentRating[0];
                stars[i].setStyle(
                        "-fx-background-color:transparent;" +
                                "-fx-font-size:22px;" +
                                "-fx-cursor:hand;" +
                                "-fx-text-fill:" + (filled ? "#FFD700" : C_BORDER) + ";"
                );
            }
        };

        for (int i = 0; i < 5; i++) {
            final int val = i + 1;
            stars[i] = new Button("★");
            stars[i].setStyle("-fx-background-color:transparent;-fx-font-size:22px;-fx-cursor:hand;-fx-text-fill:"+C_BORDER+";");
            stars[i].setOnMouseEntered(e -> {
                for (int j = 0; j < 5; j++)
                    stars[j].setStyle("-fx-background-color:transparent;-fx-font-size:22px;-fx-cursor:hand;-fx-text-fill:" + (j < val ? "#FFD700" : C_BORDER) + ";");
            });
            stars[i].setOnMouseExited(e -> paintStars.run());
            stars[i].setOnAction(e -> { currentRating[0] = val; paintStars.run(); });
            starRow.getChildren().add(stars[i]);
        }

        Label ratingValLabel = new Label("Seleciona um conteúdo e uma nota");
        ratingValLabel.setStyle("-fx-text-fill:"+C_MUTED+";-fx-font-size:11px;");

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                int existing = (int) Math.round(sel.getRating());
                currentRating[0] = existing;
                paintStars.run();
                ratingValLabel.setText("Rating atual de \"" + sel.getTitle() + "\": " +
                        (existing == 0 ? "sem avaliação" : existing + "/5 ⭐"));
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
            Interation inter = new Interation(loggedUser, c, LocalDateTime.now(),
                    (double) currentRating[0], 0.0, InterationType.RATE, iId);
            db.addInteraction(inter);

            double soma = 0; int count = 0;
            for (User u : db.users().listAll()) {
                for (Interation it : u.getInteractions()) {
                    if (it.getType() == InterationType.RATE
                            && it.getContent().getId().equals(c.getId())) {
                        soma += it.getRating(); count++;
                    }
                }
            }
            if (count > 0) c.setRating(soma / count);

            refresh.run();
            Content updated = db.contents().get(c.getId());
            if (updated != null) {
                table.getSelectionModel().select(updated);
            }
            ratingValLabel.setText("Avaliação submetida: " + currentRating[0] + "/5 ⭐  (média: " + String.format("%.1f", c.getRating()) + ")");
            ratingValLabel.setStyle("-fx-text-fill:"+C_SUCCESS+";-fx-font-size:11px;");
        });

        rCard.getChildren().addAll(starRow, ratingValLabel, bRate);

        sidebar.getChildren().addAll(sCard, iCard, rCard);
        pane.setCenter(table); pane.setRight(scroll(sidebar));
        tab.setContent(pane); return tab;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 3. TAB ARTISTAS
    // ═══════════════════════════════════════════════════════════════════════
    private Tab buildArtistsTab() {
        Tab tab = new Tab("🎭  Artistas");
        BorderPane pane = new BorderPane(); pane.setPadding(new Insets(16)); pane.setStyle("-fx-background-color:"+C_BG+";");

        TableView<Artist> table = new TableView<>();
        table.getColumns().addAll(
                col("ID",            d->d.getValue().getId()),
                col("Nome",          d->d.getValue().getName()),
                col("Nacionalidade", d->d.getValue().getNationality()),
                col("Género",        d->d.getValue().getGender()),
                col("Data Nasc.",    d->d.getValue().getBirthDate().toString()),
                col("Papel",         d->d.getValue().getRole().toString())
        );
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        Runnable refresh = () -> table.setItems(FXCollections.observableArrayList(db.artists().listAll()));
        refresh.run();

        VBox sidebar = new VBox(14); sidebar.setPrefWidth(340); sidebar.setPadding(new Insets(0,0,0,14));

        VBox sCard = card("🔍  Pesquisar Artistas");
        TextField fNome=field("Nome...");
        Button bSN=btn("Por Nome",BTN_P); bSN.setOnAction(e->table.setItems(FXCollections.observableArrayList(db.artists().searchByNameSubstring(fNome.getText().trim()))));
        HBox sr1=new HBox(8,fNome,bSN);sr1.setAlignment(Pos.CENTER_LEFT);HBox.setHgrow(fNome,Priority.ALWAYS);
        ComboBox<ArtistRole> cbRF=new ComboBox<>(FXCollections.observableArrayList(ArtistRole.values()));cbRF.setStyle(FIELD);
        Button bSR=btn("Por Papel",BTN_S),bAll=btn("Todos",BTN_S);
        TextField fData=field("yyyy-MM-dd");
        Button bSD=btn("Por Data Nasc.",BTN_S);
        bSR.setOnAction(e->{if(cbRF.getValue()!=null)table.setItems(FXCollections.observableArrayList(db.artists().searchByRole(cbRF.getValue())));});
        bSD.setOnAction(e->{try{table.setItems(FXCollections.observableArrayList(db.artists().searchByBirthDate(LocalDate.parse(fData.getText().trim()))));}catch(Exception ex){showAlert(Alert.AlertType.ERROR,"Erro","Formato de data inválido.");}});
        bAll.setOnAction(e->refresh.run());
        sCard.getChildren().addAll(sr1,new HBox(8,cbRF,bSR),new HBox(8,fData,bSD,bAll));

        VBox iCard = card("➕  Inserir / Editar Artista");
        GridPane form=grid();
        TextField fId=field("ID"),fNomeA=field("Nome"),fNac=field("PT"),fGen=field("M"),fDataN=field("1980-01-01");
        ComboBox<ArtistRole> cbRole=new ComboBox<>(FXCollections.observableArrayList(ArtistRole.values()));cbRole.setValue(ArtistRole.ACTOR);cbRole.setStyle(FIELD);
        form.addRow(0,lbl("ID:"),fId,lbl("Nome:"),fNomeA);
        form.addRow(1,lbl("Nac.:"),fNac,lbl("Género:"),fGen);
        form.addRow(2,lbl("Data Nasc.:"),fDataN,lbl("Papel:"),cbRole);
        Button bAdd=btn("Adicionar",BTN_P),bRem=btn("Remover",BTN_G),bEN=btn("Editar Nac.",BTN_S);
        bAdd.setOnAction(e->{try{Artist a=new Artist(fId.getText().trim(),fNomeA.getText().trim(),fNac.getText().trim(),fGen.getText().trim(),LocalDate.parse(fDataN.getText().trim()),cbRole.getValue());if(db.addArtist(a)){refresh.run();fId.clear();fNomeA.clear();}else showAlert(Alert.AlertType.ERROR,"Erro","ID de Artista já existe.");}catch(Exception ex){showAlert(Alert.AlertType.ERROR,"Erro","Verifica a data!");}});
        bRem.setOnAction(e->{Artist a=table.getSelectionModel().getSelectedItem();if(a!=null){db.removeArtist(a.getId());refresh.run();}});
        bEN.setOnAction(e->{Artist a=table.getSelectionModel().getSelectedItem();if(a==null)return;String nova=askInput("Nova Nacionalidade:",a.getNationality());if(nova!=null&&!nova.trim().isEmpty()){db.artists().editNationality(a.getId(),nova);refresh.run();}});
        iCard.getChildren().addAll(form,row(bAdd,bRem,bEN));

        sidebar.getChildren().addAll(sCard,iCard);
        pane.setCenter(table); pane.setRight(scroll(sidebar));
        tab.setContent(pane); return tab;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 4. TAB GRAFO / R8
    // ═══════════════════════════════════════════════════════════════════════
    private Tab buildGraphTab() {
        Tab tab = new Tab("🔗  Grafo / R8");
        BorderPane pane = new BorderPane(); pane.setPadding(new Insets(16)); pane.setStyle("-fx-background-color:"+C_BG+";");

        TextArea output = new TextArea();
        output.setEditable(false);
        output.setStyle(
                "-fx-font-family:monospace;-fx-font-size:13px;" +
                        "-fx-background-color:"+C_SURFACE+";-fx-text-fill:"+C_SUCCESS+";" +
                        "-fx-border-color:"+C_BORDER+";-fx-border-radius:8;-fx-background-radius:8;"
        );
        output.setText("// Resultados aparecem aqui...");

        VBox sidebar = new VBox(14); sidebar.setPrefWidth(340); sidebar.setPadding(new Insets(0,0,0,14));

        VBox r8a = card("R8a — Caminho mais curto");
        TextField fOrig=field("ID Origem"),fDest=field("ID Destino");
        Button bCam=btn("Calcular",BTN_P);
        bCam.setOnAction(e->{String o=fOrig.getText().trim(),d=fDest.getText().trim();List<String> path=db.getGraph().caminhoMaisCurtoBetweenUsers(o,d);double peso=db.getGraph().pesoCaminhoMaisCurto(o,d);if(path.isEmpty())output.setText("[R8a] Sem caminho de "+o+" para "+d);else output.setText(String.format("[R8a] Caminho %s → %s:\n  %s\n  Peso total: %.2f",o,d,path,peso));});
        r8a.getChildren().addAll(new HBox(8,lbl("Origem:"),fOrig),new HBox(8,lbl("Destino:"),fDest),bCam);

        VBox r8c = card("R8c — Conectividade forte");
        Button bConexo=btn("Verificar Grafo",BTN_P);
        bConexo.setOnAction(e->{boolean ok=db.getGraph().isGrafoUtilizadoresConexo();output.setText("[R8c] Grafo fortemente conexo: "+(ok?"SIM ✓":"NÃO ✗"));});
        r8c.getChildren().add(bConexo);

        VBox r8g = card("R8g — Seguidores que viram conteúdo (2024)");
        TextField fUId=field("User ID"),fCId=field("Content ID");
        Button bG=btn("Pesquisar",BTN_P);
        bG.setOnAction(e->{String u=fUId.getText().trim(),c=fCId.getText().trim();List<User> list=db.getGraph().seguidoresQueViramConteudo(u,c,LocalDateTime.of(2024,1,1,0,0),LocalDateTime.of(2024,12,31,23,59),db.follows(),db.users());if(list.isEmpty())output.setText("[R8g] Nenhum seguidor de "+u+" viu "+c+" em 2024.");else{StringBuilder sb=new StringBuilder("[R8g] Seguidores de "+u+" que viram "+c+":\n");list.forEach(usr->sb.append("  • ").append(usr.getName()).append("\n"));output.setText(sb.toString());}});
        r8g.getChildren().addAll(new HBox(8,lbl("User ID:"),fUId),new HBox(8,lbl("Content ID:"),fCId),bG);

        Button bInfo=btn("ℹ  Info do Grafo",BTN_S);
        bInfo.setOnAction(e->output.setText(String.format("[GRAFO] Vértices: %d | Arestas: %d",db.getGraph().totalVertices(),db.getGraph().totalArestas())));

        sidebar.getChildren().addAll(r8a,r8c,r8g,bInfo);
        pane.setCenter(output); pane.setRight(scroll(sidebar));
        tab.setContent(pane); return tab;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 5. TAB "A MINHA CONTA"  ← NOVA
    // ═══════════════════════════════════════════════════════════════════════
    private Tab buildMyAccountTab() {
        Tab tab = new Tab("🪪  A Minha Conta");
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(16));
        pane.setStyle("-fx-background-color:" + C_BG + ";");

        // ── Cabeçalho de perfil ──────────────────────────────────────────
        HBox profileHeader = new HBox(16);
        profileHeader.setAlignment(Pos.CENTER_LEFT);
        profileHeader.setPadding(new Insets(14, 18, 14, 18));
        profileHeader.setStyle(
                "-fx-background-color:" + C_SURFACE + ";" +
                        "-fx-border-color:" + C_ACCENT + ";" +
                        "-fx-border-width:0 0 0 4;" +
                        "-fx-border-radius:8;-fx-background-radius:8;"
        );

        Label avatar = new Label("◉");
        avatar.setStyle("-fx-text-fill:" + C_ACCENT + ";-fx-font-size:36px;");

        VBox profileInfo = new VBox(4);
        Label nameLabel = new Label(loggedUser.getName());
        nameLabel.setStyle("-fx-text-fill:" + C_TEXT + ";-fx-font-size:18px;-fx-font-weight:bold;");
        Label detailLabel = new Label(
                loggedUser.getId() + "  •  " + loggedUser.getEmail() +
                        "  •  " + loggedUser.getRegion() +
                        "  •  Registado em " + loggedUser.getRegisterDate()
        );
        detailLabel.setStyle("-fx-text-fill:" + C_MUTED + ";-fx-font-size:12px;");
        profileInfo.getChildren().addAll(nameLabel, detailLabel);

        // Badges de estatísticas
        Region spacerH = new Region();
        HBox.setHgrow(spacerH, Priority.ALWAYS);

        int totalInter  = loggedUser.getInteractions() != null ? loggedUser.getInteractions().size() : 0;
        int totalFollow = db.follows().getFollowing(loggedUser.getId()).size();
        int totalFoll   = db.follows().getFollowers(loggedUser.getId()).size();

        HBox statsHBox = new HBox(12,
                statBadge(String.valueOf(totalInter),  "Interações"),
                statBadge(String.valueOf(totalFollow), "A Seguir"),
                statBadge(String.valueOf(totalFoll),   "Seguidores")
        );
        statsHBox.setAlignment(Pos.CENTER);

        profileHeader.getChildren().addAll(avatar, profileInfo, spacerH, statsHBox);

        // ── Histórico de Interações ──────────────────────────────────────
        VBox histCard = card("🕓  Histórico de Interações");

        TableView<Interation> histTable = new TableView<>();
        histTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Interation, String> colType = new TableColumn<>("Tipo");
        colType.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getType() != null ? d.getValue().getType().toString() : "—"
        ));

        TableColumn<Interation, String> colContent = new TableColumn<>("Conteúdo");
        colContent.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getContent() != null ? d.getValue().getContent().getTitle() : "—"
        ));

        TableColumn<Interation, String> colRating = new TableColumn<>("Avaliação");
        colRating.setCellValueFactory(d -> {
            int r = (int) Math.round(d.getValue().getRating());   // getRating() pode ser double
            return new SimpleStringProperty(r > 0 ? "★".repeat(r) + " (" + r + "/5)" : "—");
        });

        TableColumn<Interation, String> colDate = new TableColumn<>("Data / Hora");
        colDate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getWatchDate() != null
                        ? d.getValue().getWatchDate().toString().replace("T", "  ")
                        : "—"
        ));

        TableColumn<Interation, String> colProgress = new TableColumn<>("Progresso");
        colProgress.setCellValueFactory(d -> {
            double p = d.getValue().getProgress();
            return new SimpleStringProperty(p > 0 ? String.format("%.0f%%", p) : "—");
        });

        histTable.getColumns().addAll(colType, colContent, colRating, colDate, colProgress);

        List<Interation> interactions = loggedUser.getInteractions();
        Runnable loadHistory = () -> {
            if (interactions != null && !interactions.isEmpty()) {
                // Sem ordenação por data (getDateTime não existe) — lista na ordem original
                histTable.setItems(FXCollections.observableArrayList(interactions));
            } else {
                histTable.setPlaceholder(new Label("Ainda não tens interações registadas."));
            }
        };
        loadHistory.run();

        // Filtro por tipo de interação
        ComboBox<String> cbFilter = new ComboBox<>(FXCollections.observableArrayList(
                "-- Todos --", "RATE", "WATCH", "LIKE", "COMMENT"
        ));
        cbFilter.setValue("-- Todos --");
        cbFilter.setStyle(FIELD);
        Button bFilter = btn("Filtrar", BTN_P);
        Button bClearF = btn("Limpar", BTN_S);

        bFilter.setOnAction(e -> {
            String sel = cbFilter.getValue();
            if (sel == null || sel.startsWith("--") || interactions == null) {
                loadHistory.run();
                return;
            }
            List<Interation> filtered = interactions.stream()
                    .filter(it -> it.getType() != null && it.getType().toString().equalsIgnoreCase(sel))
                    .toList();
            histTable.setItems(FXCollections.observableArrayList(filtered));
        });

        bClearF.setOnAction(e -> {
            cbFilter.setValue("-- Todos --");
            loadHistory.run();
        });

        HBox filterRow = new HBox(8, lbl("Filtrar por tipo:"), cbFilter, bFilter, bClearF);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        histCard.getChildren().addAll(filterRow, histTable);

        // ── Coluna central com header + histórico ────────────────────────
        VBox centerBox = new VBox(14, profileHeader, histCard);
        VBox.setVgrow(histCard, Priority.ALWAYS);
        centerBox.setPadding(new Insets(0, 14, 0, 0));

        // ── Sidebar: A Seguir + Seguidores + Recomendações R8d ───────────
        VBox sidebar = new VBox(14);
        sidebar.setPrefWidth(340);

        // A Seguir
        VBox followingCard = card("👣  A Seguir");
        ListView<String> followingList = new ListView<>();
        followingList.setPrefHeight(130);
        followingList.setStyle(
                "-fx-background-color:" + C_SURFACE + ";" +
                        "-fx-border-color:" + C_BORDER + ";-fx-border-radius:6;"
        );
        followingCard.getChildren().add(followingList);

        // Seguidores
        VBox followersCard = card("🌟  Os Meus Seguidores");
        ListView<String> followersList = new ListView<>();
        followersList.setPrefHeight(130);
        followersList.setStyle(
                "-fx-background-color:" + C_SURFACE + ";" +
                        "-fx-border-color:" + C_BORDER + ";-fx-border-radius:6;"
        );
        followersCard.getChildren().add(followersList);

        // Runnable que refresca ambas as listas sempre que necessário
        Runnable refreshFollows = () -> {
            List<User> fw = db.follows().getFollowing(loggedUser.getId());
            if (fw.isEmpty()) {
                followingList.setItems(FXCollections.observableArrayList());
                followingList.setPlaceholder(new Label("Não segues ninguém ainda."));
            } else {
                followingList.setItems(FXCollections.observableArrayList(
                        fw.stream().map(u -> u.getName() + "  (" + u.getId() + ")").toList()
                ));
            }
            List<User> fr = db.follows().getFollowers(loggedUser.getId());
            if (fr.isEmpty()) {
                followersList.setItems(FXCollections.observableArrayList());
                followersList.setPlaceholder(new Label("Ainda não tens seguidores."));
            } else {
                followersList.setItems(FXCollections.observableArrayList(
                        fr.stream().map(u -> u.getName() + "  (" + u.getId() + ")").toList()
                ));
            }
        };
        // Carrega logo na primeira vez
        refreshFollows.run();

        // Recomendações R8d
        VBox recCard = card("✨  Recomendações  (R8d)");
        Label recHint = new Label(
                "Conteúdos apreciados pelos utilizadores que segues e que ainda não viste."
        );
        recHint.setWrapText(true);
        recHint.setStyle("-fx-text-fill:" + C_MUTED + ";-fx-font-size:11px;");

        ListView<String> recList = new ListView<>();
        recList.setPrefHeight(200);
        recList.setStyle(
                "-fx-background-color:" + C_SURFACE + ";" +
                        "-fx-border-color:" + C_BORDER + ";-fx-border-radius:6;"
        );
        recList.setPlaceholder(new Label("Clica em 'Calcular' para ver recomendações."));

        Button bRec = btn("▶  Calcular Recomendações", BTN_P);
        bRec.setMaxWidth(Double.MAX_VALUE);
        bRec.setOnAction(e -> {
            recList.getItems().clear();
            recList.setPlaceholder(new Label("A calcular..."));

            List<User> following = db.follows().getFollowing(loggedUser.getId());
            if (following.isEmpty()) {
                recList.setPlaceholder(new Label("Segue alguém para obteres recomendações!"));
                return;
            }

            // IDs já vistos/avaliados pelo utilizador logado
            java.util.Set<String> seenByMe = new java.util.HashSet<>();
            if (loggedUser.getInteractions() != null) {
                for (Interation it : loggedUser.getInteractions()) {
                    if (it.getContent() != null) seenByMe.add(it.getContent().getId());
                }
            }

            // R8d — percorre interações dos seguidos e sugere conteúdos
            // com RATE ≥ 4 ou LIKE que o utilizador ainda não viu
            {
                java.util.LinkedHashMap<String, String> recs = new java.util.LinkedHashMap<>();
                for (User followed : following) {
                    if (followed.getInteractions() == null) continue;
                    for (Interation it : followed.getInteractions()) {
                        if (it.getContent() == null) continue;
                        String cId = it.getContent().getId();
                        if (seenByMe.contains(cId) || recs.containsKey(cId)) continue;
                        boolean isGood =
                                (it.getType() == InterationType.RATE && (int) Math.round(it.getRating()) >= 4)
                                        || (it.getType() != null && it.getType().toString().equalsIgnoreCase("LIKE"));
                        if (isGood) {
                            recs.put(cId,
                                    it.getContent().getTitle()
                                            + "  ★" + String.format("%.1f", it.getContent().getRating())
                                            + "  [apreciado por " + followed.getName() + "]");
                        }
                    }
                }
                if (recs.isEmpty()) {
                    recList.setPlaceholder(new Label("Sem recomendações novas de momento."));
                } else {
                    recList.setItems(FXCollections.observableArrayList(recs.values()));
                }
            }
        });

        recCard.getChildren().addAll(recHint, bRec, recList);

        sidebar.getChildren().addAll(followingCard, followersCard, recCard);

        SplitPane split = new SplitPane(centerBox, scroll(sidebar));
        split.setDividerPositions(0.65);
        split.setStyle("-fx-background-color:" + C_BG + ";");

        pane.setCenter(split);
        tab.setContent(pane);

        // Refresca follows e badges sempre que o utilizador muda para esta tab
        tab.setOnSelectionChanged(ev -> {
            if (tab.isSelected()) {
                refreshFollows.run();
                // Atualiza badges de estatísticas no cabeçalho
                int ni  = loggedUser.getInteractions() != null ? loggedUser.getInteractions().size() : 0;
                int nfw = db.follows().getFollowing(loggedUser.getId()).size();
                int nfr = db.follows().getFollowers(loggedUser.getId()).size();
                statsHBox.getChildren().setAll(
                        statBadge(String.valueOf(ni),  "Interações"),
                        statBadge(String.valueOf(nfw), "A Seguir"),
                        statBadge(String.valueOf(nfr), "Seguidores")
                );
            }
        });

        return tab;
    }

    // ── Helper: badge de estatística no cabeçalho do perfil ──────────────
    private VBox statBadge(String value, String label) {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(8, 16, 8, 16));
        box.setStyle(
                "-fx-background-color:#12121E;" +
                        "-fx-border-color:" + C_BORDER + ";" +
                        "-fx-border-radius:8;-fx-background-radius:8;"
        );
        Label val = new Label(value);
        val.setStyle("-fx-text-fill:" + C_ACCENT + ";-fx-font-size:20px;-fx-font-weight:bold;");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill:" + C_MUTED + ";-fx-font-size:10px;");
        box.getChildren().addAll(val, lbl);
        return box;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════════════
    private VBox card(String title) {
        VBox box = new VBox(10); box.setPadding(new Insets(14));
        box.setStyle("-fx-background-color:"+C_SURFACE+";-fx-border-color:"+C_BORDER+";-fx-border-radius:10;-fx-background-radius:10;");
        Label lbl = new Label(title);
        lbl.setStyle("-fx-text-fill:"+C_ACCENT+";-fx-font-weight:bold;-fx-font-size:12px;");
        Separator sep = new Separator(); sep.setStyle("-fx-background-color:"+C_BORDER+";");
        box.getChildren().addAll(lbl, sep);
        return box;
    }
    private <T> TableColumn<T, String> col(String h, java.util.function.Function<TableColumn.CellDataFeatures<T,String>,String> fn) {
        TableColumn<T,String> c = new TableColumn<>(h);
        c.setCellValueFactory(d -> new SimpleStringProperty(fn.apply(d)));
        return c;
    }
    private TextField field(String p) { TextField f=new TextField(); f.setPromptText(p); f.setStyle(FIELD); return f; }
    private PasswordField pwd(String p) { PasswordField f=new PasswordField(); f.setPromptText(p); f.setStyle(FIELD); return f; }
    private Button btn(String t, String s) { Button b=new Button(t); b.setStyle(s); return b; }
    private Label lbl(String t) { Label l=new Label(t); l.setStyle("-fx-text-fill:"+C_MUTED+";-fx-font-size:12px;"); return l; }
    private GridPane grid() { GridPane g=new GridPane(); g.setHgap(10); g.setVgap(10); return g; }
    private HBox row(javafx.scene.Node... nodes) { HBox h=new HBox(8,nodes); h.setAlignment(Pos.CENTER_LEFT); return h; }
    private ScrollPane scroll(javafx.scene.Node n) { ScrollPane s=new ScrollPane(n); s.setFitToWidth(true); s.setStyle("-fx-background-color:"+C_BG+";-fx-background:"+C_BG+";"); return s; }
    private void showAlert(Alert.AlertType t, String title, String msg) { Alert a=new Alert(t); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait(); }
    private String askInput(String header, String def) { TextInputDialog d=new TextInputDialog(def); d.setTitle("Editar"); d.setHeaderText(header); return d.showAndWait().orElse(null); }
}