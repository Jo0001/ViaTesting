package de.jo0001.viaTesting.util;

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

    public static void alert(String ex) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ViaTesting - Error");
            alert.setHeaderText(null);
            alert.setContentText(ex);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(Util.class.getResourceAsStream("/logo-256.png")));
            alert.showAndWait();
            Platform.exit();
        });
    }

    public static String idToVersion(final int id) {
        switch (id) {
            case 47:
                return "1.8.8";
            case 110:
                return "1.9.4";
            case 210:
                return "1.10.2";
            case 316:
                return "1.11.2";
            case 335:
                return "1.12";
            case 338:
                return "1.12.1";
            case 340:
                return "1.12.2";
            case 393:
                return "1.13";
            case 401:
                return "1.13.1";
            case 404:
                return "1.13.2";
            case 477:
                return "1.14";
            case 480:
                return "1.14.1";
            case 485:
                return "1.14.2";
            case 490:
                return "1.14.3";
            case 498:
                return "1.14.4";
            case 573:
                return "1.15";
            case 575:
                return "1.15.1";
            case 578:
                return "1.15.2";
            case 735:
                return "1.16";
            case 736:
                return "1.16.1";
            case 751:
                return "1.16.2";
            case 753:
                return "1.16.3";
            case 754:
                return "1.16.5";
            case 755:
                return "1.17";
            case 756:
                return "1.17.1";
            case 757:
                return "1.18.1";
            case 758:
                return "1.18.2";
            case 759:
                return "1.19";
            case 760:
                return "1.19.2";
            case 761:
                return "1.19.3";
            case 762:
                return "1.19.4";
            default:
                return "Unknown";
        }

    }
}
