package com.ducky6464.mctiers.cache;

import com.ducky6464.mctiers.ModeConfig;
import com.ducky6464.mctiers.api.McTiersApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class TierCache {
    private static final Logger LOGGER = LoggerFactory.getLogger("MC-Tiers/Cache");
    private static final long TTL = 600_000L;
    private static final long MISS_TTL = 30_000L;

    private final Map<String, TierResult> values = new ConcurrentHashMap<>();
    private final Map<String, Long> misses = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<TierResult>> pending = new ConcurrentHashMap<>();

    public CompletableFuture<TierResult> get(String username) {
        if (username == null || username.isBlank()) {
            LOGGER.warn("CACHE_INVALID_USERNAME: Ignoring an empty player name.");
            return CompletableFuture.completedFuture(null);
        }

        String selectedMode = ModeConfig.getMode();
        String key = username.toLowerCase(Locale.ROOT) + "|" + selectedMode;
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
            McTiersApi.lookup(username, selectedMode)
                .thenApply(result -> {
                    if (result == null) {
                        misses.put(key, System.currentTimeMillis() + MISS_TTL);
                        LOGGER.warn("CACHE_NO_TIER: No tier was returned for player='{}', mode='{}'. See the API diagnostic above for the cause.",
                                username, selectedMode);
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
                        misses.put(key, System.currentTimeMillis() + MISS_TTL);
                        LOGGER.error("CACHE_LOOKUP_EXCEPTION: player='{}', mode='{}'.",
                                username, selectedMode, error);
                    }
                    pending.remove(key);
                })
        );
    }

    public TierResult getCached(String username) {
        if (username == null || username.isBlank()) return null;
        String key = username.toLowerCase(Locale.ROOT) + "|" + ModeConfig.getMode();
        TierResult value = values.get(key);
        return value != null && !value.expired() ? value : null;
    }

    public void clear() {
        values.clear();
        misses.clear();
        pending.clear();
        LOGGER.info("CACHE_CLEARED: Tier cache and temporary lookup failures were cleared.");
    }
}
