package com.starmaylight.arcanotech_works.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.starmaylight.arcanotech_works.Arcanotech_works;
import com.starmaylight.arcanotech_works.block.machine.mixer.MixerBlockEntity;
import com.starmaylight.arcanotech_works.block.machine.mixer.MixerMenu;
import com.starmaylight.arcanotech_works.compat.jei.JEIHelper;
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

    // 液体タンクスロット（18x18の正方形）
    private static final int FLUID_SLOT_SIZE = 18;

    // 入力タンク（左側、2x2グリッドの左）
    private static final int INPUT_TANK1_X = 26;
    private static final int INPUT_TANK1_Y = 17;
    private static final int INPUT_TANK2_X = 26;
    private static final int INPUT_TANK2_Y = 35;

    // 出力タンク（右側、出力スロットの横）
    private static final int OUTPUT_TANK1_X = 116;
    private static final int OUTPUT_TANK1_Y = 17;
    private static final int OUTPUT_TANK2_X = 116;
    private static final int OUTPUT_TANK2_Y = 35;

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

        // 液体タンクスロット（スロット枠を描画）
        renderFluidSlot(guiGraphics, x + INPUT_TANK1_X, y + INPUT_TANK1_Y, menu.getInputFluid1());
        renderFluidSlot(guiGraphics, x + INPUT_TANK2_X, y + INPUT_TANK2_Y, menu.getInputFluid2());
        renderFluidSlot(guiGraphics, x + OUTPUT_TANK1_X, y + OUTPUT_TANK1_Y, menu.getOutputFluid1());
        renderFluidSlot(guiGraphics, x + OUTPUT_TANK2_X, y + OUTPUT_TANK2_Y, menu.getOutputFluid2());
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
     * 液体スロットを描画（18x18の正方形、液体で塗りつぶし）
     */
    private void renderFluidSlot(GuiGraphics guiGraphics, int x, int y, FluidStack fluidStack) {
        // スロット枠の描画（背景）
        guiGraphics.fill(x, y, x + FLUID_SLOT_SIZE, y + FLUID_SLOT_SIZE, 0xFF8B8B8B);  // スロット背景
        guiGraphics.fill(x, y, x + FLUID_SLOT_SIZE - 1, y + 1, 0xFF373737);  // 上の影
        guiGraphics.fill(x, y, x + 1, y + FLUID_SLOT_SIZE - 1, 0xFF373737);  // 左の影
        guiGraphics.fill(x + FLUID_SLOT_SIZE - 1, y + 1, x + FLUID_SLOT_SIZE, y + FLUID_SLOT_SIZE, 0xFFFFFFFF);  // 右のハイライト
        guiGraphics.fill(x + 1, y + FLUID_SLOT_SIZE - 1, x + FLUID_SLOT_SIZE, y + FLUID_SLOT_SIZE, 0xFFFFFFFF);  // 下のハイライト

        // 液体の描画
        if (!fluidStack.isEmpty()) {
            IClientFluidTypeExtensions fluidTypeExtensions = IClientFluidTypeExtensions.of(fluidStack.getFluid());
            ResourceLocation stillTexture = fluidTypeExtensions.getStillTexture(fluidStack);
            
            if (stillTexture != null) {
                TextureAtlasSprite sprite = minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stillTexture);
                int color = fluidTypeExtensions.getTintColor(fluidStack);

                RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
                RenderSystem.setShaderColor(
                        ((color >> 16) & 0xFF) / 255.0F,
                        ((color >> 8) & 0xFF) / 255.0F,
                        (color & 0xFF) / 255.0F,
                        1.0F
                );

                // スロット内に液体を描画（内側の16x16）
                guiGraphics.blit(x + 1, y + 1, 0, 16, 16, sprite);

                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
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

        // 入力タンク1のツールチップ
        if (isHovering(INPUT_TANK1_X, INPUT_TANK1_Y, FLUID_SLOT_SIZE, FLUID_SLOT_SIZE, mouseX, mouseY)) {
            renderFluidTooltip(guiGraphics, menu.getInputFluid1(), menu.getInputTank1Amount(), mouseX, mouseY);
        }

        // 入力タンク2のツールチップ
        if (isHovering(INPUT_TANK2_X, INPUT_TANK2_Y, FLUID_SLOT_SIZE, FLUID_SLOT_SIZE, mouseX, mouseY)) {
            renderFluidTooltip(guiGraphics, menu.getInputFluid2(), menu.getInputTank2Amount(), mouseX, mouseY);
        }

        // 出力タンク1のツールチップ
        if (isHovering(OUTPUT_TANK1_X, OUTPUT_TANK1_Y, FLUID_SLOT_SIZE, FLUID_SLOT_SIZE, mouseX, mouseY)) {
            renderFluidTooltip(guiGraphics, menu.getOutputFluid1(), menu.getOutputTank1Amount(), mouseX, mouseY);
        }

        // 出力タンク2のツールチップ
        if (isHovering(OUTPUT_TANK2_X, OUTPUT_TANK2_Y, FLUID_SLOT_SIZE, FLUID_SLOT_SIZE, mouseX, mouseY)) {
            renderFluidTooltip(guiGraphics, menu.getOutputFluid2(), menu.getOutputTank2Amount(), mouseX, mouseY);
        }

        // 進捗バーのツールチップ（JEIへのリンク提示）
        if (isHovering(PROGRESS_X, PROGRESS_Y, PROGRESS_WIDTH, PROGRESS_HEIGHT, mouseX, mouseY)) {
            guiGraphics.renderTooltip(this.font,
                    Component.translatable("tooltip.arcanotech_works.show_recipes"),
                    mouseX, mouseY);
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 右クリックで進捗バーをクリックした場合、JEIレシピを表示
        if (button == 1 && isHovering(PROGRESS_X, PROGRESS_Y, PROGRESS_WIDTH, PROGRESS_HEIGHT, mouseX, mouseY)) {
            openJEIRecipes();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * JEIのレシピ画面を開く
     */
    private void openJEIRecipes() {
        JEIHelper.showMixerRecipes();
    }
}
