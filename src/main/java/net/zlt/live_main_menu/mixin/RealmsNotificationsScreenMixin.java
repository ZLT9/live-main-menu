package net.zlt.live_main_menu.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.zlt.live_main_menu.gui.LiveTitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RealmsNotificationsScreen.class)
public abstract class RealmsNotificationsScreenMixin extends Screen {
    private RealmsNotificationsScreenMixin(Component title) {
        super(title);
    }

    @ModifyReturnValue(method = "inTitleScreen", at = @At("RETURN"))
    private boolean liveMainMenu$inTitleScreen(boolean original) {
        return original || minecraft.screen instanceof LiveTitleScreen;
    }
}
