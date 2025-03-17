package com.example.studentmanagementsystem;

import javafx.scene.control.Alert;

public class IOHelper {
    public static void showMessageBox(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);  // Create an Alert of type INFORMATION
        alert.setTitle(title);                         // Set the title of the alert
        alert.setHeaderText(null);                     // Optionally set a header; null means no header
        alert.setContentText(message);                 // Set the content text of the alert

        alert.showAndWait();                           // Show the alert and wait for the user to close it
    }
}
