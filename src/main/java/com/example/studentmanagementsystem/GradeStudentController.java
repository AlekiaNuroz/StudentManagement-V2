package com.example.studentmanagementsystem;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

import static com.example.studentmanagementsystem.IOHelper.showMessageBox;

public class GradeStudentController {

    @FXML
    public Label labelGradeStudents;
    public Button updateGradeButton;
    public TextField gradeTextField;

    @FXML
    private ComboBox<Student> studentComboBox;

    @FXML
    private ListView<Course> enrollmentListView;

    private final DatabaseManager db; // Use a single DatabaseManager instance

    public GradeStudentController() {
        this.db = new DatabaseManager(); // Initialize database manager once in constructor
    }

    @FXML
    public void initialize() {
        loadStudents();
        studentComboBox.setOnAction(_ -> populateEnrollments());

        // Set custom cell factory for ListView
        enrollmentListView.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(Course course, boolean empty) {
                super.updateItem(course, empty);
                if (empty || course == null) {
                    setText(null);
                } else {
                    Student selectedStudent = studentComboBox.getValue();
                    if (selectedStudent != null) {
                        Double grade = selectedStudent.getEnrolledCourses().get(course);
                        String gradeText = (grade != null) ? String.format("%.2f", grade) : "N/A";
                        setText(course.getId().toUpperCase() + " - " + course.getName() + " - Grade: " + gradeText);
                    } else {
                        setText(course.getId().toUpperCase() + " - " + course.getName());
                    }
                }
            }
        });
    }

    private void loadStudents() {
        List<Student> students = db.getStudents(false); // Get students from database
        ObservableList<Student> studentObservableList = FXCollections.observableArrayList(students);
        studentComboBox.setItems(studentObservableList);
    }

    private void populateEnrollments() {
        Student selectedStudent = studentComboBox.getValue();
        if (selectedStudent != null) {
            enrollmentListView.getItems().clear(); // Clear existing courses
            enrollmentListView.getItems().addAll(selectedStudent.getEnrolledCourses().keySet()); // Add courses
        }
    }

    public void handleAddGradeButtonClick() {
        Course selectedCourse = enrollmentListView.getSelectionModel().getSelectedItem();
        Student selectedStudent = studentComboBox.getValue();

        if (selectedStudent != null && selectedCourse != null) {
            try {
                String gradeText = gradeTextField.getText().trim();
                double grade = Double.parseDouble(gradeText); // Convert input to double

                if (db.assignGrade(selectedStudent.getId(), selectedCourse.getId(), grade)) {
                    // Update grade in student's enrolled courses map
                    selectedStudent.getEnrolledCourses().put(selectedCourse, grade);

                    // Refresh the UI
                    populateEnrollments(); // Update list to show new grade
                    gradeTextField.clear(); // Clear input field after update

                    showMessageBox("Student Grade Updated", "Grade for " + selectedStudent.getName() + " has been updated", Alert.AlertType.INFORMATION);
                }
            } catch (NumberFormatException e) {
                showMessageBox("Invalid Grade", "Please enter a valid numeric grade", Alert.AlertType.ERROR);
            }
        } else {
            showMessageBox("Invalid Selection", "Please select a student and a course to update the grade.", Alert.AlertType.ERROR);
        }
    }
}
