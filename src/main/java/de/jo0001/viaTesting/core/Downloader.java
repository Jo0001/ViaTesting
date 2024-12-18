package de.jo0001.viaTesting.core;

import javafx.scene.control.Alert;
import de.jo0001.viaTesting.util.DownloadUtil;
import de.jo0001.viaTesting.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Downloader extends Thread {
    private final Controller controller;
    private final File root;
    private final String proxySettings;
    private boolean withProxy;
    private String proxy;
    private final String type;
    private final String version;
    private final boolean isViaBackwards;
    private final boolean isViaRewind;
    private final boolean isViaRewindLegacySupport;
    private final URL mUrl;
    private final Logger logger;

    public Downloader(Controller controller, File root, String proxySettings, String type, String version, boolean isViaBackwards, boolean isViaRewind, boolean isViaRewindLegacySupport, URL mUrl) {
        this.controller = controller;
        this.root = root;
        this.proxySettings = proxySettings;
        this.type = type;
        this.version = version;
        this.isViaBackwards = isViaBackwards;
        this.isViaRewind = isViaRewind;
        this.isViaRewindLegacySupport = isViaRewindLegacySupport;
        this.mUrl = mUrl;
        logger = Logger.getAnonymousLogger();
    }

    @Override
    public void run() {
        List<CompletableFuture<Void>> downloads = new ArrayList<>();

        withProxy = !proxySettings.equalsIgnoreCase("None");
        if (withProxy) {
            downloads.add(CompletableFuture.runAsync(() -> {
                try {
                    downloadPaperServer();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));
            downloads.add(CompletableFuture.runAsync(() -> {
                try {
                    downloadMojangServer();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));

            if (proxySettings.contains("Bungee")) {
                proxy = "bungee";
                downloads.add(CompletableFuture.runAsync(() -> {
                    try {
                        downloadBungee();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }));
            } else if (proxySettings.contains("Velocity")) {
                proxy = "velocity";
                downloads.add(CompletableFuture.runAsync(() -> {
                    try {
                        downloadVelocityServer();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }));
            } else {
                proxy = "waterfall";
                downloads.add(CompletableFuture.runAsync(() -> {
                    try {
                        downloadWaterfallServer();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }));
            }

            downloads.add(CompletableFuture.runAsync(() -> {
                try {
                    downloadVia();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));
        } else {
            downloads.add(CompletableFuture.runAsync(() -> {
                try {
                    downloadPaperServer();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));
            downloads.add(CompletableFuture.runAsync(() -> {
                try {
                    downloadMojangServer();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));
            downloads.add(CompletableFuture.runAsync(() -> {
                try {
                    downloadVia();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));
        }

        CompletableFuture<Void> allDownloads = CompletableFuture.allOf(downloads.toArray(new CompletableFuture[0]));

        // Continue after all downloads are finished
        allDownloads.thenRun(() -> {
            try {
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
        });
        allDownloads.join();
    }

    private void downloadPaperServer() throws IOException {
        downloadFile(DownloadUtil.getDownloadURL("paper", version), getBase("paper") + "/paper-" + version + ".jar");
    }

    private void downloadMojangServer() throws IOException {
        downloadFile(mUrl, getBase("paper") + "/cache/mojang_" + version + ".jar");
    }

    private void downloadWaterfallServer() throws IOException {
        downloadFile(DownloadUtil.getDownloadURL("waterfall", DownloadUtil.getLatestProxyMCVersion("waterfall")), getBase("waterfall") + "/waterfall-latest.jar");
    }

    private void downloadVelocityServer() throws IOException {
        downloadFile(DownloadUtil.getDownloadURL("velocity", DownloadUtil.getLatestProxyMCVersion("velocity")), getBase("velocity") + "/velocity-latest.jar");
    }

    private void downloadBungee() throws IOException {
        downloadFile(new URL("https://ci.md-5.net/job/BungeeCord/lastSuccessfulBuild/artifact/bootstrap/target/BungeeCord.jar"), getBase("bungee") + "/bungee-latest.jar");
    }

    private void downloadVia() throws IOException {
        if (type.equalsIgnoreCase("Release")) {
            downloadFile(new URL(DownloadUtil.getLatestViaFromHangar("ViaVersion")), getBase("plugin") + "/ViaVersion-release.jar");
            if (isViaBackwards) {
                downloadFile(new URL(DownloadUtil.getLatestViaFromHangar("ViaBackwards")), getBase("plugin") + "/ViaBackwards-release.jar");
            }
            if (isViaRewind) {
                downloadFile(new URL(DownloadUtil.getLatestViaFromHangar("ViaRewind")), getBase("plugin") + "/ViaRewind-release.jar");
                if (isViaRewindLegacySupport) {
                    downloadFile(new URL(DownloadUtil.getLatestViaFromHangar("ViaRewindLegacySupport")), getBase("plugin") + "/ViaRewindLegacySupport-release.jar");
                }
            }
        } else {
            downloadFile(DownloadUtil.getLatestViaFileUrl("ViaVersion", type));
            if (isViaBackwards) {
                downloadFile(DownloadUtil.getLatestViaFileUrl("ViaBackwards", type));
            }
            if (isViaRewind) {
                downloadFile(DownloadUtil.getLatestViaFileUrl("ViaRewind", type));
                if (isViaRewindLegacySupport) {
                    downloadFile(DownloadUtil.getLatestViaFileUrl("ViaRewind%20Legacy%20Support", type));
                }
            }
        }
        if (proxySettings.equalsIgnoreCase("Bungee with Via") || proxySettings.equalsIgnoreCase("Waterfall with Via")) {
            downloadFile(new URL(DownloadUtil.getLatestViaFromHangar("ViaBungee", "WATERFALL")), getBase("loader") + "/ViaBungee-release.jar");
        }
    }

    private void downloadFile(String url) throws IOException {
        String[] urlArray = url.split("/");
        downloadFile(new URL(url), getBase("plugin") + "/" + urlArray[urlArray.length - 1]);
    }

    private void downloadFile(URL url, String fileName) throws IOException {
        //from https://www.baeldung.com/java-download-file
        logger.log(Level.INFO, MessageFormat.format("Starting download of {0}", url));
        ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        readableByteChannel.close();
        fileOutputStream.close();
        logger.log(Level.INFO, MessageFormat.format("Finished download of {0}", url));
    }

    private String getBase(String type) {
        if (type.equals("paper")) {
            if (withProxy) {
                return root.getPath() + "/paper-server";
            } else {
                return root.getPath();
            }
        }
        if (type.equals("plugin")) {
            if (withProxy) {
                if (proxySettings.contains("Via")) {
                    if (proxySettings.contains("Bungee") || proxySettings.contains("Waterfall")) {
                        return getBase(proxy) + "/plugins/ViaVersion";
                    } else {
                        return getBase(proxy) + "/plugins";
                    }
                } else {
                    return getBase("paper") + "/plugins";
                }
                //return (proxySettings.contains("Via") ? getBase(proxy) : getBase("paper")) + "/plugins";
            } else {
                return root.getPath() + "/plugins";
            }
        }
        if (type.equals("loader")) {
            return getBase(proxy) + "/plugins";
        }
        if (withProxy) {
            return root.getPath() + "/" + proxy + "-server";
        }
        return null;
    }
}