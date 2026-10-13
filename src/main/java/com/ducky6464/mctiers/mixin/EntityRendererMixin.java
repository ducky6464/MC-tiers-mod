package com.ducky6464.mctiers.mixin;

import com.ducky6464.mctiers.McTiersClient;
import com.ducky6464.mctiers.cache.TierResult;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Inject(method = "getNameTag", at = @At("RETURN"), cancellable = true)
    private void mctiers$appendTier(Entity entity, CallbackInfoReturnable<Component> cir) {
        if (!McTiersClient.ENABLED || !(entity instanceof Player player)) return;

        Component original = cir.getReturnValue();
        if (original == null) return;

        TierResult result = McTiersClient.CACHE.getCached(player.getGameProfile().name());
        if (result == null || result.tier() == null || result.tier().isBlank()) return;

        Component suffix = Component.empty()
                .append(Component.literal(" [").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(result.tier()).withStyle(ChatFormatting.WHITE))
                .append(Component.literal(" " + result.mode() + "]").withStyle(ChatFormatting.GRAY));

        cir.setReturnValue(Component.empty().append(original).append(suffix));
    }

    @Inject(method = "shouldShowName", at = @At("RETURN"), cancellable = true)
    private void mctiers$showOwnLabel(Entity entity, double squaredDistanceToCamera,
                                      CallbackInfoReturnable<Boolean> cir) {
        if (!McTiersClient.ENABLED || !(entity instanceof Player)) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == entity) {
            cir.setReturnValue(true);
        }
    }
}
