package auviotre.enigmatic.legacy.contents.item.generic;

import net.minecraft.world.InteractionResult;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;
import net.minecraft.util.Mth;

public class BaseDrinkableItem extends BaseItem {

    public BaseDrinkableItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, @NotNull Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (this.canDrink(level, player, stack))
            return ItemUtils.startUsingInstantly(level, player, hand);
        return InteractionResult.PASS;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player) {
            ItemStack remainingItem = stack.getCraftingRemainingItem();
            this.onConsumed(level, player, stack);

            if (player instanceof ServerPlayer serverPlayer) CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);

            player.awardStat(Stats.ITEM_USED.get(this));
            stack.consume(1, player);

            if (!player.hasInfiniteMaterials()) {
                if (stack.isEmpty()) return remainingItem;
                player.getInventory().add(remainingItem);
            }
        }
        entity.gameEvent(GameEvent.DRINK);
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.DRINK;
    }

    // 26.1.2-port: getEatingSound/getDrinkingSound were removed and vanilla only plays use sounds for items with a
    // consumable component, so the drinking sound is played here on the same schedule as before.
    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingTicks) {
        playDrinkSound(entity, remainingTicks, this.getUseDuration(stack, entity), SoundEvents.GENERIC_DRINK);
    }

    public static void playDrinkSound(LivingEntity entity, int remainingTicks, int useDuration, SoundEvent sound) {
        if (useDuration - remainingTicks > (int) (useDuration * 0.21875F) && remainingTicks % 4 == 0)
            entity.playSound(sound, 0.5F, Mth.randomBetween(entity.getRandom(), 0.9F, 1.0F));
    }

    public boolean canDrink(Level level, Player player, ItemStack stack) {
        return true;
    }

    public void onConsumed(Level level, @NotNull Player player, ItemStack stack) {
    }
}
