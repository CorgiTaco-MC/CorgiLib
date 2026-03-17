package corgitaco.corgilib.client.screen.widget;

import corgitaco.corgilib.CorgiLib;
import corgitaco.corgilib.client.AnnouncementInfo;
import corgitaco.corgilib.config.AnnouncementConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

public class EmergencyAnnouncementScreen extends Screen {

    private static final ResourceLocation ICE = ResourceLocation.withDefaultNamespace("textures/block/ice.png");

    public EmergencyAnnouncementScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        if (AnnouncementInfo.getInstance() == null) {
            onClose();
            return;
        }
        super.init();
        int width3rd = width / 3;
        int height3rd = height / 3;
        Component learnMore = AnnouncementInfo.getInstance().actionButtonText();
        Component dismiss = Component.literal("Don't Show Again");
        Component back = Component.literal("Back");

        addRenderableWidget(new Button.Builder(learnMore, ConfirmLinkScreen.confirmLink(Minecraft.getInstance().screen, AnnouncementInfo.getInstance().url())).pos(width3rd + 5, (height3rd * 2) - ((Button.DEFAULT_HEIGHT * 2) + 10)).size(width3rd - 10, Button.DEFAULT_HEIGHT).build());
        addRenderableWidget(new Button.Builder(dismiss, button -> {
            AnnouncementInfo.saveStoredAnnouncementInfo();
            Minecraft.getInstance().setScreen(null);
        }).pos(width3rd * 2 - 150 - 5, (height3rd * 2) - (Button.DEFAULT_HEIGHT + 5)).build());
        addRenderableWidget(new Button.Builder(back, button -> {
            Minecraft.getInstance().setScreen(null);
        }).pos(width3rd + 5, (height3rd * 2) - (Button.DEFAULT_HEIGHT + 5)).build());

    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        int width3rd = width / 3;
        int height3rd = height / 3;
        guiGraphics.fill(width3rd - 5, height3rd - 5, (width3rd * 2) + 5, (height3rd * 2) + 5, FastColor.ARGB32.color(255, 0, 0, 150));

        guiGraphics.blit(ICE, width3rd, height3rd, 0, 0, width3rd, height3rd, 16, 16);
        guiGraphics.fill(width3rd, height3rd, width3rd * 2, height3rd * 2, FastColor.ARGB32.color(150, 0, 0, 0));
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, AnnouncementInfo.getInstance().title(), width3rd + (width3rd / 2), height3rd + 5, FastColor.ARGB32.color(255, 255, 255, 255));
        guiGraphics.drawWordWrap(Minecraft.getInstance().font, AnnouncementInfo.getInstance().desc(), width3rd + 5, height3rd + 25, width3rd - 10, FastColor.ARGB32.color(255, 255, 255, 255));
    }

    public static void createAlertWidget(Screen screen, int scaledWidth, int scaledHeight) {
        if (screen instanceof EmergencyAnnouncementScreen) {
            return;
        }
        if (AnnouncementInfo.getInstance() != null && AnnouncementConfig.INSTANCE.get().announcementDelivery() == AnnouncementConfig.AnnouncementDelivery.WIDGET) {
            MutableComponent helpCorgiTaco = Component.literal("Help Corgi Taco");
            SpriteIconButton build = new SpriteIconButton.Builder(helpCorgiTaco, button -> {
                Minecraft.getInstance().setScreen(new EmergencyAnnouncementScreen(Component.literal("Announcement Info")));
            }, true).sprite(CorgiLib.createLocation("alert"), 32, 32).size(32, 32).build();
            build.setPosition(scaledWidth - 50, scaledHeight - 50);
            build.setTooltip(Tooltip.create(helpCorgiTaco));
            screen.addRenderableWidget(build);
        }
    }
}
