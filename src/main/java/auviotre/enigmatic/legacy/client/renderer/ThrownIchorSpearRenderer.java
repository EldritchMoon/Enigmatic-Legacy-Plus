package auviotre.enigmatic.legacy.client.renderer;

import auviotre.enigmatic.legacy.contents.entity.projectile.ThrownIchorSpear;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ThrownIchorSpearRenderer extends AbstractSpearRenderer<ThrownIchorSpear> {
    public ThrownIchorSpearRenderer(EntityRendererProvider.Context context) {
        super(context, 0.45F, new Vec3(2.0, 2.0, 1.0));
    }

    // 26.1.2-port: getTextureLocation() no longer exists on entity renderers. The spear is drawn from its item model
    // (AbstractSpearRenderer), which never used this texture ("textures/item/3d/ichor_spear.png") directly.
}
