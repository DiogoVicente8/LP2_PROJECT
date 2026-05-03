package edu.ufp.streaming.rec.gui;

import edu.ufp.streaming.rec.managers.StreamingDatabase;
import edu.ufp.streaming.rec.models.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.time.LocalDate;
import java.util.function.Consumer;

public class LoginScreenFX {

    // ── Netflix palette ───────────────────────────────────────────────────
    private static final String N_BG     = "#141414";
    private static final String N_CARD   = "#1F1F1F";
    private static final String N_RED    = "#E50914";
    private static final String N_TEXT   = "#FFFFFF";
    private static final String N_MUTED  = "#A0A0A0";
    private static final String N_INPUT  = "#333333";
    private static final String N_BORDER = "#404040";

    private double xOffset = 0, yOffset = 0;

    public LoginScreenFX(StreamingDatabase db, Stage stage, Consumer<User> onSuccess) {

        // ── Root ─────────────────────────────────────────────────────────
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color:" + N_BG + ";-fx-background-radius:12;");

        // ── Card central ─────────────────────────────────────────────────
        VBox card = new VBox(0);
        card.setAlignment(Pos.TOP_LEFT);
        card.setMaxWidth(420);
        card.setStyle(
                "-fx-background-color:" + N_CARD + ";" +
                        "-fx-background-radius:8;" +
                        "-fx-padding:48 48 40 48;"
        );

        // Drag
        root.setOnMousePressed(ev -> { xOffset = ev.getSceneX(); yOffset = ev.getSceneY(); });
        root.setOnMouseDragged(ev -> { stage.setX(ev.getScreenX()-xOffset); stage.setY(ev.getScreenY()-yOffset); });

        // ── Logo + fechar ─────────────────────────────────────────────────
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label logo = new Label("STREAMINGAPP");
        logo.setStyle(
                "-fx-text-fill:" + N_RED + ";" +
                        "-fx-font-size:26px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-font-family:'Georgia';"
        );

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        Button btnX = new Button("✕");
        btnX.setStyle("-fx-background-color:transparent;-fx-text-fill:"+N_MUTED+";-fx-font-size:14px;-fx-cursor:hand;");
        btnX.setOnAction(e -> System.exit(0));

        topRow.getChildren().addAll(logo, sp, btnX);

        // ── Mensagem de feedback ──────────────────────────────────────────
        Label msgLabel = new Label(" ");
        msgLabel.setStyle("-fx-text-fill:#FF5252;-fx-font-size:13px;");
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(Double.MAX_VALUE);

        // ── Estilos ───────────────────────────────────────────────────────
        String fStyle =
                "-fx-background-color:" + N_INPUT + ";" +
                        "-fx-text-fill:" + N_TEXT + ";" +
                        "-fx-prompt-text-fill:" + N_MUTED + ";" +
                        "-fx-border-color:" + N_BORDER + ";" +
                        "-fx-border-radius:4;-fx-background-radius:4;" +
                        "-fx-padding:14 16;-fx-font-size:14px;";

        String btnRed =
                "-fx-background-color:" + N_RED + ";" +
                        "-fx-text-fill:white;" +
                        "-fx-font-size:16px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-background-radius:4;" +
                        "-fx-padding:14;-fx-cursor:hand;";

        String btnLink =
                "-fx-background-color:transparent;" +
                        "-fx-text-fill:" + N_MUTED + ";" +
                        "-fx-font-size:13px;" +
                        "-fx-cursor:hand;-fx-underline:false;";

        // ── FORMULÁRIO LOGIN ──────────────────────────────────────────────
        VBox loginForm = new VBox(14);
        loginForm.setAlignment(Pos.TOP_LEFT);

        Label loginTitle = new Label("Iniciar Sessão");
        loginTitle.setStyle("-fx-text-fill:"+N_TEXT+";-fx-font-size:28px;-fx-font-weight:bold;");

        TextField fId = new TextField();
        fId.setPromptText("ID de utilizador");
        fId.setStyle(fStyle);
        fId.setMaxWidth(Double.MAX_VALUE);

        PasswordField fPwd = new PasswordField();
        fPwd.setPromptText("Password");
        fPwd.setStyle(fStyle);
        fPwd.setMaxWidth(Double.MAX_VALUE);

        Button btnLogin = new Button("Entrar");
        btnLogin.setStyle(btnRed);
        btnLogin.setMaxWidth(Double.MAX_VALUE);

        Separator sep1 = new Separator();
        sep1.setStyle("-fx-background-color:"+N_BORDER+";-fx-opacity:0.5;");

        HBox regRow = new HBox(4);
        regRow.setAlignment(Pos.CENTER);
        Label regTxt = new Label("Novo no StreamingApp?");
        regTxt.setStyle("-fx-text-fill:"+N_MUTED+";-fx-font-size:13px;");
        Button btnGoReg = new Button("Criar conta.");
        btnGoReg.setStyle(btnLink + "-fx-text-fill:white;-fx-font-weight:bold;");
        regRow.getChildren().addAll(regTxt, btnGoReg);

        loginForm.getChildren().addAll(loginTitle, fId, fPwd, btnLogin, sep1, regRow);

        // ── FORMULÁRIO REGISTO ────────────────────────────────────────────
        VBox registerForm = new VBox(14);
        registerForm.setAlignment(Pos.TOP_LEFT);
        registerForm.setVisible(false);
        registerForm.setManaged(false);

        Label regTitle = new Label("Criar Conta");
        regTitle.setStyle("-fx-text-fill:"+N_TEXT+";-fx-font-size:28px;-fx-font-weight:bold;");

        HBox rRow1 = new HBox(10);
        TextField rId = field(fStyle, "ID (ex: u5)");
        TextField rRegion = field(fStyle, "Região (PT)");
        HBox.setHgrow(rId, Priority.ALWAYS); HBox.setHgrow(rRegion, Priority.ALWAYS);
        rRow1.getChildren().addAll(rId, rRegion);

        TextField rName  = field(fStyle, "Nome completo");
        TextField rEmail = field(fStyle, "E-mail");
        PasswordField rPwd  = pwd(fStyle, "Password");
        PasswordField rConf = pwd(fStyle, "Confirmar password");

        Button btnReg = new Button("Criar Conta");
        btnReg.setStyle(btnRed);
        btnReg.setMaxWidth(Double.MAX_VALUE);

        Separator sep2 = new Separator();
        sep2.setStyle("-fx-background-color:"+N_BORDER+";-fx-opacity:0.5;");

        HBox loginRow = new HBox(4);
        loginRow.setAlignment(Pos.CENTER);
        Label loginTxt = new Label("Já tens conta?");
        loginTxt.setStyle("-fx-text-fill:"+N_MUTED+";-fx-font-size:13px;");
        Button btnGoLogin = new Button("Iniciar sessão.");
        btnGoLogin.setStyle(btnLink + "-fx-text-fill:white;-fx-font-weight:bold;");
        loginRow.getChildren().addAll(loginTxt, btnGoLogin);

        registerForm.getChildren().addAll(regTitle, rRow1, rName, rEmail, rPwd, rConf, btnReg, sep2, loginRow);

        // ── Lógica ────────────────────────────────────────────────────────
        btnGoReg.setOnAction(e -> {
            loginForm.setVisible(false); loginForm.setManaged(false);
            registerForm.setVisible(true); registerForm.setManaged(true);
            msgLabel.setText(" ");
        });
        btnGoLogin.setOnAction(e -> {
            registerForm.setVisible(false); registerForm.setManaged(false);
            loginForm.setVisible(true); loginForm.setManaged(true);
            msgLabel.setText(" ");
        });

        btnLogin.setOnAction(e -> {
            String id = fId.getText().trim(), pwd = fPwd.getText();
            if (id.isEmpty() || pwd.isEmpty()) { msg(msgLabel, "Preenche o ID e Password.", false); return; }
            User u = db.authenticate(id, pwd);
            if (u != null) { msg(msgLabel, "Bem-vindo, " + u.getName() + "!", true); onSuccess.accept(u); }
            else msg(msgLabel, "ID ou password incorretos.", false);
        });

        // Enter no campo de password faz login
        fPwd.setOnAction(e -> btnLogin.fire());

        btnReg.setOnAction(e -> {
            String id = rId.getText().trim(), nome = rName.getText().trim();
            String email = rEmail.getText().trim(), regiao = rRegion.getText().trim();
            String pwd = rPwd.getText(), conf = rConf.getText();
            if (id.isEmpty() || nome.isEmpty() || pwd.isEmpty()) { msg(msgLabel, "ID, Nome e Password são obrigatórios.", false); return; }
            if (!pwd.equals(conf)) { msg(msgLabel, "As passwords não coincidem.", false); return; }
            if (db.users().contains(id)) { msg(msgLabel, "O ID \"" + id + "\" já existe.", false); return; }
            User novo = new User(id, nome, email, regiao.isEmpty() ? "PT" : regiao.toUpperCase(), LocalDate.now(), pwd);
            db.addUser(novo);
            msg(msgLabel, "Conta criada! A entrar...", true);
            onSuccess.accept(novo);
        });

        // ── Montar ────────────────────────────────────────────────────────
        VBox.setMargin(loginTitle,  new Insets(0, 0, 8, 0));
        VBox.setMargin(regTitle,    new Insets(0, 0, 8, 0));
        VBox.setMargin(topRow,      new Insets(0, 0, 32, 0));
        VBox.setMargin(msgLabel,    new Insets(0, 0, 4, 0));

        card.getChildren().addAll(topRow, msgLabel, loginForm, registerForm);

        StackPane.setAlignment(card, Pos.CENTER);
        root.getChildren().add(card);

        Scene scene = new Scene(root, 420, 580);
        scene.setFill(Color.TRANSPARENT);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setScene(scene);
        stage.setTitle("Login");
        stage.centerOnScreen();
        stage.show();
    }

    private TextField    field(String s, String p) { TextField f = new TextField(); f.setPromptText(p); f.setStyle(s); f.setMaxWidth(Double.MAX_VALUE); return f; }
    private PasswordField  pwd(String s, String p) { PasswordField f = new PasswordField(); f.setPromptText(p); f.setStyle(s); f.setMaxWidth(Double.MAX_VALUE); return f; }
    private void           msg(Label l, String t, boolean ok) { l.setText(t); l.setStyle("-fx-text-fill:" + (ok ? "#46D369" : "#FF5252") + ";-fx-font-size:13px;"); }
}