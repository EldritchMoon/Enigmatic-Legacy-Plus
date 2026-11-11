package auviotre.enigmatic.legacy.compat;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import auviotre.enigmatic.legacy.EnigmaticLegacy;

public class CompatHandler {
    private static final CompatHandler INSTANCE = new CompatHandler();

    public static CompatHandler getInstance() {
        return INSTANCE;
    }

    public static boolean isLoaded(String id) {
        return ModList.get().isLoaded(id);
    }

    // 26.1.2-port: new, used by MixinApplyBonusCount (which explains the Curios bug). The check is by exact version on
    // purpose: with any other Curios the workaround stays off, and the log line says which way it went.
    private static final String CURIOS_VERSION_WITHOUT_FORTUNE = "15.0.0+26.1.2";
    private static Boolean curiosFortuneBroken;

    public static boolean curiosFortuneBroken() {
        if (curiosFortuneBroken == null) {
            String version = ModList.get().getModContainerById("curios").map(container -> container.getModInfo().getVersion().toString()).orElse("");
            curiosFortuneBroken = CURIOS_VERSION_WITHOUT_FORTUNE.equals(version);
            if (curiosFortuneBroken) {
                EnigmaticLegacy.LOGGER.info("Curios {} does not apply Fortune bonuses from curios; Enigmatic Legacy+ is applying them instead.", version);
            } else {
                EnigmaticLegacy.LOGGER.info("Curios {} is not the version with the known Fortune bug, so the Enigmatic Legacy+ workaround is off. If curios give no Fortune, see MixinApplyBonusCount.", version);
            }
        }
        return curiosFortuneBroken;
    }

    // 26.1.2-port: new, used by MixinCurioStacksHandler (which explains the Curios bug). Same exact-version rule as
    // the Fortune workaround above.
    private static Boolean curiosLoginSlotsBroken;
    private static boolean markCurioSlotsLoadedFailed;

    public static boolean curiosLoginSlotsBroken() {
        if (curiosLoginSlotsBroken == null) {
            String version = ModList.get().getModContainerById("curios").map(container -> container.getModInfo().getVersion().toString()).orElse("");
            curiosLoginSlotsBroken = CURIOS_VERSION_WITHOUT_FORTUNE.equals(version);
            if (curiosLoginSlotsBroken) {
                EnigmaticLegacy.LOGGER.info("Curios {} empties modifier-added curio slots when a player joins; Enigmatic Legacy+ is sizing the slots early instead.", version);
            } else {
                EnigmaticLegacy.LOGGER.info("Curios {} is not the version with the known slot bug on join, so the Enigmatic Legacy+ workaround is off. If curios in unlocked slots return to the inventory on join, see MixinCurioStacksHandler.", version);
            }
        }
        return curiosLoginSlotsBroken;
    }

    /** Calls CurioStacksHandler.setDataLoaded(), which is public but not part of the Curios API jar. */
    public static void markCurioSlotsLoaded(Object stacksHandler) {
        try {
            stacksHandler.getClass().getMethod("setDataLoaded").invoke(stacksHandler);
        } catch (ReflectiveOperationException exception) {
            if (!markCurioSlotsLoadedFailed) {
                markCurioSlotsLoadedFailed = true;
                EnigmaticLegacy.LOGGER.error("Could not size curio slots early; curios in unlocked slots will return to the inventory on join (see MixinCurioStacksHandler).", exception);
            }
        }
    }

    public void register(IEventBus modEventBus) {
        // TODO(port): the AppleSkin and Thirst handlers are excluded from the build until those mods are on 26.1.2
        // (phase 9, see build.gradle); restore these registrations and the two imports with them.
        // if (isLoaded("appleskin") && FMLEnvironment.getDist().isClient()) {
        //     EnigmaticLegacy.LOGGER.info("Appleskin Compat.");
        //     NeoForge.EVENT_BUS.register(new AppleSkinCompatHandler());
        // }
        // if (isLoaded("thirst")) {
        //     EnigmaticLegacy.LOGGER.info("Thirst Compat.");
        //     NeoForge.EVENT_BUS.register(new ThirstCompatHandler());
        // }
    }
}
