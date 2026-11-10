package auviotre.enigmatic.legacy.contents.entity.misc;

import auviotre.enigmatic.legacy.registries.EnigmaticEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.UUIDUtil;

public class ExplorerMarker extends Entity implements OwnableEntity {
    // 26.1.2-port: the OPTIONAL_UUID serializer was removed; owners are synced as an EntityReference (a UUID that resolves lazily)
    protected static final EntityDataAccessor<Optional<EntityReference<LivingEntity>>> DATA_OWNER_UUID = SynchedEntityData.defineId(ExplorerMarker.class, EntityDataSerializers.OPTIONAL_LIVING_ENTITY_REFERENCE);

    public ExplorerMarker(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public ExplorerMarker(Level level, BlockPos pos, LivingEntity owner) {
        super(EnigmaticEntities.EXPLORER_MARKER.get(), level);
        this.setGlowingTag(true);
        this.setOwnerUUID(owner.getUUID());
        this.setPos(pos.getBottomCenter());
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide()) {
            BlockEntity blockEntity = this.level().getBlockEntity(BlockPos.containing(this.getX(), this.getEyeY(), this.getZ()));
            if (!(blockEntity instanceof RandomizableContainer entity) || entity.getLootTable() == null) this.discard();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_OWNER_UUID, Optional.empty());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        input.read("Owner", UUIDUtil.CODEC).ifPresent(this::setOwnerUUID);
    }

    // 26.1.2-port: entity save data goes through ValueOutput/ValueInput instead of CompoundTag (same keys).
    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.storeNullable("Owner", UUIDUtil.CODEC, this.getOwnerUUID());
    }

    // 26.1.2-port: OwnableEntity asks for getOwnerReference() instead of getOwnerUUID()
    @Override
    public @Nullable EntityReference<LivingEntity> getOwnerReference() {
        return this.entityData.get(DATA_OWNER_UUID).orElse(null);
    }

    public @Nullable UUID getOwnerUUID() {
        EntityReference<LivingEntity> owner = this.getOwnerReference();
        return owner == null ? null : owner.getUUID();
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(DATA_OWNER_UUID, Optional.ofNullable(uuid).map(id -> EntityReference.<LivingEntity>of(id)));
    }

    // 26.1.2-port: hurtServer is abstract in Entity now; the marker cannot be damaged (as vanilla's Marker)
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }
}
