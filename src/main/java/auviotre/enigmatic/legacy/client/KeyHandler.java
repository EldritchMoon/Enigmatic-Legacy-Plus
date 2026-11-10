package auviotre.enigmatic.legacy.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class KeyHandler {
    @OnlyIn(Dist.CLIENT)
    public static final Lazy<KeyMapping> SPELLSTONE = Lazy.of(() -> new KeyMapping(
            "key.spellstoneAbility",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            KeyMapping.Category.MISC // 26.1.2-port: categories are objects now (was "key.categories.misc")
    ));
    @OnlyIn(Dist.CLIENT)
    public static final Lazy<KeyMapping> SCROLL = Lazy.of(() -> new KeyMapping(
            "key.scrollAbility",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            KeyMapping.Category.MISC // 26.1.2-port: categories are objects now (was "key.categories.misc")
    ));
    @OnlyIn(Dist.CLIENT)
    public static final Lazy<KeyMapping> ENDER_RING = Lazy.of(() -> new KeyMapping(
            "key.enderRing",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_I,
            KeyMapping.Category.MISC // 26.1.2-port: categories are objects now (was "key.categories.misc")
    ));
}
