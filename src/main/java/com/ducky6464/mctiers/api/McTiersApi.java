package com.ducky6464.mctiers.api;

import com.ducky6464.mctiers.cache.TierResult;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class McTiersApi {
    private static final String BASE = "https://mc-tiers.lovable.app/api/public/v1/player/";
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
                    String body = response.body();
                    if (response.statusCode() != 200 || !body.contains("\"found\":true")) {
                        return null;
                    }

                    for (String key : modeKeys(mode)) {
                        // Parse the selected mode object first, then read its tier field.
                        // This does not depend on the JSON field order (tier/label can swap).
                        Pattern modePattern = Pattern.compile(
                                "\"" + Pattern.quote(key) + "\"\\s*:\\s*\\{([^{}]*)\\}"
                        );
                        Matcher modeMatcher = modePattern.matcher(body);
                        if (!modeMatcher.find()) continue;

                        Matcher tierMatcher = Pattern.compile(
                                "\"tier\"\\s*:\\s*\"([^\"]+)\""
                        ).matcher(modeMatcher.group(1));
                        if (tierMatcher.find()) {
                            return new TierResult(tierMatcher.group(1), mode, 0);
                        }
                    }
                    return null;
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