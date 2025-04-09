package net.zlt.live_main_menu.event;

import com.mojang.blaze3d.platform.Window;
import com.mojang.realmsclient.dto.RealmsServer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.WorldData;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.zlt.live_main_menu.Config;
import net.zlt.live_main_menu.LiveMainMenu;
import net.zlt.live_main_menu.gui.LiveTitleScreen;
import net.zlt.live_main_menu.mixin.accessor.TitleScreenAccessor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class ModEvents {
    @Nullable
    private static LiveTitleScreen liveTitleScreen = null;
    private static boolean handleLevel = false;
    private static boolean openingLiveMainMenuLevel = false;
    private static boolean failed = false;

    public static boolean isHandlingLevel() {
        return handleLevel;
    }

    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (!(event.getScreen() instanceof TitleScreen titleScreen)) {
            return;
        }

        if (failed) {
            failed = false;
            return;
        }

        LiveMainMenu.LOGGER.info("Opening live title screen");
        createLiveTitleScreen(titleScreen);
        tryLoadLevel(titleScreen);
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!handleLevel) {
            return;
        }

        Level level = event.getLevel();
        if (!level.isClientSide()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.screen instanceof LiveTitleScreen)) {
            minecraft.setScreen(liveTitleScreen);
        }
    }

    public static void onLevelUnload(LevelEvent.Unload event) {
        handleLevel = false;
    }

    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        if (!handleLevel || !Config.useCustomFov) {
            return;
        }

        event.setFOV(Config.liveMainMenuFov);
    }

    /**
     * Called before creating a singleplayer world at the head of {@link net.minecraft.client.gui.screens.worldselection.WorldOpenFlows#createFreshLevel(String, LevelSettings, WorldOptions, Function, Screen) createFreshLevel} and {@link net.minecraft.client.gui.screens.worldselection.WorldOpenFlows#createLevelFromExistingSettings(LevelStorageSource.LevelStorageAccess, ReloadableServerResources, LayeredRegistryAccess, WorldData) createLevelFromExistingSettings} via mixin.
     */
    public static void onCreateLevel() {
        quitLiveMainMenuLevel();
    }

    /**
     * Called before joining a singleplayer world at the head of {@link net.minecraft.client.gui.screens.worldselection.WorldOpenFlows#openWorld(String, Runnable) openWorld} via mixin.
     */
    public static void onPlaySingleplayer() {
        if (openingLiveMainMenuLevel) {
            openingLiveMainMenuLevel = false;
            return;
        }

        quitLiveMainMenuLevel();
    }

    /**
     * Called before joining a server at the head of {@link net.minecraft.client.gui.screens.ConnectScreen#startConnecting(Screen, Minecraft, ServerAddress, ServerData, boolean, TransferState) startConnecting} via mixin.
     */
    public static void onPlayMultiplayer() {
        quitLiveMainMenuLevel();
    }

    /**
     * Called before joining a Realms server at the head of {@link com.mojang.realmsclient.RealmsMainScreen#play(RealmsServer, Screen, boolean) play} via mixin.
     */
    public static void onPlayRealms() {
        quitLiveMainMenuLevel();
    }

    /**
     * Called before deleting a world at the head of {@link WorldSelectionList.WorldListEntry#deleteWorld() deleteWorld} via mixin.
     */
    public static void onDeleteWorld() {
        quitLiveMainMenuLevel();
    }

    /**
     * Called before editing a world at the head of {@link WorldSelectionList.WorldListEntry#editWorld() editWorld} via mixin.
     */
    public static void onEditWorld() {
        quitLiveMainMenuLevel();
    }

    /**
     * Called before recreating a world at the head of {@link WorldSelectionList.WorldListEntry#recreateWorld() recreateWorld} via mixin.
     */
    public static void onRecreateWorld() {
        quitLiveMainMenuLevel();
    }

    private static void quitLiveMainMenuLevel() {
        if (!handleLevel) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        quitLevel(minecraft.screen == null ? new TitleScreen() : minecraft.screen);
    }

    public static void quitLevel(Screen screen) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        minecraft.getReportingContext().draftReportHandled(minecraft, screen, ModEvents::onQuitLevel, true);
    }

    private static void onQuitLevel() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.level.disconnect();
        if (minecraft.isLocalServer()) {
            minecraft.disconnect(new GenericMessageScreen(LiveTitleScreen.SAVING_LEVEL));
        } else {
            minecraft.disconnect();
        }
    }

    private static void createLiveTitleScreen(TitleScreen titleScreen) {
        liveTitleScreen = new LiveTitleScreen(((TitleScreenAccessor) titleScreen).isFading());

        SelectWorldScreen selectWorldScreen = new SelectWorldScreen(liveTitleScreen);
        liveTitleScreen.setSelectWorldScreen(selectWorldScreen);

        Minecraft minecraft = Minecraft.getInstance();
        Window window = minecraft.getWindow();
        selectWorldScreen.init(minecraft, window.getGuiScaledWidth(), window.getGuiScaledHeight());
    }

    private static void tryLoadLevel(TitleScreen titleScreen) {
        Minecraft minecraft = Minecraft.getInstance();
        LevelStorageSource levelSource = minecraft.getLevelSource();

        CompletableFuture<List<LevelSummary>> levelSummaryFuture;
        try {
            levelSummaryFuture = levelSource.loadLevelSummaries(levelSource.findLevelCandidates());
        } catch (Throwable e) {
            LiveMainMenu.LOGGER.warn("Couldn't load worlds", e);
            failed = true;
            minecraft.setScreen(titleScreen);
            return;
        }

        levelSummaryFuture.thenAcceptAsync(levelSummaries -> minecraft.execute(() -> {
            String levelId = null;
            for (LevelSummary levelSummary : levelSummaries) {
                if (levelSummary.getLevelName().equals("LiveMainMenu")) {
                    levelId = levelSummary.getLevelId();
                    break;
                }
            }
            if (levelId == null) {
                failed = true;
                minecraft.setScreen(titleScreen);
                return;
            }

            handleLevel = true;
            openingLiveMainMenuLevel = true;
            minecraft.createWorldOpenFlows().openWorld(levelId, () -> {
                LiveMainMenu.LOGGER.warn("Couldn't open world");
                failed = true;
                minecraft.setScreen(titleScreen);
            });
        })).exceptionally(e -> {
            minecraft.execute(() -> {
                LiveMainMenu.LOGGER.warn("Couldn't load worlds", e);
                failed = true;
                minecraft.setScreen(titleScreen);
            });
            return null;
        });
    }
}
