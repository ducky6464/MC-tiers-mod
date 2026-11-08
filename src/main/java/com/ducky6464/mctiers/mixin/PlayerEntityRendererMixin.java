package com.ducky6464.mctiers.mixin;

import com.ducky6464.mctiers.McTiersClient;
import com.ducky6464.mctiers.cache.TierResult;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {
    @Inject(method = "hasLabel", at = @At("RETURN"), cancellable = true)
    private void mctiers$showOwnLabel(
            PlayerLikeEntity player,
            double squaredDistanceToCamera,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!McTiersClient.ENABLED || player == null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == player) {
            cir.setReturnValue(true);
        }
    }

    /**
     * Append the tier and the original mode logo to the 1.21.11 player-name
     * render state. Keep the logo; do not add an automatically generated mode label.
     */
    @Inject(method = "updateRenderState", at = @At("TAIL"))
    private void mctiers$appendTierToRenderState(
            PlayerLikeEntity player,
            PlayerEntityRenderState state,
            float tickDelta,
            CallbackInfo ci
    ) {
        if (!McTiersClient.ENABLED || !(player instanceof PlayerEntity playerEntity)) return;

        TierResult result = McTiersClient.CACHE.getCached(playerEntity.getGameProfile().name());
        if (result == null || result.tier() == null || result.tier().isBlank()) return;
        if (state.playerName == null) return;

        Text icon = Text.literal(modeGlyph(result.mode())).formatted(modeColor(result.mode()));
        Text suffix = Text.empty()
                .append(Text.literal(" [").formatted(Formatting.GRAY))
                .append(Text.literal(result.tier()).formatted(Formatting.WHITE))
                .append(Text.literal(" "))
                .append(icon)
                .append(Text.literal("]").formatted(Formatting.GRAY));

        state.playerName = Text.empty().append(state.playerName).append(suffix);
    }

    private static String modeGlyph(String mode) {
        return switch (normalize(mode)) {
            case "mace" -> "✦";
            case "spear" -> "▲";
            case "sword" -> "◆";
            case "uhc" -> "♥";
            case "netpot" -> "●";
            case "diamondpot" -> "✧";
            case "crystal" -> "◇";
            case "axe" -> "■";
            case "smp" -> "⬟";
            case "diamondsmp" -> "◈";
            case "ht3cart" -> "✚";
            default -> "◆";
        };
    }

    private static Formatting modeColor(String mode) {
        return switch (normalize(mode)) {
            case "mace" -> Formatting.GOLD;
            case "spear" -> Formatting.AQUA;
            case "sword" -> Formatting.LIGHT_PURPLE;
            case "uhc" -> Formatting.RED;
            case "netpot" -> Formatting.DARK_PURPLE;
            case "diamondpot" -> Formatting.BLUE;
            case "crystal" -> Formatting.AQUA;
            case "axe" -> Formatting.DARK_GREEN;
            case "smp" -> Formatting.GREEN;
            case "diamondsmp" -> Formatting.DARK_AQUA;
            case "ht3cart" -> Formatting.YELLOW;
            default -> Formatting.GRAY;
        };
    }

    private static String normalize(String mode) {
        if (mode == null) return "";
        return mode.toLowerCase(Locale.ROOT)
                .replace("-", "")
                .replace("_", "")
                .replace(" ", "");
    }
}
