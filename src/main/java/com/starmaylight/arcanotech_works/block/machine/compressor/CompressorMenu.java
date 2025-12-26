package com.starmaylight.arcanotech_works.block.machine.compressor;

import com.starmaylight.arcanotech_works.registry.ModBlocks;
import com.starmaylight.arcanotech_works.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

/**
 * 圧縮機のメニュー（GUI）
 */
public class CompressorMenu extends AbstractContainerMenu {

    private final CompressorBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    // スロット位置
    private static final int INPUT_SLOT_X = 56;
    private static final int INPUT_SLOT_Y = 35;
    private static final int OUTPUT_SLOT_X = 116;
    private static final int OUTPUT_SLOT_Y = 35;
    private static final int COOLING_SLOT_X = 8;
    private static final int COOLING_SLOT_Y = 62;

    // プレイヤーインベントリ位置
    private static final int PLAYER_INV_X = 8;
    private static final int PLAYER_INV_Y = 84;
    private static final int PLAYER_HOTBAR_Y = 142;

    public CompressorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, 
             playerInventory.player.level().getBlockEntity(buf.readBlockPos()),
             new SimpleContainerData(6));
    }

    public CompressorMenu(int containerId, Inventory playerInventory, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.COMPRESSOR.get(), containerId);
        
        checkContainerSize(playerInventory, CompressorBlockEntity.SLOT_COUNT);
        this.blockEntity = (CompressorBlockEntity) entity;
        this.level = playerInventory.player.level();
        this.data = data;

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);

        // 機械スロット
        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            // 入力スロット
            this.addSlot(new SlotItemHandler(handler, CompressorBlockEntity.INPUT_SLOT, INPUT_SLOT_X, INPUT_SLOT_Y));
            // 出力スロット
            this.addSlot(new SlotItemHandler(handler, CompressorBlockEntity.OUTPUT_SLOT, OUTPUT_SLOT_X, OUTPUT_SLOT_Y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
            // 冷却スロット
            this.addSlot(new SlotItemHandler(handler, CompressorBlockEntity.COOLING_SLOT, COOLING_SLOT_X, COOLING_SLOT_Y));
        });

        addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, ModBlocks.COMPRESSOR.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            // 機械スロット（36-38）からプレイヤーインベントリへ
            if (index >= 36) {
                if (!this.moveItemStackTo(slotStack, 0, 36, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // プレイヤーインベントリ（0-35）から機械スロットへ
            else {
                // 冷却アイテムは冷却スロットへ
                if (isCoolingItem(slotStack)) {
                    if (!this.moveItemStackTo(slotStack, 38, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                // それ以外は入力スロットへ
                else if (!this.moveItemStackTo(slotStack, 36, 37, false)) {
                    // 入力に入らなければホットバー⇔インベントリの移動
                    if (index < 27) {
                        if (!this.moveItemStackTo(slotStack, 27, 36, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        if (!this.moveItemStackTo(slotStack, 0, 27, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return itemstack;
    }

    private boolean isCoolingItem(ItemStack stack) {
        return stack.is(com.starmaylight.arcanotech_works.registry.ModItems.COOLING_FAN.get()) ||
               stack.is(com.starmaylight.arcanotech_works.registry.ModItems.COOLING_CORE.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 
                        PLAYER_INV_X + col * 18, PLAYER_INV_Y + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, PLAYER_INV_X + col * 18, PLAYER_HOTBAR_Y));
        }
    }

    // ==================== データ取得 ====================

    public CompressorBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public int getProgress() {
        return data.get(0);
    }

    public int getMaxProgress() {
        return data.get(1);
    }

    public int getHeat() {
        return data.get(2);
    }

    public int getMaxHeat() {
        return data.get(3);
    }

    public int getMana() {
        return data.get(4);
    }

    public int getMaxMana() {
        return data.get(5);
    }

    public float getProgressPercent() {
        int max = getMaxProgress();
        return max > 0 ? (float) getProgress() / max : 0f;
    }

    public float getHeatPercent() {
        int max = getMaxHeat();
        return max > 0 ? (float) getHeat() / max : 0f;
    }

    public float getManaPercent() {
        int max = getMaxMana();
        return max > 0 ? (float) getMana() / max : 0f;
    }
}
