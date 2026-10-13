package com.ducky6464.mctiers.mixin;

import com.ducky6464.mctiers.McTiersClient;
import com.ducky6464.mctiers.cache.TierResult;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.object.AtlasTextObjectContents;
import net.minecraft.util.Identifier;
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

        String sprite = modeSprite(result.mode());
        boolean blockSprite = sprite.startsWith("block/");
        Identifier atlas = Identifier.of("minecraft", blockSprite ? "blocks" : "items");
        Text icon = Text.object(new AtlasTextObjectContents(
                atlas,
                Identifier.of("minecraft", sprite)
        ));

        Text suffix = Text.empty()
                .append(Text.literal(" [").formatted(Formatting.GRAY))
                .append(Text.literal(result.tier()).formatted(Formatting.WHITE))
                .append(Text.literal(" "))
                .append(icon)
                .append(Text.literal("]").formatted(Formatting.GRAY));

        cir.setReturnValue(Text.empty().append(original).append(suffix));
    }

    private static String modeSprite(String mode) {
        if (mode == null) return "item/mace";
        return switch (mode.toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_')) {
            case "mace" -> "item/mace";
            case "sword" -> "item/netherite_sword";
            case "uhc" -> "item/golden_apple";
            case "netpot" -> "item/netherite_ingot";
            case "diamondpot" -> "item/potion";
            case "crystal" -> "item/end_crystal";
            case "axe" -> "item/netherite_axe";
            case "smp" -> "item/netherite_chestplate";
            case "diamond_smp", "diamondsmp" -> "block/cobweb";
            default -> "item/mace";
        };
    }
}