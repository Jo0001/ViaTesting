package main.java.de.jo0001.viaTesting.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Random;

public class AssetUtil {
    /**
     * Creates the name of the root directory, either a funny name or uses {@link System#currentTimeMillis()} as fallback
     *
     * @return root directory
     */
    public static File getDir() {
        final String[] NAMES_1 = {"funny", "silver", "turbo", "glowing", "musical", "fluffy", "super", "friendly",
                "fictional", "automatic", "rotary", "upgraded", "reimagined", "fantastic", "ubiquitous", "redesigned",
                "special", "miniature", "cautious", "urban", "bookish", "refactored", "animated", "shiny", "symmetrical"};
        final String[] NAMES_2 = {"chicken", "cow", "mooshroom", "pig", "sheep", "squid", "villager", "wandering_trader", "bat",
                "ocelot", "cat", "horse", "donkey", "mule", "skeleton_horse", "strider", "fox", "rabbit", "parrot", "turtle", "cod",
                "salmon", "pufferfish", "tropical_fish", "enderman", "piglin", "zombified_piglin", "dolphin", "bee", "wolf",
                "spider", "cave_spider", "polar_bear", "llama", "iron_golem", "panda"};
        Random random = new Random();
        File dir = new File("ViaTesting-" + NAMES_1[random.nextInt(NAMES_1.length)] + "-" + NAMES_2[random.nextInt(NAMES_2.length)]);
        if (dir.exists()) {
            dir = new File("ViaTesting-" + System.currentTimeMillis());
        }
        return dir;
    }

    public static void loadServerAssets(String asset, File dir) {
        String[] paperBase = {"banned-ips.json", "banned-players.json", "bukkit.yml", "commands.yml", "eula.txt", "ops.json", "paper.yml", "permissions.yml", "server.properties", "server-icon.png", "whitelist.json"};
        String[] paperWaterfall = {"paper.yml", "server.properties"};
        String[] waterfall = {"config.yml", "server-icon.png", "waterfall.yml"};

        if (asset.equals("paperProxy")) {
            copyFiles(paperBase, "paper", dir);
            copyFiles(paperWaterfall, "paper-waterfall", dir);
        } else if (asset.equalsIgnoreCase("waterfall")) {
            copyFiles(waterfall, "waterfall", dir);
        } else {
            copyFiles(paperBase, "paper", dir);
        }
    }

    private static void copyFiles(String[] files, String base, File output) {
        for (String s : files) {
            InputStream stream = AssetUtil.class.getResourceAsStream("/assets/" + base + "/" + s);
            try {
                Files.copy(stream, Paths.get(output + "/" + s), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Creates a start.bat
     *
     * @param name jar name
     * @param dir  target directory
     */
    public static void createStartBat(String name, File dir) throws IOException {
        FileOutputStream fos = new FileOutputStream(new File(dir, "start.bat"));
        String start = "@echo off\ntitle " + dir.getName() + "\necho Starting ViaTesting server\n java -jar " + name + ".jar nogui\npause";
        fos.write(start.getBytes(StandardCharsets.UTF_8));
        fos.close();
    }
}
