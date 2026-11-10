package auviotre.enigmatic.legacy.mixin;

import auviotre.enigmatic.legacy.compat.CompatHandler;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * 26.1.2-port: new, a workaround for a bug in Curios 15.0.0+26.1.2 (found when the Ring of the Seven Curses gave no
 * Fortune in the first in-game test).
 *
 * The Fortune bonus of this mod's curios is ICurioItem.getFortuneLevel(), which Curios is supposed to add to the
 * tool's Fortune level with its own mixin on this same method. That mixin still looks for the call
 * EnchantmentHelper.getItemEnchantmentLevel(Holder, ItemStack); in 26.1.2 the method takes an ItemInstance, so Curios'
 * injection point matches nothing and, being optional, is skipped without an error. No curio gives Fortune.
 *
 * This is Curios' mixin again with the 26.1.2 descriptor, asking Curios' API for the bonus. It only acts for the
 * Curios version known to have the bug (see CompatHandler.curiosFortuneBroken), so that a fixed Curios does not get
 * the bonus added twice. Remove it once Curios is fixed.
 */
@Mixin(ApplyBonusCount.class)
public class MixinApplyBonusCount {
    @Shadow
    @Final
    private Holder<Enchantment> enchantment;

    // index 4 is the local variable that holds the enchantment level (this, stack, context, tool, level), as in Curios' mixin
    @ModifyVariable(
            method = "run",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getItemEnchantmentLevel(Lnet/minecraft/core/Holder;Lnet/minecraft/world/item/ItemInstance;)I",
                    ordinal = 0),
            index = 4
    )
    private int enigmaticlegacy$applyCurioFortune(int enchantmentLevel, ItemStack stack, LootContext lootContext) {
        if (CompatHandler.curiosFortuneBroken() && this.enchantment.is(Enchantments.FORTUNE)
                && lootContext.getOptionalParameter(LootContextParams.THIS_ENTITY) instanceof LivingEntity entity) {
            return enchantmentLevel + CuriosApi.getCuriosInventory(entity).map(handler -> handler.getFortuneLevel(lootContext)).orElse(0);
        }
        return enchantmentLevel;
    }
}
