package corgitaco.corgilib.client.screen.widget;
/*
import com.google.common.collect.Lists;
import corgitaco.corgilib.CorgiLib;
import corgitaco.corgilib.client.AnnouncementInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

import java.util.List;

public class AnnouncementWidget extends AbstractWidget {
    private static final ResourceLocation CORGI_TACO_WAVE = CorgiLib.createLocation("textures/gui/corgi_taco_wave.png");

    private final List<AbstractWidget> children = Lists.newArrayList();

    private int imageWidth = 0;

    public AnnouncementWidget(int guiWidth, int guiHeight, int width, int height, Component message) {
        super(guiWidth / 2, guiHeight / 2, width, height, message);
        int maxWidth = Math.min(guiWidth / 6, 250);

        int renderX = guiWidth - 30 - maxWidth;
        int renderY = guiHeight - 10 - (Minecraft.getInstance().font.lineHeight * 3);

        AnnouncementInfo announcementInfo = AnnouncementInfo.getInstance();
        MultiLineTextWidget announcementHeader = new MultiLineTextWidget(renderX, renderY, announcementInfo.title(), Minecraft.getInstance().font).setCentered(true).setMaxWidth(maxWidth);
        int yOffset = announcementHeader.getHeight() + 15;
        addRenderableWidget(announcementHeader);


        MultiLineTextWidget announcement = new MultiLineTextWidget(renderX, renderY + yOffset, announcementInfo.desc(), Minecraft.getInstance().font).setCentered(true).setMaxWidth(maxWidth);
        yOffset += announcement.getHeight() + 5;

        addRenderableWidget(announcement);
        int buttonWidth = (int) (announcement.getWidth() * 0.48);

        int buttonSpacing = (int) (announcement.getWidth() * 0.04);

        Button takeMeThere = new Button.Builder(announcementInfo.actionButtonText(), ConfirmLinkScreen.confirmLink(Minecraft.getInstance().screen, announcementInfo.url())).pos(renderX, renderY + yOffset).width(buttonWidth).build();

        addRenderableWidget(takeMeThere);

        Button dismiss = new Button.Builder(Component.literal("Dismiss"), button -> {
            this.visible = false;
            AnnouncementInfo.saveStoredAnnouncementInfo();
        }).pos(renderX + takeMeThere.getWidth() + buttonSpacing, renderY + yOffset).width(buttonWidth).build();
        addRenderableWidget(dismiss);

        yOffset += dismiss.getHeight() + 5;


        for (AbstractWidget widget : this.children) {
            widget.setY(widget.getY() - yOffset);
        }

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;

        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (AbstractWidget child : this.children) {
            minX = Math.min(child.getRectangle().left(), minX);
            minX = Math.min(child.getRectangle().right(), minX);

            minY = Math.min(child.getRectangle().bottom(), minY);
            minY = Math.min(child.getRectangle().top(), minY);

            maxX = Math.max(child.getRectangle().left(), maxX);
            maxX = Math.max(child.getRectangle().right(), maxX);

            maxY = Math.max(child.getRectangle().bottom(), maxY);
            maxY = Math.max(child.getRectangle().top(), maxY);
        }


        minX -= 10;
        maxX += 10;
        minY -= 10;
        maxY += 10;


        if (guiWidth > 1000) {

            double corgiImageHeight = 666;
            double corgiImageWidth = 553;

            double ratio = corgiImageWidth / corgiImageHeight;
            minX -= 10;

            this.height = maxY - minY;
            this.imageWidth = (int) ((this.height - 10) * ratio);
            minX -= imageWidth;
        }

        this.setY(minY);
        this.height = maxY - minY;
        this.setX(minX);
        this.setWidth(maxX - minX);
    }

    protected <T extends AbstractWidget> T addRenderableWidget(T widget) {
        return this.addWidget(widget);
    }

    protected <T extends AbstractWidget> T addWidget(T listener) {
        this.children.add(listener);
        return listener;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (visible) {
            guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), ARGB.color(255, 255, 0, 0));
            guiGraphics.setColor(0.3F, 0.3F, 0.3F, 1);

            guiGraphics.blit(RenderType::guiOverlay, Screen.MENU_BACKGROUND, getX() + 1, getY() + 1, getWidth() - 2, getHeight() - 2, 0.0F, 0.0F, this.width, this.height, 32, 32);
            guiGraphics.setColor(1, 1, 1, 1);

            for (Renderable child : this.children) {
                child.render(guiGraphics, mouseX, mouseY, partialTick);
            }
            if (imageWidth > 0) {
                int renderHeight = getHeight() - 10;
                guiGraphics.blit(RenderType::guiTextured, CORGI_TACO_WAVE, getX() + 10, getY() + 5, imageWidth, renderHeight, 0, 0, 553, 666, 553, 666);
            }
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (AbstractWidget child : this.children) {
            child.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void playDownSound(SoundManager handler) {
    }
}*/
