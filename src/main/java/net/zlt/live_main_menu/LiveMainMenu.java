package net.zlt.live_main_menu;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.zlt.live_main_menu.event.ModEvents;
import org.slf4j.Logger;

@Mod(value = LiveMainMenu.ID, dist = Dist.CLIENT)
public class LiveMainMenu {
    public static final String ID = "live_main_menu";
    public static final Logger LOGGER = LogUtils.getLogger();

    public LiveMainMenu(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.addListener(ModEvents::onScreenOpening);
        NeoForge.EVENT_BUS.addListener(ModEvents::onLevelTick);
        NeoForge.EVENT_BUS.addListener(ModEvents::onLevelUnload);
        NeoForge.EVENT_BUS.addListener(ModEvents::onComputeFov);

        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
