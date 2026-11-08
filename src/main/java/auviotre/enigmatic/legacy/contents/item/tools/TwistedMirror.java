package auviotre.enigmatic.legacy.contents.item.tools;

import net.minecraft.world.InteractionResult;
import auviotre.enigmatic.legacy.client.ClientHelper;
import auviotre.enigmatic.legacy.api.item.IItemHelper;
import auviotre.enigmatic.legacy.contents.item.generic.BaseCursedItem;
import auviotre.enigmatic.legacy.contents.item.potions.RecallPotion;
import auviotre.enigmatic.legacy.handlers.EnigmaticHandler;
import auviotre.enigmatic.legacy.handlers.TooltipHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

public class TwistedMirror extends BaseCursedItem {
    public TwistedMirror() {
        super(IItemHelper.singleProperties().rarity(Rarity.UNCOMMON));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> list, TooltipFlag flag) {
        if (ClientHelper.hasShiftDown()) {
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.twistedMirror1");
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.twistedMirror2");
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.twistedMirror3");
        } else TooltipHandler.holdShift(list);
        TooltipHandler.line(list);
        TooltipHandler.cursedOnly(list, stack);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!EnigmaticHandler.canUse(player, stack) || player.getCooldowns().isOnCooldown(this.getDefaultInstance()))
            return InteractionResult.PASS;
        player.startUsingItem(hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            RecallPotion.teleportToRespawnPoint(player, ParticleTypes.WITCH);
            player.getCooldowns().addCooldown(this.getDefaultInstance(), player.hasInfiniteMaterials() ? 20 : 250);
        }
        return stack;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 18;
    }
}
