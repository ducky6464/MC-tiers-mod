package com.ducky6464.mctiers;
import com.ducky6464.mctiers.cache.TierCache;
import net.fabricmc.api.ClientModInitializer;
public final class McTiersClient implements ClientModInitializer {
    public static final TierCache CACHE = new TierCache();
    public static volatile boolean ENABLED = true;
    @Override public void onInitializeClient() {}
}
