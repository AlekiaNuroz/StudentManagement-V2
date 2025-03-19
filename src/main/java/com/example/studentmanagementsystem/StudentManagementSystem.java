package com.example.studentmanagementsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Objects;

@SuppressWarnings("unused")
public class StudentManagementSystem extends Application {
    private BorderPane mainLayout;
    private Label dashboardLabel;

    @Override
    public void start(Stage primaryStage) {
        // Initialize the main layout
        mainLayout = new BorderPane();

        // Create and configure the sidebar
        VBox sidebar = createSidebar();
        sidebar.setMinWidth(250);
        mainLayout.setLeft(sidebar);

        try {
            // Load the FXML file and create the scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard.fxml"));
            mainLayout.setCenter(loader.load());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set up the scene and stage
        Scene scene = new Scene(mainLayout, 1280, 720);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
        primaryStage.setTitle("Student Management System");
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("person-bounding-box.png"))));
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
        Label manageStudentTitle = new Label("Students");
        Button viewStudentButton = createButtonWithIcon("Manage Students", "bi-person-lines-fill");
        Button assignGradeButton = createButtonWithIcon("Assign Grade", "bi-check");

        Label manageCourseTitle = new Label("Courses");
        Button viewCourseButton = createButtonWithIcon("Manage Courses", "bi-calendar2-range");
        Button enrollCourseButton = createButtonWithIcon("Enroll Courses", "bi-calendar-check");

        // Set action for viewing/editing students button
        viewStudentButton.setOnAction(e -> manageStudents());

        assignGradeButton.setOnAction(e -> gradeStudent());

        // Set action for Courses button
        viewCourseButton.setOnAction(e -> manageCourses());

        enrollCourseButton.setOnAction(e -> enrollCourses());

        // Add buttons to the sidebar
        sidebar.getChildren().addAll(manageStudentTitle, viewStudentButton, assignGradeButton, manageCourseTitle,
                viewCourseButton, enrollCourseButton);
        return sidebar;
    }

    private void gradeStudent() {
        try {
            // Load the FXML file and create the scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("gradeStudent.fxml"));
            mainLayout.setCenter(loader.load());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Button createButtonWithIcon(String text, String iconName) {
        Button button = new Button(text);
        button.setMinWidth(200);
        FontIcon icon = new FontIcon(iconName); // Create FontIcon with the icon name
        icon.getStyleClass().add("font-icon");
        button.setGraphic(icon); // Set the graphic to the button
        return button;
    }

    private void manageStudents() {
        try {
            // Load the FXML file and create the scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("manageStudents.fxml"));
            mainLayout.setCenter(loader.load());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addStudents() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addStudent.fxml"));
            mainLayout.setCenter(loader.load());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void manageCourses() {
        try {
            // Load the FXML file and create the scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("manageCourses.fxml"));
            mainLayout.setCenter(loader.load());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void enrollCourses() {
        try {
            // Load the FXML file and create the scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("courseEnrollment.fxml"));
            mainLayout.setCenter(loader.load());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}