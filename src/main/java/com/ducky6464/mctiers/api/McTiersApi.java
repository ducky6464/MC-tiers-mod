package com.ducky6464.mctiers.api;

import com.ducky6464.mctiers.cache.TierResult;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.concurrent.CompletableFuture;

public final class McTiersApi {
    private static final String BASE = "https://mc-tiers.pages.dev/api/public/v1/player/";
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(5))
            .build();

    private McTiersApi() {}

    public static CompletableFuture<TierResult> lookup(String username, String mode) {
        if (username == null || !username.matches("[A-Za-z0-9_]{1,16}")) {
            return CompletableFuture.completedFuture(null);
        }

        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE + username))
                .timeout(java.time.Duration.ofSeconds(8))
                .header("Accept", "application/json")
                .GET()
                .build();

        return HTTP.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) return null;
                    try {
                        JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
                        if (!root.has("found") || !root.get("found").getAsBoolean()) return null;

                        // The public player endpoint stores all mode tiers inside its "tiers" object.
                        // Reading the selected mode by ID makes new website modes work automatically.
                        JsonObject tiers = root.getAsJsonObject("tiers");
                        if (tiers == null || !tiers.has(mode)) return null;

                        JsonElement selected = tiers.get(mode);
                        if (selected == null || !selected.isJsonObject()) return null;
                        JsonObject selectedTier = selected.getAsJsonObject();
                        if (!selectedTier.has("tier") || selectedTier.get("tier").isJsonNull()) return null;

                        String tier = selectedTier.get("tier").getAsString();
                        if (tier.isBlank()) return null;
                        return new TierResult(tier, mode, 0);
                    } catch (RuntimeException ignored) {
                        return null;
                    }
                })
                .exceptionally(error -> null);
    }

    private static List<String> modeKeys(String mode) {
        return switch (mode) {
            case "diamond_smp" -> List.of("diamond_smp", "diamondSmp", "diamondSMP", "diamondsmp");
            case "diamondpot" -> List.of("diamondpot", "diamondPot", "diamond_pot", "diamond_potting");
            case "netpot" -> List.of("netpot", "netPot", "netheritePot", "netherite_pot");
            default -> List.of(mode);
        };
    }
}