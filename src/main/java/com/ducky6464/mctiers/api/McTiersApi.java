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
    private static final String REPORT_URL = "https://mctiers-error-reporter.duckbahaduri.workers.dev/report";
    private static final String MOD_VERSION = "1.0.0";
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(5))
            .build();

    private McTiersApi() {}

    public static CompletableFuture<TierResult> lookup(String username, String mode) {
        if (username == null || !username.matches("[A-Za-z0-9_]{1,16}")) {
            LOGGER.warn("LOOKUP_INVALID_USERNAME: Invalid player name supplied.");
            return CompletableFuture.completedFuture(null);
        }

        LOGGER.info("LOOKUP_START: mode='{}'.", mode);

        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE + username))
                .timeout(java.time.Duration.ofSeconds(8))
                .header("Accept", "application/json")
                .GET()
                .build();

        return HTTP.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    int status = response.statusCode();
                    if (status != 200) {
                        LOGGER.error("LOOKUP_HTTP_ERROR: mode='{}', HTTP status={}.", mode, status);
                        report("HTTP_ERROR", mode, status, null, "MC-Tiers API returned a non-200 status.", null);
                        return null;
                    }

                    try {
                        JsonElement parsed = JsonParser.parseString(response.body());
                        if (!parsed.isJsonObject()) {
                            LOGGER.error("LOOKUP_BAD_JSON_SHAPE: Expected a JSON object.");
                            report("BAD_JSON", mode, null, "UnexpectedJsonShape", "API response was not a JSON object.", null);
                            return null;
                        }

                        JsonObject root = parsed.getAsJsonObject();
                        if (!root.has("found") || !root.get("found").isJsonPrimitive()) {
                            LOGGER.error("LOOKUP_MISSING_FOUND_FIELD: Top-level JSON keys: {}", root.keySet());
                            report("MISSING_FIELD", mode, null, null, "API response is missing a valid 'found' field.", root.keySet().toString());
                            return null;
                        }
                        if (!root.get("found").getAsBoolean()) {
                            LOGGER.warn("LOOKUP_PLAYER_NOT_FOUND: Website API did not find the requested player.");
                            report("PLAYER_NOT_FOUND", mode, null, null, "The website API returned found=false.", null);
                            return null;
                        }

                        JsonObject tiers = root.has("tiers") && root.get("tiers").isJsonObject()
                                ? root.getAsJsonObject("tiers") : null;
                        if (tiers == null) {
                            LOGGER.error("LOOKUP_MISSING_TIERS_OBJECT: Top-level JSON keys: {}", root.keySet());
                            report("MISSING_FIELD", mode, null, null, "API response is missing the tiers object.", root.keySet().toString());
                            return null;
                        }
                        if (!tiers.has(mode)) {
                            LOGGER.error("LOOKUP_MODE_MISSING: requested mode='{}'. Available mode keys: {}", mode, tiers.keySet());
                            report("MODE_MISSING", mode, null, null, "Requested mode key was not present in the API response.", tiers.keySet().toString());
                            return null;
                        }

                        JsonElement selected = tiers.get(mode);
                        if (selected == null || !selected.isJsonObject()) {
                            LOGGER.error("LOOKUP_BAD_MODE_ENTRY: Expected a JSON object for the selected mode.");
                            report("BAD_JSON", mode, null, "UnexpectedModeEntry", "Selected mode entry was not a JSON object.", null);
                            return null;
                        }

                        JsonObject selectedTier = selected.getAsJsonObject();
                        if (!selectedTier.has("tier") || selectedTier.get("tier").isJsonNull()) {
                            LOGGER.error("LOOKUP_TIER_FIELD_MISSING: Mode entry keys: {}", selectedTier.keySet());
                            report("TIER_MISSING", mode, null, null, "Selected mode entry is missing its tier value.", selectedTier.keySet().toString());
                            return null;
                        }

                        String tier = selectedTier.get("tier").getAsString();
                        if (tier.isBlank()) {
                            LOGGER.error("LOOKUP_EMPTY_TIER: API returned an empty tier value.");
                            report("TIER_MISSING", mode, null, null, "API returned an empty tier value.", null);
                            return null;
                        }

                        LOGGER.info("LOOKUP_SUCCESS: mode='{}', tier='{}'.", mode, tier);
                        return new TierResult(tier, mode, 0);
                    } catch (RuntimeException error) {
                        LOGGER.error("LOOKUP_JSON_PARSE_ERROR: Could not parse the API response.", error);
                        report("BAD_JSON", mode, null, error.getClass().getSimpleName(), "Failed to parse the API response.", null);
                        return null;
                    }
                })
                .exceptionally(error -> {
                    Throwable cause = error instanceof CompletionException && error.getCause() != null
                            ? error.getCause() : error;
                    LOGGER.error("LOOKUP_NETWORK_ERROR: mode='{}'. {}", mode, cause.getClass().getSimpleName(), cause);
                    report("NETWORK_ERROR", mode, null, cause.getClass().getSimpleName(), "Request to MC-Tiers API failed.", null);
                    return null;
                });
    }

    private static void report(String category, String mode, Integer httpStatus,
                               String errorType, String message, String details) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("version", MOD_VERSION);
            body.addProperty("category", category);
            body.addProperty("mode", mode);
            if (httpStatus != null) body.addProperty("httpStatus", httpStatus);
            if (errorType != null) body.addProperty("errorType", errorType);
            if (message != null) body.addProperty("message", message);
            if (details != null) body.addProperty("details", details);

            HttpRequest request = HttpRequest.newBuilder(URI.create(REPORT_URL))
                    .timeout(java.time.Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HTTP.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .thenAccept(response -> {
                        if (response.statusCode() >= 200 && response.statusCode() < 300) {
                            LOGGER.info("DIAGNOSTIC_REPORT_SENT: category='{}'.", category);
                        } else {
                            LOGGER.warn("DIAGNOSTIC_REPORT_FAILED: reporting service returned HTTP {}.", response.statusCode());
                        }
                    })
                    .exceptionally(error -> {
                        LOGGER.warn("DIAGNOSTIC_REPORT_FAILED: Could not reach diagnostic reporting service.");
                        return null;
                    });
        } catch (RuntimeException error) {
            LOGGER.warn("DIAGNOSTIC_REPORT_FAILED: Could not prepare diagnostic report.", error);
        }
    }
}
