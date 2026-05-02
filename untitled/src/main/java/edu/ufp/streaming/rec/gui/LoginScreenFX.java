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

    private double xOffset = 0;
    private double yOffset = 0;

    public LoginScreenFX(StreamingDatabase db, Stage stage, Consumer<User> onSuccess) {
        VBox root = new VBox(15);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #0A0A12; -fx-background-radius: 20; -fx-border-radius: 20; -fx-border-color: #E53935; -fx-border-width: 2 0 0 0;");
        root.setAlignment(Pos.CENTER);

        // ── Header (Título e Botão de Fechar) ──
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("▶ StreamingApp");
        title.setStyle("-fx-text-fill: #F0F0F5; -fx-font-size: 22px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS); // Empurra o botão X para a direita

        Button btnCloseTop = new Button("✕");
        btnCloseTop.setStyle("-fx-background-color: transparent; -fx-text-fill: #8C8CA0; -fx-font-size: 16px; -fx-cursor: hand;");
        btnCloseTop.setOnAction(e -> System.exit(0));

        header.getChildren().addAll(title, spacer, btnCloseTop);

        // Label para mensagens de erro/sucesso
        Label msgLabel = new Label(" ");
        msgLabel.setStyle("-fx-text-fill: #FF5050;"); // Vermelho por defeito

        // Estilos CSS globais para caixas e botões
        String fieldStyle = "-fx-background-color: #1C1C2A; -fx-text-fill: white; -fx-border-color: #323246; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 10;";
        String btnPrimary = "-fx-background-color: #E53935; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10 20; -fx-cursor: hand;";
        String btnSecondary = "-fx-background-color: transparent; -fx-text-fill: #8C8CA0; -fx-underline: true; -fx-cursor: hand;";

        // ─────────────────────────────────────────────────────────────────
        // FORMULÁRIO DE LOGIN
        // ─────────────────────────────────────────────────────────────────
        VBox loginForm = new VBox(10);
        loginForm.setAlignment(Pos.CENTER);

        TextField txtLoginId = new TextField();
        txtLoginId.setPromptText("ID de utilizador");
        txtLoginId.setStyle(fieldStyle);

        PasswordField txtLoginPwd = new PasswordField();
        txtLoginPwd.setPromptText("Password");
        txtLoginPwd.setStyle(fieldStyle);

        Button btnLogin = new Button("Entrar");
        btnLogin.setStyle(btnPrimary);
        btnLogin.setMaxWidth(Double.MAX_VALUE);

        Button btnGoToRegister = new Button("Não tens conta? Criar nova conta");
        btnGoToRegister.setStyle(btnSecondary);

        loginForm.getChildren().addAll(txtLoginId, txtLoginPwd, btnLogin, new Label(" "), btnGoToRegister);

        // ─────────────────────────────────────────────────────────────────
        // FORMULÁRIO DE REGISTO
        // ─────────────────────────────────────────────────────────────────
        VBox registerForm = new VBox(10);
        registerForm.setAlignment(Pos.CENTER);
        registerForm.setVisible(false); // Escondido por defeito
        registerForm.setManaged(false); // Não ocupa espaço enquanto estiver escondido

        HBox row1 = new HBox(10);
        TextField txtRegId = new TextField(); txtRegId.setPromptText("ID (ex: u5)"); txtRegId.setStyle(fieldStyle);
        TextField txtRegRegion = new TextField(); txtRegRegion.setPromptText("Região (ex: PT)"); txtRegRegion.setStyle(fieldStyle);
        HBox.setHgrow(txtRegId, Priority.ALWAYS); HBox.setHgrow(txtRegRegion, Priority.ALWAYS);
        row1.getChildren().addAll(txtRegId, txtRegRegion);

        TextField txtRegName = new TextField(); txtRegName.setPromptText("Nome completo"); txtRegName.setStyle(fieldStyle);
        TextField txtRegEmail = new TextField(); txtRegEmail.setPromptText("E-mail"); txtRegEmail.setStyle(fieldStyle);
        PasswordField txtRegPwd = new PasswordField(); txtRegPwd.setPromptText("Password"); txtRegPwd.setStyle(fieldStyle);
        PasswordField txtRegConfirm = new PasswordField(); txtRegConfirm.setPromptText("Confirmar password"); txtRegConfirm.setStyle(fieldStyle);

        Button btnRegister = new Button("Criar Conta");
        btnRegister.setStyle(btnPrimary);
        btnRegister.setMaxWidth(Double.MAX_VALUE);

        Button btnGoToLogin = new Button("Já tens conta? Entrar");
        btnGoToLogin.setStyle(btnSecondary);

        registerForm.getChildren().addAll(row1, txtRegName, txtRegEmail, txtRegPwd, txtRegConfirm, btnRegister, new Label(" "), btnGoToLogin);

        // ─────────────────────────────────────────────────────────────────
        // LÓGICA DOS BOTÕES
        // ─────────────────────────────────────────────────────────────────

        // Alternar para Registo
        btnGoToRegister.setOnAction(e -> {
            loginForm.setVisible(false); loginForm.setManaged(false);
            registerForm.setVisible(true); registerForm.setManaged(true);
            msgLabel.setText(" "); // limpa erros
        });

        // Alternar para Login
        btnGoToLogin.setOnAction(e -> {
            registerForm.setVisible(false); registerForm.setManaged(false);
            loginForm.setVisible(true); loginForm.setManaged(true);
            msgLabel.setText(" "); // limpa erros
        });

        // Ação: Entrar
        btnLogin.setOnAction(e -> {
            String id = txtLoginId.getText().trim();
            String pwd = txtLoginPwd.getText();

            if (id.isEmpty() || pwd.isEmpty()) {
                msgLabel.setStyle("-fx-text-fill: #FF5050;");
                msgLabel.setText("Preenche o ID e Password.");
                return;
            }

            User u = db.authenticate(id, pwd);
            if (u != null) {
                msgLabel.setStyle("-fx-text-fill: #2ECC71;");
                msgLabel.setText("Bem-vindo(a), " + u.getName() + "!");
                onSuccess.accept(u); // Entra na App
            } else {
                msgLabel.setStyle("-fx-text-fill: #FF5050;");
                msgLabel.setText("ID ou password incorretos.");
            }
        });

        // Ação: Criar Conta
        btnRegister.setOnAction(e -> {
            String id = txtRegId.getText().trim();
            String nome = txtRegName.getText().trim();
            String email = txtRegEmail.getText().trim();
            String regiao = txtRegRegion.getText().trim();
            String pwd = txtRegPwd.getText();
            String confirm = txtRegConfirm.getText();

            if (id.isEmpty() || nome.isEmpty() || pwd.isEmpty()) {
                msgLabel.setStyle("-fx-text-fill: #FF5050;");
                msgLabel.setText("ID, Nome e Password são obrigatórios.");
                return;
            }
            if (!pwd.equals(confirm)) {
                msgLabel.setStyle("-fx-text-fill: #FF5050;");
                msgLabel.setText("As passwords não coincidem.");
                return;
            }
            if (db.users().contains(id)) {
                msgLabel.setStyle("-fx-text-fill: #FF5050;");
                msgLabel.setText("O ID \"" + id + "\" já existe no sistema.");
                return;
            }

            String regiaoFinal = regiao.isEmpty() ? "PT" : regiao.toUpperCase();
            User novo = new User(id, nome, email, regiaoFinal, LocalDate.now(), pwd);
            db.addUser(novo);

            msgLabel.setStyle("-fx-text-fill: #2ECC71;");
            msgLabel.setText("Conta criada! A entrar...");
            onSuccess.accept(novo); // Entra na App automaticamente após criar
        });

        // ─────────────────────────────────────────────────────────────────
        // MONTAR A JANELA
        // ─────────────────────────────────────────────────────────────────
        root.getChildren().addAll(header, new Label(" "), msgLabel, loginForm, registerForm);

        Scene scene = new Scene(root, 400, 560);
        scene.setFill(Color.TRANSPARENT);

        // Arrastar a janela
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setScene(scene);
        stage.setTitle("Login / Registo");
        stage.centerOnScreen();
        stage.show();
    }
}