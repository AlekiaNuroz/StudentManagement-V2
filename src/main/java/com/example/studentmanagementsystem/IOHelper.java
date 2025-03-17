package com.example.studentmanagementsystem;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class IOHelper {
    public static void showMessageBox(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);  // Create an Alert of type INFORMATION
        alert.setTitle(title);                         // Set the title of the alert
        alert.setHeaderText(null);                     // Optionally set a header; null means no header
        alert.setContentText(message);                 // Set the content text of the alert

        alert.showAndWait();                           // Show the alert and wait for the user to close it
    }

    public static boolean showConfirmationDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION); // Create confirmation alert
        alert.setTitle(title);                                // Set the dialog title
        alert.setHeaderText(null);                            // No header text
        alert.setContentText(message);                        // Set the content message

        // Optionally, you can set custom button types
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        // Show the alert and wait for user response
        Optional<ButtonType> result = alert.showAndWait(); // Show alert and wait for response

        // Return true if the user clicked YES, otherwise false
        return result.isPresent() && result.get() == ButtonType.YES;
    }
}
