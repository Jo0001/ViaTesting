package main.java.de.jo0001.viaTesting.util;

import com.google.gson.*;
import main.java.de.jo0001.viaTesting.core.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadUtil {
    public static URL getDownloadURL(String type, String version) throws IOException {
        int build = getLatestBuild(type, version);
        return new URL("https://papermc.io/api/v2/projects/" + type + "/versions/" + version + "/builds/" + build + "/downloads/" + type + "-" + version + "-" + build + ".jar");
    }

    private static int getLatestBuild(String type, String version) throws IOException {
        String url = "https://papermc.io/api/v2/projects/" + type + "/versions/" + version;
        JsonArray builds = fetchData(url).getAsJsonArray("builds");
        return builds.get(builds.size() - 1).getAsInt();
    }

    public static String getLatestWaterfallMCVersion() throws IOException {
        JsonArray versions = fetchData("https://papermc.io/api/v2/projects/waterfall").getAsJsonArray("versions");
        return versions.get(versions.size() - 1).getAsString();
    }

    public static String getLatestViaFileUrl(String project) throws IOException {
        return "https://ci.viaversion.com/job/" + project + "/lastSuccessfulBuild/artifact/" + fetchData("https://ci.viaversion.com/job/" + project + "/lastSuccessfulBuild/api/json?tree=artifacts[relativePath]").getAsJsonArray("artifacts").get(0).getAsJsonObject().get("relativePath").getAsString();
    }

    public static JsonArray getVersions() throws IOException {
        return fetchData("https://papermc.io/api/v2/projects/paper").getAsJsonArray("versions");
    }

    private static JsonObject fetchData(String url) throws IOException {
        //stolen from Eduard
        final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestProperty("User-Agent", "ViaTesting");
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
