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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.example.studentmanagementsystem.IOHelper.showConfirmationDialog;
import static com.example.studentmanagementsystem.IOHelper.showMessageBox;

public class StudentsController {
    @FXML
    public Label labelEditStudent;

    @FXML
    public Label labelActiveStudents;

    @FXML
    public ListView<Student> listViewStudents;

    @FXML
    public TextField txtStudentId;

    @FXML
    public TextField txtStudentName;

    @FXML
    public Label labelStudentId;

    @FXML
    public Label labelStudentName;

    DatabaseManager db = new DatabaseManager();

    public void initialize() {
        HBox.setHgrow(txtStudentId, Priority.ALWAYS);
        HBox.setHgrow(txtStudentName, Priority.ALWAYS);
        List<Student> students = db.getStudents(false);

        listViewStudents.getItems().clear();
        if (students != null) {
            listViewStudents.getItems().addAll(students);
        }
        setupListViewClickHandler();
    }

    private void setupListViewClickHandler() {
        listViewStudents.setOnMouseClicked(_ -> {
            // Get the selected student from ListView
            Student selectedStudent = listViewStudents.getSelectionModel().getSelectedItem();

            if (selectedStudent != null) { // Check if a student is selected
                txtStudentId.setText(selectedStudent.getId()); // Assuming Student class has getId() method
                txtStudentName.setText(selectedStudent.getName()); // Assuming Student class has getName() method
            }
        });
    }

    public void handleEditStudentClick() {
        String newStudentName = txtStudentName.getText();
        Student selectedStudent = listViewStudents.getSelectionModel().getSelectedItem();

        if (selectedStudent != null && !newStudentName.trim().isEmpty()) { // trim() to avoid empty input
            // Call the DatabaseManager to update the student name
            if (db.updateStudentName(selectedStudent.getId(), newStudentName)) {
                // Create a new Student object with the updated name
                Student updatedStudent = new Student(selectedStudent.getId(), newStudentName);

                int selectedIndex = listViewStudents.getSelectionModel().getSelectedIndex();
                // Update the item in the ListView
                listViewStudents.getItems().set(selectedIndex, updatedStudent);

                txtStudentId.clear();
                txtStudentName.clear();

                showMessageBox("Update Student", "Student name updated successfully", Alert.AlertType.INFORMATION);
            } else {
                showMessageBox("Update Student", "Unable to update student", Alert.AlertType.ERROR);
            }
        } else {
            showMessageBox("No Student Selected", "Please select a student and provide a new name", Alert.AlertType.ERROR);
        }
    }

    public void handleDeleteStudentClick() {
        Student selectedStudent = listViewStudents.getSelectionModel().getSelectedItem();

        if (selectedStudent != null) {
            // Show the confirmation dialog
            boolean confirmed = showConfirmationDialog("Confirm Deletion",
                    "Are you sure you want to delete the student: " + selectedStudent.getName() + "?");

            if (confirmed) {
                // Proceed with deletion logic (e.g., remove from database, remove from ListView)
                db.deleteRestoreStudent(selectedStudent, true);
                listViewStudents.getItems().remove(selectedStudent);
                showMessageBox("Deletion Successful", "Student deleted successfully!", Alert.AlertType.INFORMATION);
            } else {
                // User decided not to delete
                showMessageBox("Deletion Canceled", "Student deletion was canceled.", Alert.AlertType.INFORMATION);
            }
        } else {
            showMessageBox("No Student Selected", "Please select a student to delete.", Alert.AlertType.WARNING);
        }
    }

    public void showStudentDetailsClick() {
        Student selectedStudent = listViewStudents.getSelectionModel().getSelectedItem();

        if (selectedStudent != null) {
            showStudentDetails(selectedStudent);
        }
    }

    public void handleAddStudentButtonClick() {
        Optional<Student> newStudentOptional = showAddStudentDialog();

        newStudentOptional.ifPresent(newStudent -> {
            // Add the new student to the database
            db.insertStudent(newStudent);
            // Optionally, update the UI (e.g., refresh a table view)
            listViewStudents.getItems().add(newStudent);
        });
    }

    public void handleRestoreDeletedStudentButtonClick() {
        Optional<Student> newStudentOptional = showRestoreStudentDialog(db.getStudents(true));

        newStudentOptional.ifPresent(newStudent -> {
            // Add the new student to the database
            db.deleteRestoreStudent(newStudent, false);
            // Optionally, update the UI (e.g., refresh a table view)
            listViewStudents.getItems().add(newStudent);
        });
    }

    private Optional<Student> showRestoreStudentDialog(List<Student> deletedStudents) {
        Dialog<Student> dialog = new Dialog<>();
        dialog.setTitle("Restore Deleted Student");
        dialog.setHeaderText("Select a student to restore:");

        ButtonType restoreButtonType = new ButtonType("Restore", ButtonType.OK.getButtonData());
        dialog.getDialogPane().getButtonTypes().addAll(restoreButtonType, ButtonType.CANCEL);

        ListView<Student> studentListView = new ListView<>();
        ObservableList<Student> observableDeletedCourses = FXCollections.observableArrayList(deletedStudents);
        studentListView.setItems(observableDeletedCourses);

        dialog.getDialogPane().lookupButton(restoreButtonType).setDisable(true); // Disable initially

        studentListView.getSelectionModel().selectedItemProperty().addListener((_, _, newVal) ->
                dialog.getDialogPane().lookupButton(restoreButtonType).setDisable(newVal == null)
        );

        VBox vbox = new VBox(studentListView);
        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == restoreButtonType) {
                return studentListView.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        return dialog.showAndWait();
    }

    public void showStudentDetails(Student student) {
        // Create the dialog
        Dialog<Student> dialog = new Dialog<>();
        dialog.setTitle("Student Details");
        dialog.setWidth(600);
        dialog.setHeaderText("Details of " + student.getName());

        // Create content for the dialog
        VBox dialogPane = new VBox(10);
        dialogPane.getChildren().addAll(
                new Label("Student ID: " + student.getId()),
                new Label("Name: " + student.getName())
        );

        // Create a TableView for enrollment data
        TableView<Map.Entry<Course, Double>> tableViewEnrollments = new TableView<>();

        // Create columns for the TableView: Course ID, Course Name, and Grade
        TableColumn<Map.Entry<Course, Double>, String> colCourseId = new TableColumn<>("Course ID");
        colCourseId.setCellValueFactory(entry ->
                new SimpleStringProperty(entry.getValue().getKey().getId().toUpperCase()));

        TableColumn<Map.Entry<Course, Double>, String> colCourseName = new TableColumn<>("Course Name");
        colCourseName.setCellValueFactory(entry ->
                new SimpleStringProperty(entry.getValue().getKey().getName()));
        colCourseName.setMinWidth(300);

        TableColumn<Map.Entry<Course, Double>, Double> colGrade = new TableColumn<>("Grade");
        colGrade.setCellValueFactory(entry -> {
            Map.Entry<Course, Double> mapEntry = entry.getValue();
            if (mapEntry.getValue() != null) {
                return new SimpleDoubleProperty(mapEntry.getValue()).asObject();
            } else {
                return new SimpleDoubleProperty(0.0).asObject(); // Or handle null as needed
            }
        });

        // Set the columns to the TableView
        tableViewEnrollments.getColumns().addAll(List.of(colCourseId, colCourseName, colGrade));

        // Populate the TableView with the student's enrollments
        ObservableList<Map.Entry<Course, Double>> enrollmentData = FXCollections.observableArrayList(student.getEnrolledCourses().entrySet());
        tableViewEnrollments.setItems(enrollmentData);

        // Add the TableView to the dialog pane
        dialogPane.getChildren().add(tableViewEnrollments);

        // Set the dialog's content
        dialog.getDialogPane().setContent(dialogPane);

        // Add OK button
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        // Show the dialog and wait for the user response
        dialog.showAndWait();
    }

    public static Optional<Student> showAddStudentDialog() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Add Student");
        dialog.setHeaderText("Enter student details:");

        ButtonType addButtonType = new ButtonType("Add", ButtonType.OK.getButtonData());
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField studentIdField = new TextField();
        studentIdField.setPromptText("Student ID");
        TextField nameField = new TextField();
        nameField.setPromptText("Name");

        grid.add(new Label("Student ID:"), 0, 0);
        grid.add(studentIdField, 1, 0);
        grid.add(new Label("Name:"), 0, 1);
        grid.add(nameField, 1, 1);

        dialog.getDialogPane().lookupButton(addButtonType).setDisable(true);

        studentIdField.textProperty().addListener((_, _, newValue) -> dialog.getDialogPane().lookupButton(addButtonType).setDisable(newValue.trim().isEmpty() || nameField.getText().trim().isEmpty()));

        nameField.textProperty().addListener((_, _, newValue) -> dialog.getDialogPane().lookupButton(addButtonType).setDisable(newValue.trim().isEmpty() || studentIdField.getText().trim().isEmpty()));

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return new Pair<>(studentIdField.getText(), nameField.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        // Convert the Pair to a Student object
        return result.map(studentData -> new Student(studentData.getKey(), studentData.getValue()));
    }
}
