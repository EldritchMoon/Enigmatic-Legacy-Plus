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
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

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

    /**
     * 26.1.2-port: the old SwordItem.createToolProperties() for items that are not full swords:
     * mines cobwebs fast, sword-efficient blocks at 1.5x, 2 durability per block.
     */
    static Tool swordTool() {
        HolderGetter<Block> blocks = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
        return new Tool(List.of(
                Tool.Rule.minesAndDrops(HolderSet.direct(Blocks.COBWEB.builtInRegistryHolder()), 15.0F),
                Tool.Rule.overrideSpeed(blocks.getOrThrow(BlockTags.SWORD_EFFICIENT), 1.5F)
        ), 1.0F, 2, false);
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
