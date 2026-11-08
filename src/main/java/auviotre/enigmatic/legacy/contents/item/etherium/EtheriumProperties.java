package auviotre.enigmatic.legacy.contents.item.etherium;

import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.contents.attribute.EtheriumShieldAttribute;
import auviotre.enigmatic.legacy.registries.EnigmaticAttributes;
import auviotre.enigmatic.legacy.registries.EnigmaticSounds;
import auviotre.enigmatic.legacy.registries.EnigmaticTags;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface EtheriumProperties {
    // 26.1.2-port: Tier/SimpleTier became the ToolMaterial record; repair items are a tag now.
    ToolMaterial TIER = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2794, 8.0F, 5F, 24, EnigmaticTags.Items.REPAIRS_ETHERIUM);

    // 26.1.2-port: armor materials are plain records (no registry). The worn texture comes from the
    // equipment asset assets/enigmaticlegacyplus/equipment/etherium.json. Durability 132 is the old
    // type.getDurability(132) multiplier.
    ResourceKey<EquipmentAsset> ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, EnigmaticLegacy.location("etherium"));
    ArmorMaterial MATERIAL = new ArmorMaterial(132, Map.of(
            ArmorType.HELMET, 4,
            ArmorType.CHESTPLATE, 9,
            ArmorType.LEGGINGS, 7,
            ArmorType.BOOTS, 4,
            ArmorType.BODY, 12
    ), 24, EnigmaticSounds.ARMOR_EQUIP_ETHERIUM, 4.0F, 0.0F, EnigmaticTags.Items.REPAIRS_ETHERIUM, ASSET);

    static ItemAttributeModifiers createAttributes(float attackDamage, float attackSpeed, float threshold) {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, attackDamage + TIER.attackDamageBonus(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(EnigmaticAttributes.ETHERIUM_SHIELD, new AttributeModifier(EtheriumShieldAttribute.BASE_ID, threshold, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build();
    }

    static double getShieldThreshold(@NotNull LivingEntity entity) {
        AttributeInstance attribute = entity.getAttribute(EnigmaticAttributes.ETHERIUM_SHIELD);
        if (attribute == null) return 0;
        return attribute.getValue();
    }

    static boolean hasShield(@NotNull LivingEntity entity) {
        return entity.getMaxHealth() * getShieldThreshold(entity) > entity.getHealth();
    }
}
