package com.ducky6464.mctiers.mixin;

import com.ducky6464.mctiers.McTiersClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.entity.PlayerLikeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {
    @Inject(method = "hasLabel", at = @At("RETURN"), cancellable = true)
    private void mctiers$showOwnLabel(PlayerLikeEntity player, double squaredDistanceToCamera, CallbackInfoReturnable<Boolean> cir) {
        if (!McTiersClient.ENABLED || player == null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == player) {
            cir.setReturnValue(true);
        }
    }
}
