package com.ducky6464.mctiers.cache;

import com.ducky6464.mctiers.api.McTiersApi;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class TierCache {
    private static final long TTL = 600_000L;

    private final Map<String, TierResult> values = new ConcurrentHashMap<>();
    private final Map<String, Long> misses = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<TierResult>> pending = new ConcurrentHashMap<>();

    public CompletableFuture<TierResult> get(String username) {
        String key = username.toLowerCase(Locale.ROOT);
        TierResult cached = values.get(key);
        if (cached != null && !cached.expired()) {
            return CompletableFuture.completedFuture(cached);
        }

        Long missUntil = misses.get(key);
        if (missUntil != null) {
            if (System.currentTimeMillis() < missUntil) {
                return CompletableFuture.completedFuture(null);
            }
            misses.remove(key, missUntil);
        }

        return pending.computeIfAbsent(key, ignored ->
            McTiersApi.lookup(username)
                .thenApply(result -> {
                    if (result == null) {
                        misses.put(key, System.currentTimeMillis() + TTL);
                    } else {
                        values.put(key, new TierResult(
                            result.tier(),
                            result.mode(),
                            System.currentTimeMillis() + TTL
                        ));
                        misses.remove(key);
                    }
                    return result;
                })
                .whenComplete((result, error) -> {
                    if (error != null) {
                        misses.put(key, System.currentTimeMillis() + TTL);
                    }
                    pending.remove(key);
                })
        );
    }

    public TierResult getCached(String username) {
        TierResult value = values.get(username.toLowerCase(Locale.ROOT));
        return value != null && !value.expired() ? value : null;
    }

    public void clear() {
        values.clear();
        misses.clear();
        pending.clear();
    }
}
