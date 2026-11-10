package auviotre.enigmatic.legacy.client.renderer;

import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.contents.entity.projectile.DragonBreathArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;

// 26.1.2-port: renderers have a second type parameter, the render state they extract from the entity and then draw
public class DragonBreathArrowRender extends ArrowRenderer<DragonBreathArrow, ArrowRenderState> {
    public static final Identifier LOCATION = EnigmaticLegacy.location("textures/entity/projectiles/dragon_breath_arrow.png");

    public DragonBreathArrowRender(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected Identifier getTextureLocation(ArrowRenderState state) { // 26.1.2-port: takes the render state, not the entity
        return LOCATION;
    }

    @Override
    public ArrowRenderState createRenderState() {
        return new ArrowRenderState();
    }
}
