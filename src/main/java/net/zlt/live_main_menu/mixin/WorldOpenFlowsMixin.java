package net.zlt.live_main_menu.mixin;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import net.zlt.live_main_menu.event.ModEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(WorldOpenFlows.class)
public abstract class WorldOpenFlowsMixin {
    @Inject(method = "createFreshLevel", at = @At("HEAD"))
    private void liveMainMenu$onCreateFreshLevel(String levelName, LevelSettings levelSettings, WorldOptions worldOptions, Function<RegistryAccess, WorldDimensions> dimensionGetter, Screen lastScreen, CallbackInfo ci) {
        ModEvents.onCreateLevel();
    }

    @Inject(method = "createLevelFromExistingSettings", at = @At("HEAD"))
    private void liveMainMenu$onCreateLevelFromExistingSettings(LevelStorageSource.LevelStorageAccess levelStorage, ReloadableServerResources resources, LayeredRegistryAccess<RegistryLayer> registries, WorldData worldData, CallbackInfo ci) {
        ModEvents.onCreateLevel();
    }

    @Inject(method = "openWorld", at = @At("HEAD"))
    private void liveMainMenu$onOpenWorld(String worldName, Runnable onFail, CallbackInfo ci) {
        ModEvents.onPlaySingleplayer();
    }
}
