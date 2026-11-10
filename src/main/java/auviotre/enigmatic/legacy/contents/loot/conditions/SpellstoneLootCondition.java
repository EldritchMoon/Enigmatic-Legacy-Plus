package auviotre.enigmatic.legacy.contents.loot.conditions;

import auviotre.enigmatic.legacy.handlers.EnigmaticHandler;
import auviotre.enigmatic.legacy.registries.EnigmaticItems;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

public record SpellstoneLootCondition(float baseChance, float bonusChance) implements LootItemCondition {
    public static final MapCodec<SpellstoneLootCondition> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            Codec.floatRange(0.0F, 1.0F).fieldOf("baseChance").forGetter(SpellstoneLootCondition::baseChance),
            Codec.floatRange(0.0F, 1.0F).fieldOf("bonusChance").forGetter(SpellstoneLootCondition::bonusChance)
    ).apply(instance, SpellstoneLootCondition::new));

    public static LootItemCondition.Builder chance(float base, float bonus) {
        return () -> new SpellstoneLootCondition(base, bonus);
    }

    // 26.1.2-port: LootItemConditionType was removed; a condition returns its MapCodec directly (was getType())
    @Override
    public @NotNull MapCodec<SpellstoneLootCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(@NotNull LootContext context) {
        float chance = this.baseChance;
        if (context.getOptionalParameter(LootContextParams.THIS_ENTITY) instanceof LivingEntity entity) { // 26.1.2-port: getParamOrNull -> getOptionalParameter
            if (EnigmaticHandler.hasCurio(entity, EnigmaticItems.SPELLTUNER)) {
                chance += this.bonusChance;
            }
        }
        return context.getRandom().nextFloat() < chance;
    }
}
