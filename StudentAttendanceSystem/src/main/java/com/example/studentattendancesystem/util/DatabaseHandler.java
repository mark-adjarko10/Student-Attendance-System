package com.example.studentattendancesystem.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseHandler {
    private static final String DATABASE = "attendance_db";
    private static final String URL = "jdbc:mysql://localhost:3306/" + DATABASE;
    private static final String USERNAME = "root";
    private static final String PASSWORD = "attakakera";

    public static Connection getConnection() throws SQLException {
        try {
            // Register driver instance directly
            DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}