package com.example.studentattendancesystem.view;

import com.example.studentattendancesystem.attendance.model.Student;
import com.example.studentattendancesystem.util.DatabaseHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;

public class DashboardView {

    private static final ObservableList<Student> studentList = FXCollections.observableArrayList();
    private static TableView<Student> table = new TableView<>();

    // Primary 3-parameter render method
    public static void render(Stage stage, String repName, Node themeBtn) {
        BorderPane root = new BorderPane();
        root.getStyleClass().addAll("root", "light-mode");

        // Header
        HBox header = new HBox(15);
        header.getStyleClass().add("header-bar");
        header.setAlignment(Pos.CENTER_LEFT);

        Label brand = new Label("Student Attendance Dashboard | Welcome, " + repName);
        brand.getStyleClass().add("header-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        if (themeBtn != null) {
            header.getChildren().addAll(brand, spacer, themeBtn);
        } else {
            header.getChildren().addAll(brand, spacer);
        }
        root.setTop(header);

        // Sidebar - Student Form
        VBox formCard = new VBox(12);
        formCard.getStyleClass().add("card-panel");
        formCard.setPrefWidth(300);

        Label formTitle = new Label("Student Registration");
        formTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");

        TextField indexInput = new TextField();
        indexInput.setPromptText("Index Number");

        TextField nameInput = new TextField();
        nameInput.setPromptText("Full Name");

        ComboBox<String> levelSelect = new ComboBox<>(FXCollections.observableArrayList("Level 100", "Level 200", "Level 300", "Level 400"));
        levelSelect.getSelectionModel().select("Level 200");

        Label classLabel = new Label("Class Section:");
        ToggleGroup classGroup = new ToggleGroup();
        HBox radioBox = new HBox(8);
        for (String group : new String[]{"A", "B", "C", "D"}) {
            RadioButton rb = new RadioButton(group);
            rb.setToggleGroup(classGroup);
            if (group.equals("A")) rb.setSelected(true);
            radioBox.getChildren().add(rb);
        }

        Button addBtn = new Button("Add Student");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setMaxWidth(Double.MAX_VALUE);

        Button updateBtn = new Button("Update Name");
        updateBtn.getStyleClass().add("btn-secondary");
        updateBtn.setMaxWidth(Double.MAX_VALUE);

        Button deleteBtn = new Button("Delete Student");
        deleteBtn.getStyleClass().add("btn-danger");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);

        formCard.getChildren().addAll(
                formTitle, new Label("Index Number"), indexInput,
                new Label("Full Name"), nameInput,
                new Label("Academic Level"), levelSelect,
                classLabel, radioBox, addBtn, updateBtn, deleteBtn
        );

        // Center Area - Filters & Table
        VBox centerBox = new VBox(15);
        centerBox.setPadding(new Insets(15));

        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.getStyleClass().add("card-panel");

        ComboBox<String> courseSelect = new ComboBox<>();
        loadCourses(courseSelect);

        ComboBox<String> filterLevel = new ComboBox<>(FXCollections.observableArrayList("Level 100", "Level 200", "Level 300", "Level 400"));
        filterLevel.getSelectionModel().select("Level 200");

        ComboBox<String> filterClass = new ComboBox<>(FXCollections.observableArrayList("A", "B", "C", "D"));
        filterClass.getSelectionModel().select("A");

        DatePicker datePicker = new DatePicker(LocalDate.now());

        Button fetchBtn = new Button("Load Class");
        fetchBtn.getStyleClass().add("btn-secondary");

        filterBar.getChildren().addAll(
                new Label("Course:"), courseSelect,
                new Label("Level:"), filterLevel,
                new Label("Class:"), filterClass,
                new Label("Date:"), datePicker, fetchBtn
        );

        // Table Setup
        TableColumn<Student, String> colIndex = new TableColumn<>("Index Number");
        colIndex.setCellValueFactory(data -> data.getValue().indexNumberProperty());

        TableColumn<Student, String> colName = new TableColumn<>("Student Name");
        colName.setCellValueFactory(data -> data.getValue().fullNameProperty());

        TableColumn<Student, String> colLevel = new TableColumn<>("Level");
        colLevel.setCellValueFactory(data -> data.getValue().levelProperty());

        TableColumn<Student, String> colClass = new TableColumn<>("Class");
        colClass.setCellValueFactory(data -> data.getValue().classGroupProperty());

        TableColumn<Student, String> colStatus = new TableColumn<>("Attendance");
        colStatus.setCellValueFactory(data -> data.getValue().attendanceStatusProperty());
        colStatus.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<String> statusBox = new ComboBox<>(FXCollections.observableArrayList("PRESENT", "ABSENT", "LATE"));

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    statusBox.setValue(getItem());
                    statusBox.setOnAction(e -> {
                        Student s = getTableView().getItems().get(getIndex());
                        s.setAttendanceStatus(statusBox.getValue());
                    });
                    setGraphic(statusBox);
                }
            }
        });

        table.getColumns().setAll(colIndex, colName, colLevel, colClass, colStatus);
        table.setItems(studentList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button saveAttendanceBtn = new Button("Submit Daily Attendance");
        saveAttendanceBtn.getStyleClass().add("btn-primary");

        centerBox.getChildren().addAll(filterBar, table, saveAttendanceBtn);

        root.setLeft(formCard);
        root.setCenter(centerBox);

        // Initial table load
        loadStudents(filterLevel.getValue(), filterClass.getValue());

        // Action Handlers
        fetchBtn.setOnAction(e -> loadStudents(filterLevel.getValue(), filterClass.getValue()));

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                indexInput.setText(newSel.getIndexNumber());
                nameInput.setText(newSel.getFullName());
            }
        });

        addBtn.setOnAction(e -> {
            RadioButton selectedRadio = (RadioButton) classGroup.getSelectedToggle();
            if (indexInput.getText().trim().isEmpty() || nameInput.getText().trim().isEmpty()) {
                showAlert("Please enter both Index Number and Full Name.");
                return;
            }

            try (Connection conn = DatabaseHandler.getConnection()) {
                String sql = "INSERT INTO students (index_number, full_name, programme, level, class_group) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, indexInput.getText().trim());
                ps.setString(2, nameInput.getText().trim());
                ps.setString(3, "BTech Computer Science");
                ps.setString(4, levelSelect.getValue());
                ps.setString(5, selectedRadio.getText());
                ps.executeUpdate();

                indexInput.clear();
                nameInput.clear();
                loadStudents(filterLevel.getValue(), filterClass.getValue());
                showAlert("Student added successfully!");
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert("Error: Index Number already exists or database query failed.");
            }
        });

        updateBtn.setOnAction(e -> {
            Student selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Select a student from the table first.");
                return;
            }

            try (Connection conn = DatabaseHandler.getConnection()) {
                String sql = "UPDATE students SET full_name = ? WHERE id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, nameInput.getText().trim());
                ps.setInt(2, selected.getId());
                ps.executeUpdate();

                loadStudents(filterLevel.getValue(), filterClass.getValue());
                showAlert("Student updated successfully!");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        deleteBtn.setOnAction(e -> {
            Student selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Select a student from the table first.");
                return;
            }

            try (Connection conn = DatabaseHandler.getConnection()) {
                String sql = "DELETE FROM students WHERE id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, selected.getId());
                ps.executeUpdate();

                loadStudents(filterLevel.getValue(), filterClass.getValue());
                showAlert("Student deleted successfully!");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        saveAttendanceBtn.setOnAction(e -> {
            String selectedCourse = courseSelect.getValue();
            LocalDate date = datePicker.getValue();

            if (selectedCourse == null || date == null) {
                showAlert("Please select a Course and Date.");
                return;
            }

            try (Connection conn = DatabaseHandler.getConnection()) {
                int courseId = getCourseId(conn, selectedCourse);
                String sql = "INSERT INTO attendance (student_id, course_id, attendance_date, status) " +
                        "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE status = VALUES(status)";

                PreparedStatement ps = conn.prepareStatement(sql);
                for (Student s : studentList) {
                    ps.setInt(1, s.getId());
                    ps.setInt(2, courseId);
                    ps.setDate(3, Date.valueOf(date));
                    ps.setString(4, s.getAttendanceStatus());
                    ps.addBatch();
                }
                ps.executeBatch();
                showAlert("Attendance successfully recorded!");
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert("Database Error saving attendance: " + ex.getMessage());
            }
        });

        Scene scene = new Scene(root, 1100, 650);
        try {
            scene.getStylesheets().add(DashboardView.class.getResource("/theme.css").toExternalForm());
        } catch (Exception ex) {
            System.out.println("CSS file /theme.css not found, loading default styling.");
        }

        stage.setScene(scene);
        stage.setTitle("BTech Computer Science - Attendance Dashboard");
        stage.show();
    }

    // Overloaded 2-parameter render method for simple calls from AuthView
    public static void render(Stage stage, String fullName) {
        render(stage, fullName, null);
    }

    private static void loadCourses(ComboBox<String> box) {
        try (Connection conn = DatabaseHandler.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT course_code FROM courses");
            while (rs.next()) {
                box.getItems().add(rs.getString("course_code"));
            }
            if (!box.getItems().isEmpty()) box.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            System.err.println("Could not load courses from database: " + e.getMessage());
        }
    }

    private static void loadStudents(String level, String classGroup) {
        studentList.clear();
        try (Connection conn = DatabaseHandler.getConnection()) {
            String sql = "SELECT * FROM students WHERE level = ? AND class_group = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, level);
            ps.setString(2, classGroup);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                studentList.add(new Student(
                        rs.getInt("id"),
                        rs.getString("index_number"),
                        rs.getString("full_name"),
                        rs.getString("programme"),
                        rs.getString("level"),
                        rs.getString("class_group")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Could not load students: " + e.getMessage());
        }
    }

    private static int getCourseId(Connection conn, String code) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("SELECT id FROM courses WHERE course_code = ?");
        ps.setString(1, code);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getInt("id");
        return 1;
    }

    private static void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}