package main.java.de.jo0001.viaTesting.core;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import main.java.de.jo0001.viaTesting.util.AssetUtil;
import main.java.de.jo0001.viaTesting.util.DownloadUtil;
import main.java.de.jo0001.viaTesting.util.Util;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    ChoiceBox typeCB, choiceBox, proxyCB;
    @FXML
    CheckBox vB, vR, vRSup;
    @FXML
    Button btn;
    @FXML
    Label inWorkNumber;
    @FXML
    ProgressBar inWorkProgress;
    final int MAX_CONCURRENT_SETUPS = 4;

    private int count = 0;

    public Controller() {
        System.out.println("Controller loading");
    }



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //todo check vR when vRSup is checked
        vR.setOnAction(actionEvent -> {
            if (!vR.isSelected() && vRSup.isSelected()) {
                vRSup.setSelected(false);
            }
        });
        proxyCB.setOnAction(actionEvent -> {
            if (proxyCB.getValue().toString().equalsIgnoreCase("Waterfall with Via")) {
                vRSup.setSelected(false);
            }
        });
        //todo cancel vRSup check event when  "Waterfall with Via" is selected
        btn.setOnAction(actionEvent -> {
            try {
                create();
            } catch (IOException e) {
                e.printStackTrace();
                Util.alert("Error", e.toString(), Alert.AlertType.ERROR);
            }
        });
    }

    public void incrementCount() {
        count++;
        updateCount(count);
    }

    public void decrementCount() {
        count--;
        updateCount(count);

    }

    private void updateCount(int count) {
        Platform.runLater(() -> {
            inWorkNumber.setText(count + "/" + MAX_CONCURRENT_SETUPS);
            inWorkProgress.setProgress((float) 1 / MAX_CONCURRENT_SETUPS * count);
        });
    }

    private void create() throws IOException {
        if (count < MAX_CONCURRENT_SETUPS) {
            incrementCount();
            File dir = AssetUtil.getDir();
            dir.mkdir();

            String proxySettings = proxyCB.getValue().toString();
            boolean withProxy = !proxySettings.equalsIgnoreCase("None");
            String version = choiceBox.getValue().toString();
            String type = typeCB.getValue().toString();

            //load the default server assets
            String serverAssets = withProxy ? "paperProxy" : "paper";
            File pluginsDir;
            if (withProxy) {
                File splitDir = new File(dir.getAbsolutePath() + "/Paper-Server");
                splitDir.mkdir();
                AssetUtil.loadServerAssets(serverAssets, splitDir);
                AssetUtil.createStartBat("paper-" + version, splitDir);
                pluginsDir = new File(splitDir.getPath() + "/plugins");
                pluginsDir.mkdir();
                splitDir = new File(dir.getAbsolutePath() + "/Waterfall-Server");
                splitDir.mkdir();
                pluginsDir = new File(splitDir.getPath() + "/plugins");
                AssetUtil.loadServerAssets("waterfall", splitDir);
                AssetUtil.createStartBat("waterfall-latest", splitDir);
            } else {
                AssetUtil.loadServerAssets(serverAssets, dir);
                AssetUtil.createStartBat("paper-" + version, dir);
                pluginsDir = new File(dir.getPath() + "/plugins");
            }
            pluginsDir.mkdir();

            Downloader downloader = new Downloader(this, dir, proxySettings, type, version, vB.isSelected(), vR.isSelected(), vRSup.isSelected());
            downloader.start();
        } else {
            Util.alert(MAX_CONCURRENT_SETUPS + " is a nice number", "Please wait. There are already " + MAX_CONCURRENT_SETUPS + " testing setups in work", Alert.AlertType.INFORMATION);
        }
    }
}