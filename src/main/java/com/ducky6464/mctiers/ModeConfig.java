package com.ducky6464.mctiers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

public final class ModeConfig {
    public record ModeOption(String id, String label) {}

    private static final List<ModeOption> DEFAULT_MODES = List.of(
            new ModeOption("mace", "Mace"),
            new ModeOption("spear", "Spear Mace"),
            new ModeOption("sword", "Sword"),
            new ModeOption("axe", "Axe"),
            new ModeOption("uhc", "UHC"),
            new ModeOption("netpot", "NetPot"),
            new ModeOption("diamondpot", "DiamondPot"),
            new ModeOption("crystal", "Crystal"),
            new ModeOption("smp", "SMP"),
            new ModeOption("diamondsmp", "Diamond SMP"),
            new ModeOption("ht3cart", "HT3 Cart")
    );

    private static final URI MODES_URI = URI.create("https://mc-tiers.pages.dev/api/public/v1/modes");
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("mctiers.properties");

    private static volatile List<ModeOption> modes = DEFAULT_MODES;
    private static volatile String selectedMode = "mace";

    private ModeConfig() {}

    public static List<ModeOption> getModes() {
        return modes;
    }

    public static String getMode() {
        return selectedMode;
    }

    public static void setMode(String mode) {
        String wanted = canonicalId(mode);
        for (ModeOption option : modes) {
            if (option.id().equals(wanted)) {
                selectedMode = option.id();
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
            String saved = canonicalId(properties.getProperty("mode", "mace"));
            if (DEFAULT_MODES.stream().anyMatch(option -> option.id().equals(saved))) {
                selectedMode = saved;
            }
        } catch (IOException ignored) {
            selectedMode = "mace";
        }
    }

    /** Pulls the live mode catalog so newly published website modes appear without a mod update. */
    public static void refreshModes() {
        HttpRequest request = HttpRequest.newBuilder(MODES_URI)
                .timeout(Duration.ofSeconds(8))
                .header("Accept", "application/json")
                .GET()
                .build();

        HTTP.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() != 200) return;
                    try {
                        JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
                        JsonArray array = root.getAsJsonArray("modes");
                        if (array == null) return;

                        List<ModeOption> loaded = new ArrayList<>();
                        Set<String> seen = new HashSet<>();
                        for (JsonElement element : array) {
                            if (!element.isJsonObject()) continue;
                            JsonObject item = element.getAsJsonObject();
                            if (!item.has("id") || !item.has("label")) continue;

                            String id = canonicalId(item.get("id").getAsString());
                            String label = item.get("label").getAsString().trim();
                            if (!id.matches("[a-z0-9_]{1,32}") || label.isEmpty() || !seen.add(id)) continue;
                            loaded.add(new ModeOption(id, label));
                        }

                        if (loaded.isEmpty()) return;
                        modes = List.copyOf(loaded);
                        if (modes.stream().noneMatch(option -> option.id().equals(selectedMode))) {
                            selectedMode = modes.get(0).id();
                            save();
                        }
                        McTiersClient.CACHE.clear();
                    } catch (RuntimeException ignored) {
                        // Keep the built-in catalog if the endpoint returns unexpected data.
                    }
                })
                .exceptionally(error -> null);
    }

    private static String canonicalId(String mode) {
        String id = mode == null ? "mace" : mode.toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        return id.equals("diamond_smp") ? "diamondsmp" : id;
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