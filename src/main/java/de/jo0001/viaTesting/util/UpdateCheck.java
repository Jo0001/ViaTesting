package de.jo0001.viaTesting.util;

import com.google.gson.JsonObject;
import de.jo0001.viaTesting.core.Main;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UpdateCheck extends Thread {
    private final Logger logger = Logger.getAnonymousLogger();

    @Override
    public void run() {
        try {
            logger.log(Level.INFO, "Checking for updates ...");
            JsonObject releaseData = DownloadUtil.getLatestViaTesting();//Note: API doesn't show pre-releases
            double releaseVersion = Double.parseDouble(releaseData.get("tag_name").getAsString());
            boolean currentVersionIsSnapshot = Main.VERSION.contains("SNAPSHOT");
            double currentVersion = Double.parseDouble(currentVersionIsSnapshot ? Main.VERSION.split("-")[0] : Main.VERSION);
            if ((!currentVersionIsSnapshot && releaseVersion > currentVersion) || (currentVersionIsSnapshot && releaseVersion == currentVersion)) {
                logger.log(Level.INFO, "Found a newer version: " + releaseVersion);
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("ViaTesting - Update");
                    alert.setHeaderText(null);
                    alert.setContentText("There is a new version available. Open the GitHub Release Page?");
                    Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(new Image(getClass().getResourceAsStream("/logo-256.png")));

                    Optional<ButtonType> result = alert.showAndWait();
                    if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
                        try {
                            Desktop.getDesktop().browse(URI.create("https://github.com/Jo0001/ViaTesting/releases/latest"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
