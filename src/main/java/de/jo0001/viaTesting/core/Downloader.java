package main.java.de.jo0001.viaTesting.core;

import javafx.scene.control.Alert;
import main.java.de.jo0001.viaTesting.util.DownloadUtil;
import main.java.de.jo0001.viaTesting.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Downloader extends Thread {
    private final Controller controller;
    private File root;
    private final String proxySettings;
    private final String type;
    private final String version;
    private final boolean isViaBackwards;
    private final boolean isViaRewind;
    private final boolean isViaRewindLegacySupport;
    private final Logger logger;

    public Downloader(Controller controller, File root, String proxySettings, String type, String version, boolean isViaBackwards, boolean isViaRewind, boolean isViaRewindLegacySupport) {
        this.controller = controller;
        this.root = root;
        this.proxySettings = proxySettings;
        this.type = type;
        this.version = version;
        this.isViaBackwards = isViaBackwards;
        this.isViaRewind = isViaRewind;
        this.isViaRewindLegacySupport = isViaRewindLegacySupport;
        logger = Logger.getAnonymousLogger();
    }

    @Override
    public void run() {
        try {
            boolean withProxy = !proxySettings.equalsIgnoreCase("None");
            if (withProxy) {
                File defaultRoot = root;
                root = new File(root.getAbsolutePath() + "/Paper-Server");
                downloadPaperServer();
                root = new File(defaultRoot.getAbsolutePath() + "/Waterfall-Server");
                downloadWaterfallServer();
                if (!proxySettings.equalsIgnoreCase("Waterfall with Via")) {
                    root = new File(defaultRoot.getAbsolutePath() + "/Paper-Server");
                }
                downloadVia();
                root = defaultRoot;
            } else {
                downloadPaperServer();
                downloadVia();
            }

            if (System.getProperty("os.name").startsWith("Windows")) {
                if (withProxy) {
                    File temp = new File(root.getAbsolutePath());
                    Runtime.getRuntime().exec("explorer.exe " + temp);
                } else {
                    File temp = new File(root.getAbsolutePath() + "/start.bat");
                    Runtime.getRuntime().exec("explorer.exe /select," + temp);
                }
            } else {
                Util.alert("Testserver is ready (" + root + ")", Alert.AlertType.INFORMATION);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Util.alert("Error", e.toString(), Alert.AlertType.ERROR);
        } finally {
            controller.decrementCount();
        }
    }

    private void downloadPaperServer() throws IOException {
        downloadFile(DownloadUtil.getDownloadURL("paper", version), "/paper-" + version + ".jar");
    }

    private void downloadWaterfallServer() throws IOException {
        downloadFile(DownloadUtil.getDownloadURL("waterfall", DownloadUtil.getLatestWaterfallMCVersion()), "/waterfall-latest.jar");
    }

    private void downloadVia() throws IOException {
        if (type.equalsIgnoreCase("#links")) {
            downloadFile("https://ci.viaversion.com/job/ViaVersion/lastSuccessfulBuild/artifact/build/libs/ViaVersion-4.1.2-SNAPSHOT.jar");
            if (isViaBackwards) {
                downloadFile("https://ci.viaversion.com/view/ViaBackwards/job/ViaBackwards/lastSuccessfulBuild/artifact/build/libs/ViaBackwards-4.1.2-SNAPSHOT.jar");
            }
            if (isViaRewind) {
                downloadFile("https://ci.viaversion.com/view/ViaRewind/job/ViaRewind/lastSuccessfulBuild/artifact/all/target/ViaRewind-2.0.3-SNAPSHOT.jar");
                if (isViaRewindLegacySupport) {
                    downloadFile("https://ci.viaversion.com/view/All/job/ViaRewind%20Legacy%20Support/lastSuccessfulBuild/artifact/target/viarewind-legacy-support-1.4.1.jar");
                }
            }
        } else {
            downloadFile(new URL("https://api.spiget.org/v2/resources/19254/download"), "/plugins/ViaVersion-release.jar");
            if (isViaBackwards) {
                downloadFile(new URL("https://api.spiget.org/v2/resources/27448/download"), "/plugins/ViaBackwards-release.jar");
            }
            if (isViaRewind) {
                downloadFile(new URL("https://api.spiget.org/v2/resources/52109/download"), "/plugins/ViaRewind-release.jar");
                if (isViaRewindLegacySupport) {
                    downloadFile(new URL("https://api.spiget.org/v2/resources/52924/download"), "/plugins/ViaRewindLegacySupport-release.jar");
                }
            }
        }

    }

    private void downloadFile(String url) throws IOException {
        String[] urlArray = url.split("/");
        downloadFile(new URL(url), "/plugins/" + urlArray[urlArray.length - 1]);
    }

    private void downloadFile(URL url, String fileName) throws IOException {
        //from https://www.baeldung.com/java-download-file
        logger.log(Level.INFO, MessageFormat.format("Starting download of {0}", url));
        ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
        FileOutputStream fileOutputStream = new FileOutputStream(root + fileName);
        fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        readableByteChannel.close();
        fileOutputStream.close();
        logger.log(Level.INFO, MessageFormat.format("Finished download of {0}", url));
    }
}