package auviotre.enigmatic.legacy.api.item;

import auviotre.enigmatic.legacy.registries.RegistryIds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public interface IItemHelper {
    List<ItemLike> UNKNOWN_ITEMS = new ArrayList<>();

    // 26.1.2-port: Properties must carry the item ID before the item is built (see RegistryIds).
    static Item.Properties properties() {
        return new Item.Properties().setId(RegistryIds.currentItem());
    }

    static Item.Properties properties(int size) {
        return properties().stacksTo(size);
    }

    static Item.Properties singleProperties() {
        return properties().stacksTo(1);
    }

    /** 26.1.2-port: block items keep the old "block.modid.name" translation key. */
    static Item.Properties blockItemProperties() {
        return properties().useBlockDescriptionPrefix();
    }

    static @NotNull ItemAttributeModifiers createAttributes(float attackDamage, float attackSpeed) {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, attackDamage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build();
    }

    static Identifier getLocation(ItemLike item) {
        return BuiltInRegistries.ITEM.getKey(item.asItem());
    }
}
