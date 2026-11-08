package com.ducky6464.mctiers.mixin;

import com.ducky6464.mctiers.McTiersClient;
import com.ducky6464.mctiers.cache.TierResult;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void mctiers$appendTier(Entity entity, CallbackInfoReturnable<Text> cir) {
        if (!McTiersClient.ENABLED || !(entity instanceof PlayerEntity player)) return;

        Text original = cir.getReturnValue();
        if (original == null) return;

        TierResult result = McTiersClient.CACHE.getCached(player.getGameProfile().name());
        if (result == null || result.tier() == null || result.tier().isBlank()) return;

        Text icon = Text.literal(modeGlyph(result.mode())).formatted(modeColor(result.mode()));
        Text suffix = Text.empty()
                .append(Text.literal(" [").formatted(Formatting.GRAY))
                .append(Text.literal(result.tier()).formatted(Formatting.WHITE))
                .append(Text.literal(" "))
                .append(icon)
                .append(Text.literal("]").formatted(Formatting.GRAY));

        cir.setReturnValue(Text.empty().append(original).append(suffix));
    }

    // Text glyphs are part of the mod's text, not Minecraft item textures.
    // Resource packs that replace the End Crystal or other item models cannot change these.
    private static String modeGlyph(String mode) {
        String normalized = normalize(mode);
        return switch (normalized) {
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
        String normalized = mode.toLowerCase(Locale.ROOT).replace("-", "").replace("_", "").replace(" ", "");
        return normalized.equals("diamondsmp") ? "diamondsmp" : normalized;
    }
}