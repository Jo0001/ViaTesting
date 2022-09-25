package main.java.de.jo0001.viaTesting.util;

import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Properties;
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
                "special", "miniature", "cautious", "urban", "bookish", "refactored", "animated", "shiny", "symmetrical", "upsidedown"};
        final String[] NAMES_2 = {"chicken", "cow", "mooshroom", "pig", "sheep", "squid", "villager", "wandering_trader", "bat",
                "ocelot", "cat", "horse", "donkey", "mule", "skeleton_horse", "strider", "fox", "rabbit", "parrot", "turtle", "cod",
                "salmon", "pufferfish", "tropical_fish", "enderman", "piglin", "zombified_piglin", "dolphin", "bee", "wolf",
                "spider", "cave_spider", "polar_bear", "llama", "iron_golem", "panda", "warden", "allay", "slime", "vex", "stray", "vindicator", "witch"};
        Random random = new Random();
        File dir = new File(System.getProperty("java.io.tmpdir") + "ViaTesting-" + NAMES_1[random.nextInt(NAMES_1.length)] + "-" + NAMES_2[random.nextInt(NAMES_2.length)]);
        if (dir.exists()) {
            dir = new File(System.getProperty("java.io.tmpdir") + "ViaTesting-" + System.currentTimeMillis());
        }
        return dir;
    }

    public static void loadServerAssets(String asset, File dir) {
        loadServerAssets(asset, false, false, dir);
    }

    public static void loadServerAssets(String asset, boolean nether, boolean end, File dir) {
        String[] paperBase = {"banned-ips.json", "banned-players.json", "bukkit.yml", "commands.yml", "eula.txt", "ops.json", "paper.yml", "server.properties", "server-icon.png", "whitelist.json"};
        String[] paperWaterfall = {"spigot.yml"};
        String[] waterfall = {"config.yml", "server-icon.png", "waterfall.yml"};

        if (asset.startsWith("paper")) {
            copyFiles(paperBase, "paper", dir);
            if (nether) {
                changeProperties(dir, "allow-nether", "true");
            }
            if (end) {
                changeYML(dir, "/bukkit.yml", "settings", "allow-end", "true");
            }
            if (asset.equals("paperProxy")) {
                copyFiles(paperWaterfall, "paper-waterfall", dir);
                changeProperties(dir, "online-mode", "false");
                changeProperties(dir, "server-port", "25566");
            }
        } else if (asset.equalsIgnoreCase("waterfall")) {
            copyFiles(waterfall, "waterfall", dir);
        }
    }

    private static void changeProperties(File file, String property, String value) {
        file = new File(file + "/server.properties");
        try {
            FileInputStream input = new FileInputStream(file);
            Properties prop = new Properties();
            prop.load(input);
            input.close();
            FileOutputStream output = new FileOutputStream(file);
            prop.replace(property, value);
            prop.store(output, null);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void changeYML(File dir, String file, String nest, String property, String value) {
        try {
            YamlReader reader = new YamlReader(new FileReader(dir + file));
            LinkedHashMap<String, Object> list = (LinkedHashMap<String, Object>) reader.read();
            if (nest != null) {
                LinkedHashMap<String, Object> subList = (LinkedHashMap<String, Object>) list.get(nest);
                subList.replace(property, value);
            } else {
                list.replace(property, value);
            }
            YamlWriter writer = new YamlWriter(new FileWriter(dir + file));
            writer.write(list);
            writer.close();
        } catch (IOException exception) {
            exception.printStackTrace();
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
     * @param java Java version
     * @param dir  target directory
     */
    public static void createStartBat(String name, String java, File dir) throws IOException {
        FileOutputStream fos = new FileOutputStream(new File(dir, "start.bat"));
        String start = "@echo off\ntitle " + dir.getName() + "\necho Starting ViaTesting server\n " + getJava(java) + " -jar " + name + ".jar nogui\npause";
        fos.write(start.getBytes(StandardCharsets.UTF_8));
        fos.close();
    }

    private static String getJava(String java) {
        if (java.startsWith("System")) {
            return "java";
        } else {
            return System.getProperty("user.dir") + "\\java-" + java.split("Java ")[1] + "\\bin\\java.exe";
        }
    }
}
