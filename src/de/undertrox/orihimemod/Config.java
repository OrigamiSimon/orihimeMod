package de.undertrox.orihimemod;

import de.undertrox.orihimemod.keybind.Keybind;
import javafx.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Config {
    private static Config instance;

    public boolean SHOW_NUMBER_TOOLTIPS = false;
    public String GENERATED_VERSION = "error loading version";
    private List<Pair<String, String>> parsed = new ArrayList<>();
    private List<Keybind> keybinds = new ArrayList<>();

    private Config() {
    }

    private static Config getInstance() {
        if (instance == null) {
            throw new RuntimeException("Tried to access Config before loading Config file.");
        }
        return instance;
    }

    public static boolean showNumberTooltips() {
        return getInstance().SHOW_NUMBER_TOOLTIPS;
    }

    public static String generatedVersion() {
        return getInstance().GENERATED_VERSION;
    }

    public static List<Keybind> keybinds() {
        return getInstance().keybinds;
    }

    public static void load(String configFileName) {
        instance = new Config();
        File file = new File(configFileName);
        if (!file.exists()) {
            createConfigFile(configFileName);
        }
        String line;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine()) != null) {
                Pair<String, String> parsedLine = parseLine(line);
                if (parsedLine != null) {
                    instance.parsed.add(parsedLine);
                    parsePair(parsedLine);
                }
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private static void createConfigFile(String configFileName) {
        System.out.println("No config file found, generating default config file.");
        InputStream reader = instance.getClass().getResourceAsStream("orihimeKeybinds.cfg");
        OutputStream writer;
        try {
            writer = new FileOutputStream(new File(configFileName));
            copy(reader, writer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void copy(InputStream from, OutputStream to) {
        byte[] buffer = new byte[1024];
        int length;
        while (true) {
            try {
                if (!((length = from.read(buffer)) > 0)) break;
                to.write(buffer, 0, length);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Nullable
    private static Pair<String, String> parseLine(String line) {
        line = line.trim();
        if (line.length() == 0 || line.charAt(0) == '#') { // Comments and empty lines
            return null;
        }
        StringBuilder configName = new StringBuilder();
        StringBuilder configValue = new StringBuilder();
        boolean foundEquals = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (!foundEquals) {
                if (c == '=') {
                    foundEquals = true;
                } else {
                    configName.append(Character.toLowerCase(c));
                }
            } else {
                if (!Character.isWhitespace(c)) {
                    configValue.append(Character.toLowerCase(c));
                }
            }
        }
        return new Pair<>(configName.toString(), configValue.toString());
    }

    private static void parsePair(@NotNull Pair<String, String> pair) {
        String key = pair.getKey();
        String value = pair.getValue();
        if (key.equals("orihimekeybinds.generatedversion")) {
            instance.GENERATED_VERSION = value;
        } else if (key.equals("orihimekeybinds.showkeybindidtooltips")) {
            instance.SHOW_NUMBER_TOOLTIPS = Boolean.parseBoolean(value);
        }else if ((key.matches("orihimekeybinds.button.[0-9]+"))) {
            Keybind keybind = parseKeybind(pair);
            if (keybind != null) {
                instance.keybinds.add(keybind);
            }
        }
    }

    @Nullable
    private static Keybind parseKeybind(Pair<String, String> pair) {
        String key = pair.getKey();
        String value = pair.getValue();
        if (value.equals("")) {
            return null;
        }

        boolean ctrl = false;
        boolean alt = false;
        boolean shift = false;
        if (value.contains("ctrl+")) ctrl = true;
        if (value.contains("alt+")) alt = true;
        if (value.contains("shift+")) shift = true;
        int button = Integer.parseInt(key.substring(key.lastIndexOf('.') + 1));

        String keyChar = value.substring(value.lastIndexOf('+') + 1);
        if (keyChar.startsWith("kc")) {
            return new Keybind(button, Integer.parseInt(keyChar.substring(2)), shift, ctrl, alt);
        } else {
            if (keyChar.length() != 1) {
                System.err.println("Keybind Syntax Error! '" + keyChar + "' is not 1 character long.");
            } else {
                return new Keybind(button, keyChar.charAt(0), shift, ctrl, alt);
            }
        }

        return null;
    }


}