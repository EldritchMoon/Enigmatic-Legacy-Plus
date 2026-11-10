package auviotre.enigmatic.legacy.client.renderer.state;

import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

// 26.1.2-port: new file. Entity renderers and models no longer see the entity while drawing; IchorSpriteRenderer copies
// what they need into this object first. It is a file of its own (not nested in the renderer) because IchorSpriteModel
// uses it too. ArmedEntityRenderState carries the held items for ItemInHandLayer.
public class IchorSpriteRenderState extends ArmedEntityRenderState {
    /** IchorSprite.getAttackAnimationScale(partialTick), 0..1. */
    public float attackScale;
    /** From the sprite's eyes to the middle of its attack target; null when it has no target (no beam drawn). */
    public @Nullable Vec3 beamVector;
    public float beamLength;
    /** Which of the two end-cap frames to show (tickCount % 2 == 0). */
    public boolean beamCapFrame;
}
