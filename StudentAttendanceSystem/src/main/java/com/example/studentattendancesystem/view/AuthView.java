package com.example.studentattendancesystem.view;

import com.example.studentattendancesystem.util.DatabaseHandler;
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

    // Make sure 'primaryStage' is passed into the render method:
    public static void render(Stage primaryStage) {
        VBox root = new VBox(15);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("root");

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

            String sql = "SELECT * FROM course_reps WHERE username = ? AND password = ?";

            try (Connection conn = DatabaseHandler.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, username);
                ps.setString(2, password);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String fullName = rs.getString("full_name");
                    System.out.println("Login successful for: " + fullName);

                    // Calls the 2-parameter render method in DashboardView
                    DashboardView.render(primaryStage, fullName);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Authentication Failed", "Invalid username or password.");
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Could not connect to database: " + ex.getMessage());
            }
        });

        root.getChildren().addAll(titleLabel, usernameField, passwordField, loginBtn);

        Scene scene = new Scene(root, 400, 350);
        try {
            scene.getStylesheets().add(AuthView.class.getResource("/theme.css").toExternalForm());
        } catch (Exception ex) {
            System.out.println("theme.css not found, rendering unstyled AuthView.");
        }

        primaryStage.setScene(scene);
        primaryStage.setTitle("BTech Computer Science - Sign In");
        primaryStage.centerOnScreen();
    }

    private static void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}