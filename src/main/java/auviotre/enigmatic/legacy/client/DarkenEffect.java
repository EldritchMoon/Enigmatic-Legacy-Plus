package auviotre.enigmatic.legacy.client;

import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.client.handlers.ClientEventHandler;
import auviotre.enigmatic.legacy.contents.item.materials.AbyssalHeart;
import auviotre.enigmatic.legacy.mixin.accessor.PostChainAccessor;
import auviotre.enigmatic.legacy.mixin.accessor.PostPassAccessor;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * 26.1.2-port: new. The Abyssal Heart "darken" screen effect (Annihilating Sword charge, Violence Scroll timer),
 * which used to live in MixinGameRenderer.
 *
 * Before, the mixin built its own PostChain from shaders/post/darken.json and set the passes' uniforms every frame
 * through EffectInstance. Now:
 * - post effects are files in post_effect/, loaded and cached by the ShaderManager (it also reloads them, so the
 *   mixin's resize / close / reload hooks are gone);
 * - a pass keeps its uniforms in GPU buffers that are filled once from the json and cannot be written afterwards.
 *   So for each new PostChain instance this swaps the two buffers the effect needs for writable ones, the way
 *   GlobalSettingsUniform makes its buffer, and writes those every frame;
 * - the vanilla color_convolve and bits shaders no longer have Gray / Saturation as uniforms, so the effect has its
 *   own two shaders (shaders/post/darken_convolve.fsh and darken_bits.fsh).
 * The numbers are the old ones.
 */
public final class DarkenEffect {
    private static final Identifier CHAIN = EnigmaticLegacy.location("darken");
    private static final String CONVOLVE_BLOCK = "DarkenConfig";      // darken_convolve.fsh
    private static final String BITS_BLOCK = "DarkenBitsConfig";      // darken_bits.fsh
    // must match the member order of the two uniform blocks
    private static final int CONVOLVE_SIZE = new Std140SizeCalculator().putFloat().putVec3().putVec3().putVec3().putVec3().get();
    private static final int BITS_SIZE = new Std140SizeCalculator().putFloat().putFloat().putFloat().get();
    private static final float[] GRAY = {0.299F, 0.587F, 0.114F};

    private static PostChain preparedChain;
    private static GpuBuffer convolveBuffer;
    private static GpuBuffer bitsBuffer;

    private DarkenEffect() {
    }

    /** Called from MixinGameRenderer, after the level is drawn and before the entity outlines are put on top. */
    public static void process(Minecraft minecraft, Camera camera, GraphicsResourceAllocator resourcePool) {
        if (minecraft.player == null || minecraft.level == null) return;
        float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        float mix = Math.clamp(Mth.lerp(partialTick, ClientEventHandler.darkenLastTick, ClientEventHandler.darkenTick) / 800.0F, 0.0F, 1.0F);
        if (mix <= 0 || !(camera.entity() instanceof LivingEntity)) return;
        if (AbyssalHeart.specialVisualEffects == null || !AbyssalHeart.specialVisualEffects.get()) return;

        PostChain chain = minecraft.getShaderManager().getPostChain(CHAIN, LevelTargetBundle.MAIN_TARGETS);
        if (chain == null) return; // could not be loaded; the ShaderManager has logged why
        if (chain != preparedChain) prepare(chain);
        if (convolveBuffer == null || bitsBuffer == null) return; // logged in prepare()

        // The old code did this inside its loop over the two passes, taking the square root of mix once per pass:
        // Gray used the plain value, the matrices the first root, and Saturation (set on both passes) the second.
        float firstRoot = Mth.sqrt(mix);
        float secondRoot = Mth.sqrt(firstRoot);
        float[] red = normalized(Mth.lerp(firstRoot, 1, GRAY[0]), GRAY[1] * firstRoot, GRAY[2] * firstRoot);
        float[] green = normalized(GRAY[0] * firstRoot, Mth.lerp(firstRoot, 1, GRAY[1]), GRAY[2] * firstRoot);
        float[] blue = normalized(GRAY[0] * firstRoot, GRAY[1] * firstRoot, Mth.lerp(firstRoot, 1, GRAY[2]));
        float saturation = 1.0F - secondRoot * 0.36F;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer convolve = Std140Builder.onStack(stack, CONVOLVE_SIZE)
                    .putFloat(saturation)
                    .putVec3(red[0], red[1], red[2])
                    .putVec3(green[0], green[1], green[2])
                    .putVec3(blue[0], blue[1], blue[2])
                    .putVec3(0.3F * (1 - mix), 0.59F * (1 - mix), 0.11F * (1 - mix))
                    .get();
            ByteBuffer bits = Std140Builder.onStack(stack, BITS_SIZE)
                    .putFloat(256.0F)   // Resolution
                    .putFloat(0.1F)     // MosaicSize
                    .putFloat(saturation)
                    .get();
            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
            encoder.writeToBuffer(convolveBuffer.slice(), convolve);
            encoder.writeToBuffer(bitsBuffer.slice(), bits);
        }
        chain.process(minecraft.getMainRenderTarget(), resourcePool);
    }

    private static void prepare(PostChain chain) {
        preparedChain = chain;
        convolveBuffer = null;
        bitsBuffer = null;
        for (PostPass pass : ((PostChainAccessor) chain).getPasses()) {
            Map<String, GpuBuffer> uniforms = ((PostPassAccessor) pass).getCustomUniforms();
            if (uniforms.containsKey(CONVOLVE_BLOCK)) convolveBuffer = makeWritable(uniforms, CONVOLVE_BLOCK, CONVOLVE_SIZE);
            if (uniforms.containsKey(BITS_BLOCK)) bitsBuffer = makeWritable(uniforms, BITS_BLOCK, BITS_SIZE);
        }
        if (convolveBuffer == null || bitsBuffer == null) {
            EnigmaticLegacy.LOGGER.error("Post effect {} has no {} or no {} uniform block (changed by a resource pack?); the darken effect is off.", CHAIN, CONVOLVE_BLOCK, BITS_BLOCK);
        }
    }

    // The pass closes whatever is in its map when the chain is closed, so the new buffer needs no clean-up here.
    // Rounded up to 16 bytes, the size GLSL gives a std140 block.
    private static GpuBuffer makeWritable(Map<String, GpuBuffer> uniforms, String block, int size) {
        GpuBuffer writable = RenderSystem.getDevice().createBuffer(() -> "Enigmatic Legacy+ darken / " + block, GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, (size + 15) & ~15);
        uniforms.put(block, writable).close();
        return writable;
    }

    private static float[] normalized(float r, float g, float b) {
        float sum = r + g + b;
        return new float[]{r / sum, g / sum, b / sum};
    }
}
