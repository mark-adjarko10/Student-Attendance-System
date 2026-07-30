module com.example.studentattendancesystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;

    // Export & Open View package for JavaFX
    exports com.example.studentattendancesystem.view;
    opens com.example.studentattendancesystem.view to javafx.fxml, javafx.graphics;

    exports com.example.studentattendancesystem.attendance.model;
    opens com.example.studentattendancesystem.attendance.model to javafx.fxml, javafx.graphics;

    exports com.example.studentattendancesystem.util;
}