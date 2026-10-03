package com.ducky6464.mctiers.mixin;

import com.ducky6464.mctiers.McTiersClient;
import com.ducky6464.mctiers.cache.TierResult;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;
import net.minecraft.text.StyleSpriteSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void mctiers$appendTier(
            PlayerEntity player,
            CallbackInfoReturnable<Text> cir
    ) {
        if (!McTiersClient.ENABLED || player == null) return;

        Text original = cir.getReturnValue();
        if (original == null) return;

        TierResult result = McTiersClient.CACHE.getCached(player.getGameProfile().name());
        if (result == null || result.tier() == null || result.tier().isBlank()) return;

        String label = result.label();
        if (label == null || label.isBlank()) return;

        cir.setReturnValue(
                Text.empty()
                        .append(original)
                        .append(Text.literal(" §7[§f" + label + " §7"))
                        .append(Text.literal("\uE000").setStyle(Style.EMPTY.withFont(new StyleSpriteSource.Font(Identifier.of("mctiers", "mace")))))
                        .append(Text.literal("§7]"))
        );
    }
}
