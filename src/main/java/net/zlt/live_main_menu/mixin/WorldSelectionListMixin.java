package net.zlt.live_main_menu.mixin;

import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.world.level.storage.LevelSummary;
import net.zlt.live_main_menu.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldSelectionList.class)
public class WorldSelectionListMixin {

    @Inject(method = "filterAccepts", at = @At("HEAD"), cancellable = true)
    private void restrictWorld(String filter, LevelSummary level, CallbackInfoReturnable<Boolean> cir) {
        if(!Config.hideWorld) return;
        if(level.getLevelId().equalsIgnoreCase("LiveMainMenu") || level.getLevelName().equalsIgnoreCase("LiveMainMenu")) cir.setReturnValue(false);
    }
}
