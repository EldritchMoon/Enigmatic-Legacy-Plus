package auviotre.enigmatic.legacy.proxy;

import auviotre.enigmatic.legacy.registries.EnigmaticItems;
import auviotre.enigmatic.legacy.registries.EnigmaticPotions;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.ProjectileDispenseBehavior;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.jetbrains.annotations.Nullable;

public class CommonProxy {
    public void init() {
        DispenserBlock.registerBehavior(EnigmaticItems.ICHOR_SPEAR, new ProjectileDispenseBehavior(EnigmaticItems.ICHOR_SPEAR.asItem()) {
            @Override
            protected void playSound(BlockSource blockSource) {
                blockSource.level().playSound(null, blockSource.pos(), SoundEvents.TRIDENT_THROW.value(), SoundSource.BLOCKS, 1.0F, 1.2F);
            }
        });
        EnigmaticPotions.ULTIMATE_POTIONS.add(EnigmaticPotions.ULTIMATE_NIGHT_VISION);
        EnigmaticPotions.ULTIMATE_POTIONS.add(EnigmaticPotions.ULTIMATE_INVISIBILITY);
        EnigmaticPotions.ULTIMATE_POTIONS.add(EnigmaticPotions.ULTIMATE_LEAPING);
        EnigmaticPotions.ULTIMATE_POTIONS.add(EnigmaticPotions.ULTIMATE_FIRE_RESISTANCE);
        EnigmaticPotions.ULTIMATE_POTIONS.add(EnigmaticPotions.ULTIMATE_SWIFTNESS);
        EnigmaticPotions.ULTIMATE_POTIONS.add(EnigmaticPotions.ULTIMATE_SLOWNESS);
        EnigmaticPotions.ULTIMATE_POTIONS.add(EnigmaticPotions.ULTIMATE_TURTLE_MASTER);
        EnigmaticPotions.ULTIMATE_POTIONS.add(EnigmaticPotions.ULTIMATE_WATER_BREATHING);
        EnigmaticPotions.ULTIMATE_POTIONS.add(EnigmaticPotions.ULTIMATE_HEALING);
        EnigmaticPotions.ULTIMATE_POTIONS.add(EnigmaticPotions.ULTIMATE_HARMING);
        EnigmaticPotions.ULTIMATE_POTIONS.add(EnigmaticPotions.ULTIMATE_POISON);
        EnigmaticPotions.ULTIMATE_POTIONS.add(EnigmaticPotions.ULTIMATE_REGENERATION);
        EnigmaticPotions.ULTIMATE_POTIONS.add(EnigmaticPotions.ULTIMATE_STRENGTH);
        EnigmaticPotions.ULTIMATE_POTIONS.add(EnigmaticPotions.ULTIMATE_WEAKNESS);
        EnigmaticPotions.ULTIMATE_POTIONS.add(EnigmaticPotions.ULTIMATE_LUCK);
        EnigmaticPotions.ULTIMATE_POTIONS.add(EnigmaticPotions.ULTIMATE_SLOW_FALLING);
    }

    public void clientInit() {

    }

    /**
     * Whether the vanilla "Operator Items" tab should expose the debug-only items.
     * <p>
     * This default implementation is safe on a dedicated server: it never touches
     * {@code net.minecraft.client.Minecraft}, so that client-only class is not resolved
     * server-side. The real option lookup lives in {@link ClientProxy}.
     *
     * @return {@code false} on the server, which matches the vanilla default for
     * {@code operatorItemsTab}.
     */
    public boolean isOperatorItemsTabEnabled() {
        return false;
    }

    public void displayPermanentDeathScreen() {
    }

    public String getClientUsername() {
        return null;
    }

    /**
     * 26.1.2-port: new. The player on this client, or null on a dedicated server.
     * <p>
     * Common code must use this and not {@code Minecraft.getInstance().player}: that field is a LocalPlayer, a class
     * a dedicated server does not have, and NeoForge 26.1.2 no longer strips {@code @OnlyIn(Dist.CLIENT)} methods
     * out of classes there. The return type is the common Player on purpose.
     */
    public @Nullable Player getClientPlayer() {
        return null;
    }

    /** 26.1.2-port: new. The level on this client, or null on a dedicated server; see {@link #getClientPlayer()}. */
    public @Nullable Level getClientLevel() {
        return null;
    }
}
