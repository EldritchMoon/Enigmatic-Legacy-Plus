package auviotre.enigmatic.legacy.contents.item.misc;

import auviotre.enigmatic.legacy.api.item.IItemHelper;
import auviotre.enigmatic.legacy.handlers.TooltipHandler;
import auviotre.enigmatic.legacy.registries.EnigmaticComponents;
import auviotre.enigmatic.legacy.registries.EnigmaticItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

public class StarlightPearl extends Item {
    public StarlightPearl() {
        super(IItemHelper.properties().fireResistant());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> list, TooltipFlag flag) {
        TooltipHandler.line(list, "tooltip.enigmaticlegacy.starlightPearl");
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.PRIMARY && slot.mayPlace(stack) && slot.mayPickup(player) && slot.hasItem()) {
            ItemStack other = slot.getItem();
            if (other.is(EnigmaticItems.ETHERIUM_CORE) || other.is(EnigmaticItems.ETHEREAL_FORGING_CHARM)) {
                boolean boost = other.getOrDefault(EnigmaticComponents.BOOLEAN, false);
                if (!boost) {
                    other.set(EnigmaticComponents.BOOLEAN, true);
                    stack.consume(1, player);
                    return true;
                }
            }
        }
        return super.overrideStackedOnOther(stack, slot, action, player);
    }
}
