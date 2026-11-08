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
     * Append only the tier code to the actual 1.21.11 player-name render state.
     * Do not add mode names, symbols, or inferred game-mode labels.
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

        Text suffix = Text.empty()
                .append(Text.literal(" [").formatted(Formatting.GRAY))
                .append(Text.literal(result.tier()).formatted(Formatting.WHITE))
                .append(Text.literal("]").formatted(Formatting.GRAY));

        state.playerName = Text.empty().append(state.playerName).append(suffix);
    }
}
