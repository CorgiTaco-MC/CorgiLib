package corgitaco.corgilib.client.screen.widget;

import corgitaco.corgilib.CorgiLib;
import corgitaco.corgilib.client.AnnouncementInfo;
import corgitaco.corgilib.config.AnnouncementConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.FittingMultiLineTextWidget;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

public class EmergencyAnnouncementScreen extends Screen {
    private static final Identifier ICE = Identifier.withDefaultNamespace("textures/block/ice.png");
    private static final Map<Screen, WeakReference<AbstractWidget>> ALERT_WIDGETS = new WeakHashMap<>();

    @Nullable
    private final Screen parent;
    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;

    public EmergencyAnnouncementScreen(Component title) {
        this(title, null);
    }

    public EmergencyAnnouncementScreen(Component title, @Nullable Screen parent) {
        super(title);
        this.parent = parent;
    }

    @Override
    protected void init() {
        AnnouncementInfo info = AnnouncementInfo.getInstance();
        if (info == null) {
            onClose();
            return;
        }

        this.panelWidth = Math.min(420, this.width - 32);
        this.panelHeight = Math.min(this.height - 32, Math.max(180, this.height * 2 / 3));
        this.panelX = (this.width - this.panelWidth) / 2;
        this.panelY = (this.height - this.panelHeight) / 2;
        int contentWidth = this.panelWidth - 16;

        MultiLineTextWidget heading = new MultiLineTextWidget(info.title(), this.font)
                .setMaxWidth(contentWidth).setMaxRows(3).setCentered(true);
        heading.setPosition(this.panelX + (this.panelWidth - heading.getWidth()) / 2, this.panelY + 8);
        addRenderableWidget(heading);

        int descriptionY = heading.getY() + heading.getHeight() + 8;
        int buttonY = this.panelY + this.panelHeight - (Button.DEFAULT_HEIGHT * 2 + 16);
        addRenderableWidget(new FittingMultiLineTextWidget(this.panelX + 8, descriptionY,
                contentWidth, Math.max(20, buttonY - descriptionY - 8), info.desc(), this.font));

        addRenderableWidget(Button.builder(info.actionButtonText(), ConfirmLinkScreen.confirmLink(this, info.url()))
                .pos(this.panelX + 8, buttonY).size(contentWidth, Button.DEFAULT_HEIGHT).build());
        int halfWidth = (contentWidth - 4) / 2;
        addRenderableWidget(Button.builder(Component.literal("Back"), button -> onClose())
                .pos(this.panelX + 8, buttonY + Button.DEFAULT_HEIGHT + 4).size(halfWidth, Button.DEFAULT_HEIGHT).build());
        addRenderableWidget(Button.builder(Component.literal("Don't Show Again"), button -> {
                    AnnouncementInfo.saveStoredAnnouncementInfo();
                    onClose();
                }).pos(this.panelX + 12 + halfWidth, buttonY + Button.DEFAULT_HEIGHT + 4)
                .size(contentWidth - halfWidth - 4, Button.DEFAULT_HEIGHT).build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(this.panelX - 5, this.panelY - 5, this.panelX + this.panelWidth + 5,
                this.panelY + this.panelHeight + 5, 0xFF000096);
        graphics.blit(RenderPipelines.GUI_TEXTURED, ICE, this.panelX, this.panelY, 0, 0,
                this.panelWidth, this.panelHeight, 16, 16);
        graphics.fill(this.panelX, this.panelY, this.panelX + this.panelWidth,
                this.panelY + this.panelHeight, 0x96000000);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    public static void createAlertWidget(Screen screen, int scaledWidth, int scaledHeight) {
        if (screen instanceof EmergencyAnnouncementScreen || AnnouncementConfig.INSTANCE.get().announcementDelivery() != AnnouncementConfig.AnnouncementDelivery.WIDGET) {
            return;
        }
        if (AnnouncementInfo.getInstance() != null) {
            addAlertWidget(screen, scaledWidth, scaledHeight);
        } else {
            AnnouncementInfo.whenLoaded(info -> {
                if (Minecraft.getInstance().screen == screen && AnnouncementConfig.INSTANCE.get().announcementDelivery() == AnnouncementConfig.AnnouncementDelivery.WIDGET) {
                    addAlertWidget(screen, screen.width, screen.height);
                }
            });
        }
    }

    private static void addAlertWidget(Screen screen, int scaledWidth, int scaledHeight) {
        WeakReference<AbstractWidget> existing = ALERT_WIDGETS.get(screen);
        if (existing != null && screen.children().contains(existing.get())) {
            return;
        }
        Component helpCorgiTaco = Component.literal("Help Corgi Taco");
        SpriteIconButton alert = SpriteIconButton.builder(helpCorgiTaco, button ->
                        Minecraft.getInstance().setScreen(new EmergencyAnnouncementScreen(Component.literal("Announcement Info"), screen)), true)
                .sprite(CorgiLib.createLocation("alert"), 32, 32).size(32, 32).build();
        alert.setPosition(Math.max(0, scaledWidth - 50), Math.max(0, scaledHeight - 50));
        alert.setTooltip(Tooltip.create(helpCorgiTaco));
        screen.addRenderableWidget(alert);
        ALERT_WIDGETS.put(screen, new WeakReference<>(alert));
    }
}
