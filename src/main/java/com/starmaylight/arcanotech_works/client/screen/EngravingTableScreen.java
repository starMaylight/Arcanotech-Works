package com.starmaylight.arcanotech_works.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.starmaylight.arcanotech_works.Arcanotech_works;
import com.starmaylight.arcanotech_works.block.engraving.EngravingTableMenu;
import com.starmaylight.arcanotech_works.network.EngravingClickPacket;
import com.starmaylight.arcanotech_works.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

/**
 * 刻印台のGUIスクリーン
 */
public class EngravingTableScreen extends AbstractContainerScreen<EngravingTableMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcanotech_works.MODID, "textures/gui/engraving_table.png");

    // GUI サイズ
    private static final int GUI_WIDTH = 220;
    private static final int GUI_HEIGHT = 220;

    // グリッド位置とサイズ
    private static final int GRID_X = 58;
    private static final int GRID_Y = 20;
    private static final int CELL_SIZE = 16;
    private static final int GRID_SIZE = 7;

    // タンク表示位置
    private static final int TANK_X = 34;
    private static final int TANK_Y = 20;
    private static final int TANK_WIDTH = 16;
    private static final int TANK_HEIGHT = 84;

    // クリックエフェクト用
    private int clickEffectX = -1;
    private int clickEffectY = -1;
    private int clickEffectTicks = 0;
    private boolean lastClickSuccess = false;

    public EngravingTableScreen(EngravingTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY = 6;
        this.inventoryLabelX = 30;
        this.inventoryLabelY = 128;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        // クリックエフェクトのタイマー
        if (clickEffectTicks > 0) {
            clickEffectTicks--;
        }
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

        // タンク表示（溶融魔導銀）
        int tankFill = (int) (TANK_HEIGHT * menu.getTankFillPercent());
        if (tankFill > 0) {
            graphics.blit(TEXTURE,
                    x + TANK_X, y + TANK_Y + TANK_HEIGHT - tankFill,
                    220, 0,
                    TANK_WIDTH, tankFill);
        }

        // 7x7 グリッド描画
        renderEngravingGrid(graphics, x, y, mouseX, mouseY);
    }

    /**
     * 7x7 刻印グリッドを描画
     */
    private void renderEngravingGrid(GuiGraphics graphics, int guiX, int guiY, int mouseX, int mouseY) {
        boolean hasPlate = menu.hasPlate();
        int[] hoverPos = getGridPosition(mouseX, mouseY);

        for (int gy = 0; gy < GRID_SIZE; gy++) {
            for (int gx = 0; gx < GRID_SIZE; gx++) {
                int cellX = guiX + GRID_X + gx * CELL_SIZE;
                int cellY = guiY + GRID_Y + gy * CELL_SIZE;

                if (hasPlate) {
                    boolean isEngraved = menu.isEngraved(gx, gy);
                    boolean isHovered = hoverPos != null && hoverPos[0] == gx && hoverPos[1] == gy;
                    boolean isClickEffect = clickEffectTicks > 0 && clickEffectX == gx && clickEffectY == gy;

                    if (isEngraved) {
                        // 彫刻済み = グレーアウト
                        graphics.blit(TEXTURE, cellX, cellY, 236, 16, CELL_SIZE, CELL_SIZE);
                    } else {
                        // 未彫刻 = 銀色
                        graphics.blit(TEXTURE, cellX, cellY, 236, 0, CELL_SIZE, CELL_SIZE);
                    }

                    // クリックエフェクト（光る）
                    if (isClickEffect) {
                        int alpha = (int) (255 * (clickEffectTicks / 10.0f));
                        int color = lastClickSuccess ? 
                                (alpha << 24) | 0x00FF00 :  // 緑（成功）
                                (alpha << 24) | 0xFF0000;   // 赤（失敗）
                        graphics.fill(cellX + 1, cellY + 1, cellX + CELL_SIZE - 1, cellY + CELL_SIZE - 1, color);
                    }
                    // ホバーエフェクト
                    else if (isHovered && !isEngraved) {
                        // 白い半透明オーバーレイ
                        graphics.fill(cellX + 1, cellY + 1, cellX + CELL_SIZE - 1, cellY + CELL_SIZE - 1, 0x40FFFFFF);
                        // 枠を明るく
                        graphics.renderOutline(cellX, cellY, CELL_SIZE, CELL_SIZE, 0xFFFFFFFF);
                    }
                }
            }
        }

        // 魔導板がない場合のヒント
        if (!hasPlate) {
            // グリッド中央にヒントテキストを表示
            String hint = "魔導板を入れてください";
            int textX = guiX + GRID_X + (GRID_SIZE * CELL_SIZE - font.width(hint)) / 2;
            int textY = guiY + GRID_Y + (GRID_SIZE * CELL_SIZE - font.lineHeight) / 2;
            graphics.drawString(font, hint, textX, textY, 0x404040, false);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // タンクツールチップ
        if (isHovering(TANK_X, TANK_Y, TANK_WIDTH, TANK_HEIGHT, mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    Component.translatable("tooltip.arcanotech_works.molten_mithril",
                            menu.getMoltenMithril(), menu.getTankCapacity()),
                    mouseX, mouseY);
        }

        // グリッドツールチップ
        if (menu.hasPlate()) {
            int[] gridPos = getGridPosition(mouseX, mouseY);
            if (gridPos != null) {
                boolean engraved = menu.isEngraved(gridPos[0], gridPos[1]);
                String key = engraved ? "tooltip.arcanotech_works.engraved" : "tooltip.arcanotech_works.click_to_engrave";
                graphics.renderTooltip(this.font, Component.translatable(key), mouseX, mouseY);
            }
        }

        // ステータス表示（右側）
        renderStatus(graphics, x, y);
    }

    /**
     * ステータス情報を表示
     */
    private void renderStatus(GuiGraphics graphics, int guiX, int guiY) {
        int statusX = guiX + 172;
        int statusY = guiY + 20;

        // 溶融魔導銀量
        String mithrilText = menu.getMoltenMithril() + "mb";
        graphics.drawString(font, mithrilText, statusX, statusY, 0x404040, false);

        // 彫刻済みセル数
        if (menu.hasPlate()) {
            int engravedCount = 0;
            for (int gy = 0; gy < GRID_SIZE; gy++) {
                for (int gx = 0; gx < GRID_SIZE; gx++) {
                    if (menu.isEngraved(gx, gy)) engravedCount++;
                }
            }
            String countText = engravedCount + "/49";
            graphics.drawString(font, countText, statusX, statusY + 12, 0x404040, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && menu.hasPlate()) { // 左クリック
            int[] gridPos = getGridPosition(mouseX, mouseY);
            if (gridPos != null) {
                int gx = gridPos[0];
                int gy = gridPos[1];

                // 既に彫刻済みかチェック
                if (menu.isEngraved(gx, gy)) {
                    // 既に彫刻済み - 失敗サウンド
                    playSound(false);
                    showClickEffect(gx, gy, false);
                    return true;
                }

                // 溶融魔導銀チェック
                if (menu.getMoltenMithril() < 1) {
                    // 魔導銀不足
                    playSound(false);
                    showClickEffect(gx, gy, false);
                    if (minecraft != null && minecraft.player != null) {
                        minecraft.player.displayClientMessage(
                                Component.translatable("message.arcanotech_works.no_molten_mithril"), true);
                    }
                    return true;
                }

                // サーバーにクリックパケットを送信
                ModNetwork.CHANNEL.sendToServer(new EngravingClickPacket(
                        menu.getBlockEntity().getBlockPos(), gx, gy));

                // クリック効果（成功想定）
                playSound(true);
                showClickEffect(gx, gy, true);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * クリック効果を表示
     */
    private void showClickEffect(int gx, int gy, boolean success) {
        clickEffectX = gx;
        clickEffectY = gy;
        clickEffectTicks = 10;
        lastClickSuccess = success;
    }

    /**
     * サウンドを再生
     */
    private void playSound(boolean success) {
        Minecraft mc = Minecraft.getInstance();
        if (success) {
            // 彫刻成功サウンド
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.GRINDSTONE_USE, 1.5f, 0.5f));
        } else {
            // 失敗サウンド
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BASS.get(), 0.5f, 0.5f));
        }
    }

    /**
     * マウス位置からグリッド座標を取得
     */
    private int[] getGridPosition(double mouseX, double mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        int relX = (int) mouseX - x - GRID_X;
        int relY = (int) mouseY - y - GRID_Y;

        if (relX >= 0 && relX < GRID_SIZE * CELL_SIZE &&
            relY >= 0 && relY < GRID_SIZE * CELL_SIZE) {
            int gx = relX / CELL_SIZE;
            int gy = relY / CELL_SIZE;
            if (gx >= 0 && gx < GRID_SIZE && gy >= 0 && gy < GRID_SIZE) {
                return new int[] {gx, gy};
            }
        }
        return null;
    }
}
