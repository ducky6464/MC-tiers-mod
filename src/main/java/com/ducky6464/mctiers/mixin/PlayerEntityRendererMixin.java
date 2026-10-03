package com.ducky6464.mctiers.mixin;
import com.ducky6464.mctiers.McTiersClient;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {
    @Inject(method="renderLabelIfPresent",at=@At("HEAD"))
    private void mctiers$prependTier(PlayerEntityRenderState state,MatrixStack matrices,OrderedRenderCommandQueue queue,CameraRenderState camera,CallbackInfo ci){
        if(!McTiersClient.ENABLED||state.playerName==null)return;
        String name=state.playerName.getString();
        McTiersClient.CACHE.get(name).thenAccept(r->{
            if(r!=null&&!r.expired())state.playerName=Text.literal(r.label()+" ").append(state.playerName);
        });
    }
}
