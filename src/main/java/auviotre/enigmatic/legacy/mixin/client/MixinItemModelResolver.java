package auviotre.enigmatic.legacy.mixin.client;

import auviotre.enigmatic.legacy.api.item.IItemHelper;
import auviotre.enigmatic.legacy.registries.EnigmaticItems;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// 26.1.2-port: replaces MixinItemRenderer. ItemRenderer, ItemModelShaper and BakedModel are gone; an item's model is
// now looked up by the id in its item_model component, in ItemModelResolver.appendItemLayers(). The items on the
// "unknown" list are drawn as the Unknown item by answering that lookup with the Unknown item's model id (its
// registry id, as it does not override the component).
@OnlyIn(Dist.CLIENT)
@Mixin(ItemModelResolver.class)
public class MixinItemModelResolver {
    @WrapOperation(method = "appendItemLayers", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
    private Object useUnknownModel(ItemStack stack, DataComponentType<?> type, Operation<Object> original) {
        if (type == DataComponents.ITEM_MODEL && IItemHelper.UNKNOWN_ITEMS.stream().anyMatch(itemLike -> stack.is(itemLike.asItem()))) {
            return EnigmaticItems.UNKNOWN.getId();
        }
        return original.call(stack, type);
    }
}
