package net.zlt.live_main_menu.mixin;

import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.zlt.live_main_menu.event.ModEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldSelectionList.WorldListEntry.class)
public abstract class WorldListEntryMixin {
    @Inject(method = "deleteWorld", at = @At("HEAD"))
    private void liveMainMenu$onDeleteWorld(CallbackInfo ci) {
        ModEvents.onDeleteWorld();
    }

    @Inject(method = "editWorld", at = @At("HEAD"))
    private void liveMainMenu$onEditWorld(CallbackInfo ci) {
        ModEvents.onEditWorld();
    }

    @Inject(method = "recreateWorld", at = @At("HEAD"))
    private void liveMainMenu$onRecreateWorld(CallbackInfo ci) {
        ModEvents.onRecreateWorld();
    }
}
