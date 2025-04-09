package net.zlt.live_main_menu.mixin.accessor;

import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TitleScreen.class)
public interface TitleScreenAccessor {
    @Accessor("TITLE")
    static Component getTitle() {
        throw new AssertionError();
    }

    @Accessor("COPYRIGHT_TEXT")
    static Component getCopyrightText() {
        throw new AssertionError();
    }

    @Accessor("fading")
    boolean isFading();
}
