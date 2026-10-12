package com.ducky6464.mctiers.api;

import com.ducky6464.mctiers.cache.TierResult;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class McTiersApi {
    private static final String BASE = "https://mc-tiers.lovable.app/api/public/v1/player/";
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(5))
            .build();
    private static final Pattern MACE_TIER = Pattern.compile(
            "\\\"mace\\\"\\s*:\\s*\\{[^}]*\\\"tier\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"[^}]*\\\"label\\\"\\s*:\\s*\\\"([^\\\"]+)\\\""
    );

    private McTiersApi() {}

    public static CompletableFuture<TierResult> lookup(String username) {
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
                    if (response.statusCode() != 200 || !response.body().contains("\"found\":true")) {
                        return null;
                    }

                    Matcher mace = MACE_TIER.matcher(response.body());
                    if (!mace.find()) {
                        return null;
                    }
                    return new TierResult(mace.group(1), mace.group(2), 0);
                })
                .exceptionally(error -> null);
    }
}
