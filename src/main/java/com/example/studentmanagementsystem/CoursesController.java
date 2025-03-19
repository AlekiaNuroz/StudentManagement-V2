package com.example.studentmanagementsystem;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

        List<Course> courses = db.getCourses(false);

        listViewCourses.getItems().clear();
        if (courses != null) {
            listViewCourses.getItems().addAll(courses);
        }
        setupListViewClickHandler();
    }

    private void setupListViewClickHandler() {
        listViewCourses.setOnMouseClicked(_ -> {
            // Get the selected course from ListView
            Course selectedCourse = listViewCourses.getSelectionModel().getSelectedItem();

            if (selectedCourse != null) { // Check if a course is selected
                txtCourseId.setText(selectedCourse.getId().toUpperCase()); // Assuming Course class has getId() method
                txtCourseName.setText(selectedCourse.getName()); // Assuming Course class has getName() method
                txtCourseMaxCapacity.setText(String.valueOf(selectedCourse.getMaxCapacity()));
            }
        });
    }

    @FXML
    public void handleEditCourseClick() {
        String newCourseName = txtCourseName.getText();
        int newMaxCapacity = Integer.parseInt(txtCourseMaxCapacity.getText());
        Course selectedCourse = listViewCourses.getSelectionModel().getSelectedItem();

        if (selectedCourse != null && !newCourseName.trim().isEmpty()) { // trim() to avoid empty input
            Course updatedCourse = new Course(selectedCourse.getId(), newCourseName, newMaxCapacity);
            // Call the DatabaseManager to update the course name
            if (db.updateCourse(updatedCourse)) {
                int selectedIndex = listViewCourses.getSelectionModel().getSelectedIndex();
                // Update the item in the ListView
                listViewCourses.getItems().set(selectedIndex, updatedCourse);

                txtCourseId.clear();
                txtCourseName.clear();
                txtCourseMaxCapacity.clear();

                showMessageBox("Update Course", "Course updated successfully", Alert.AlertType.INFORMATION);
            } else {
                showMessageBox("Update Course", "Unable to update course.", Alert.AlertType.ERROR);
            }
        } else {
            showMessageBox("No Course Selected", "Please select a course and provide a new name.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleDeleteCourseClick() {
        Course selectedCourse = listViewCourses.getSelectionModel().getSelectedItem();

        if (selectedCourse != null) {
            // Show the confirmation dialog
            boolean confirmed = showConfirmationDialog("Confirm Deletion",
                    "Are you sure you want to delete the course: " + selectedCourse.getName() + "?");

            if (confirmed) {
                // Proceed with deletion logic (e.g., remove from database, remove from ListView)
                db.deleteRestoreCourse(selectedCourse, true);
                listViewCourses.getItems().remove(selectedCourse);
                showMessageBox("Deletion Successful", "Course deleted successfully!", Alert.AlertType.INFORMATION);
            } else {
                // User decided not to delete
                showMessageBox("Deletion Canceled", "Course deletion was canceled.", Alert.AlertType.INFORMATION);
            }
        } else {
            showMessageBox("No Course Selected", "Please select a course to delete.", Alert.AlertType.WARNING);
        }
    }

    public void handleAddCourseButtonClick() {
        Optional<Course> newCourseOptional = showAddCourseDialog();

        newCourseOptional.ifPresent(newCourse -> {
            // Add the new student to the database
            db.insertCourse(newCourse);
            // Optionally, update the UI (e.g., refresh a table view)
            listViewCourses.getItems().add(newCourse);
        });
    }

    public void handleRestoreDeletedCourseButtonClick() {
        Optional<Course> newCourseOptional = showRestoreCourseDialog(db.getCourses(true));

        newCourseOptional.ifPresent(newCourse -> {
            // Add the new student to the database
            db.deleteRestoreCourse(newCourse, false);
            // Optionally, update the UI (e.g., refresh a table view)
            listViewCourses.getItems().add(newCourse);
        });
    }

    public void showCourseDetailsClick() {
        Course selectedCourse = listViewCourses.getSelectionModel().getSelectedItem();

        if (selectedCourse != null) {
            showCourseDetails(selectedCourse);
        }
    }

    public void showCourseDetails(Course course) {
        // Create the dialog
        Dialog<Course> dialog = new Dialog<>();
        dialog.setTitle("Course Details");
        dialog.setWidth(600);
        dialog.setHeaderText("Details of " + course.getName());

        // Create content for the dialog
        VBox dialogPane = new VBox(10);
        dialogPane.getChildren().addAll(
                new Label("Course ID: " + course.getId()),
                new Label("Name: " + course.getName()),
                new Label("Max Capacity: " + course.getMaxCapacity()),
                new Label("Current Enrollment: " + course.getCurrentEnrollment())
        );

        // Create a TableView for students enrolled in the course
        TableView<Map.Entry<Student, Double>> tableViewStudents = new TableView<>();

        // Create columns for the TableView: Student ID, Student Name, and Grade
        TableColumn<Map.Entry<Student, Double>, String> colStudentId = new TableColumn<>("Student ID");
        colStudentId.setCellValueFactory(entry ->
                new SimpleStringProperty(entry.getValue().getKey().getId().toUpperCase()));

        TableColumn<Map.Entry<Student, Double>, String> colStudentName = new TableColumn<>("Student Name");
        colStudentName.setCellValueFactory(entry ->
                new SimpleStringProperty(entry.getValue().getKey().getName()));
        colStudentName.setMinWidth(300);

        TableColumn<Map.Entry<Student, Double>, Double> colGrade = new TableColumn<>("Grade");
        colGrade.setCellValueFactory(entry -> {
            Double grade = entry.getValue().getValue();
            if (grade == null) {
                return new SimpleDoubleProperty(0.0).asObject(); // Or return null, if you prefer
            } else {
                return new SimpleDoubleProperty(grade).asObject();
            }
        });

        // Set the columns to the TableView
        tableViewStudents.getColumns().addAll(colStudentId, colStudentName, colGrade);

        // Populate the TableView with the students enrolled in the course
        ObservableList<Map.Entry<Student, Double>> studentData = FXCollections.observableArrayList(getStudentsEnrolledInCourse(course).entrySet());
        tableViewStudents.setItems(studentData);

        // Add the TableView to the dialog pane
        dialogPane.getChildren().add(tableViewStudents);

        // Set the dialog's content
        dialog.getDialogPane().setContent(dialogPane);

        // Add OK button
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        // Show the dialog and wait for the user response
        dialog.showAndWait();
    }

    public static Optional<Course> showAddCourseDialog() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Add Course");
        dialog.setHeaderText("Enter course details:");

        ButtonType addButtonType = new ButtonType("Add", ButtonType.OK.getButtonData());
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField courseIdField = new TextField();
        courseIdField.setPromptText("Course ID");
        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        TextField maxCapacityField = new TextField();
        maxCapacityField.setPromptText("Max Capacity");

        grid.add(new Label("Course ID:"), 0, 0);
        grid.add(courseIdField, 1, 0);
        grid.add(new Label("Name:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Max Capacity:"), 0, 2);
        grid.add(maxCapacityField, 1, 2);

        dialog.getDialogPane().lookupButton(addButtonType).setDisable(true);

        courseIdField.textProperty().addListener((_, _, newValue) ->
                dialog.getDialogPane().lookupButton(addButtonType).setDisable(
                        newValue.trim().isEmpty() ||
                                nameField.getText().trim().isEmpty() ||
                                maxCapacityField.getText().trim().isEmpty()
                )
        );

        nameField.textProperty().addListener((_, _, newValue) ->
                dialog.getDialogPane().lookupButton(addButtonType).setDisable(
                        newValue.trim().isEmpty() ||
                                courseIdField.getText().trim().isEmpty() ||
                                maxCapacityField.getText().trim().isEmpty()
                )
        );

        maxCapacityField.textProperty().addListener((_, _, newValue) ->
                dialog.getDialogPane().lookupButton(addButtonType).setDisable(
                        newValue.trim().isEmpty() ||
                                courseIdField.getText().trim().isEmpty() ||
                                nameField.getText().trim().isEmpty()
                )
        );

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return new Pair<>(courseIdField.getText(), nameField.getText() + "," + maxCapacityField.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        return result.map(courseData -> {
            String[] parts = courseData.getValue().split(",");
            if (parts.length == 2) {
                try {
                    int maxCapacity = Integer.parseInt(parts[1].trim());
                    return new Course(courseData.getKey(), parts[0].trim(), maxCapacity, 0);
                } catch (NumberFormatException e) {
                    // Handle invalid max capacity input
                    return null;
                }
            }
            return null;
        });
    }

    public static Optional<Course> showRestoreCourseDialog(List<Course> deletedCourses) {
        Dialog<Course> dialog = new Dialog<>();
        dialog.setTitle("Restore Deleted Course");
        dialog.setHeaderText("Select a course to restore:");

        ButtonType restoreButtonType = new ButtonType("Restore", ButtonType.OK.getButtonData());
        dialog.getDialogPane().getButtonTypes().addAll(restoreButtonType, ButtonType.CANCEL);

        ListView<Course> courseListView = new ListView<>();
        ObservableList<Course> observableDeletedCourses = FXCollections.observableArrayList(deletedCourses);
        courseListView.setItems(observableDeletedCourses);

        dialog.getDialogPane().lookupButton(restoreButtonType).setDisable(true); // Disable initially

        courseListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) ->
                dialog.getDialogPane().lookupButton(restoreButtonType).setDisable(newVal == null)
        );

        VBox vbox = new VBox(courseListView);
        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == restoreButtonType) {
                return courseListView.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private Map<Student, Double> getStudentsEnrolledInCourse(Course course) {
        if (course == null) {
            return new HashMap<>(); // Return an empty map to avoid NPE
        }
        return DatabaseManager.getStudentsEnrolledInCourse(course);
    }
}