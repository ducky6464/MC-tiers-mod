package com.ducky6464.mctiers.mixin;

import com.ducky6464.mctiers.McTiersClient;
import com.ducky6464.mctiers.cache.TierResult;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Locale;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void mctiers$appendTier(Entity entity, CallbackInfoReturnable<Text> cir) {
        if (!McTiersClient.ENABLED || !(entity instanceof PlayerEntity player)) return;

        Text original = cir.getReturnValue();
        if (original == null) return;

        TierResult result = McTiersClient.CACHE.getCached(player.getGameProfile().name());
        if (result == null || result.tier() == null || result.tier().isBlank()) return;

        String icon = modeIcon(result.mode());
        Text suffix = Text.empty()
                .append(Text.literal(" [").formatted(Formatting.GRAY))
                .append(Text.literal(result.tier()).formatted(Formatting.WHITE))
                .append(Text.literal(" " + icon + "]").formatted(Formatting.GRAY));

        cir.setReturnValue(Text.empty().append(original).append(suffix));
    }

    private static String modeIcon(String mode) {
        if (mode == null) return "?";
        return switch (mode.toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_')) {
            case "mace" -> "✦";
            case "sword" -> "⚔";
            case "uhc" -> "♥";
            case "netpot" -> "◉";
            case "diamondpot" -> "◆";
            case "crystal" -> "◇";
            case "axe" -> "⚒";
            case "smp" -> "▣";
            case "diamond_smp", "diamondsmp" -> "⛓";
            default -> "?";
        };
    }
}