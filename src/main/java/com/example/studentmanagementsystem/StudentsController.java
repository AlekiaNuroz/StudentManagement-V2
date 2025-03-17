package com.example.studentmanagementsystem;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.List;

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
        listViewStudents.setOnMouseClicked(event -> {
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
}
