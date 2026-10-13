package com.ducky6464.mctiers;

import com.ducky6464.mctiers.cache.TierCache;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class McTiersClient implements ClientModInitializer {
    public static final TierCache CACHE = new TierCache();
    public static volatile boolean ENABLED = true;
    private static KeyMapping openModeMenu;

    @Override
    public void onInitializeClient() {
        ModeConfig.load();
        openModeMenu = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.mctiers.open_mode_menu",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                KeyMapping.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openModeMenu.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new ModeSelectionScreen(null));
                }
            }

            if (!ENABLED || client.level == null) return;
            client.level.players().forEach(player ->
                CACHE.get(player.getGameProfile().name())
            );
        });
    }
}
