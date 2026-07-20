package net.zlt.live_main_menu;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = LiveMainMenu.ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue USE_CUSTOM_FOV = BUILDER
        .comment("Whether to use the FOV defined below or the vanilla FOV.")
        .define("useCustomFov", true);

    private static final ModConfigSpec.IntValue LIVE_MAIN_MENU_FOV = BUILDER
        .comment("The FOV of the camera in the live main menu.")
        .defineInRange("liveMainMenuFov", 70, 30, 110);

    private static final ModConfigSpec.BooleanValue HIDE_WORLD = BUILDER
            .comment("Whether to hide the world used in the menu from the singleplayer selection screen.")
            .define("hideWorld", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean useCustomFov;
    public static int liveMainMenuFov;
    public static boolean hideWorld;

    @SubscribeEvent
    public static void onLoad(ModConfigEvent event) {
        useCustomFov = USE_CUSTOM_FOV.get();
        liveMainMenuFov = LIVE_MAIN_MENU_FOV.get();
        hideWorld = HIDE_WORLD.get();
    }
}
