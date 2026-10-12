package com.ducky6464.mctiers;

import com.ducky6464.mctiers.cache.TierCache;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class McTiersClient implements ClientModInitializer {
    public static final TierCache CACHE = new TierCache();
    public static volatile boolean ENABLED = true;
    private static KeyBinding openModeMenu;

    @Override
    public void onInitializeClient() {
        ModeConfig.load();
        openModeMenu = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mctiers.open_mode_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                KeyBinding.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openModeMenu.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new ModeSelectionScreen(null));
                }
            }

            if (!ENABLED || client.world == null) return;
            client.world.getPlayers().forEach(player ->
                CACHE.get(player.getGameProfile().name())
            );
        });
    }
}