package com.example.studentattendancesystem.view;

import com.example.studentattendancesystem.attendance.model.Student;
import com.example.studentattendancesystem.util.DatabaseHandler;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;

public class DashboardView {

    private static final ObservableList<Student> studentList = FXCollections.observableArrayList();
    private static final TableView<Student> table = new TableView<>();

    public static void render(Stage stage, String repName, String defaultLevel) {
        BorderPane root = new BorderPane();
        root.getStyleClass().addAll("root", "light-mode");

        // --- HEADER BAR ---
        HBox header = new HBox(15);
        header.getStyleClass().add("header-bar");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 15, 10, 15));

        Label brand = new Label("Student Attendance Dashboard | " + repName + " (" + defaultLevel + " Rep)");
        brand.getStyleClass().add("header-title");
        brand.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button historyBtn = new Button("View Attendance History");
        historyBtn.getStyleClass().add("btn-secondary");

        Button logoutBtn = new Button("Logout");
        logoutBtn.getStyleClass().add("btn-danger");

        header.getChildren().addAll(brand, spacer, historyBtn, logoutBtn);
        root.setTop(header);

        // --- LEFT SIDEBAR: STUDENT REGISTRATION & MANAGEMENT ---
        VBox formCard = new VBox(12);
        formCard.getStyleClass().add("card-panel");
        formCard.setPrefWidth(280);
        formCard.setPadding(new Insets(15));

        Label formTitle = new Label("Student Registration");
        formTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");

        TextField indexInput = new TextField();
        indexInput.setPromptText("Index Number");

        TextField nameInput = new TextField();
        nameInput.setPromptText("Full Name");

        ComboBox<String> levelSelect = new ComboBox<>(FXCollections.observableArrayList("Level 100", "Level 200", "Level 300", "Level 400"));
        levelSelect.getSelectionModel().select(defaultLevel != null ? defaultLevel : "Level 200");

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

        Button updateBtn = new Button("Update Student");
        updateBtn.getStyleClass().add("btn-secondary");
        updateBtn.setMaxWidth(Double.MAX_VALUE);

        Button deleteBtn = new Button("Delete Student");
        deleteBtn.getStyleClass().add("btn-danger");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);

        formCard.getChildren().addAll(
                formTitle,
                new Label("Index Number"), indexInput,
                new Label("Full Name"), nameInput,
                new Label("Academic Level"), levelSelect,
                new Label("Class Section"), radioBox,
                addBtn, updateBtn, deleteBtn
        );

        // --- CENTER AREA: FILTER BAR & MAIN TABLE ---
        VBox centerBox = new VBox(15);
        centerBox.setPadding(new Insets(15));

        HBox filterBar = new HBox(10);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.getStyleClass().add("card-panel");
        filterBar.setPadding(new Insets(10));

        ComboBox<String> courseSelect = new ComboBox<>();
        loadCourses(courseSelect);

        ComboBox<String> filterLevel = new ComboBox<>(FXCollections.observableArrayList("Level 100", "Level 200", "Level 300", "Level 400"));
        filterLevel.getSelectionModel().select(defaultLevel != null ? defaultLevel : "Level 200");

        ComboBox<String> filterClass = new ComboBox<>(FXCollections.observableArrayList("A", "B", "C", "D"));
        filterClass.getSelectionModel().select("A");

        DatePicker datePicker = new DatePicker(LocalDate.now());

        Button loadBtn = new Button("Load Session");
        loadBtn.getStyleClass().add("btn-secondary");

        filterBar.getChildren().addAll(
                new Label("Course:"), courseSelect,
                new Label("Level:"), filterLevel,
                new Label("Class:"), filterClass,
                new Label("Date:"), datePicker, loadBtn
        );

        // --- TABLE COLUMNS SETUP ---
        TableColumn<Student, String> colIndex = new TableColumn<>("Index Number");
        colIndex.setCellValueFactory(data -> data.getValue().indexNumberProperty());

        TableColumn<Student, String> colName = new TableColumn<>("Student Name");
        colName.setCellValueFactory(data -> data.getValue().fullNameProperty());

        TableColumn<Student, String> colLevel = new TableColumn<>("Level");
        colLevel.setCellValueFactory(data -> data.getValue().levelProperty());

        TableColumn<Student, String> colClass = new TableColumn<>("Class");
        colClass.setCellValueFactory(data -> data.getValue().classGroupProperty());

        TableColumn<Student, String> colStatus = new TableColumn<>("Attendance Status");
        colStatus.setCellValueFactory(data -> data.getValue().attendanceStatusProperty());

        // FIXED: Custom TableCell implementation preventing duplication/re-rendering bugs
        colStatus.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<String> statusBox = new ComboBox<>(FXCollections.observableArrayList("PRESENT", "ABSENT", "LATE"));

            {
                statusBox.setOnAction(e -> {
                    Student s = getTableView() != null && getIndex() < getTableView().getItems().size() ? getTableView().getItems().get(getIndex()) : null;
                    if (s != null && statusBox.getValue() != null) {
                        s.setAttendanceStatus(statusBox.getValue());
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    statusBox.setValue(item != null ? item : "PRESENT");
                    setGraphic(statusBox);
                }
            }
        });

        table.getColumns().setAll(colIndex, colName, colLevel, colClass, colStatus);
        table.setItems(studentList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button saveAttendanceBtn = new Button("Save / Update Attendance for Selected Date");
        saveAttendanceBtn.getStyleClass().add("btn-primary");
        saveAttendanceBtn.setMaxWidth(Double.MAX_VALUE);

        centerBox.getChildren().addAll(filterBar, table, saveAttendanceBtn);

        root.setLeft(formCard);
        root.setCenter(centerBox);

        // Initial Table Load
        loadAttendanceForDate(filterLevel.getValue(), filterClass.getValue(), courseSelect.getValue(), datePicker.getValue());

        // --- EVENT HANDLERS ---

        // 1. Refresh Table on Filter or Date Changes
        loadBtn.setOnAction(e -> loadAttendanceForDate(filterLevel.getValue(), filterClass.getValue(), courseSelect.getValue(), datePicker.getValue()));
        datePicker.setOnAction(e -> loadAttendanceForDate(filterLevel.getValue(), filterClass.getValue(), courseSelect.getValue(), datePicker.getValue()));

        // 2. Populate input fields on table row selection
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                indexInput.setText(newSel.getIndexNumber());
                nameInput.setText(newSel.getFullName());
            }
        });

        // 3. Add Student Action
        addBtn.setOnAction(e -> {
            RadioButton selectedRadio = (RadioButton) classGroup.getSelectedToggle();
            String indexNo = indexInput.getText().trim();
            String fullName = nameInput.getText().trim();

            if (indexNo.isEmpty() || fullName.isEmpty() || selectedRadio == null) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter both Index Number and Full Name.");
                return;
            }

            try (Connection conn = DatabaseHandler.getConnection()) {
                String sql = "INSERT INTO students (index_number, full_name, programme, level, class_group) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, indexNo);
                ps.setString(2, fullName);
                ps.setString(3, "BTech Computer Science");
                ps.setString(4, levelSelect.getValue());
                ps.setString(5, selectedRadio.getText());
                ps.executeUpdate();

                indexInput.clear();
                nameInput.clear();
                loadAttendanceForDate(filterLevel.getValue(), filterClass.getValue(), courseSelect.getValue(), datePicker.getValue());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Student registered successfully under " + levelSelect.getValue() + "!");
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Index Number already exists or database execution failed.");
            }
        });

        // 4. Update Student Action
        updateBtn.setOnAction(e -> {
            Student selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a student from the table first.");
                return;
            }

            String newName = nameInput.getText().trim();
            String newIndex = indexInput.getText().trim();

            if (newName.isEmpty() || newIndex.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Index Number and Full Name cannot be empty.");
                return;
            }

            try (Connection conn = DatabaseHandler.getConnection()) {
                String sql = "UPDATE students SET full_name = ?, index_number = ? WHERE id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, newName);
                ps.setString(2, newIndex);
                ps.setInt(3, selected.getId());
                ps.executeUpdate();

                loadAttendanceForDate(filterLevel.getValue(), filterClass.getValue(), courseSelect.getValue(), datePicker.getValue());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Student details updated successfully!");
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Update Error", "Could not update student: " + ex.getMessage());
            }
        });

        // 5. Delete Student Action (Clears linked logs first)
        deleteBtn.setOnAction(e -> {
            Student selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a student from the table to delete.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Deletion");
            confirm.setHeaderText(null);
            confirm.setContentText("Are you sure you want to delete " + selected.getFullName() + " (" + selected.getIndexNumber() + ")?\nAll associated attendance records will also be deleted.");

            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                try (Connection conn = DatabaseHandler.getConnection()) {
                    conn.setAutoCommit(false); // Begin transaction

                    // Delete linked attendance logs
                    String deleteAttendanceSql = "DELETE FROM attendance WHERE student_id = ?";
                    try (PreparedStatement psAtt = conn.prepareStatement(deleteAttendanceSql)) {
                        psAtt.setInt(1, selected.getId());
                        psAtt.executeUpdate();
                    }

                    // Delete student record
                    String deleteStudentSql = "DELETE FROM students WHERE id = ?";
                    try (PreparedStatement psStu = conn.prepareStatement(deleteStudentSql)) {
                        psStu.setInt(1, selected.getId());
                        psStu.executeUpdate();
                    }

                    conn.commit(); // Commit transaction

                    indexInput.clear();
                    nameInput.clear();
                    loadAttendanceForDate(filterLevel.getValue(), filterClass.getValue(), courseSelect.getValue(), datePicker.getValue());

                    showAlert(Alert.AlertType.INFORMATION, "Success", "Student deleted successfully!");

                } catch (SQLException ex) {
                    showAlert(Alert.AlertType.ERROR, "Delete Error", "Could not delete student: " + ex.getMessage());
                }
            }
        });

        // 6. Save Daily Attendance Action
        saveAttendanceBtn.setOnAction(e -> {
            String selectedCourse = courseSelect.getValue();
            LocalDate selectedDate = datePicker.getValue();

            if (selectedCourse == null || selectedDate == null) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a valid Course and Date.");
                return;
            }

            try (Connection conn = DatabaseHandler.getConnection()) {
                int courseId = getCourseId(conn, selectedCourse);

                String sql = "INSERT INTO attendance (student_id, course_id, attendance_date, status) " +
                        "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE status = VALUES(status)";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    for (Student s : studentList) {
                        ps.setInt(1, s.getId());
                        ps.setInt(2, courseId);
                        ps.setDate(3, Date.valueOf(selectedDate));
                        ps.setString(4, s.getAttendanceStatus() == null ? "PRESENT" : s.getAttendanceStatus());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                showAlert(Alert.AlertType.INFORMATION, "Success", "Attendance for " + selectedDate + " (" + selectedCourse + ") saved successfully!");
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Could not save attendance: " + ex.getMessage());
            }
        });

        // 7. Open History Modal
        historyBtn.setOnAction(e -> showHistoryWindow(filterLevel.getValue()));

        // 8. Logout Action & Redirection
        logoutBtn.setOnAction(e -> {
            Alert confirmLogout = new Alert(Alert.AlertType.CONFIRMATION);
            confirmLogout.setTitle("Confirm Logout");
            confirmLogout.setHeaderText(null);
            confirmLogout.setContentText("Are you sure you want to log out?");

            if (confirmLogout.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                try {
                    var loginView = new LoginView();
                    loginView.start(stage);
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not return to Login View: " + ex.getMessage());
                }
            }
        });

        Scene scene = new Scene(root, 1150, 680);
        try {
            scene.getStylesheets().add(DashboardView.class.getResource("/theme.css").toExternalForm());
        } catch (Exception ex) {
            System.out.println("theme.css not found, rendering unstyled Dashboard.");
        }

        stage.setScene(scene);
        stage.setTitle("BTech Computer Science - Attendance Dashboard");
        stage.show();
    }

    public static void render(Stage stage, String fullName) {
        render(stage, fullName, "Level 200");
    }

    // --- FETCH ATTENDANCE FOR A SPECIFIC DATE & CLASS ---
    private static void loadAttendanceForDate(String level, String classGroup, String selectedCourseItem, LocalDate date) {
        studentList.clear();
        if (selectedCourseItem == null || date == null || level == null || classGroup == null) return;

        String courseCode = selectedCourseItem.contains(" - ") ? selectedCourseItem.split(" - ")[0].trim() : selectedCourseItem;

        String sql = "SELECT s.id, s.index_number, s.full_name, s.programme, s.level, s.class_group, " +
                "COALESCE(a.status, 'PRESENT') AS current_status " +
                "FROM students s " +
                "LEFT JOIN courses c ON c.course_code = ? " +
                "LEFT JOIN attendance a ON a.student_id = s.id AND a.course_id = c.id AND a.attendance_date = ? " +
                "WHERE s.level = ? AND s.class_group = ?";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, courseCode);
            ps.setDate(2, Date.valueOf(date));
            ps.setString(3, level);
            ps.setString(4, classGroup);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Student s = new Student(
                        rs.getInt("id"),
                        rs.getString("index_number"),
                        rs.getString("full_name"),
                        rs.getString("programme"),
                        rs.getString("level"),
                        rs.getString("class_group")
                );
                s.setAttendanceStatus(rs.getString("current_status"));
                studentList.add(s);
            }
        } catch (SQLException e) {
            System.err.println("Database load error: " + e.getMessage());
        }
    }

    // --- ATTENDANCE HISTORY WINDOW ---
    private static void showHistoryWindow(String repLevel) {
        Stage historyStage = new Stage();
        historyStage.setTitle("Attendance History Log");

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        HBox filterBar = new HBox(10);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        DatePicker historyDate = new DatePicker(LocalDate.now());
        ComboBox<String> historyLevel = new ComboBox<>(FXCollections.observableArrayList("Level 100", "Level 200", "Level 300", "Level 400"));
        historyLevel.getSelectionModel().select(repLevel != null ? repLevel : "Level 200");

        Button searchBtn = new Button("Search Log");
        filterBar.getChildren().addAll(new Label("Select Date:"), historyDate, new Label("Level:"), historyLevel, searchBtn);

        TableView<AttendanceRecord> historyTable = new TableView<>();

        TableColumn<AttendanceRecord, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate()));

        TableColumn<AttendanceRecord, String> colCode = new TableColumn<>("Course Code");
        colCode.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCourseCode()));

        TableColumn<AttendanceRecord, String> colTitle = new TableColumn<>("Course Title");
        colTitle.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCourseTitle()));

        TableColumn<AttendanceRecord, String> colIndex = new TableColumn<>("Index Number");
        colIndex.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIndexNo()));

        TableColumn<AttendanceRecord, String> colName = new TableColumn<>("Student Name");
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));

        TableColumn<AttendanceRecord, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

        historyTable.getColumns().addAll(colDate, colCode, colTitle, colIndex, colName, colStatus);
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        ObservableList<AttendanceRecord> logData = FXCollections.observableArrayList();
        historyTable.setItems(logData);

        searchBtn.setOnAction(e -> {
            logData.clear();
            if (historyDate.getValue() == null || historyLevel.getValue() == null) return;

            String sql = "SELECT a.attendance_date, c.course_code, c.course_title, s.index_number, s.full_name, a.status " +
                    "FROM attendance a " +
                    "JOIN students s ON a.student_id = s.id " +
                    "JOIN courses c ON a.course_id = c.id " +
                    "WHERE a.attendance_date = ? AND s.level = ?";

            try (Connection conn = DatabaseHandler.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setDate(1, Date.valueOf(historyDate.getValue()));
                ps.setString(2, historyLevel.getValue());

                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    logData.add(new AttendanceRecord(
                            rs.getDate("attendance_date").toString(),
                            rs.getString("course_code"),
                            rs.getString("course_title"),
                            rs.getString("index_number"),
                            rs.getString("full_name"),
                            rs.getString("status")
                    ));
                }
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Search Error", "Could not fetch history: " + ex.getMessage());
            }
        });

        root.getChildren().addAll(new Label("Attendance History Records"), filterBar, historyTable);
        Scene scene = new Scene(root, 850, 450);
        historyStage.setScene(scene);
        historyStage.show();
    }

    // --- HELPER: LOAD COURSE CODE & TITLE INTO COMBOBOX ---
    private static void loadCourses(ComboBox<String> box) {
        box.getItems().clear();
        String sql = "SELECT course_code, course_title FROM courses";
        try (Connection conn = DatabaseHandler.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String display = rs.getString("course_code") + " - " + rs.getString("course_title");
                box.getItems().add(display);
            }
            if (!box.getItems().isEmpty()) {
                box.getSelectionModel().selectFirst();
            }
        } catch (SQLException e) {
            System.err.println("Could not load courses: " + e.getMessage());
        }
    }

    // --- HELPER: GET COURSE ID FROM DROPDOWN STRING ---
    private static int getCourseId(Connection conn, String selectedCourseItem) throws SQLException {
        if (selectedCourseItem == null) return 1;

        String code = selectedCourseItem.contains(" - ") ? selectedCourseItem.split(" - ")[0].trim() : selectedCourseItem;

        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM courses WHERE course_code = ?")) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        }
        return 1;
    }

    private static void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // --- MODEL CLASS FOR HISTORY TABLE ---
    public static class AttendanceRecord {
        private final String date;
        private final String courseCode;
        private final String courseTitle;
        private final String indexNo;
        private final String name;
        private final String status;

        public AttendanceRecord(String date, String courseCode, String courseTitle, String indexNo, String name, String status) {
            this.date = date;
            this.courseCode = courseCode;
            this.courseTitle = courseTitle;
            this.indexNo = indexNo;
            this.name = name;
            this.status = status;
        }

        public String getDate() { return date; }
        public String getCourseCode() { return courseCode; }
        public String getCourseTitle() { return courseTitle; }
        public String getIndexNo() { return indexNo; }
        public String getName() { return name; }
        public String getStatus() { return status; }
    }

    private static class LoginView {
        public void start(Stage stage) {
        }
    }
}
