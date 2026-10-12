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
                    if (response.statusCode() != 200 || !body.contains("\\"found\\":true")) {
                        return null;
                    }

                    for (String key : modeKeys(mode)) {
                        Pattern pattern = Pattern.compile(
                                "\\\"" + Pattern.quote(key) + "\\"\\s*:\\s*\\{[^}]*" +
                                "\\\"tier\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"[^}]*" +
                                "\\\"label\\\"\\s*:\\s*\\\"([^\\\"]+)\\\""
                        );
                        Matcher matcher = pattern.matcher(body);
                        if (matcher.find()) {
                            return new TierResult(matcher.group(1), matcher.group(2), 0);
                        }
                    }
                    return null;
                })
                .exceptionally(error -> null);
    }

    private static List<String> modeKeys(String mode) {
        return switch (mode) {
            case "diamond_smp" -> List.of("diamond_smp", "diamondSmp", "diamondSMP");
            case "diamondpot" -> List.of("diamondpot", "diamondPot", "diamond_pot");
            case "netpot" -> List.of("netpot", "netPot", "netheritePot");
            default -> List.of(mode);
        };
    }
}