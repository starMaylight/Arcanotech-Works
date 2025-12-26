package com.starmaylight.arcanotech_works.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.starmaylight.arcanotech_works.Arcanotech_works;
import com.starmaylight.arcanotech_works.block.machine.mixer.MixerBlockEntity;
import com.starmaylight.arcanotech_works.block.machine.mixer.MixerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

/**
 * 混合機のGUI画面
 */
public class MixerScreen extends AbstractContainerScreen<MixerMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcanotech_works.MODID, "textures/gui/mixer.png");

    // 進捗バーの位置とサイズ
    private static final int PROGRESS_X = 84;
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

    // 液体タンクの位置とサイズ
    private static final int TANK_WIDTH = 16;
    private static final int TANK_HEIGHT = 52;

    // 入力タンク
    private static final int INPUT_TANK1_X = 26;
    private static final int INPUT_TANK1_Y = 8;
    private static final int INPUT_TANK2_X = 26;
    private static final int INPUT_TANK2_Y = 62;

    // 出力タンク
    private static final int OUTPUT_TANK1_X = 134;
    private static final int OUTPUT_TANK1_Y = 8;
    private static final int OUTPUT_TANK2_X = 134;
    private static final int OUTPUT_TANK2_Y = 62;

    public MixerScreen(MixerMenu menu, Inventory playerInventory, Component title) {
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

        // 液体タンク
        renderFluidTank(guiGraphics, x + INPUT_TANK1_X, y + INPUT_TANK1_Y, menu.getInputFluid1(), menu.getInputTank1Percent());
        renderFluidTank(guiGraphics, x + INPUT_TANK2_X, y + INPUT_TANK2_Y, menu.getInputFluid2(), menu.getInputTank2Percent());
        renderFluidTank(guiGraphics, x + OUTPUT_TANK1_X, y + OUTPUT_TANK1_Y, menu.getOutputFluid1(), menu.getOutputTank1Percent());
        renderFluidTank(guiGraphics, x + OUTPUT_TANK2_X, y + OUTPUT_TANK2_Y, menu.getOutputFluid2(), menu.getOutputTank2Percent());
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

    /**
     * 液体タンクを描画
     */
    private void renderFluidTank(GuiGraphics guiGraphics, int x, int y, FluidStack fluidStack, float fillPercent) {
        if (fluidStack.isEmpty() || fillPercent <= 0) return;

        IClientFluidTypeExtensions fluidTypeExtensions = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        ResourceLocation stillTexture = fluidTypeExtensions.getStillTexture(fluidStack);
        
        if (stillTexture == null) return;

        TextureAtlasSprite sprite = minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stillTexture);
        int color = fluidTypeExtensions.getTintColor(fluidStack);

        int fluidHeight = (int) (TANK_HEIGHT * fillPercent);
        int yOffset = TANK_HEIGHT - fluidHeight;

        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.setShaderColor(
                ((color >> 16) & 0xFF) / 255.0F,
                ((color >> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F,
                ((color >> 24) & 0xFF) / 255.0F
        );

        // スプライトをタイル状に描画
        int remaining = fluidHeight;
        int currentY = y + yOffset;
        
        while (remaining > 0) {
            int drawHeight = Math.min(remaining, 16);
            guiGraphics.blit(x, currentY, 0, TANK_WIDTH, drawHeight, sprite);
            remaining -= drawHeight;
            currentY += drawHeight;
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

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

        // 入力タンク1のツールチップ
        if (isHovering(INPUT_TANK1_X, INPUT_TANK1_Y, TANK_WIDTH, TANK_HEIGHT, mouseX, mouseY)) {
            renderFluidTooltip(guiGraphics, menu.getInputFluid1(), menu.getInputTank1Amount(), mouseX, mouseY);
        }

        // 入力タンク2のツールチップ
        if (isHovering(INPUT_TANK2_X, INPUT_TANK2_Y, TANK_WIDTH, TANK_HEIGHT, mouseX, mouseY)) {
            renderFluidTooltip(guiGraphics, menu.getInputFluid2(), menu.getInputTank2Amount(), mouseX, mouseY);
        }

        // 出力タンク1のツールチップ
        if (isHovering(OUTPUT_TANK1_X, OUTPUT_TANK1_Y, TANK_WIDTH, TANK_HEIGHT, mouseX, mouseY)) {
            renderFluidTooltip(guiGraphics, menu.getOutputFluid1(), menu.getOutputTank1Amount(), mouseX, mouseY);
        }

        // 出力タンク2のツールチップ
        if (isHovering(OUTPUT_TANK2_X, OUTPUT_TANK2_Y, TANK_WIDTH, TANK_HEIGHT, mouseX, mouseY)) {
            renderFluidTooltip(guiGraphics, menu.getOutputFluid2(), menu.getOutputTank2Amount(), mouseX, mouseY);
        }
    }

    private void renderFluidTooltip(GuiGraphics guiGraphics, FluidStack fluidStack, int amount, int mouseX, int mouseY) {
        if (fluidStack.isEmpty()) {
            guiGraphics.renderTooltip(this.font,
                    Component.translatable("tooltip.arcanotech_works.tank_empty"),
                    mouseX, mouseY);
        } else {
            guiGraphics.renderTooltip(this.font,
                    Component.translatable("tooltip.arcanotech_works.tank_fluid",
                            fluidStack.getDisplayName(),
                            amount, MixerBlockEntity.TANK_CAPACITY),
                    mouseX, mouseY);
        }
    }
}
