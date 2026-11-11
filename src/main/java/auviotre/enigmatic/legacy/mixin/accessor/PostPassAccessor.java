package auviotre.enigmatic.legacy.mixin.accessor;

import com.mojang.blaze3d.buffers.GpuBuffer;
import net.minecraft.client.renderer.PostPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

// 26.1.2-port: new, replaces EffectInstanceAccessor. A pass's uniform blocks are GPU buffers in this map; see
// DarkenEffect.
@Mixin(PostPass.class)
public interface PostPassAccessor {
    @Accessor("customUniforms")
    Map<String, GpuBuffer> getCustomUniforms();
}
