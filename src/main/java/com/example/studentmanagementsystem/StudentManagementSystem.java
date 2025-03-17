package com.example.studentmanagementsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Objects;

@SuppressWarnings("unused")
public class StudentManagementSystem extends Application {
    private BorderPane mainLayout;
    private VBox sidebar;
    private Label dashboardLabel;

    @Override
    public void start(Stage primaryStage) {
        // Initialize the main layout
        mainLayout = new BorderPane();

        // Create and configure the sidebar
        sidebar = createSidebar();
        sidebar.setMinWidth(250);
        mainLayout.setLeft(sidebar);

        // Create and configure the dashboard area
        dashboardLabel = new Label("Welcome to the Student Management System\nSelect an option from the sidebar.");
        mainLayout.setCenter(dashboardLabel);

        // Set up the scene and stage
        Scene scene = new Scene(mainLayout, 1280, 720);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
        primaryStage.setTitle("Student Management System");
        primaryStage.setMaxWidth(1280);
        primaryStage.setMaxHeight(720);
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.setAlignment(Pos.TOP_CENTER);
        sidebar.getStyleClass().add("sidebar");


        // Create buttons for managing Students and Courses
        Label manageStudentTitle = new Label("Manage Students");
        Button viewStudentButton = createButtonWithIcon("View Students", "bi-person-lines-fill");
        Button addStudentButton = createButtonWithIcon("Add Students", "bi-person-plus");
        Button deleteStudentButton = createButtonWithIcon("Delete Students", "bi-person-dash");
        Button editStudentButton = createButtonWithIcon("Edit Students", "bi-person-check");

        Label manageCourseTitle = new Label("Manage Courses");
        Button viewCourseButton = createButtonWithIcon("View Courses", "bi-calendar2-range");
        Button addCourseButton = createButtonWithIcon("Add Courses", "bi-calendar2-plus");
        Button deleteCourseButton = createButtonWithIcon("Delete Courses", "bi-calendar2-minus");
        Button editCourseButton = createButtonWithIcon("Edit Courses", "bi-calendar2-check");

        // Set action for Students button
        viewStudentButton.setOnAction(e -> ListStudents());

        // Set action for Courses button
        viewCourseButton.setOnAction(e -> showCoursesManagement());

        // Add buttons to the sidebar
        sidebar.getChildren().addAll(manageStudentTitle, viewStudentButton, addStudentButton, deleteStudentButton,
                editStudentButton, manageCourseTitle, viewCourseButton, addCourseButton, deleteCourseButton,
                editCourseButton);
        return sidebar;
    }

    private Button createButtonWithIcon(String text, String iconName) {
        Button button = new Button(text);
        button.setMinWidth(200);
        FontIcon icon = new FontIcon(iconName); // Create FontIcon with the icon name
        icon.getStyleClass().add("font-icon");
        button.setGraphic(icon); // Set the graphic to the button
        return button;
    }

    private void ListStudents() {
        try {
            // Load the FXML file and create the scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("list_students.fxml"));
            mainLayout.setCenter(loader.load());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showCoursesManagement() {
        dashboardLabel.setText("Course Management:\nHere you can add, edit, or delete courses.");
        // Additional functionality to manage courses can be added here
    }

    public static void main(String[] args) {
        launch();
    }
}