package auviotre.enigmatic.legacy.contents.item.materials;

import auviotre.enigmatic.legacy.api.item.IItemHelper;
import auviotre.enigmatic.legacy.api.item.ITaintable;
import auviotre.enigmatic.legacy.contents.item.generic.BaseCursedItem;
import auviotre.enigmatic.legacy.handlers.EnigmaticHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import org.jetbrains.annotations.Nullable;

public class TwistedHeart extends BaseCursedItem implements ITaintable {
    public TwistedHeart() {
        super(IItemHelper.singleProperties().rarity(Rarity.UNCOMMON));
    }

    @Override
    public boolean canTaint(Player player) {
        return EnigmaticHandler.isTheCursedOne(player);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (entity instanceof Player player && !level.isClientSide())
            this.handleTaintable(stack, player);
    }
}
