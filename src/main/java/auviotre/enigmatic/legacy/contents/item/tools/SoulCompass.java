package auviotre.enigmatic.legacy.contents.item.tools;

import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.api.item.IItemHelper;
import auviotre.enigmatic.legacy.contents.entity.misc.PermanentItemEntity;
import auviotre.enigmatic.legacy.contents.item.generic.BaseCursedItem;
import auviotre.enigmatic.legacy.handlers.EnigmaticHandler;
import auviotre.enigmatic.legacy.handlers.SoulArchive;
import auviotre.enigmatic.legacy.registries.EnigmaticItems;
import com.google.common.base.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public class SoulCompass extends BaseCursedItem {
    @OnlyIn(Dist.CLIENT)
    private CompassWobble wobble;
    @OnlyIn(Dist.CLIENT)
    private CompassWobble wobbleRandom;
    @Nullable
    @OnlyIn(Dist.CLIENT)
    private BlockPos nearestCrystal;

    public SoulCompass() {
        super(IItemHelper.singleProperties().rarity(Rarity.UNCOMMON).fireResistant());
    }

    @OnlyIn(Dist.CLIENT)
    public void setNearestCrystal(BlockPos nearestCrystal) {
        if (!Objects.equal(this.nearestCrystal, nearestCrystal)) {
            if (nearestCrystal != null && this.nearestCrystal != null) {
                this.wobble.rotation = 1.0;
                this.wobble.deltaRotation = 0.3;
            } else {
                this.wobble.rotation = this.wobbleRandom.rotation;
                this.wobble.deltaRotation = this.wobbleRandom.deltaRotation;
            }
            this.nearestCrystal = nearestCrystal;
        }
    }

    // 26.1.2-port: was registerVariants(). It created the two needles and registered the "angle" item property as an
    // anonymous ClampedItemPropertyFunction; ItemProperties is gone, so only the needles are left here and the function
    // body is getAngle() below, called by ELItemModelProperties.SoulCompassAngle.
    @OnlyIn(Dist.CLIENT)
    public void initNeedle() {
        this.wobble = new CompassWobble(0.1);
        this.wobbleRandom = new CompassWobble(0.6);
    }

    // 26.1.2-port: the old function got a LivingEntity and fell back to stack.getEntityRepresentation() (an item frame
    // or a dropped item), which no longer exists. Item model properties get an ItemOwner now, which is the living
    // holder or that other entity, so "entity" became "owner" and "living" is owner.asLivingEntity().
    @OnlyIn(Dist.CLIENT)
    // 26.1.2-port (dedicated server): the level parameter was a ClientLevel, a class a server does not have, and it
    // sat in this common class's method signature. Only Level methods are used; the caller is client-only either way.
    public float getAngle(ItemStack stack, @Nullable Level level, @Nullable ItemOwner owner, int seed) {
        if (owner == null) return 0.0F;
        LivingEntity living = owner.asLivingEntity();
        if (level == null && owner.level().isClientSide()) { // was: owner.level() instanceof ClientLevel
            level = owner.level();
        }
        assert level != null;
        BlockPos target = this.nearestCrystal;
        long gameTime = level.getGameTime();

        if (target != null && !(owner.position().distanceToSqr(target.getX() + 0.5D, owner.position().y(), target.getZ() + 0.5D) < 1.0E-5F)) {
            boolean isLocalPlayer = living instanceof Player player && player.isLocalPlayer();
            double bodyRotation;

            if (isLocalPlayer) bodyRotation = living.getYRot();
            else return this.randomAngle(gameTime, seed);
            ItemStack item = EnigmaticHandler.getItem(living, EnigmaticItems.SOUL_COMPASS);
            if (!EnigmaticHandler.canUse(living, stack) || !ItemStack.isSameItemSameComponents(item, stack) || level.getBiome(living.blockPosition()).is(Biomes.SOUL_SAND_VALLEY))
                return this.randomAngle(gameTime, seed);

            bodyRotation = Mth.positiveModulo(bodyRotation / 360.0D, 1.0D);
            double angle = this.getAngleTo(Vec3.atCenterOf(target), owner.position()) / ((float) Math.PI * 2F);
            double otherAngle;

            if (this.wobble.shouldUpdate(gameTime)) {
                this.wobble.update(gameTime, 0.5D - (bodyRotation - 0.25D));
            }

            otherAngle = angle + this.wobble.rotation;

            return Mth.positiveModulo((float) otherAngle, 1.0F);
        } else return this.randomAngle(gameTime, seed);
    }

    @OnlyIn(Dist.CLIENT)
    private float randomAngle(long gameTime, int seed) {
        if (this.wobbleRandom.shouldUpdate(gameTime)) {
            this.wobbleRandom.update(gameTime, Math.random());
        }
        double randomAngle = this.wobbleRandom.rotation + this.hash(seed) / 2.14748365E9F;
        return Mth.positiveModulo((float) randomAngle, 1.0F);
    }

    private int hash(int value) {
        return value * 1327217883;
    }

    private double getAngleTo(Vec3 pos, Vec3 from) { // 26.1.2-port: takes the owner's position instead of an Entity
        return Math.atan2(pos.z() - from.z(), pos.x() - from.x());
    }

    @OnlyIn(Dist.CLIENT)
    private static class CompassWobble {
        double needleMobility;
        double rotation;
        private double deltaRotation;
        private long lastUpdateTick;

        CompassWobble(double needleMobility) {
            this.needleMobility = needleMobility;
        }

        boolean shouldUpdate(long pGameTime) {
            return this.lastUpdateTick != pGameTime;
        }

        void update(long gameTime, double wobbleAmount) {
            this.lastUpdateTick = gameTime;
            double var = wobbleAmount - this.rotation;
            var = Mth.positiveModulo(var + 0.5D, 1.0D) - 0.5D;
            this.deltaRotation += var * this.needleMobility;
            this.deltaRotation *= 0.8D;

            this.rotation = Mth.positiveModulo(this.rotation + this.deltaRotation, 1.0D);
        }
    }

    @Mod(value = EnigmaticLegacy.MODID)
    @EventBusSubscriber(modid = EnigmaticLegacy.MODID)
    public static class Events {
        public static final Map<Player, Integer> LAST_SOUL_COMPASS_UPDATE = new WeakHashMap<>();

        @SubscribeEvent
        private static void onPlayerTravel(PlayerEvent.@NotNull PlayerChangedDimensionEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                LAST_SOUL_COMPASS_UPDATE.remove(player);
            }
        }

        @SubscribeEvent
        private static void onPlayerTick(PlayerTickEvent.@NotNull Pre event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                if (EnigmaticHandler.hasItem(player, EnigmaticItems.SOUL_COMPASS)) {
                    Integer lastUpdate = LAST_SOUL_COMPASS_UPDATE.get(player);

                    if (lastUpdate == null || player.tickCount - lastUpdate > 10) {
                        var optional = SoulArchive.updateSoulCompass(player);
                        optional.ifPresent(tuple -> {
                            BlockPos pos = tuple.getB();
                            if (player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < 256) {
                                player.level().getChunkAt(pos);
                                UUID id = tuple.getA();
                                AABB box = new AABB(pos).inflate(3);
                                List<PermanentItemEntity> list = player.level().getEntitiesOfClass(PermanentItemEntity.class, box, entity -> entity.getUUID().equals(id));

                                if (list.isEmpty()) SoulArchive.getInstance().removeItem(id);
                            }
                        });
                    }
                }
            }
        }

        @SubscribeEvent
        private static void onPlayerRespawn(PlayerEvent.@NotNull PlayerRespawnEvent event) {
            if (!event.getEntity().level().isClientSide()) {
                LAST_SOUL_COMPASS_UPDATE.remove(event.getEntity());
            }
        }
    }
}
