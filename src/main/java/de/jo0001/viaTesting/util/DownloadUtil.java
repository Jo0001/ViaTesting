package de.jo0001.viaTesting.util;

import com.google.gson.*;
import de.jo0001.viaTesting.core.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadUtil {
    public static URL getDownloadURL(String type, String version) throws IOException {
        int build = getLatestBuild(type, version);
        return new URL("https://api.papermc.io/v2/projects/" + type + "/versions/" + version + "/builds/" + build + "/downloads/" + type + "-" + version + "-" + build + ".jar");
    }

    private static int getLatestBuild(String type, String version) throws IOException {
        String url = "https://api.papermc.io/v2/projects/" + type + "/versions/" + version;
        JsonArray builds = fetchData(url).getAsJsonArray("builds");
        return builds.get(builds.size() - 1).getAsInt();
    }

    public static String getLatestProxyMCVersion(String proxy) throws IOException {
        JsonArray versions = fetchData("https://api.papermc.io/v2/projects/" + proxy).getAsJsonArray("versions");
        return versions.get(versions.size() - 1).getAsString();
    }

    public static String getLatestViaFileUrl(String project, String type) throws IOException {
        if (type.equalsIgnoreCase("dev") && !project.equalsIgnoreCase("ViaRewind%20Legacy%20Support")) {
            project = project + "-DEV";
        } else if (type.equalsIgnoreCase("Downgraded") && !project.equalsIgnoreCase("ViaRewind%20Legacy%20Support")) {
            project = project + "-Java8";
        }
        return "https://ci.viaversion.com/job/" + project + "/lastSuccessfulBuild/artifact/" + fetchData("https://ci.viaversion.com/job/" + project + "/lastSuccessfulBuild/api/json?tree=artifacts[relativePath]").getAsJsonArray("artifacts").get(0).getAsJsonObject().get("relativePath").getAsString();
    }

    public static String getLatestViaFromHangar(String project) throws IOException {
        return getLatestViaFromHangar(project, "PAPER");
    }

    public static String getLatestViaFromHangar(String project, String platform) throws IOException {
        JsonObject data = fetchData("https://hangar.papermc.io/api/v1/projects/" + project + "/versions?channel=Release&limit=1&offset=0&platform=" + platform).getAsJsonArray("result").get(0).getAsJsonObject().getAsJsonObject("downloads").getAsJsonObject(platform);
        return data.get("fileInfo").isJsonNull() ? data.get("externalUrl").getAsString() : data.get("downloadUrl").getAsString();
    }

    public static JsonArray getVersions() throws IOException {
        return fetchData("https://api.papermc.io/v2/projects/paper").getAsJsonArray("versions");
    }

    public static JsonObject getLatestViaTesting() throws IOException {
        return fetchData("https://api.github.com/repos/Jo0001/ViaTesting/releases/latest");
    }

    public static JsonObject getDumpData(String url) throws IOException {
        return fetchData(url);
    }

    public static JsonObject getMojangData() throws IOException {
        return fetchData("https://piston-meta.mojang.com/mc/game/version_manifest.json");
    }

    public static String getMojangJarUrl(String meta) throws IOException {
        return fetchData(meta).get("downloads").getAsJsonObject().get("server").getAsJsonObject().get("url").getAsString();
    }

    private static JsonObject fetchData(String url) throws IOException {
        //stolen from Eduard
        final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestProperty("User-Agent", "ViaTesting-" + Main.VERSION);
        final StringBuilder contentBuilder = new StringBuilder();
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String input;
            while ((input = in.readLine()) != null) {
                contentBuilder.append(input);
            }
        }
        final String content = contentBuilder.toString();
        return Main.GSON.fromJson(content, JsonObject.class);
    }
}
