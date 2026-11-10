package auviotre.enigmatic.legacy.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StarlightParticle extends SimpleAnimatedParticle {
    protected StarlightParticle(ClientLevel level, double posX, double posY, double posZ, double dx, double dy, double dz, SpriteSet spriteSet) {
        super(level, posX, posY, posZ, spriteSet, 0.0F);
        this.xd = dx;
        this.yd = dy;
        this.zd = dz;
        this.quadSize *= 0.64F;
        this.friction = 0.9F;
        this.hasPhysics = false;
        this.lifetime = 128;
        this.setSpriteFromAge(spriteSet);
        this.setFadeColor(0x22FFFFFF);
    }

    @Override
    public SingleQuadParticle.Layer getLayer() { // 26.1.2-port: getRenderType -> getLayer
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    @Override
    public int getLightCoords(float partialTick) { // 26.1.2-port: getLightColor -> getLightCoords
        return 15728880;
    }

    @Override
    public void move(double dx, double dy, double dz) {
        this.setBoundingBox(this.getBoundingBox().move(dx, dy, dz));
        this.setLocationFromBoundingbox();
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double posX, double posY, double posZ, double speedX, double speedY, double speedZ, RandomSource random) { // 26.1.2-port: gained the RandomSource
            return new StarlightParticle(level, posX, posY, posZ, speedX, speedY, speedZ, this.sprite);
        }
    }
}
