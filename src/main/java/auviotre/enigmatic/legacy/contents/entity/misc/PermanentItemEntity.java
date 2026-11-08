package auviotre.enigmatic.legacy.contents.entity.misc;


import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.api.item.IPermanentCrystal;
import auviotre.enigmatic.legacy.api.item.ISharableItem;
import auviotre.enigmatic.legacy.contents.item.misc.SoulCrystal;
import auviotre.enigmatic.legacy.contents.item.misc.StorageCrystal;
import auviotre.enigmatic.legacy.handlers.SoulArchive;
import auviotre.enigmatic.legacy.packets.client.AcceptorSyncPacket;
import auviotre.enigmatic.legacy.registries.EnigmaticComponents;
import auviotre.enigmatic.legacy.registries.EnigmaticEntities;
import auviotre.enigmatic.legacy.registries.EnigmaticItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.include.com.google.common.collect.ImmutableMap;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.UUIDUtil;
import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Optional;

public class PermanentItemEntity extends Entity {
    private static final EntityDataAccessor<ItemStack> ITEM = SynchedEntityData.defineId(PermanentItemEntity.class, EntityDataSerializers.ITEM_STACK);
    public float hoverStart = (float) (Math.random() * Math.PI * 2.0D);
    private int age;
    private int pickupDelay;
    private int health = 5;
    private UUID thrower;
    private UUID owner;
    private Vec3 position;
    // for Storage Crystal
    private Map<String, ItemStack> restoreMap;
    // for Abyssal Heart
    public Set<UUID> acceptors;

    public PermanentItemEntity(EntityType<PermanentItemEntity> type, Level world) {
        super(type, world);
    }

    public PermanentItemEntity(Level world, double x, double y, double z) {
        this(EnigmaticEntities.PERMANENT_ITEM_ENTITY.get(), world);
        y = y <= world.getMinBuildHeight() ? world.getMinBuildHeight() + 8 : y;

        this.setPos(x, y, z);
        this.setYRot(this.random.nextFloat() * 360.0F);
        this.setInvulnerable(true);

        this.setNoGravity(true);
        this.position = new Vec3(x, y, z);
    }

    public PermanentItemEntity(Level worldIn, double x, double y, double z, ItemStack stack) {
        this(worldIn, x, y, z);
        this.setItem(stack);
    }

    @OnlyIn(Dist.CLIENT)
    private PermanentItemEntity(PermanentItemEntity entity) {
        super(entity.getType(), entity.level());
        this.setItem(entity.getItem().copy());
        this.copyPosition(entity);
        this.age = entity.age;
        this.hoverStart = entity.hoverStart;
        this.acceptors = entity.acceptors;
    }

    @OnlyIn(Dist.CLIENT)
    public PermanentItemEntity copy() {
        return new PermanentItemEntity(this);
    }

    @OnlyIn(Dist.CLIENT)
    public float getItemHover(float partialTicks) {
        return (this.getAge() + partialTicks) / 20.0F + this.hoverStart;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean shouldRenderAtSqrDistance(double distance) {
        double d0 = this.getBoundingBox().getSize() * 4.0D;
        if (Double.isNaN(d0)) d0 = 4.0D;
        d0 = d0 * 64.0D;
        return distance < d0 * d0;
    }

    @OnlyIn(Dist.CLIENT)
    public int getAge() {
        return this.age;
    }

    @Override
    public boolean dampensVibrations() {
        return this.getItem().is(ItemTags.DAMPENS_VIBRATIONS);
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        builder.define(PermanentItemEntity.ITEM, ItemStack.EMPTY);
    }

    @Override
    public void tick() {
        if (this.getItem().isEmpty()) {
            this.discard();
        } else {
            if (!this.level().isClientSide() && this.position != null) {
                if (!this.position().equals(this.position))
                    this.teleportTo(this.position.x, this.position.y, this.position.z);
            }

            super.tick();
            if (this.pickupDelay > 0 && this.pickupDelay != 32767) --this.pickupDelay;

            this.xo = this.getX();
            this.yo = this.getY();
            this.zo = this.getZ();
            Vec3 vec3d = this.getDeltaMovement();

            if (!this.isNoGravity()) this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.04D, 0.0D));

            if (this.level().isClientSide()) {
                this.noPhysics = false;
                LocalPlayer player = Minecraft.getInstance().player;
                if (this.acceptors == null || (player != null && this.acceptors.contains(player.getUUID()))) {
                    this.level().addParticle(ParticleTypes.PORTAL, this.getX(), this.getY() + (this.getBbHeight() / 2), this.getZ(), ((Math.random() - 0.5) * 2.0), ((Math.random() - 0.5) * 2.0), ((Math.random() - 0.5) * 2.0));
                }
            } else if (this.level() instanceof ServerLevel level && this.tickCount % 20 == 0) {
                if (this.getItem().getItem() instanceof ISharableItem) {
                    PacketDistributor.sendToPlayersNear(level, null, this.getX(), this.getY(), this.getZ(), 24, new AcceptorSyncPacket(this.getId(), this.acceptors));
                }
            }

            ++this.age;
            if (!this.level().isClientSide()) {
                double d0 = this.getDeltaMovement().subtract(vec3d).lengthSqr();
                if (d0 > 0.01D) {
                    this.hasImpulse = true;
                }
            }

            ItemStack item = this.getItem();
            if (item.isEmpty()) this.discard();
            // Portal Cooldown
            this.setPortalCooldown();
        }
    }

    @Override
    public int getDimensionChangingDelay() {
        return Short.MAX_VALUE;
    }

    @Override
    public Entity changeDimension(TeleportTransition transition) {
        return null;
    }

    @Override
    public boolean canChangeDimensions(Level oldLevel, Level newLevel) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide() || !this.isAlive()) return false;

        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            EnigmaticLegacy.LOGGER.warn("Attacked permanent item entity with absolute DamageSource: {}", source);
            this.kill();
            return true;
        }
        return false;
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (reason == RemovalReason.DISCARDED || reason == RemovalReason.KILLED) {
            EnigmaticLegacy.LOGGER.warn("Removing Permanent Item Entity: {}", this);
            if (!this.level().isClientSide()) SoulArchive.getInstance().removeItem(this);
        }
        super.remove(reason);
    }

    // 26.1.2-port: entity save data goes through ValueOutput/ValueInput instead of CompoundTag (same keys).
    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        output.putShort("Health", (short) this.health);
        output.putShort("Age", (short) this.age);
        output.putShort("PickupDelay", (short) this.pickupDelay);
        output.storeNullable("Thrower", UUIDUtil.CODEC, this.getThrowerId());
        output.storeNullable("Owner", UUIDUtil.CODEC, this.getOwnerId());
        if (!this.getItem().isEmpty()) output.store("Item", ItemStack.CODEC, this.getItem());
        if (this.position != null) {
            output.putDouble("BoundX", this.position.x);
            output.putDouble("BoundY", this.position.y);
            output.putDouble("BoundZ", this.position.z);
        }
        Map<String, ItemStack> map = new HashMap<>();
        if (restoreMap != null) {
            for (String key : restoreMap.keySet()) {
                ItemStack item = restoreMap.get(key);
                if (!item.isEmpty()) map.put(key, item);
            }
        }
        output.store("restoreMap", RESTORE_MAP_CODEC, map);

        Map<String, UUID> acceptorMap = new HashMap<>();
        int i = 0;
        if (acceptors != null) {
            for (UUID uuid : acceptors) {
                if (uuid != null) acceptorMap.put(String.valueOf(i++), uuid);
            }
        }
        output.store("acceptorSet", ACCEPTORS_CODEC, acceptorMap);
    }

    // Same NBT layout as the old CompoundTag code (a compound of key -> item / index -> UUID).
    private static final Codec<Map<String, ItemStack>> RESTORE_MAP_CODEC = Codec.unboundedMap(Codec.STRING, ItemStack.CODEC);
    private static final Codec<Map<String, UUID>> ACCEPTORS_CODEC = Codec.unboundedMap(Codec.STRING, UUIDUtil.CODEC);

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        this.health = input.getShortOr("Health", (short) 0);
        this.age = input.getShortOr("Age", (short) 0);
        input.read("PickupDelay", Codec.SHORT).ifPresent(delay -> this.pickupDelay = delay);

        input.read("Owner", UUIDUtil.CODEC).ifPresent(uuid -> this.owner = uuid);

        input.read("Thrower", UUIDUtil.CODEC).ifPresent(uuid -> this.thrower = uuid);

        Optional<Double> x = input.read("BoundX", Codec.DOUBLE);
        Optional<Double> y = input.read("BoundY", Codec.DOUBLE);
        Optional<Double> z = input.read("BoundZ", Codec.DOUBLE);
        if (x.isPresent() && y.isPresent() && z.isPresent()) this.position = new Vec3(x.get(), y.get(), z.get());

        ImmutableMap.Builder<String, ItemStack> builder = ImmutableMap.builder();
        input.read("restoreMap", RESTORE_MAP_CODEC).ifPresent(map -> map.forEach((key, stack) -> {
            if (!stack.isEmpty()) builder.put(key, stack);
        }));
        this.restoreMap = builder.build();

        this.acceptors = new HashSet<>(input.read("acceptorSet", ACCEPTORS_CODEC).map(Map::values).orElse(Set.of()));

        Optional<ItemStack> item = input.read("Item", ItemStack.CODEC);
        if (item.isPresent()) this.setItem(item.get());
        else this.discard();
        if (this.getItem().isEmpty()) this.discard();
    }

    @Override
    public boolean isInvisibleTo(Player player) {
        if (this.getItem().getItem() instanceof ISharableItem)
            if (this.acceptors != null && !this.acceptors.contains(player.getUUID())) return false;
        return super.isInvisibleTo(player);
    }

    @Override
    public void playerTouch(Player player) {
        if (this.level() instanceof ServerLevel level) {
            if (this.pickupDelay > 0) return;

            ItemStack stack = this.getItem();
            Item item = stack.getItem();
            int count = stack.getCount();

            ItemStack copy = stack.copy();
            boolean isPlayerOwner = player.getUUID().equals(this.getOwnerId());
            boolean allowPickUp = item instanceof IPermanentCrystal && isPlayerOwner;

            if (item instanceof ISharableItem) {
                if (acceptors != null && acceptors.contains(player.getUUID())) {
                    if (player.getInventory().add(copy)) {
                        acceptors.remove(player.getUUID());
                        level.sendParticles((ServerPlayer) player, ParticleTypes.DRAGON_BREATH, false, this.getX(), this.getY(0.5), this.getZ(), 48, 0, 0, 0, 0.03);
                        EnigmaticLegacy.LOGGER.info("Player {} picking up (share: {}): {}", player.getGameProfile().getName(), stack, this);
                        player.awardStat(Stats.ITEM_PICKED_UP.get(item), count);
                        if (acceptors.isEmpty()) this.discard();
                        else PacketDistributor.sendToAllPlayers(new AcceptorSyncPacket(this.getId(), this.acceptors));
                    }
                }
                return;
            }

            if (allowPickUp) {
                if (item instanceof StorageCrystal) {
                    StorageCrystal.Info info = stack.get(EnigmaticComponents.STORAGE_INFO);
                    if (info != null) {
                        ItemStack crystal = info.soulCrystal();
                        StorageCrystal.retrieveDropsFromCrystal(stack, (ServerPlayer) player, crystal, this.position(), this.restoreMap);
                    }
                    SoulArchive.getInstance().removeItem(this);
                } else {
                    if (!SoulCrystal.retrieveSoulFromCrystal(player)) return;
                    else SoulArchive.getInstance().removeItem(this);
                }
                level.sendParticles(ParticleTypes.DRAGON_BREATH, this.getX(), this.getY(0.5), this.getZ(), 48, 0, 0, 0, 0.03);
                player.take(this, count);
                EnigmaticLegacy.LOGGER.info("Player {} picking up (crystal): {}", player.getGameProfile().getName(), this);
                this.discard();
                stack.setCount(0);
            } else if (this.pickupDelay == 0 && (this.owner == null || this.owner.equals(player.getUUID())) && (count <= 0 || player.getInventory().add(stack))) {
                copy.setCount(copy.getCount() - this.getItem().getCount());
                EnigmaticLegacy.LOGGER.info("Player {} picking up (item: {}): {}", player.getGameProfile().getName(), copy, this);
                if (stack.isEmpty()) {
                    this.discard();
                    stack.setCount(count);
                }
                player.awardStat(Stats.ITEM_PICKED_UP.get(item), count);
            }
        }
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (this.pickupDelay > 0) return InteractionResult.PASS;
        if (!player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) return InteractionResult.PASS;
        if (player.isSpectator()) return InteractionResult.SUCCESS;
        ItemStack stack = this.getItem();
        boolean flag = stack.getItem() instanceof SoulCrystal;
        if (this.level() instanceof ServerLevel level) {
            if (flag && player.getUUID().equals(this.getOwnerId())) {
                if (player.addItem(stack.copy())) {
                    this.discard();
                    level.sendParticles(ParticleTypes.DRAGON_BREATH, this.getX(), this.getY(0.5), this.getZ(), 48, 0, 0, 0, 0.03);
                    return InteractionResult.CONSUME;
                }
            }
            return InteractionResult.PASS;
        }
        return flag ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    public void setRestoreMap(Map<String, ItemStack> restoreMap) {
        this.restoreMap = restoreMap;
    }

    public boolean containsSoul() {
        return this.getItem().is(EnigmaticItems.SOUL_CRYSTAL);
    }

    @Override
    public Component getName() {
        Component itextcomponent = this.getCustomName();
        ItemStack item = this.getItem();
        return itextcomponent != null ? itextcomponent : item.getItem().getName(item);
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean isCurrentlyGlowing() {
        return true;
    }

    public ItemStack getItem() {
        return this.getEntityData().get(PermanentItemEntity.ITEM);
    }

    public void setItem(ItemStack stack) {
        this.getEntityData().set(PermanentItemEntity.ITEM, stack);
    }

    public @Nullable UUID getOwnerId() {
        return this.owner;
    }

    public void setOwnerId(@Nullable UUID ownerId) {
        this.owner = ownerId;
    }

    public @Nullable UUID getThrowerId() {
        return this.thrower;
    }

    public void setThrowerId(@Nullable UUID throwerId) {
        this.thrower = throwerId;
    }

    public void setDefaultPickupDelay() {
        this.pickupDelay = 10;
    }

    public void setNoPickupDelay() {
        this.pickupDelay = 0;
    }

    public void setInfinitePickupDelay() {
        this.pickupDelay = 32767;
    }

    public void setPickupDelay(int ticks) {
        this.pickupDelay = ticks;
    }

    public boolean cannotPickup() {
        return this.pickupDelay > 0;
    }

    public void makeFakeItem() {
        this.setInfinitePickupDelay();
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
        return new ClientboundAddEntityPacket(this, entity);
    }
}