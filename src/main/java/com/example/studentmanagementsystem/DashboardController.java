package com.example.studentmanagementsystem;

import javafx.scene.control.Label;

public class DashboardController {
    public Label totalStudentsLabel;
    public Label activeStudentsLabel;
    public Label inactiveStudentsLabel;
    public Label totalCoursesLabel;
    public Label activeCoursesLabel;
    public Label inactiveCoursesLabel;
    public Label totalEnrolledStudentsLabel;

    DatabaseManager db = new DatabaseManager();

    public void initialize() {
        int activeStudents = db.getStudents(false).size();
        int inactiveStudents = db.getStudents(true).size();
        int totalStudents = activeStudents + inactiveStudents;
        int activeCourses = db.getCourses(false).size();
        int inactiveCourses = db.getCourses(true).size();
        int totalCourses = activeCourses + inactiveCourses;
        int activeEnrolledStudents = db.countUniqueStudentsEnrolled();

        activeStudentsLabel.setText(String.valueOf(activeStudents));
        inactiveStudentsLabel.setText(String.valueOf(inactiveStudents));
        totalStudentsLabel.setText(String.valueOf(totalStudents));
        activeCoursesLabel.setText(String.valueOf(activeCourses));
        inactiveCoursesLabel.setText(String.valueOf(inactiveCourses));
        totalCoursesLabel.setText(String.valueOf(totalCourses));
        totalEnrolledStudentsLabel.setText(String.valueOf(activeEnrolledStudents));
    }
}
