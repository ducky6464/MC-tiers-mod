package com.ducky6464.mctiers.mixin;

import com.ducky6464.mctiers.McTiersClient;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    private static final String MODE_ICON_GLYPH = "\uE000";

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void mctiers$appendTier(Entity entity, CallbackInfoReturnable<Text> cir) {
        if (!McTiersClient.ENABLED || !(entity instanceof PlayerEntity player)) return;

        Text original = cir.getReturnValue();
        if (original == null) return;

        TierResult result = McTiersClient.CACHE.getCached(player.getGameProfile().name());
        if (result == null || result.tier() == null || result.tier().isBlank()) return;

        String fontName = normalize(result.mode());
        if (!hasWebsiteModeTexture(fontName)) return;

        // Each mode font uses the exact Minecraft item/block texture used by the
        // website's ModeIcon component. The glyph is a bitmap texture, not a Unicode symbol.
        Text icon = Text.literal(MODE_ICON_GLYPH)
                .styled(style -> style.withFont(Identifier.of("mctiers", fontName)));

        Text suffix = Text.empty()
                .append(Text.literal(" [").formatted(Formatting.GRAY))
                .append(Text.literal(result.tier()).formatted(Formatting.WHITE))
                .append(Text.literal(" "))
                .append(icon)
                .append(Text.literal("]").formatted(Formatting.GRAY));

        cir.setReturnValue(Text.empty().append(original).append(suffix));
    }

    private static boolean hasWebsiteModeTexture(String mode) {
        return switch (mode) {
            case "mace", "spear", "sword", "axe", "uhc", "netpot",
                    "diamondpot", "crystal", "smp", "diamondsmp", "ht3cart" -> true;
            default -> false;
        };
    }

    private static String normalize(String mode) {
        if (mode == null) return "";
        String normalized = mode.toLowerCase(Locale.ROOT)
                .replace("-", "")
                .replace("_", "")
                .replace(" ", "");
        return normalized.equals("diamondsmp") ? "diamondsmp" : normalized;
    }
}
