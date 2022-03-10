package main.java.de.jo0001.viaTesting.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogEvent;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Util {
    public static void alert(String message, Alert.AlertType alertType) {
        alert("", message, alertType);
    }

    public static void alert(String title, String message, Alert.AlertType alertType) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle((title.equals("")) ? "ViaTesting" : "ViaTesting - " + title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(Util.class.getResourceAsStream("/logo-256.png")));
            alert.showAndWait();
        });
    }

    public static void alert(String ex){
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle( "ViaTesting - Error");
            alert.setHeaderText(null);
            alert.setContentText(ex);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(Util.class.getResourceAsStream("/logo-256.png")));
            alert.showAndWait();
            Platform.exit();
        });
    }
}
