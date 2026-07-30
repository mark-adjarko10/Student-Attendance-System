package com.example.studentattendancesystem.attendance.model;

import com.example.studentattendancesystem.view.AuthView;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    // Public default constructor required by JavaFX Launcher
    public MainApp() {}

    @Override
    public void start(Stage primaryStage) {
        try {
            // Renders initial authentication screen
            AuthView.render(primaryStage);
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Error launching JavaFX Student Attendance System:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
