package com.starmaylight.arcanotech_works.menu;

import com.starmaylight.arcanotech_works.block.refinery.ManaRefineryBlockEntity;
import com.starmaylight.arcanotech_works.item.ManaGemItem;
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
 * 魔石精錬機のGUIメニュー
 */
public class ManaRefineryMenu extends AbstractContainerMenu {

    private final ManaRefineryBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    // スロット位置
    private static final int INPUT_SLOT_X = 56;
    private static final int INPUT_SLOT_Y = 35;
    private static final int OUTPUT_SLOT_X = 116;
    private static final int OUTPUT_SLOT_Y = 35;

    public ManaRefineryMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory,
                playerInventory.player.level().getBlockEntity(extraData.readBlockPos()),
                new SimpleContainerData(6));
    }

    public ManaRefineryMenu(int containerId, Inventory playerInventory, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.MANA_REFINERY.get(), containerId);

        this.blockEntity = (ManaRefineryBlockEntity) entity;
        this.level = playerInventory.player.level();
        this.data = data;

        addDataSlots(data);

        // 入力スロット（魔石のみ）
        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            // 入力スロット
            this.addSlot(new SlotItemHandler(handler, 0, INPUT_SLOT_X, INPUT_SLOT_Y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.getItem() instanceof ManaGemItem;
                }
            });

            // 出力スロット（取り出しのみ）
            this.addSlot(new SlotItemHandler(handler, 1, OUTPUT_SLOT_X, OUTPUT_SLOT_Y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
        });

        // プレイヤーインベントリ
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        8 + col * 18, 84 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            // 機械スロット(0-1)からプレイヤーインベントリへ
            if (index < 2) {
                if (!this.moveItemStackTo(slotStack, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // プレイヤーインベントリから入力スロットへ
            else {
                if (slotStack.getItem() instanceof ManaGemItem) {
                    if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                // インベントリ↔ホットバー
                else if (index < 29) {
                    if (!this.moveItemStackTo(slotStack, 29, 38, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 38) {
                    if (!this.moveItemStackTo(slotStack, 2, 29, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, ModBlocks.MANA_REFINERY.get());
    }

    // ========== データ取得 ==========

    public int getRefineProgress() {
        return data.get(0);
    }

    public int getRefineTarget() {
        return data.get(1);
    }

    public int getManaStored() {
        return data.get(2);
    }

    public int getMaxMana() {
        return data.get(3);
    }

    public int getCurrentInputQuality() {
        return data.get(4);
    }

    public int getTargetOutputQuality() {
        return data.get(5);
    }

    public float getRefineProgressPercent() {
        int target = getRefineTarget();
        if (target == 0) return 0;
        return getRefineProgress() / (float) target;
    }

    public float getManaProgress() {
        int max = getMaxMana();
        if (max == 0) return 0;
        return getManaStored() / (float) max;
    }

    public boolean isRefining() {
        return getRefineTarget() > 0;
    }

    public ManaRefineryBlockEntity getBlockEntity() {
        return blockEntity;
    }
}
