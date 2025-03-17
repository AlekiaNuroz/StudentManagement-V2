package com.example.studentmanagementsystem;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.util.List;

import static com.example.studentmanagementsystem.IOHelper.showMessageBox;

public class ListStudentsController {
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

    DatabaseManager db = new DatabaseManager();

    public void initialize() {
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
}
