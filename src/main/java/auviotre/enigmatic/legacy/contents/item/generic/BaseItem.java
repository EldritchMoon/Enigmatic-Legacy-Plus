package auviotre.enigmatic.legacy.contents.item.generic;

import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.api.item.IItemHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.IItemDecorator;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import org.jetbrains.annotations.Nullable;

public class BaseItem extends Item {
    public BaseItem() {
        super(IItemHelper.properties());
    }

    public BaseItem(Properties properties) {
        super(properties);
    }

    public static BaseItem Unknown() {
        return new BaseItem(IItemHelper.singleProperties().component(DataComponents.CREATIVE_SLOT_LOCK, Unit.INSTANCE)) {
            @Override
            public Component getName(ItemStack stack) {
                return Component.literal("?");
            }

            @Override
            @OnlyIn(Dist.CLIENT)
            public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> list, TooltipFlag flag) {
                list.accept(Component.literal("Coming soon...").withStyle(ChatFormatting.GRAY));
            }

            @Override
            public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
                stack.shrink(1);
            }
        };
    }

    @OnlyIn(Dist.CLIENT)
    public static class WIPDecorator implements IItemDecorator {
        static final Identifier SPRITE = EnigmaticLegacy.location("decorator/wip_warn");

        @Override
        public boolean render(@NotNull GuiGraphicsExtractor graphics, Font font, @NotNull ItemStack stack, int x, int y) {
            graphics.pose().pushMatrix(); // 26.1.2-port: pushPose -> pushMatrix (2D matrix stack)
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITE, x, y, 16, 16); // 26.1.2-port: blitSprite names its pipeline
            graphics.pose().popMatrix();
            return true;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class UnknownDecorator implements IItemDecorator {
        static final Identifier SPRITE = EnigmaticLegacy.location("decorator/unknown");

        @Override
        public boolean render(@NotNull GuiGraphicsExtractor graphics, Font font, @NotNull ItemStack stack, int x, int y) {
            graphics.pose().pushMatrix(); // 26.1.2-port: pushPose -> pushMatrix (2D matrix stack)
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITE, x, y, 16, 16); // 26.1.2-port: blitSprite names its pipeline
            graphics.pose().popMatrix();
            return true;
        }
    }
}
