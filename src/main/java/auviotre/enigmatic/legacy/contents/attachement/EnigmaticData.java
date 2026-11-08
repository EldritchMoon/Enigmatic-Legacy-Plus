package auviotre.enigmatic.legacy.contents.attachement;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.UUIDUtil;
import auviotre.enigmatic.legacy.handlers.SaveDataHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class EnigmaticData implements ValueIOSerializable {
    private @Nullable UUID engineHook;
    private int spellstoneCooldown = 0;
    private boolean magnetRingEffect = false;
    private boolean unlockedNarrator = false;
    private boolean nebulaPower = false;
    private boolean isElytraBoosting = false;
    private boolean isForbiddenCursed = false;
    private boolean isThirstForbiddenCursed = false;
    private long timeWithCurses, timeWithoutCurses;
    private int fireImmunityTimer = 0;
    private int fireImmunityTimerLast = 0;
    private int inBeaconRange = 0;
    private int witherKills = 0;
    private float etherealShield = 0;
    private long etheriumShieldTick = 0;
    private int reviveCooldown = 0;
    private int annihilatingTick = 0;
    private int[] annihilationSweepData = new int[3];

    public UUID getEngineHook() {
        return engineHook;
    }

    public void setEngineHook(UUID engineHook) {
        this.engineHook = engineHook;
    }

    public int getSpellstoneCooldown() {
        return spellstoneCooldown;
    }

    public void setSpellstoneCooldown(int spellstoneCooldown) {
        this.spellstoneCooldown = spellstoneCooldown;
    }

    public boolean isMagnetRingEnable() {
        return magnetRingEffect;
    }

    public void setMagnetRingEnable(boolean enable) {
        this.magnetRingEffect = enable;
    }

    public void toggleMagnetRingEffect() {
        this.magnetRingEffect = !magnetRingEffect;
    }

    public long getTimeWithoutCurses() {
        return this.timeWithoutCurses;
    }

    public void setTimeWithoutCurses(long time) {
        this.timeWithoutCurses = time;
    }

    public boolean getUnlockedNarrator() {
        return this.unlockedNarrator;
    }

    public void setUnlockedNarrator(Boolean unlockedNarrator) {
        this.unlockedNarrator = unlockedNarrator;
    }

    public boolean getNebulaPower() {
        return this.nebulaPower;
    }

    public void setNebulaPower(boolean power) {
        this.nebulaPower = power;
    }

    public boolean isElytraBoosting() {
        return this.isElytraBoosting;
    }

    public void setElytraBoosting(boolean boosting) {
        this.isElytraBoosting = boosting;
    }

    public boolean isForbiddenCursed() {
        return this.isForbiddenCursed;
    }

    public void setForbiddenCursed(boolean forbidden) {
        this.isForbiddenCursed = forbidden;
    }

    public boolean isThirstForbiddenCursed() {
        return this.isThirstForbiddenCursed;
    }

    public void setThirstForbiddenCursed(boolean forbidden) {
        this.isThirstForbiddenCursed = forbidden;
    }

    public boolean isInBeaconRange() {
        return this.inBeaconRange > 0;
    }

    public void setInBeaconRangeTick(int tick) {
        this.inBeaconRange = tick;
    }

    public void InBeaconRangeTick() {
        this.inBeaconRange--;
    }

    public int getWitherKills() {
        return this.witherKills;
    }

    public void setWitherKills(int amount) {
        this.witherKills = amount;
    }

    public float getEtherealShield() {
        return this.etherealShield;
    }

    public void setEtherealShield(float amount) {
        this.etherealShield = amount;
    }

    public long getEtheriumShieldTick() {
        return this.etheriumShieldTick;
    }

    public void setEtheriumShieldTick(long tick) {
        this.etheriumShieldTick = tick;
    }

    public int getFireImmunityTimer() {
        return fireImmunityTimer;
    }

    public void setFireImmunityTimer(int timer) {
        this.fireImmunityTimerLast = this.fireImmunityTimer;
        this.fireImmunityTimer = Math.clamp(timer, 0, getFireImmunityCap());
    }

    public int getFireImmunityTimerLast() {
        return this.fireImmunityTimerLast;
    }

    public int getFireImmunityCap() {
        return 30000;
    }

    public long getTimeWithCurses() {
        return this.timeWithCurses;
    }

    public void setTimeWithCurses(long time) {
        this.timeWithCurses = time;
    }

    public void incrementTimeWithoutCurses() {
        ++this.timeWithoutCurses;
    }

    public void incrementTimeWithCurses() {
        ++this.timeWithCurses;
    }

    public int getReviveCooldown() {
        return this.reviveCooldown;
    }

    public void setReviveCooldown(int reviveCooldown) {
        this.reviveCooldown = reviveCooldown;
    }

    public int getAnnihilatingTick() {
        return this.annihilatingTick;
    }

    public void setAnnihilatingTick(int tick) {
        this.annihilatingTick = tick;
    }

    public int[] getAnnihilationSweepData() {
        return this.annihilationSweepData;
    }

    public void setAnnihilationSweepData(int damage, int tick, int level) {
        this.annihilationSweepData = new int[]{damage, tick, level};
    }

    // 26.1.2-port: attachments serialize through ValueOutput/ValueInput (INBTSerializable was removed).
    // Same keys as before. save/load keep a CompoundTag form for the sync packet.
    @Override
    public void serialize(ValueOutput output) {
        output.storeNullable("EngineHook", UUIDUtil.CODEC, this.engineHook);
        output.putBoolean("MagnetRingEffect", this.magnetRingEffect);
        output.putBoolean("UnlockedNarrator", this.unlockedNarrator);
        output.putBoolean("NebulaPower", this.nebulaPower);
        output.putBoolean("ElytraBoosting", this.isElytraBoosting);
        output.putBoolean("isForbiddenCursed", this.isForbiddenCursed);
        output.putBoolean("isThirstForbiddenCursed", this.isThirstForbiddenCursed);
        output.putInt("SpellstoneCooldown", this.spellstoneCooldown);
        output.putInt("FireImmunityTimer", this.fireImmunityTimer);
        output.putInt("FireImmunityTimerLast", this.fireImmunityTimerLast);
        output.putInt("InBeaconRangeTick", this.inBeaconRange);
        output.putInt("WitherKills", this.witherKills);
        output.putFloat("EtherealShield", this.etherealShield);
        output.putLong("EtheriumShieldTick", this.etheriumShieldTick);
        output.putLong("timeWithCurses", this.timeWithCurses);
        output.putLong("timeWithoutCurses", this.timeWithoutCurses);
        output.putInt("reviveCooldown", this.reviveCooldown);
        output.putInt("annihilatingTick", this.annihilatingTick);
        output.putIntArray("annihilationSweepData", this.annihilationSweepData);
    }

    @Override
    public void deserialize(ValueInput input) {
        this.engineHook = input.read("EngineHook", UUIDUtil.CODEC).orElse(null);
        this.magnetRingEffect = input.getBooleanOr("MagnetRingEffect", false);
        this.unlockedNarrator = input.getBooleanOr("UnlockedNarrator", false);
        this.nebulaPower = input.getBooleanOr("NebulaPower", false);
        this.isElytraBoosting = input.getBooleanOr("ElytraBoosting", false);
        this.isForbiddenCursed = input.getBooleanOr("isForbiddenCursed", false);
        this.isThirstForbiddenCursed = input.getBooleanOr("isThirstForbiddenCursed", false);
        this.spellstoneCooldown = input.getIntOr("SpellstoneCooldown", 0);
        this.fireImmunityTimer = input.getIntOr("FireImmunityTimer", 0);
        this.fireImmunityTimerLast = input.getIntOr("FireImmunityTimerLast", 0);
        this.inBeaconRange = input.getIntOr("InBeaconRangeTick", 0);
        this.witherKills = input.getIntOr("WitherKills", 0);
        this.etherealShield = input.getFloatOr("EtherealShield", 0.0F);
        this.etheriumShieldTick = input.getLongOr("EtheriumShieldTick", 0L);
        this.timeWithCurses = input.getLongOr("timeWithCurses", 0L);
        this.timeWithoutCurses = input.getLongOr("timeWithoutCurses", 0L);
        this.reviveCooldown = input.getIntOr("reviveCooldown", 0);
        this.annihilatingTick = input.getIntOr("annihilatingTick", 0);
        this.annihilationSweepData = input.getIntArray("annihilationSweepData").orElse(new int[0]);
    }

    public CompoundTag save(HolderLookup.Provider provider) {
        return SaveDataHelper.toTag(provider, this::serialize);
    }

    public void load(HolderLookup.Provider provider, @NotNull CompoundTag tag) {
        SaveDataHelper.fromTag(provider, tag, this::deserialize);
    }
}
