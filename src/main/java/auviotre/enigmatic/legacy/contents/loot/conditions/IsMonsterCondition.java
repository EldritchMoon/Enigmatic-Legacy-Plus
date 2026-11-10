package auviotre.enigmatic.legacy.contents.loot.conditions;

import auviotre.enigmatic.legacy.registries.EnigmaticLoots;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public record IsMonsterCondition(LootContext.EntityTarget entityTarget) implements LootItemCondition {
    public static final MapCodec<IsMonsterCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(LootContext.EntityTarget.CODEC.fieldOf("entity").forGetter(IsMonsterCondition::entityTarget)).apply(instance, IsMonsterCondition::new));

    public static LootItemCondition.Builder target(LootContext.EntityTarget target) {
        return () -> new IsMonsterCondition(target);
    }

    @Override
    public LootItemConditionType getType() {
        return EnigmaticLoots.IS_MONSTER.get();
    }

    @Override
    public boolean test(LootContext context) {
        Entity entity = context.getParameter(this.entityTarget.contextParam()); // 26.1.2-port: getParam -> getParameter, EntityTarget.getParam -> contextParam
        return entity instanceof Monster;
    }
}
