package auviotre.enigmatic.legacy.contents.item.generic;

import auviotre.enigmatic.legacy.handlers.TooltipHandler;
import auviotre.enigmatic.legacy.registries.EnigmaticComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

public class BaseCursedItem extends BaseItem {
    public BaseCursedItem(Properties properties) {
        super(properties.component(EnigmaticComponents.CURSED, true));
    }

    public BaseCursedItem(Properties properties, boolean betrayal) {
        super(properties.component(EnigmaticComponents.CURSED, true).component(EnigmaticComponents.BLESSED, betrayal));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> list, TooltipFlag flag) {
        TooltipHandler.cursedOnly(list, stack);
    }
}
