package com.ducky6464.mctiers;

import com.ducky6464.mctiers.cache.TierCache;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class McTiersClient implements ClientModInitializer {
    public static final TierCache CACHE = new TierCache();
    public static volatile boolean ENABLED = true;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!ENABLED || client.world == null) return;
            client.world.getPlayers().forEach(player ->
                CACHE.get(player.getGameProfile().name())
            );
        });
    }
}