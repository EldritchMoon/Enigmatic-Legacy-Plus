package auviotre.enigmatic.legacy.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

/**
 * 26.1.2-port: new. First-person position of the mod's blocking items while they are being used.
 *
 * For the BLOCK use animation, ItemInHandRenderer now tilts every item that is not a ShieldItem the way a blocking
 * sword is held (a translate plus rotations of -102.25, 13.365 and 78.05 degrees), after moving it to the arm. The
 * mod's blocking items are not ShieldItems, and their models already contain the first-person blocking transform
 * they were drawn for (infernal_shield_blocking_in_hand, etherium_sword_blocking, 3d/annihilating_sword_blocking,
 * spellstone_sword/void_pearl_blocking), so with the extra tilt the Infernal Shield was turned out of view.
 *
 * This takes over through NeoForge's applyForgeHandTransform and does only the "move to the arm" step
 * (ItemInHandRenderer.applyItemArmTransform, which is private), leaving the rest to the model. Third person is not
 * touched: it uses the arm pose, not this.
 */
public class BlockingHandTransform implements IClientItemExtensions {
    @Override
    public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack stack, float partialTick, float equipProcess, float swingProcess) {
        return apply(poseStack, player, arm, stack, equipProcess);
    }

    /** @return true if the item in this arm is being used with the BLOCK animation and the transform was applied */
    public static boolean apply(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack stack, float equipProcess) {
        InteractionHand hand = arm == player.getMainArm() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        // the same test ItemInHandRenderer makes before it looks at the use animation
        if (!player.isUsingItem() || player.getUseItemRemainingTicks() <= 0 || player.getUsedItemHand() != hand) return false;
        if (stack.getUseAnimation() != ItemUseAnimation.BLOCK) return false;
        int invert = arm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate(invert * 0.56F, -0.52F + equipProcess * -0.6F, -0.72F);
        return true;
    }
}
