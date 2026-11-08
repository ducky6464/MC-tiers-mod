package com.ducky6464.mctiers.api;

import com.ducky6464.mctiers.cache.TierResult;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public final class McTiersApi {
    private static final Logger LOGGER = LoggerFactory.getLogger("MC-Tiers/API");
    private static final String BASE = "https://mc-tiers.pages.dev/api/public/v1/player/";
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(5))
            .build();

    private McTiersApi() {}

    public static CompletableFuture<TierResult> lookup(String username, String mode) {
        if (username == null || !username.matches("[A-Za-z0-9_]{1,16}")) {
            LOGGER.warn("LOOKUP_INVALID_USERNAME: Cannot look up username '{}'.", username);
            return CompletableFuture.completedFuture(null);
        }

        LOGGER.info("LOOKUP_START: player='{}', mode='{}'.", username, mode);

        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE + username))
                .timeout(java.time.Duration.ofSeconds(8))
                .header("Accept", "application/json")
                .GET()
                .build();

        return HTTP.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    int status = response.statusCode();
                    if (status != 200) {
                        LOGGER.error("LOOKUP_HTTP_ERROR: player='{}', mode='{}', HTTP status={}. Response preview: {}",
                                username, mode, status, preview(response.body()));
                        return null;
                    }

                    try {
                        JsonElement parsed = JsonParser.parseString(response.body());
                        if (!parsed.isJsonObject()) {
                            LOGGER.error("LOOKUP_BAD_JSON_SHAPE: player='{}', mode='{}'. Expected a JSON object, got: {}",
                                    username, mode, parsed.getClass().getSimpleName());
                            return null;
                        }

                        JsonObject root = parsed.getAsJsonObject();
                        if (!root.has("found") || !root.get("found").isJsonPrimitive()) {
                            LOGGER.error("LOOKUP_MISSING_FOUND_FIELD: player='{}'. Top-level JSON keys: {}",
                                    username, root.keySet());
                            return null;
                        }
                        if (!root.get("found").getAsBoolean()) {
                            LOGGER.warn("LOOKUP_PLAYER_NOT_FOUND: Website API did not find player '{}'.", username);
                            return null;
                        }

                        JsonObject tiers = root.has("tiers") && root.get("tiers").isJsonObject()
                                ? root.getAsJsonObject("tiers") : null;
                        if (tiers == null) {
                            LOGGER.error("LOOKUP_MISSING_TIERS_OBJECT: player='{}'. Top-level JSON keys: {}",
                                    username, root.keySet());
                            return null;
                        }
                        if (!tiers.has(mode)) {
                            LOGGER.error("LOOKUP_MODE_MISSING: player='{}', requested mode='{}'. Available mode keys: {}",
                                    username, mode, tiers.keySet());
                            return null;
                        }

                        JsonElement selected = tiers.get(mode);
                        if (selected == null || !selected.isJsonObject()) {
                            LOGGER.error("LOOKUP_BAD_MODE_ENTRY: player='{}', mode='{}'. Expected an object, got: {}",
                                    username, mode, selected);
                            return null;
                        }

                        JsonObject selectedTier = selected.getAsJsonObject();
                        if (!selectedTier.has("tier") || selectedTier.get("tier").isJsonNull()) {
                            LOGGER.error("LOOKUP_TIER_FIELD_MISSING: player='{}', mode='{}'. Mode entry keys: {}",
                                    username, mode, selectedTier.keySet());
                            return null;
                        }

                        String tier = selectedTier.get("tier").getAsString();
                        if (tier.isBlank()) {
                            LOGGER.error("LOOKUP_EMPTY_TIER: player='{}', mode='{}'.", username, mode);
                            return null;
                        }

                        LOGGER.info("LOOKUP_SUCCESS: player='{}', mode='{}', tier='{}'.", username, mode, tier);
                        return new TierResult(tier, mode, 0);
                    } catch (RuntimeException error) {
                        LOGGER.error("LOOKUP_JSON_PARSE_ERROR: player='{}', mode='{}'. Response preview: {}",
                                username, mode, preview(response.body()), error);
                        return null;
                    }
                })
                .exceptionally(error -> {
                    Throwable cause = error instanceof CompletionException && error.getCause() != null
                            ? error.getCause() : error;
                    LOGGER.error("LOOKUP_NETWORK_ERROR: player='{}', mode='{}'. {}", username, mode,
                            cause.toString(), cause);
                    return null;
                });
    }

    private static String preview(String body) {
        if (body == null) return "<no response body>";
        String compact = body.replaceAll("\\s+", " ");
        return compact.length() <= 300 ? compact : compact.substring(0, 300) + "...";
    }
}
