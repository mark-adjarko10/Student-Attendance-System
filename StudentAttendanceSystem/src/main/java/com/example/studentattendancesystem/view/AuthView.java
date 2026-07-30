package com.example.studentattendancesystem.view;

import com.example.studentattendancesystem.util.DatabaseHandler;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthView {

    public static void render(Stage primaryStage) {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // --- TAB 1: LOGIN ---
        Tab loginTab = new Tab("Sign In");
        VBox loginBox = createLoginForm(primaryStage);
        loginTab.setContent(loginBox);

        // --- TAB 2: REGISTER ---
        Tab registerTab = new Tab("Register Rep");
        VBox registerBox = createRegisterForm(tabPane);
        registerTab.setContent(registerBox);

        tabPane.getTabs().addAll(loginTab, registerTab);

        Scene scene = new Scene(tabPane, 420, 530);
        try {
            scene.getStylesheets().add(AuthView.class.getResource("/theme.css").toExternalForm());
        } catch (Exception ex) {
            System.out.println("theme.css not found, rendering unstyled AuthView.");
        }

        primaryStage.setScene(scene);
        primaryStage.setTitle("BTech Computer Science - Course Rep Portal");
        primaryStage.centerOnScreen();
    }

    private static VBox createLoginForm(Stage primaryStage) {
        VBox root = new VBox(15);
        root.setPadding(new Insets(25));
        root.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Course Rep Sign In");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(300);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(300);

        Button loginBtn = new Button("Sign In");
        loginBtn.getStyleClass().add("btn-primary");
        loginBtn.setMaxWidth(300);

        loginBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter both username and password.");
                return;
            }

            String sql = "SELECT full_name, academic_level FROM course_reps WHERE username = ? AND password = ?";

            try (Connection conn = DatabaseHandler.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, username);
                ps.setString(2, password);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String fullName = rs.getString("full_name");
                    String repLevel = rs.getString("academic_level");
                    if (repLevel == null || repLevel.isEmpty()) repLevel = "Level 200";

                    System.out.println("Login successful for: " + fullName + " (" + repLevel + ")");

                    // Render Dashboard tailored to this Rep's Level
                    DashboardView.render(primaryStage, fullName, repLevel);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Authentication Failed", "Invalid username or password.");
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Could not connect to database: " + ex.getMessage());
            }
        });

        root.getChildren().addAll(titleLabel, usernameField, passwordField, loginBtn);
        return root;
    }

    private static VBox createRegisterForm(TabPane tabPane) {
        VBox root = new VBox(12);
        root.setPadding(new Insets(25));
        root.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Register Course Rep");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        nameField.setMaxWidth(300);

        ComboBox<String> levelSelect = new ComboBox<>(FXCollections.observableArrayList("Level 100", "Level 200", "Level 300", "Level 400"));
        levelSelect.getSelectionModel().select("Level 200");
        levelSelect.setMaxWidth(300);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(300);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(300);

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");
        confirmPasswordField.setMaxWidth(300);

        Button registerBtn = new Button("Create Account");
        registerBtn.getStyleClass().add("btn-primary");
        registerBtn.setMaxWidth(300);

        registerBtn.setOnAction(e -> {
            String fullName = nameField.getText().trim();
            String repLevel = levelSelect.getValue();
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "All fields are required.");
                return;
            }

            if (!password.equals(confirmPassword)) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Passwords do not match.");
                return;
            }

            String sql = "INSERT INTO course_reps (full_name, academic_level, username, password) VALUES (?, ?, ?, ?)";

            try (Connection conn = DatabaseHandler.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, fullName);
                ps.setString(2, repLevel);
                ps.setString(3, username);
                ps.setString(4, password);

                ps.executeUpdate();

                showAlert(Alert.AlertType.INFORMATION, "Registration Successful", "Account created for " + repLevel + "! You can now sign in.");

                nameField.clear();
                usernameField.clear();
                passwordField.clear();
                confirmPasswordField.clear();

                tabPane.getSelectionModel().select(0);

            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Registration Error", "Username already exists or database issue occurred.");
            }
        });

        root.getChildren().addAll(
                titleLabel, nameField,
                new Label("Assigned Level:"), levelSelect,
                usernameField, passwordField, confirmPasswordField, registerBtn
        );
        return root;
    }

    private static void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
