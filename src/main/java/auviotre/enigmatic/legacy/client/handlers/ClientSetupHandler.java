package auviotre.enigmatic.legacy.client.handlers;

import auviotre.enigmatic.legacy.EnigmaticLegacy;
import auviotre.enigmatic.legacy.api.item.IItemHelper;
import auviotre.enigmatic.legacy.client.BlockingHandTransform;
import auviotre.enigmatic.legacy.client.ELItemModelProperties;
import auviotre.enigmatic.legacy.client.InsigniaNameplate;
import auviotre.enigmatic.legacy.client.KeyHandler;
import auviotre.enigmatic.legacy.client.particles.IchorParticle;
import auviotre.enigmatic.legacy.client.particles.StarDustParticle;
import auviotre.enigmatic.legacy.client.particles.StarlightParticle;
import auviotre.enigmatic.legacy.client.renderer.*;
import auviotre.enigmatic.legacy.client.renderer.layer.EnigmaticElytraLayer;
import auviotre.enigmatic.legacy.client.renderer.layer.EtheriumShieldLayer;
import auviotre.enigmatic.legacy.client.renderer.model.EngineHookModel;
import auviotre.enigmatic.legacy.client.renderer.model.ExplorerMarkerModel;
import auviotre.enigmatic.legacy.client.renderer.model.IchorSpriteModel;
import auviotre.enigmatic.legacy.client.renderer.model.SpellstoneModel;
import auviotre.enigmatic.legacy.client.screen.AntiqueBagScreen;
import auviotre.enigmatic.legacy.client.screen.LoreInscriberScreen;
import auviotre.enigmatic.legacy.client.screen.SpellstoneTableScreen;
import auviotre.enigmatic.legacy.client.screen.button.EnderChestInventoryButton;
import auviotre.enigmatic.legacy.client.screen.button.MagnetRingInventoryButton;
import auviotre.enigmatic.legacy.contents.item.charms.HellBladeCharm;
import auviotre.enigmatic.legacy.contents.item.generic.BaseItem;
import auviotre.enigmatic.legacy.contents.item.spellstones.IllusionLantern;
import auviotre.enigmatic.legacy.contents.item.spellstones.other.SpellstoneSword;
import auviotre.enigmatic.legacy.contents.item.spellstones.other.Spelltuner;
import auviotre.enigmatic.legacy.contents.item.tools.AnnihilatingSword;
import auviotre.enigmatic.legacy.contents.item.tools.TotemOfMalice;
import auviotre.enigmatic.legacy.registries.*;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.particle.EndRodParticle;
import net.minecraft.client.particle.SpellParticle;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.level.ItemLike;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.renderstate.AvatarRenderStateModifier;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.client.ICuriosScreen;

@Mod(value = EnigmaticLegacy.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = EnigmaticLegacy.MODID, value = Dist.CLIENT)
public class ClientSetupHandler {
    @SubscribeEvent(priority = EventPriority.LOW)
    private static void onInventoryGuiInit(ScreenEvent.Init.@NotNull Post event) {
        Screen screen = event.getScreen();
        if (screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen || screen instanceof ICuriosScreen) {
            AbstractContainerScreen<?> gui = (AbstractContainerScreen<?>) screen;
            boolean isCreative = screen instanceof CreativeModeInventoryScreen;
            event.addListener(MagnetRingInventoryButton.getInstance(gui, isCreative));
            event.addListener(EnderChestInventoryButton.getInstance(gui, isCreative));
        }
    }

    @SubscribeEvent
    private static void registerItemDecorator(@NotNull RegisterItemDecorationsEvent event) {
        event.register(EnigmaticItems.HELL_BLADE_CHARM, new HellBladeCharm.Decorator());
        event.register(EnigmaticItems.TOTEM_OF_MALICE, new TotemOfMalice.Decorator());
        event.register(EnigmaticItems.ILLUSION_LANTERN, new IllusionLantern.Decorator());
        event.register(EnigmaticItems.SPELLSTONE_SWORD, new SpellstoneSword.Decorator());
        event.register(EnigmaticItems.ANNIHILATING_SWORD, new AnnihilatingSword.Decorator());
        for (ItemLike item : IItemHelper.UNKNOWN_ITEMS) event.register(item, new BaseItem.UnknownDecorator());
        event.register(EnigmaticBlocks.STARLIGHT_BLOCK, new BaseItem.WIPDecorator());
    }

    // 26.1.2-port: was registerItemColor(RegisterColorHandlersEvent.Item), which is gone. Item colours are tint sources
    // named by the item model definition; the Spelltuner's is ELItemModelProperties.SpelltunerTint.
    @SubscribeEvent
    private static void registerItemTintSources(RegisterColorHandlersEvent.@NotNull ItemTintSources event) {
        ELItemModelProperties.registerTintSources(event);
    }

    @SubscribeEvent
    private static void registerScreens(@NotNull RegisterMenuScreensEvent event) {
        event.register(EnigmaticMenus.ANTIQUE_BAG_MENU.get(), AntiqueBagScreen::new);
        event.register(EnigmaticMenus.LORE_INSCRIBER_MENU.get(), LoreInscriberScreen::new);
        event.register(EnigmaticMenus.SPELLSTONE_TABLE_MENU.get(), SpellstoneTableScreen::new);
    }

    @SubscribeEvent
    private static void registerParticleProvider(@NotNull RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(EnigmaticParticles.SPELL.get(), SpellParticle.MobEffectProvider::new);
        event.registerSpriteSet(EnigmaticParticles.ICHOR.get(), IchorParticle.Provider::new);
        event.registerSpriteSet(EnigmaticParticles.ICHOR_CURSE.get(), SpellParticle.Provider::new);
        event.registerSpriteSet(EnigmaticParticles.VIOLENCE_CURSE.get(), SpellParticle.Provider::new);
        event.registerSpriteSet(EnigmaticParticles.ETHER.get(), EndRodParticle.Provider::new);
        event.registerSpriteSet(EnigmaticParticles.STARLIGHT.get(), StarlightParticle.Provider::new);
        event.registerSpriteSet(EnigmaticParticles.BLUE_STAR_DUST.get(), StarDustParticle.Provider::new);
        event.registerSpriteSet(EnigmaticParticles.RED_STAR_DUST.get(), StarDustParticle.Provider::new);
        event.registerSpriteSet(EnigmaticParticles.PURPLE_STAR_DUST.get(), StarDustParticle.Provider::new);
    }

    @SubscribeEvent
    private static void registerEntityRenderers(EntityRenderersEvent.@NotNull RegisterRenderers event) {
        event.registerEntityRenderer(EnigmaticEntities.PERMANENT_ITEM_ENTITY.get(), PermanentItemRenderer::new);
        event.registerEntityRenderer(EnigmaticEntities.COBWEB_BALL.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(EnigmaticEntities.DRAGON_BREATH_ARROW.get(), DragonBreathArrowRender::new);
        event.registerEntityRenderer(EnigmaticEntities.THROWN_ICHOR_SPEAR.get(), ThrownIchorSpearRenderer::new);
        event.registerEntityRenderer(EnigmaticEntities.ICHOR_SPRITE.get(), IchorSpriteRenderer::new);
        event.registerEntityRenderer(EnigmaticEntities.SOUL_FLAME_BALL.get(), SoulFlameBallRenderer::new);
        event.registerEntityRenderer(EnigmaticEntities.ULTIMATE_WITHER_SKULL.get(), UltimateWitherSkullRenderer::new);
        event.registerEntityRenderer(EnigmaticEntities.EXPLORER_MARKER.get(), ExplorerMarkerRender::new);
        event.registerEntityRenderer(EnigmaticEntities.STARLIGHT_METEOR.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(EnigmaticEntities.ABYSS_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(EnigmaticEntities.ENGINE_HOOK.get(), EngineHookRenderer::new);
        event.registerEntityRenderer(EnigmaticEntities.ANGEL_BEAM.get(), AngelBeamRenderer::new);
        event.registerEntityRenderer(EnigmaticEntities.PIGLIN_WANDERER.get(), context -> new PiglinWandererRender(context, ModelLayers.PIGLIN_BRUTE, ModelLayers.PIGLIN_BRUTE_ARMOR)); // 26.1.2-port: inner/outer armour layers -> one ArmorModelSet
        event.registerBlockEntityRenderer(EnigmaticBlockEntities.DIMENSIONAL_ANCHOR_ENTITY.get(), DimensionalAnchorRender::new);
        event.registerBlockEntityRenderer(EnigmaticBlockEntities.SPELLSTONE_TABLE_ENTITY.get(), SpellstoneTableRender::new);
    }

    @SubscribeEvent
    private static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SpellstoneModel.LAYER, SpellstoneModel::createLayer);
        event.registerLayerDefinition(ExplorerMarkerModel.LAYER, ExplorerMarkerModel::createLayer);
        event.registerLayerDefinition(EngineHookModel.LAYER, EngineHookModel::createLayer);
        event.registerLayerDefinition(IchorSpriteModel.LAYER, IchorSpriteModel::createBodyLayer);
    }

    @SubscribeEvent
    private static void registerKeybindings(@NotNull RegisterKeyMappingsEvent event) {
        event.register(KeyHandler.SPELLSTONE.get());
        event.register(KeyHandler.SCROLL.get());
        event.register(KeyHandler.ENDER_RING.get());
    }

    @SubscribeEvent
    private static void registerClientExtensions(@NotNull RegisterClientExtensionsEvent event) {
        event.registerItem(new SpellstoneSword.ClientExtension(), EnigmaticItems.SPELLSTONE_SWORD);
        // 26.1.2-port: the other items that use the BLOCK animation; see BlockingHandTransform. The Spellstone Sword
        // calls it from its own extension.
        event.registerItem(new BlockingHandTransform(), EnigmaticItems.INFERNAL_SHIELD, EnigmaticItems.ETHERIUM_SWORD,
                EnigmaticItems.ANNIHILATING_SWORD, EnigmaticItems.SACRED_CHALICE);
    }

    // 26.1.2-port: PlayerSkin.Model -> PlayerModelType and getSkin() -> getPlayerRenderer(). The event hands out typed
    // renderers now, so the raw LivingEntityRenderer casts (and their @SuppressWarnings) are gone.
    @SubscribeEvent
    private static void addLayers(EntityRenderersEvent.AddLayers event) {
        addPlayerLayer(event, PlayerModelType.WIDE);
        addPlayerLayer(event, PlayerModelType.SLIM);
        if (event.getRenderer(EntityType.ARMOR_STAND) instanceof ArmorStandRenderer renderer) {
            renderer.addLayer(new EnigmaticElytraLayer<>(renderer, event.getEntityModels()));
        }
    }

    private static void addPlayerLayer(EntityRenderersEvent.AddLayers event, PlayerModelType skin) {
        AvatarRenderer<AbstractClientPlayer> renderer = event.getPlayerRenderer(skin);
        if (renderer != null) {
            renderer.addLayer(new EnigmaticElytraLayer<>(renderer, event.getEntityModels()));
            renderer.addLayer(new EtheriumShieldLayer(renderer, event.getEntityModels()));
        }
    }

    // 26.1.2-port: new. Layers cannot see the entity any more, so what the two layers above need from it is copied into
    // the render state here (NeoForge calls these after vanilla has filled the state, once per entity per frame).
    @SubscribeEvent
    private static void registerRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerAvatarEntityModifier(new AvatarRenderStateModifier() {
            @Override
            public <T extends Avatar & ClientAvatarEntity> void accept(T avatar, AvatarRenderState state) {
                EnigmaticElytraLayer.extractElytra(avatar, state);
                EtheriumShieldLayer.extractShield(avatar, state);
                InsigniaNameplate.extractNameplate(avatar, state);
            }
        });
        event.registerEntityModifier(ArmorStandRenderer.class, (ArmorStand stand, ArmorStandRenderState state) -> EnigmaticElytraLayer.extractElytra(stand, state));
    }

    // 26.1.2-port: new. ItemProperties is gone; the properties that pick item models are registered types that item
    // model definitions refer to by id (see ELItemModelProperties for the list and what each one replaces).
    @SubscribeEvent
    private static void registerConditionalItemModelProperties(RegisterConditionalItemModelPropertyEvent event) {
        ELItemModelProperties.registerConditional(event);
    }

    @SubscribeEvent
    private static void registerRangeSelectItemModelProperties(RegisterRangeSelectItemModelPropertyEvent event) {
        ELItemModelProperties.registerRangeSelect(event);
    }
}
