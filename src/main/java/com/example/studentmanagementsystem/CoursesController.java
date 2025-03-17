package com.example.studentmanagementsystem;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

import static com.example.studentmanagementsystem.IOHelper.showConfirmationDialog;
import static com.example.studentmanagementsystem.IOHelper.showMessageBox;

public class CoursesController {
    public Label labelActiveCourses;
    public ListView<Course> listViewCourses;
    public Label labelEditCourse;
    public TextField txtCourseId;
    public TextField txtCourseName;
    public TextField txtCourseMaxCapacity;
    public Label labelCourseId;
    public Label labelCourseName;
    public Label labelMaxCapacity;

    DatabaseManager db = new DatabaseManager();

    public void initialize() {
        HBox.setHgrow(txtCourseId, Priority.ALWAYS);
        HBox.setHgrow(txtCourseName, Priority.ALWAYS);
        HBox.setHgrow(txtCourseMaxCapacity, Priority.ALWAYS);

        List<Course> students = db.getCourses(false);

        listViewCourses.getItems().clear();
        if (students != null) {
            listViewCourses.getItems().addAll(students);
        }
        setupListViewClickHandler();
    }

    private void setupListViewClickHandler() {
        listViewCourses.setOnMouseClicked(event -> {
            // Get the selected student from ListView
            Course selectedCourse = listViewCourses.getSelectionModel().getSelectedItem();

            if (selectedCourse != null) { // Check if a student is selected
                txtCourseId.setText(selectedCourse.getId()); // Assuming Student class has getId() method
                txtCourseName.setText(selectedCourse.getName()); // Assuming Student class has getName() method
                txtCourseMaxCapacity.setText(String.valueOf(selectedCourse.getMaxCapacity()));
            }
        });
    }

    @FXML
    public void handleEditCourseClick(ActionEvent actionEvent) {
    }

    @FXML
    public void handleDeleteCourseClick(ActionEvent actionEvent) {
    }
}