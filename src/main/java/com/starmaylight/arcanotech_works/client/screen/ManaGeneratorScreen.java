package com.starmaylight.arcanotech_works.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.starmaylight.arcanotech_works.Arcanotech_works;
import com.starmaylight.arcanotech_works.menu.ManaGeneratorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 魔石燃焼炉のGUIスクリーン
 */
public class ManaGeneratorScreen extends AbstractContainerScreen<ManaGeneratorMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcanotech_works.MODID, "textures/gui/mana_generator.png");

    // テクスチャ内の位置
    private static final int BURN_X = 81;
    private static final int BURN_Y = 53;
    private static final int BURN_WIDTH = 14;
    private static final int BURN_HEIGHT = 14;

    private static final int MANA_BAR_X = 152;
    private static final int MANA_BAR_Y = 10;
    private static final int MANA_BAR_WIDTH = 16;
    private static final int MANA_BAR_HEIGHT = 56;

    public ManaGeneratorScreen(ManaGeneratorMenu menu, Inventory playerInventory, Component title) {
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

        // 燃焼炎
        if (menu.isBurning()) {
            int burnHeight = (int) (BURN_HEIGHT * (1.0f - menu.getBurnProgress()));
            graphics.blit(TEXTURE, 
                    x + BURN_X, y + BURN_Y + BURN_HEIGHT - burnHeight,
                    176, BURN_HEIGHT - burnHeight,
                    BURN_WIDTH, burnHeight);
        }

        // 魔力バー
        int manaHeight = (int) (MANA_BAR_HEIGHT * menu.getManaProgress());
        if (manaHeight > 0) {
            graphics.blit(TEXTURE,
                    x + MANA_BAR_X, y + MANA_BAR_Y + MANA_BAR_HEIGHT - manaHeight,
                    176, 14 + MANA_BAR_HEIGHT - manaHeight,
                    MANA_BAR_WIDTH, manaHeight);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);

        // 魔力バーのツールチップ
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        if (isHovering(MANA_BAR_X, MANA_BAR_Y, MANA_BAR_WIDTH, MANA_BAR_HEIGHT, mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    Component.translatable("tooltip.arcanotech_works.mana_stored",
                            menu.getManaStored(), menu.getMaxMana()),
                    mouseX, mouseY);
        }

        // 燃焼情報のツールチップ
        if (menu.isBurning() && isHovering(BURN_X, BURN_Y, BURN_WIDTH, BURN_HEIGHT, mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    Component.translatable("tooltip.arcanotech_works.generating",
                            menu.getCurrentManaGeneration()),
                    mouseX, mouseY);
        }
    }
}
