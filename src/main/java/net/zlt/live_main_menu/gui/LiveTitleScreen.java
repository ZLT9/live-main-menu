package net.zlt.live_main_menu.gui;

import com.mojang.authlib.minecraft.BanDetails;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.CreditsAndAttributionScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.options.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.gui.ModListScreen;
import net.neoforged.neoforge.client.gui.widget.ModsButton;
import net.neoforged.neoforge.internal.BrandingControl;
import net.zlt.live_main_menu.LiveMainMenu;
import net.zlt.live_main_menu.event.ModEvents;
import net.zlt.live_main_menu.mixin.accessor.TitleScreenAccessor;

import javax.annotation.Nullable;
import java.io.IOException;

public class LiveTitleScreen extends Screen {
    public static final Component SAVING_LEVEL = Component.translatable("live_main_menu.menu.savingLevel");

    private final LogoRenderer logoRenderer;
    @Nullable
    private SplashRenderer splashRenderer;
    @Nullable
    private RealmsNotificationsScreen realmsNotificationsScreen;
    private boolean fading;
    private long fadeInStart;
    @Nullable
    SelectWorldScreen selectWorldScreen;

    public LiveTitleScreen(boolean fading) {
        super(TitleScreenAccessor.getTitle());
        this.fading = fading;
        logoRenderer = new LogoRenderer(false);
    }

    public void setSelectWorldScreen(SelectWorldScreen screen) {
        selectWorldScreen = screen;
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    protected void init() {
        if (splashRenderer == null) {
            splashRenderer = minecraft.getSplashManager().getSplash();
        }

        int i = font.width(TitleScreenAccessor.getCopyrightText());
        int j = width - i - 2;
        int l = height / 4 + 32;

        if (minecraft.isDemo()) {
            createDemoMenuOptions(l, 24);
        } else {
            createNormalMenuOptions(l, 24);

            addRenderableWidget(new ModsButton(
                Button.builder(
                        Component.translatable("fml.menu.mods"),
                        button -> minecraft.setScreen(new ModListScreen(this))
                    )
                    .bounds(width / 2 - 100, l + 24 * 3, 200, 20)
            ));
            l += 22;
        }

        SpriteIconButton languageButton = addRenderableWidget(CommonButtons.language(20, button -> minecraft.setScreen(new LanguageSelectScreen(this, minecraft.options, minecraft.getLanguageManager())), true));
        languageButton.setPosition(width / 2 - 124, l + 72 + 12);

        addRenderableWidget(
            Button.builder(
                    Component.translatable("menu.options"),
                    button -> minecraft.setScreen(new OptionsScreen(this, minecraft.options))
                )
                .bounds(width / 2 - 100, l + 72 + 12, 98, 20)
                .build()
        );

        addRenderableWidget(
            Button.builder(
                    Component.translatable("menu.quit"),
                    button -> minecraft.stop()
                )
                .bounds(width / 2 + 2, l + 72 + 12, 98, 20)
                .build()
        );

        SpriteIconButton accessibilityButton = addRenderableWidget(CommonButtons.accessibility(20, button -> minecraft.setScreen(new AccessibilityOptionsScreen(this, minecraft.options)), true));
        accessibilityButton.setPosition(width / 2 + 104, l + 72 + 12);

        addRenderableWidget(new PlainTextButton(j, height - 10, i, 10, TitleScreenAccessor.getCopyrightText(), button -> minecraft.setScreen(new CreditsAndAttributionScreen(this)), font));

        if (!realmsNotificationsEnabled()) {
            realmsNotificationsScreen = new RealmsNotificationsScreen();
        }
        realmsNotificationsScreen.init(minecraft, width, height);
    }

    @Override
    public void tick() {
        if (!ModEvents.isHandlingLevel()) {
            minecraft.setScreen(new TitleScreen());
            return;
        }

        if (realmsNotificationsEnabled()) {
            realmsNotificationsScreen.tick();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button) || realmsNotificationsEnabled() && realmsNotificationsScreen.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void removed() {
        if (realmsNotificationsEnabled()) {
            realmsNotificationsScreen.removed();
        }
    }

    @Override
    public void added() {
        if (realmsNotificationsEnabled()) {
            realmsNotificationsScreen.added();
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (fadeInStart == 0 && fading) {
            fadeInStart = Util.getMillis();
        }

        float f = 1.0f;
        if (fading) {
            float f1 = (Util.getMillis() - fadeInStart) / 2000.0f;
            if (f1 > 1.0f) {
                fading = false;
            } else {
                f1 = Mth.clamp(f1, 0.0f, 1.0f);
                f = Mth.clampedMap(f1, 0.5f, 1.0f, 0.0f, 1.0f);
            }
            fadeWidgets(f);
        }

        int i = Mth.ceil(f * 255.0f) << 24;
        if ((i & -67108864) != 0) {
            super.render(guiGraphics, mouseX, mouseY, partialTick);
            logoRenderer.renderLogo(guiGraphics, width, f);
            ClientHooks.renderMainMenu(null, guiGraphics, font, width, height, i);
            if (splashRenderer != null && !minecraft.options.hideSplashTexts().get()) {
                splashRenderer.render(guiGraphics, width, font, i);
            }

            BrandingControl.forEachLine(true, true, (brdline, brd) -> {
                guiGraphics.drawString(font, brd, 2, height - (10 + brdline * (font.lineHeight + 1)), 16777215 | i);
            });

            BrandingControl.forEachAboveCopyrightLine((brdline, brd) -> {
                guiGraphics.drawString(font, brd, width - font.width(brd), height - (10 + (brdline + 1) * (font.lineHeight + 1)), 16777215 | i);
            });

            if (realmsNotificationsEnabled()) {
                RenderSystem.enableDepthTest();
                realmsNotificationsScreen.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }
    }

    private void fadeWidgets(float alpha) {
        for (GuiEventListener child : children()) {
            if (child instanceof AbstractWidget abstractWidget) {
                abstractWidget.setAlpha(alpha);
            }
        }
    }

    private void createNormalMenuOptions(int y, int rowHeight) {
        addRenderableWidget(
            Button.builder(
                    Component.translatable("menu.singleplayer"),
                    button -> {
                        if (selectWorldScreen != null) {
                            minecraft.setScreen(selectWorldScreen);
                        }
                    }
                )
                .bounds(width / 2 - 100, y, 200, 20)
                .build()
        );

        Component multiplayerDisabledReason = getMultiplayerDisabledReason();
        boolean isMultiplayerEnabled = multiplayerDisabledReason == null;
        Tooltip multiplayerDisabledReasonTooltip = multiplayerDisabledReason == null ? null : Tooltip.create(multiplayerDisabledReason);

        addRenderableWidget(
            Button.builder(
                    Component.translatable("menu.multiplayer"),
                    button -> minecraft.setScreen(minecraft.options.skipMultiplayerWarning ? new JoinMultiplayerScreen(this) : new SafetyScreen(this))
                )
                .bounds(width / 2 - 100, y + rowHeight, 200, 20)
                .tooltip(multiplayerDisabledReasonTooltip)
                .build()
        ).active = isMultiplayerEnabled;

        addRenderableWidget(
            Button.builder(
                    Component.translatable("menu.online"),
                    button -> minecraft.setScreen(new RealmsMainScreen(this))
                )
                .bounds(width / 2 - 100, y + rowHeight * 2, 200, 20)
                .tooltip(multiplayerDisabledReasonTooltip)
                .build()
        ).active = isMultiplayerEnabled;
    }

    @Nullable
    private Component getMultiplayerDisabledReason() {
        if (minecraft.allowsMultiplayer()) {
            return null;
        }

        if (minecraft.isNameBanned()) {
            return Component.translatable("title.multiplayer.disabled.banned.name");
        }

        BanDetails banDetails = minecraft.multiplayerBan();
        if (banDetails == null) {
            return Component.translatable("title.multiplayer.disabled");
        }

        return banDetails.expires() == null ? Component.translatable("title.multiplayer.disabled.banned.permanent") : Component.translatable("title.multiplayer.disabled.banned.temporary");
    }

    private void createDemoMenuOptions(int y, int rowHeight) {
        boolean isDemoWorldPresent = checkDemoWorldPresence();
        addRenderableWidget(
            Button.builder(
                    Component.translatable("menu.playdemo"),
                    button -> {
                        ModEvents.quitLevel(this);
                        if (isDemoWorldPresent) {
                            minecraft.createWorldOpenFlows().openWorld("Demo_World", () -> minecraft.setScreen(new TitleScreen()));
                        } else {
                            minecraft.createWorldOpenFlows().createFreshLevel("Demo_World", MinecraftServer.DEMO_SETTINGS, WorldOptions.DEMO_OPTIONS, WorldPresets::createNormalWorldDimensions, new TitleScreen());
                        }
                    })
                .bounds(width / 2 - 100, y, 200, 20)
                .build()
        );
        addRenderableWidget(
            Button.builder(
                    Component.translatable("menu.resetdemo"),
                    button -> {
                        LevelStorageSource levelSource = minecraft.getLevelSource();

                        try (LevelStorageSource.LevelStorageAccess levelAccess = levelSource.createAccess("Demo_World")) {
                            if (levelAccess.hasWorldData()) {
                                minecraft.setScreen(new ConfirmScreen(
                                    this::confirmDemo,
                                    Component.translatable("selectWorld.deleteQuestion"),
                                    Component.translatable("selectWorld.deleteWarning", MinecraftServer.DEMO_SETTINGS.levelName()),
                                    Component.translatable("selectWorld.deleteButton"),
                                    CommonComponents.GUI_CANCEL
                                ));
                            }
                        } catch (IOException e) {
                            SystemToast.onWorldAccessFailure(minecraft, "Demo_World");
                            LiveMainMenu.LOGGER.warn("Failed to access demo world", e);
                        }
                    }
                )
                .bounds(width / 2 - 100, y + rowHeight, 200, 20)
                .build()
        ).active = isDemoWorldPresent;
    }

    private boolean checkDemoWorldPresence() {
        try {
            boolean isDemoWorldPresent;
            try (LevelStorageSource.LevelStorageAccess levelAccess = minecraft.getLevelSource().createAccess("Demo_World")) {
                isDemoWorldPresent = levelAccess.hasWorldData();
            }
            return isDemoWorldPresent;
        } catch (IOException e) {
            SystemToast.onWorldAccessFailure(minecraft, "Demo_World");
            LiveMainMenu.LOGGER.warn("Failed to read demo world data", e);
            return false;
        }
    }

    private void confirmDemo(boolean confirmed) {
        if (confirmed) {
            try (LevelStorageSource.LevelStorageAccess levelAccess = minecraft.getLevelSource().createAccess("Demo_World")) {
                levelAccess.deleteLevel();
            } catch (IOException e) {
                SystemToast.onWorldDeleteFailure(minecraft, "Demo_World");
                LiveMainMenu.LOGGER.warn("Failed to delete demo world", e);
            }
        }
        minecraft.setScreen(this);
    }

    private boolean realmsNotificationsEnabled() {
        return realmsNotificationsScreen != null;
    }
}
