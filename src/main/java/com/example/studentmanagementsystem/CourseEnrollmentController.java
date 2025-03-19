package com.example.studentmanagementsystem;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CourseEnrollmentController {

    public Label labelEnrollCourse;
    @FXML
    private ComboBox<Student> studentComboBox;

    @FXML
    private ListView<Course> availableCoursesListView;

    @FXML
    private ListView<Course> enrolledCoursesListView;

    @FXML
    private Button addButton;

    @FXML
    private Button removeButton;

    private DatabaseManager db;

    @FXML
    public void initialize() {
        // Create a placeholder label
        Label placeholderLabel = new Label("No items to display.");

        // Set the placeholder
        availableCoursesListView.setPlaceholder(placeholderLabel);
        enrolledCoursesListView.setPlaceholder(placeholderLabel);

        db = new DatabaseManager();
        loadStudents();
        studentComboBox.setOnAction(_ -> populateCourses());

        // Customize cell rendering for availableCoursesListView
        availableCoursesListView.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(Course course, boolean empty) {
                super.updateItem(course, empty);
                if (empty || course == null) {
                    setText(null);
                } else {
                    setText(course.getId().toUpperCase() + " - " + course.getName());
                }
            }
        });

        // Customize cell rendering for enrolledCoursesListView
        enrolledCoursesListView.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(Course course, boolean empty) {
                super.updateItem(course, empty);
                if (empty || course == null) {
                    setText(null);
                } else {
                    setText(course.getId().toUpperCase() + " - " + course.getName());
                }
            }
        });
    }

    private void loadStudents() {
        List<Student> students = db.getStudents(false);
        ObservableList<Student> studentObservableList = FXCollections.observableArrayList(students);
        studentComboBox.setItems(studentObservableList);
    }

    private void populateCourses() {
        Student selectedStudent = studentComboBox.getValue();
        if (selectedStudent != null) {
            // Clear the listviews before loading data.
            availableCoursesListView.getItems().clear();
            enrolledCoursesListView.getItems().clear();
            loadAvailableCourses(selectedStudent);
            loadEnrolledCourses(selectedStudent);
        }
    }

    private void loadAvailableCourses(Student student) {
        List<Course> allCourses = db.getCourses(false);
        List<Course> availableCourses = new ArrayList<>();
        Map<Course, Double> enrolledCourses = student.getEnrolledCourses();

        for (Course course : allCourses) {
            if (!enrolledCourses.containsKey(course)) {
                availableCourses.add(course);
            }
        }
        availableCoursesListView.setItems(FXCollections.observableArrayList(availableCourses));
    }

    private void loadEnrolledCourses(Student student) {
        enrolledCoursesListView.setItems(FXCollections.observableArrayList(student.getEnrolledCourses().keySet()));
    }

    @FXML
    private void handleAddCourse() {
        Student selectedStudent = studentComboBox.getValue();
        Course selectedCourse = availableCoursesListView.getSelectionModel().getSelectedItem();
        if (selectedStudent != null && selectedCourse != null) {
            selectedStudent.enrollInCourse(selectedCourse);
            db.enrollStudentInCourse(selectedStudent.getId(), selectedCourse.getId()); // Add to database
            populateCourses(); // Refresh lists
        }
    }

    @FXML
    private void handleRemoveCourse() {
        Student selectedStudent = studentComboBox.getValue();
        Course selectedCourse = enrolledCoursesListView.getSelectionModel().getSelectedItem();
        if (selectedStudent != null && selectedCourse != null) {
            selectedStudent.getEnrolledCourses().remove(selectedCourse);
            db.unenrollStudentFromCourse(selectedStudent.getId(), selectedCourse.getId()); // Remove from database
            populateCourses(); // Refresh lists
        }
    }
}