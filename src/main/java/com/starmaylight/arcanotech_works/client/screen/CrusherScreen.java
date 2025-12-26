package com.starmaylight.arcanotech_works.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.starmaylight.arcanotech_works.Arcanotech_works;
import com.starmaylight.arcanotech_works.block.machine.crusher.CrusherMenu;
import com.starmaylight.arcanotech_works.compat.jei.JEIHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 粉砕機のGUI画面
 */
public class CrusherScreen extends AbstractContainerScreen<CrusherMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcanotech_works.MODID, "textures/gui/crusher.png");

    // 進捗バーの位置とサイズ
    private static final int PROGRESS_X = 79;
    private static final int PROGRESS_Y = 35;
    private static final int PROGRESS_WIDTH = 24;
    private static final int PROGRESS_HEIGHT = 17;
    private static final int PROGRESS_U = 176;
    private static final int PROGRESS_V = 0;

    // 熱ゲージの位置とサイズ
    private static final int HEAT_X = 152;
    private static final int HEAT_Y = 8;
    private static final int HEAT_WIDTH = 16;
    private static final int HEAT_HEIGHT = 52;
    private static final int HEAT_U = 176;
    private static final int HEAT_V = 17;

    // マナゲージの位置とサイズ
    private static final int MANA_X = 8;
    private static final int MANA_Y = 8;
    private static final int MANA_WIDTH = 16;
    private static final int MANA_HEIGHT = 52;
    private static final int MANA_U = 192;
    private static final int MANA_V = 17;

    public CrusherScreen(CrusherMenu menu, Inventory playerInventory, Component title) {
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
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        // 進捗バー
        renderProgressBar(guiGraphics, x, y);
        
        // 熱ゲージ
        renderHeatGauge(guiGraphics, x, y);
        
        // マナゲージ
        renderManaGauge(guiGraphics, x, y);
    }

    private void renderProgressBar(GuiGraphics guiGraphics, int x, int y) {
        float progress = menu.getProgressPercent();
        if (progress > 0) {
            int width = (int) (PROGRESS_WIDTH * progress);
            guiGraphics.blit(TEXTURE, x + PROGRESS_X, y + PROGRESS_Y,
                    PROGRESS_U, PROGRESS_V, width, PROGRESS_HEIGHT);
        }
    }

    private void renderHeatGauge(GuiGraphics guiGraphics, int x, int y) {
        float heat = menu.getHeatPercent();
        if (heat > 0) {
            int height = (int) (HEAT_HEIGHT * heat);
            int yOffset = HEAT_HEIGHT - height;
            guiGraphics.blit(TEXTURE, x + HEAT_X, y + HEAT_Y + yOffset,
                    HEAT_U, HEAT_V + yOffset, HEAT_WIDTH, height);
        }
    }

    private void renderManaGauge(GuiGraphics guiGraphics, int x, int y) {
        float mana = menu.getManaPercent();
        if (mana > 0) {
            int height = (int) (MANA_HEIGHT * mana);
            int yOffset = MANA_HEIGHT - height;
            guiGraphics.blit(TEXTURE, x + MANA_X, y + MANA_Y + yOffset,
                    MANA_U, MANA_V + yOffset, MANA_WIDTH, height);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);

        // 熱ゲージのツールチップ
        if (isHovering(HEAT_X, HEAT_Y, HEAT_WIDTH, HEAT_HEIGHT, mouseX, mouseY)) {
            guiGraphics.renderTooltip(this.font,
                    Component.translatable("tooltip.arcanotech_works.heat",
                            menu.getHeat(), menu.getMaxHeat()),
                    mouseX, mouseY);
        }

        // マナゲージのツールチップ
        if (isHovering(MANA_X, MANA_Y, MANA_WIDTH, MANA_HEIGHT, mouseX, mouseY)) {
            guiGraphics.renderTooltip(this.font,
                    Component.translatable("tooltip.arcanotech_works.mana_stored",
                            menu.getMana(), menu.getMaxMana()),
                    mouseX, mouseY);
        }

        // 進捗バーのツールチップ（JEIへのリンク提示）
        if (isHovering(PROGRESS_X, PROGRESS_Y, PROGRESS_WIDTH, PROGRESS_HEIGHT, mouseX, mouseY)) {
            guiGraphics.renderTooltip(this.font,
                    Component.translatable("tooltip.arcanotech_works.show_recipes"),
                    mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 右クリックで進捗バーをクリックした場合、JEIレシピを表示
        if (button == 1 && isHovering(PROGRESS_X, PROGRESS_Y, PROGRESS_WIDTH, PROGRESS_HEIGHT, mouseX, mouseY)) {
            JEIHelper.showCrusherRecipes();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
