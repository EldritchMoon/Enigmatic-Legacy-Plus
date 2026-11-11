package auviotre.enigmatic.legacy.contents.item.charms;

import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.client.ClientHelper;
import auviotre.enigmatic.legacy.api.item.IItemHelper;
import auviotre.enigmatic.legacy.contents.item.generic.BaseCurioItem;
import auviotre.enigmatic.legacy.handlers.EnigmaticHandler;
import auviotre.enigmatic.legacy.handlers.TooltipHandler;
import auviotre.enigmatic.legacy.registries.EnigmaticComponents;
import auviotre.enigmatic.legacy.registries.EnigmaticItems;
import auviotre.enigmatic.legacy.registries.EnigmaticSounds;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

public class Insignia extends BaseCurioItem {
    public Insignia() {
        super(IItemHelper.singleProperties().rarity(Rarity.UNCOMMON).component(EnigmaticComponents.BOOLEAN, true));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> list, TooltipFlag flag) {
        TooltipHandler.line(list);
        if (ClientHelper.hasShiftDown()) {
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.insignia1");
            TooltipHandler.line(list);
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.insignia2");
            TooltipHandler.line(list, "tooltip.enigmaticlegacy.insignia3");
        } else TooltipHandler.holdShift(list);
        TooltipHandler.line(list);

        this.getCustomName(stack).ifPresent(component -> TooltipHandler.line(list, "tooltip.enigmaticlegacy.insigniaName", component));

        Component option = stack.getOrDefault(EnigmaticComponents.BOOLEAN, true) ? Component.translatable("tooltip.enigmaticlegacy.enabled") : Component.translatable("tooltip.enigmaticlegacy.disabled");
        TooltipHandler.line(list, "tooltip.enigmaticlegacy.insigniaTagDisplay", option);
    }

    public Optional<Component> getCustomName(ItemStack stack) {
        if (stack.has(DataComponents.CUSTOM_NAME)) {
            return Optional.ofNullable(stack.get(DataComponents.CUSTOM_NAME));
        }
        // 26.1.2-port: every item carries an item_name component by default now (it is where the item's own name
        // comes from), so has(ITEM_NAME) would always be true and every Insignia would count as named "Insignia".
        // Only an item_name set on this particular stack is a custom name, which is what the check meant before.
        Optional<Component> itemName = stack.getComponentsPatch().getPatch(DataComponents.ITEM_NAME);
        if (itemName != null && itemName.isPresent()) {
            return itemName;
        }
        return Optional.empty();
    }

    public boolean canSeeTrueName(@Nullable Player player) {
        return player != null && player.isCreative() || EnigmaticHandler.hasCurio(player, EnigmaticItems.ENIGMATIC_EYE);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player.isCrouching()) {
            ItemStack stack = player.getItemInHand(hand);
            RandomSource random = player.getRandom();
            if (stack.getOrDefault(EnigmaticComponents.BOOLEAN, true)) {
                stack.set(EnigmaticComponents.BOOLEAN, false);
                level.playSound(null, player.blockPosition(), EnigmaticSounds.CHARGED_OFF.get(), SoundSource.PLAYERS, 0.8F + random.nextFloat() * 0.2F, 0.8F + random.nextFloat() * 0.2F);
            } else {
                stack.set(EnigmaticComponents.BOOLEAN, true);
                level.playSound(null, player.blockPosition(), EnigmaticSounds.CHARGED_ON.get(), SoundSource.PLAYERS, 0.8F + random.nextFloat() * 0.2F, 0.8F + random.nextFloat() * 0.2F);
            }
            player.swing(hand);
            return InteractionResult.SUCCESS;
        }
        return super.use(level, player, hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() == null) return InteractionResult.PASS;
        return this.use(context.getLevel(), context.getPlayer(), context.getHand()); // 26.1.2-port: use() returns the InteractionResult itself
    }

    @Override
    public boolean canEquipFromUse(SlotContext context, ItemStack stack) {
        return false;
    }

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext context, Identifier id, ItemStack stack) {
        Multimap<Holder<Attribute>, AttributeModifier> attributes = HashMultimap.create();
        CuriosApi.addSlotModifier(attributes, "charm", IItemHelper.getLocation(this), 1.0, AttributeModifier.Operation.ADD_VALUE);
        return attributes;
    }

    /** The small "(real name)" line under an Insignia name tag; only set for viewers who may see it. */
    public static final ContextKey<Component> TRUE_NAME = new ContextKey<>(EnigmaticLegacy.location("insignia_true_name"));

    // 26.1.2-port: this and submitTrueName() replace renderInsigniaNameplate(), which MixinEntityRenderer called in
    // place of EntityRenderer.renderNameTag(). That method is gone: the name tag is a field of the render state, and
    // vanilla draws it. So the decision is made here, while the player's render state is filled (registered in
    // ClientSetupHandler), by changing state.nameTag: the custom name, or the real name for a viewer who may see it
    // when the custom name is blank, or nothing at all. Same rules as before. The below-name scoreboard line is
    // cleared, because the old injector replaced that call too.
    @OnlyIn(Dist.CLIENT)
    public static void extractNameplate(LivingEntity entity, EntityRenderState state) {
        if (state.nameTag == null || !(entity instanceof Player player)) return;
        ItemStack insignia = EnigmaticHandler.getCurio(player, EnigmaticItems.INSIGNIA);
        if (insignia.isEmpty()) return;
        Optional<Component> customName = EnigmaticItems.INSIGNIA.get().getCustomName(insignia);
        if (customName.isEmpty()) return;
        Player viewer = EnigmaticLegacy.PROXY.getClientPlayer();
        boolean seesTrueName = entity != viewer && EnigmaticItems.INSIGNIA.get().canSeeTrueName(viewer);
        state.scoreText = null;
        if (Minecraft.getInstance().font.width(customName.get()) > 0) {
            state.nameTag = customName.get();
            if (seesTrueName) state.setRenderData(TRUE_NAME, Component.literal("(" + entity.getDisplayName().getString() + ")"));
        } else {
            state.nameTag = seesTrueName ? entity.getDisplayName() : null;
        }
    }

    // 26.1.2-port: the second, smaller line. Called from ClientEventHandler for RenderNameTagEvent.DoRender, i.e. just
    // before vanilla submits the main line. submitNameTag() does the translate/rotate/scale itself, so the old extra
    // scale(0.4) is applied in front of it and the attachment point is divided by 0.4 to stay where it was.
    @OnlyIn(Dist.CLIENT)
    public static void submitTrueName(RenderNameTagEvent.DoRender event) {
        EntityRenderState state = event.getEntityRenderState();
        Component trueName = state.getRenderData(TRUE_NAME);
        Vec3 attachment = state.nameTagAttachment;
        if (trueName == null || attachment == null || event.getContent() != state.nameTag) return;
        float scale = 0.4F;
        int offset = (int) (-10 * (1.0F / scale));
        PoseStack stack = event.getPoseStack();
        stack.pushPose();
        stack.scale(scale, scale, scale);
        Vec3 scaledAttachment = new Vec3(attachment.x / scale, (attachment.y + 0.5) / scale - 0.5, attachment.z / scale);
        event.getSubmitNodeCollector().submitNameTag(stack, scaledAttachment, -offset, trueName, !state.isDiscrete, state.lightCoords, state.distanceToCameraSq, event.getCameraRenderState());
        stack.popPose();
    }
}
