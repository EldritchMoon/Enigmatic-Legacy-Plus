package auviotre.enigmatic.legacy.mixin.client;

import auviotre.enigmatic.legacy.client.DarkenEffect;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// 26.1.2-port: rewritten. This used to own the darken PostChain (build, resize, close, reload) and set its uniforms;
// all of that is DarkenEffect now and only the hook is left, at the same place as before: in render(), after the
// level is drawn and right before doEntityOutline().
@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    @Final
    private Camera mainCamera;
    @Shadow
    @Final
    private CrossFrameResourcePool resourcePool;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;doEntityOutline()V"))
    private void enigmaticlegacy$darken(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo info) {
        DarkenEffect.process(this.minecraft, this.mainCamera, this.resourcePool);
    }
}
