package auviotre.enigmatic.legacy.contents.entity;

import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.api.entity.SpearAttackMob;
import auviotre.enigmatic.legacy.contents.entity.behavior.SpearAttack;
import auviotre.enigmatic.legacy.contents.entity.projectile.ThrownIchorSpear;
import auviotre.enigmatic.legacy.contents.item.tools.IchorSpear;
import auviotre.enigmatic.legacy.registries.EnigmaticEntities;
import auviotre.enigmatic.legacy.registries.EnigmaticItems;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.ActivityData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import net.minecraft.tags.ItemTags;
import auviotre.enigmatic.legacy.handlers.SaveDataHelper;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.entity.EntitySpawnReason;

public class PiglinWanderer extends AbstractPiglin implements SpearAttackMob {
    protected static final ImmutableList<SensorType<? extends Sensor<? super PiglinWanderer>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.NEAREST_PLAYERS,
            SensorType.HURT_BY,
            SensorType.PIGLIN_BRUTE_SPECIFIC_SENSOR
    );
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.DOORS_TO_CLOSE,
            MemoryModuleType.NEAREST_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_PLAYER,
            MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
            MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS,
            MemoryModuleType.NEARBY_ADULT_PIGLINS,
            MemoryModuleType.HURT_BY,
            MemoryModuleType.HURT_BY_ENTITY,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.ATTACK_COOLING_DOWN,
            MemoryModuleType.INTERACTION_TARGET,
            MemoryModuleType.PATH,
            MemoryModuleType.ANGRY_AT,
            MemoryModuleType.NEAREST_VISIBLE_NEMESIS,
            MemoryModuleType.HOME
    );
    // 26.1.2-port: a brain is built in one step now; the provider also takes the activities (was Ai.makeBrain)
    private static final Brain.Provider<PiglinWanderer> BRAIN_PROVIDER = Brain.provider(MEMORY_TYPES, SENSOR_TYPES, Ai::getActivities);
    private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING = SynchedEntityData.defineId(PiglinWanderer.class, EntityDataSerializers.BOOLEAN);

    public PiglinWanderer(EntityType<? extends AbstractPiglin> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 12;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 32.0).add(Attributes.MOVEMENT_SPEED, 0.34).add(Attributes.ATTACK_KNOCKBACK, 1.6).add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_IS_CHARGING, false);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnType, @Nullable SpawnGroupData spawnGroupData) {
        Ai.initMemories(this);
        this.populateDefaultEquipmentSlots(level.getRandom(), difficulty);
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.MAINHAND, EnigmaticItems.ICHOR_SPEAR.toStack());
    }

    @Override
    protected void customServerAiStep(ServerLevel level) { // 26.1.2-port: customServerAiStep takes the level
        this.timeInOverworld = 0;
        Profiler.get().push("piglinWandererBrain");
        this.getBrain().tick(level, this);
        Profiler.get().pop();
        Ai.updateActivity(this);
        super.customServerAiStep(level);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity entity) { // 26.1.2-port: doHurtTarget takes the level
        boolean flag = super.doHurtTarget(level, entity);
        if (flag) this.knockback(0.1, -Mth.sin(this.getYRot() * 0.017453292F), Mth.cos(this.getYRot() * 0.017453292F));
        return flag;
    }

    // 26.1.2-port: brainProvider() is gone and makeBrain takes the saved memories (Brain.Packed) instead of a Dynamic
    @Override
    protected Brain<PiglinWanderer> makeBrain(Brain.Packed packedBrain) {
        return BRAIN_PROVIDER.makeBrain(this, packedBrain);
    }

    @Override
    public Brain<PiglinWanderer> getBrain() {
        return (Brain<PiglinWanderer>) super.getBrain();
    }

    @Override
    public boolean wantsToPickUp(ServerLevel level, ItemStack stack) { // 26.1.2-port: wantsToPickUp takes the level
        return stack.is(EnigmaticItems.ICHOR_SPEAR) && super.wantsToPickUp(level, stack);
    }

    @Override
    protected boolean canHunt() {
        return false;
    }

    @Override
    protected void playConvertedSound() {
        this.makeSound(SoundEvents.PIGLIN_CONVERTED_TO_ZOMBIFIED);
    }

    protected void playAngrySound() {
        this.makeSound(SoundEvents.PIGLIN_ANGRY);
    }

    // 26.1.2-port: hurt -> hurtServer (server side only, so the client-side early return is gone)
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean flag = super.hurtServer(level, source, amount);
        if (flag && source.getEntity() instanceof LivingEntity)
            Ai.wasHurtBy(level, this, (LivingEntity) source.getEntity());
        return flag;
    }

    @Override
    public PiglinArmPose getArmPose() {
        if (this.isAggressive() && this.isHoldingMeleeWeapon()) return PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON;
        if (this.isCharging()) return PiglinArmPose.CROSSBOW_HOLD;
        return PiglinArmPose.DEFAULT;
    }

    @Override
    public boolean isCharging() {
        return this.entityData.get(DATA_IS_CHARGING);
    }

    @Override
    public void setCharging(boolean isCharging) {
        this.entityData.set(DATA_IS_CHARGING, isCharging);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        InteractionHand hand = ProjectileUtil.getWeaponHoldingHand(this, (item) -> item instanceof IchorSpear);
        ItemStack stack = this.getItemInHand(hand);
        float velocity = 1.1F + this.level().getDifficulty().getId() * 0.02F;
        if (stack.getItem() instanceof IchorSpear && this.level() instanceof ServerLevel server) {
            ThrownIchorSpear spear = new ThrownIchorSpear(this, server, stack.copyWithCount(1));
            Vec3 pos = target.position().add(target.getDeltaMovement().scale(0.5));
            double dx = pos.x() - this.getX();
            double dy = pos.y() + target.getBbHeight() / 3 - spear.getY();
            double dz = pos.z() - this.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            spear.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            spear.shoot(dx, dy + dist * 0.284, dz, velocity, 4 - this.level().getDifficulty().getId());
            server.addFreshEntity(spear);
            this.playSound(SoundEvents.TRIDENT_THROW.value(), 1.0F, 0.84F);
        }
    }

    private static class WeaponSwitch extends Behavior<PiglinWanderer> {
        public WeaponSwitch() {
            super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT), 1200);
        }

        private static LivingEntity getAttackTarget(LivingEntity shooter) {
            return shooter.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        }

        @Override
        protected void start(ServerLevel level, PiglinWanderer piglin, long gameTime) {
            LivingEntity target = getAttackTarget(piglin);
            if (target == null) return;
            ItemStack backup = ItemStack.EMPTY;
            if (!piglin.getPersistentData().getCompoundOrEmpty("BackupItem").isEmpty())
                backup = SaveDataHelper.parseItem(piglin.registryAccess(), piglin.getPersistentData().getCompoundOrEmpty("BackupItem"));
            if (target.closerThan(piglin, 5) && piglin.isHolding(is -> is.getItem() instanceof IchorSpear)) {
                InteractionHand hand = piglin.getMainHandItem().getItem() instanceof IchorSpear ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                ItemStack copy = piglin.getItemInHand(hand).copy();
                if (backup.isEmpty())
                    piglin.setItemSlot(EquipmentSlot.MAINHAND, Items.GOLDEN_SWORD.getDefaultInstance());
                else piglin.setItemSlot(EquipmentSlot.MAINHAND, backup.copy());
                piglin.getPersistentData().put("BackupItem", SaveDataHelper.saveItem(piglin.registryAccess(), copy));
                piglin.setCharging(false);
            }
            if (!target.closerThan(piglin, 9) && piglin.isHolding(is -> is.is(ItemTags.SWORDS))) {
                InteractionHand hand = piglin.getMainHandItem().is(ItemTags.SWORDS) ? /* 26.1.2-port: SwordItem -> swords tag */ InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                ItemStack copy = piglin.getItemInHand(hand).copy();
                if (backup.isEmpty())
                    piglin.setItemSlot(EquipmentSlot.MAINHAND, EnigmaticItems.ICHOR_SPEAR.toStack());
                else piglin.setItemSlot(EquipmentSlot.MAINHAND, backup.copy());
                piglin.getPersistentData().put("BackupItem", SaveDataHelper.saveItem(piglin.registryAccess(), copy));
                piglin.setCharging(false);
            }
            doStop(level, piglin, gameTime);
        }
    }

    protected static class Ai {
        // 26.1.2-port: activities are returned as ActivityData and handed to the Brain constructor, which also sets
        // CORE as the core activity and starts the default activity (IDLE), as the old makeBrain did by hand.
        protected static List<ActivityData<PiglinWanderer>> getActivities(PiglinWanderer wanderer) {
            return List.of(initCoreActivity(), initIdleActivity(), initFightActivity(wanderer));
        }

        protected static void initMemories(PiglinWanderer wanderer) {
            GlobalPos globalpos = GlobalPos.of(wanderer.level().dimension(), wanderer.blockPosition());
            wanderer.getBrain().setMemory(MemoryModuleType.HOME, globalpos);
        }

        private static ActivityData<PiglinWanderer> initCoreActivity() {
            return ActivityData.<PiglinWanderer>create(Activity.CORE, 0, ImmutableList.of(
                    new LookAtTargetSink(45, 90),
                    new MoveToTargetSink(),
                    InteractWithDoor.create(),
                    StopBeingAngryIfTargetDead.create()
            ));
        }

        // 26.1.2-port: target finders and stop conditions receive the ServerLevel as their first argument
        private static ActivityData<PiglinWanderer> initIdleActivity() {
            return ActivityData.<PiglinWanderer>create(Activity.IDLE, 10, ImmutableList.of(
                    StartAttacking.<PiglinWanderer>create(Ai::findNearestValidAttackTarget),
                    createIdleLookBehaviors(),
                    createIdleMovementBehaviors(),
                    SetLookAndInteract.create(EntityType.PLAYER, 4)
            ));
        }

        private static ActivityData<PiglinWanderer> initFightActivity(PiglinWanderer wanderer) {
            return ActivityData.<PiglinWanderer>create(Activity.FIGHT, 10, ImmutableList.of(
                    StopAttackingIfTargetInvalid.<PiglinWanderer>create((level, entity) -> !isNearestValidAttackTarget(level, wanderer, entity)),
                    BehaviorBuilder.triggerIf(Ai::hasSpear, BackUpIfTooClose.create(10, 0.75F)),
                    SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(piglin -> hasSpear(piglin) ? (isCharging(piglin) ? 0.4F : 0.6F) : 1.0F),
                    new SpearAttack<>(),
                    new WeaponSwitch(),
                    MeleeAttack.create(20)
            ), MemoryModuleType.ATTACK_TARGET);
        }

        private static RunOne<PiglinWanderer> createIdleLookBehaviors() {
            return new RunOne<>(ImmutableList.of(
                    Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 8.0F), 1),
                    Pair.of(SetEntityLookTarget.create(EntityType.PIGLIN, 8.0F), 1),
                    Pair.of(SetEntityLookTarget.create(EntityType.PIGLIN_BRUTE, 8.0F), 1),
                    Pair.of(SetEntityLookTarget.create(8.0F), 1),
                    Pair.of(new DoNothing(30, 60), 1)
            ));
        }

        private static RunOne<PiglinWanderer> createIdleMovementBehaviors() {
            return new RunOne<>(ImmutableList.of(
                    Pair.of(RandomStroll.stroll(0.6F), 2),
                    Pair.of(InteractWith.of(EntityType.PIGLIN, 8, MemoryModuleType.INTERACTION_TARGET, 0.6F, 2), 2),
                    Pair.of(InteractWith.of(EntityType.PIGLIN_BRUTE, 8, MemoryModuleType.INTERACTION_TARGET, 0.6F, 2), 2),
                    Pair.of(StrollToPoi.create(MemoryModuleType.HOME, 0.6F, 2, 100), 2),
                    Pair.of(StrollAroundPoi.create(MemoryModuleType.HOME, 0.6F, 5), 2),
                    Pair.of(new DoNothing(30, 60), 1)
            ));
        }

        protected static void updateActivity(PiglinWanderer wanderer) {
            Brain<PiglinWanderer> brain = wanderer.getBrain();
            Activity activity = brain.getActiveNonCoreActivity().orElse(null);
            brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
            Activity activity1 = brain.getActiveNonCoreActivity().orElse(null);
            if (activity != activity1) playActivitySound(wanderer);
            wanderer.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
        }

        private static boolean isNearestValidAttackTarget(ServerLevel level, AbstractPiglin piglin, LivingEntity target) {
            return findNearestValidAttackTarget(level, piglin).filter((entity) -> entity == target).isPresent();
        }

        private static Optional<? extends LivingEntity> findNearestValidAttackTarget(ServerLevel level, AbstractPiglin piglin) {
            Optional<LivingEntity> optional = BehaviorUtils.getLivingEntityFromUUIDMemory(piglin, MemoryModuleType.ANGRY_AT);
            if (optional.isPresent() && Sensor.isEntityAttackableIgnoringLineOfSight(level, piglin, optional.get())) {
                return optional;
            } else {
                Optional<? extends LivingEntity> optional1 = getTargetIfWithinRange(piglin);
                return optional1.isPresent() ? optional1 : piglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS);
            }
        }

        private static Optional<? extends LivingEntity> getTargetIfWithinRange(AbstractPiglin piglin) {
            return piglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER).filter(entity -> entity.closerThan(piglin, 16.0));
        }

        protected static void wasHurtBy(ServerLevel level, PiglinWanderer wanderer, LivingEntity target) {
            if (!(target instanceof AbstractPiglin)) {
                PiglinAi.maybeRetaliate(level, wanderer, target);
            }
        }

        private static boolean hasSpear(LivingEntity piglin) {
            return piglin.isHolding((is) -> is.getItem() instanceof IchorSpear);
        }


        private static boolean isCharging(LivingEntity entity) {
            return entity instanceof PiglinWanderer piglin && piglin.isCharging();
        }

        private static void playActivitySound(PiglinWanderer wanderer) {
            wanderer.getBrain().getActiveNonCoreActivity().ifPresent((activity) -> {
                if (activity == Activity.FIGHT) wanderer.playAngrySound();
            });
        }
    }

    @Mod(value = EnigmaticLegacy.MODID)
    @EventBusSubscriber(modid = EnigmaticLegacy.MODID)
    public static class Events {
        @SubscribeEvent
        private static void onEntitySpawn(@NotNull FinalizeSpawnEvent event) {
            if (event.getEntity().getType().equals(EntityType.PIGLIN) && (event.getSpawnType() == EntitySpawnReason.NATURAL || event.getSpawnType() == EntitySpawnReason.CHUNK_GENERATION)) {
                ServerLevelAccessor level = event.getLevel();
                if (level.getRandom().nextFloat() > 0.1F) return;
                PiglinWanderer piglinWanderer = EnigmaticEntities.PIGLIN_WANDERER.get().create(level.getLevel(), event.getSpawnType());
                SpawnGroupData spawnData = event.getSpawnData();
                if (piglinWanderer != null) {
                    event.setSpawnCancelled(true);
                    event.setCanceled(true);
                    piglinWanderer.setPos(event.getX(), event.getY(), event.getZ());
                    piglinWanderer.finalizeSpawn(level, event.getDifficulty(), event.getSpawnType(), spawnData);
                    level.addFreshEntity(piglinWanderer);
                }
            }
        }
    }
}
