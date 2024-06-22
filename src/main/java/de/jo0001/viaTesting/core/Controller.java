package de.jo0001.viaTesting.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.jo0001.viaTesting.util.UpdateCheck;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import de.jo0001.viaTesting.util.AssetUtil;
import de.jo0001.viaTesting.util.DownloadUtil;
import de.jo0001.viaTesting.util.Util;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Controller implements Initializable {
    @FXML
    ChoiceBox typeCB, versionCB, proxyCB, javaCB;
    @FXML
    CheckBox vB, vR, vRSup, aNether, aEnd;
    @FXML
    Button btn, loadBtn;
    @FXML
    Label inWorkNumber;
    @FXML
    ProgressBar inWorkProgress;
    @FXML
    TextField dumpUrl;
    final int MAX_CONCURRENT_SETUPS = 4;

    private int count = 0;
    private final Logger logger = Logger.getAnonymousLogger();
    private final ArrayList<String> mcVersionsPaper = new ArrayList<>();
    private final HashMap<String, String> mojangjars = new HashMap<>();

    public Controller() {
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        new UpdateCheck().start();

        try {
            logger.log(Level.INFO, "Fetching Mojang data");
            DownloadUtil.getVersions().forEach(e -> mcVersionsPaper.add(e.getAsString()));
            JsonObject mojangData = DownloadUtil.getMojangData();
            mojangData.getAsJsonArray("versions").forEach(e -> {
                JsonObject eo = e.getAsJsonObject();
                String id = eo.get("id").getAsString();
                if (eo.get("type").getAsString().equals("release") && mcVersionsPaper.contains(id)) {
                    try {
                        mojangjars.put(id, DownloadUtil.getMojangJarUrl(eo.get("url").getAsString()));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            logger.log(Level.INFO, "Finished fetching Mojang data");
        }

        Platform.runLater(() -> {
            ObservableList<String> list = FXCollections.observableArrayList();
            list.addAll(mcVersionsPaper);
            Collections.reverse(list);
            versionCB.setItems(list);
            versionCB.setValue(list.get(0));
        });
        ObservableList items = javaCB.getItems();
        String value = "System (" + System.getProperty("java.version") + ")";
        items.add(0, value);
        int[] javaVersions = {8, 11, 16, 17, 19, 20, 21, 22};
        for (int javaVersion : javaVersions) {
            if (new File("java-" + javaVersion).exists()) {
                items.add("Java " + javaVersion);
            }
        }
        javaCB.setItems(items);
        javaCB.setValue(value);


        vRSup.setOnAction(actionEvent -> {
            if (vRSup.isSelected()) {
                vR.setSelected(true);
            }
        });
        vR.setOnAction(actionEvent -> {
            if (!vR.isSelected() && vRSup.isSelected()) {
                vRSup.setSelected(false);
            }
        });
        proxyCB.setOnAction(actionEvent -> {
            if (proxyCB.getValue().toString().equalsIgnoreCase("Waterfall with Via") || proxyCB.getValue().toString().equalsIgnoreCase("Bungee with Via") || proxyCB.getValue().toString().equalsIgnoreCase("Velocity with Via")) {
                vRSup.setSelected(false);
                vRSup.setDisable(true);
            } else {
                vRSup.setDisable(false);
            }
        });

        btn.setOnAction(actionEvent -> {
            try {
                create();
            } catch (IOException e) {
                e.printStackTrace();
                Util.alert("Error", e.toString(), Alert.AlertType.ERROR);
            }
        });

        loadBtn.setOnAction(actionEvent -> {
            try {
                loadFromDump();
            } catch (Exception e) {
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
            String version = versionCB.getValue().toString();
            String type = typeCB.getValue().toString();
            String java = javaCB.getValue().toString();
            boolean allowNether = aNether.isSelected();
            boolean allowEnd = aEnd.isSelected();

            //load the default server assets
            String serverAssets = withProxy ? "paperProxy" : "paper";
            File pluginsDir, cacheDir;
            if (withProxy) {
                File splitDir = new File(dir.getAbsolutePath() + "/Paper-Server");
                splitDir.mkdir();
                cacheDir = new File(splitDir.getPath() + "/cache");
                AssetUtil.loadServerAssets(serverAssets, allowNether, allowEnd, splitDir);
                AssetUtil.createStartBat("paper-" + version, java, splitDir);
                pluginsDir = new File(splitDir.getPath() + "/plugins");
                pluginsDir.mkdir();
                splitDir = proxySettings.contains("Bungee") ? new File(dir.getAbsolutePath() + "/Bungee-Server") : proxySettings.contains("Velocity") ? new File(dir.getAbsolutePath() + "/Velocity-Server") : new File(dir.getAbsolutePath() + "/Waterfall-Server");
                splitDir.mkdir();
                pluginsDir = new File(splitDir.getPath() + "/plugins");
                if (proxySettings.contains("Velocity")) {
                    AssetUtil.loadServerAssets("velocity", splitDir);
                } else {
                    AssetUtil.loadServerAssets("waterfall", splitDir);
                }
                AssetUtil.createStartBat(proxySettings.contains("Bungee") ? "bungee-latest" : proxySettings.contains("Velocity") ? "velocity-latest" : "waterfall-latest", java, splitDir);
            } else {
                AssetUtil.loadServerAssets(serverAssets, allowNether, allowEnd, dir);
                AssetUtil.createStartBat("paper-" + version, java, dir);
                pluginsDir = new File(dir.getPath() + "/plugins");
                cacheDir = new File(dir.getPath() + "/cache");
            }
            pluginsDir.mkdir();
            cacheDir.mkdir();

            Downloader downloader = new Downloader(this, dir, proxySettings, type, version, vB.isSelected(), vR.isSelected(), vRSup.isSelected(), new URL(mojangjars.get(version)));
            downloader.start();
        } else {
            Util.alert(MAX_CONCURRENT_SETUPS + " is a nice number", "Please wait. There are already " + MAX_CONCURRENT_SETUPS + " testing setups in work", Alert.AlertType.INFORMATION);
        }
    }

    private void loadFromDump() throws IOException {
        String dUrl = dumpUrl.getText();
        if (dUrl.startsWith("https://dump.viaversion.com/")) {
            dUrl = "https://dump.viaversion.com/raw/" + dUrl.split("/")[3];
            logger.log(Level.INFO, "Fetching dump from " + dUrl);
            JsonObject dumpData = DownloadUtil.getDumpData(dUrl);
            JsonObject versionInfo = dumpData.getAsJsonObject("versionInfo");
            boolean isProxy = false;
            String platform = versionInfo.get("platformName").getAsString();
            if (platform.equalsIgnoreCase("Waterfall")) {
                proxyCB.setValue("Waterfall with Via");
                isProxy = true;
            } else if (platform.equalsIgnoreCase("BungeeCord")) {
                proxyCB.setValue("Bungee with Via");
                isProxy = true;
            }

            for (JsonElement element : versionInfo.getAsJsonArray("subPlatforms")) {
                String sub = element.getAsString().toLowerCase();
                if (sub.contains("viabackwards")) {
                    vB.setSelected(true);
                } else if (sub.contains("viarewind")) {
                    vR.setSelected(true);
                }
            }

            if (!isProxy) {//don't set version or check for VRLS on proxies
                String version = Util.idToVersion(versionInfo.get("serverProtocol").getAsInt());
                if (!version.equalsIgnoreCase("Unknown")) {
                    versionCB.setValue(version);
                }

                for (JsonElement el : dumpData.getAsJsonObject("platformDump").getAsJsonArray("plugins")) {
                    if (el.getAsJsonObject().get("name").getAsString().equalsIgnoreCase("ViaRewind-Legacy-Support")) {
                        vRSup.setSelected(true);
                        break;
                    }
                }
            }
        }
    }
}