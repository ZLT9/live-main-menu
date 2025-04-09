package net.zlt.live_main_menu.mixin;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.dto.RealmsServer;
import net.minecraft.client.gui.screens.Screen;
import net.zlt.live_main_menu.event.ModEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RealmsMainScreen.class)
public abstract class RealmsMainScreenMixin {
    @Inject(method = "play(Lcom/mojang/realmsclient/dto/RealmsServer;Lnet/minecraft/client/gui/screens/Screen;Z)V", at = @At("HEAD"))
    private static void liveMainMenu$onPlayRealms(RealmsServer realmsServer, Screen lastScreen, boolean allowSnapshots, CallbackInfo ci) {
        ModEvents.onPlayRealms();
    }
}
