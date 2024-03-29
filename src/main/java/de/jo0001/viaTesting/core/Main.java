package de.jo0001.viaTesting.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class Main extends Application {
    public static Gson GSON = new GsonBuilder().create();
    public static final String VERSION = detectVersion();

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL fxmlpath = Main.class.getClassLoader().getResource("core.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlpath);
        Parent root = loader.load();
        Controller controller = loader.getController();
        primaryStage.setTitle("ViaTesting - " + VERSION);
        primaryStage.setScene(new Scene(root, 600, 430));
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/logo-256.png")));
        primaryStage.resizableProperty().setValue(Boolean.FALSE);
        primaryStage.getScene().getStylesheets().add(Main.class.getResource("/style.css").toExternalForm());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static String detectVersion() {
        try {
            InputStream input = Main.class.getResourceAsStream("/version.properties");
            Properties prop = new Properties();
            prop.load(input);
            input.close();
            return prop.get("version").toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
