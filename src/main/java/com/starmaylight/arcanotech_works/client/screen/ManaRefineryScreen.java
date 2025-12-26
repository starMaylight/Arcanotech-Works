package com.starmaylight.arcanotech_works.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.starmaylight.arcanotech_works.Arcanotech_works;
import com.starmaylight.arcanotech_works.menu.ManaRefineryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 魔石精錬機のGUIスクリーン
 */
public class ManaRefineryScreen extends AbstractContainerScreen<ManaRefineryMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcanotech_works.MODID, "textures/gui/mana_refinery.png");

    // テクスチャ内の位置
    private static final int PROGRESS_X = 79;
    private static final int PROGRESS_Y = 35;
    private static final int PROGRESS_WIDTH = 24;
    private static final int PROGRESS_HEIGHT = 17;

    private static final int MANA_BAR_X = 8;
    private static final int MANA_BAR_Y = 10;
    private static final int MANA_BAR_WIDTH = 16;
    private static final int MANA_BAR_HEIGHT = 56;

    public ManaRefineryScreen(ManaRefineryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // 背景
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        // 精錬進捗（矢印）
        if (menu.isRefining()) {
            int progressWidth = (int) (PROGRESS_WIDTH * menu.getRefineProgressPercent());
            graphics.blit(TEXTURE,
                    x + PROGRESS_X, y + PROGRESS_Y,
                    176, 0,
                    progressWidth, PROGRESS_HEIGHT);
        }

        // 魔力バー
        int manaHeight = (int) (MANA_BAR_HEIGHT * menu.getManaProgress());
        if (manaHeight > 0) {
            graphics.blit(TEXTURE,
                    x + MANA_BAR_X, y + MANA_BAR_Y + MANA_BAR_HEIGHT - manaHeight,
                    176, 17 + MANA_BAR_HEIGHT - manaHeight,
                    MANA_BAR_WIDTH, manaHeight);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // 魔力バーのツールチップ
        if (isHovering(MANA_BAR_X, MANA_BAR_Y, MANA_BAR_WIDTH, MANA_BAR_HEIGHT, mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    Component.translatable("tooltip.arcanotech_works.mana_stored",
                            menu.getManaStored(), menu.getMaxMana()),
                    mouseX, mouseY);
        }

        // 進捗バーのツールチップ
        if (menu.isRefining() && isHovering(PROGRESS_X, PROGRESS_Y, PROGRESS_WIDTH, PROGRESS_HEIGHT, mouseX, mouseY)) {
            int currentQuality = menu.getCurrentInputQuality();
            int targetQuality = menu.getTargetOutputQuality();
            int progress = menu.getRefineProgress();
            int target = menu.getRefineTarget();
            
            graphics.renderTooltip(this.font,
                    Component.translatable("tooltip.arcanotech_works.refining",
                            currentQuality, targetQuality, progress, target),
                    mouseX, mouseY);
        }
    }
}
