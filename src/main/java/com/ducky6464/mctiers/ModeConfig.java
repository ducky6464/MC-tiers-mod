package com.ducky6464.mctiers;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class ModeConfig {
    public static final String[] MODES = {
            "mace", "sword", "uhc", "netpot", "diamondpot",
            "crystal", "axe", "smp", "diamond_smp"
    };

    private static String selectedMode = "mace";
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("mctiers.properties");

    private ModeConfig() {}

    public static String getMode() {
        return selectedMode;
    }

    public static void setMode(String mode) {
        for (String allowed : MODES) {
            if (allowed.equalsIgnoreCase(mode)) {
                selectedMode = allowed;
                save();
                McTiersClient.CACHE.clear();
                return;
            }
        }
    }

    public static void load() {
        if (!Files.exists(FILE)) return;
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(FILE)) {
            properties.load(reader);
            String saved = properties.getProperty("mode", "mace");
            for (String mode : MODES) {
                if (mode.equalsIgnoreCase(saved)) {
                    selectedMode = mode;
                    return;
                }
            }
        } catch (IOException ignored) {
            selectedMode = "mace";
        }
    }

    private static void save() {
        Properties properties = new Properties();
        properties.setProperty("mode", selectedMode);
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE)) {
                properties.store(writer, "MC-Tiers client settings");
            }
        } catch (IOException ignored) {
            // The selection still applies for this session if the config cannot be written.
        }
    }
}