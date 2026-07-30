package com.example.studentattendancesystem.attendance.model;

import javafx.beans.property.*;

public class Student {
    private final IntegerProperty id;
    private final StringProperty indexNumber;
    private final StringProperty fullName;
    private final StringProperty programme;
    private final StringProperty level;
    private final StringProperty classGroup;
    private final StringProperty attendanceStatus;

    public Student(int id, String indexNumber, String fullName, String programme, String level, String classGroup) {
        this.id = new SimpleIntegerProperty(id);
        this.indexNumber = new SimpleStringProperty(indexNumber);
        this.fullName = new SimpleStringProperty(fullName);
        this.programme = new SimpleStringProperty(programme);
        this.level = new SimpleStringProperty(level);
        this.classGroup = new SimpleStringProperty(classGroup);
        this.attendanceStatus = new SimpleStringProperty("PRESENT");
    }

    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }

    public String getIndexNumber() { return indexNumber.get(); }
    public StringProperty indexNumberProperty() { return indexNumber; }

    public String getFullName() { return fullName.get(); }
    public StringProperty fullNameProperty() { return fullName; }
    public void setFullName(String val) { this.fullName.set(val); }

    public String getProgramme() { return programme.get(); }
    public StringProperty programmeProperty() { return programme; }

    public String getLevel() { return level.get(); }
    public StringProperty levelProperty() { return level; }

    public String getClassGroup() { return classGroup.get(); }
    public StringProperty classGroupProperty() { return classGroup; }

    public String getAttendanceStatus() { return attendanceStatus.get(); }
    public StringProperty attendanceStatusProperty() { return attendanceStatus; }
    public void setAttendanceStatus(String val) { this.attendanceStatus.set(val); }
}