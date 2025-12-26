package com.starmaylight.arcanotech_works.block.machine.mixer;

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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.SlotItemHandler;

/**
 * 混合機のメニュー（GUI）
 */
public class MixerMenu extends AbstractContainerMenu {

    private final MixerBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    // アイテム入力スロット位置（2x2グリッド）
    private static final int INPUT_SLOT_1_X = 44;
    private static final int INPUT_SLOT_1_Y = 17;
    private static final int INPUT_SLOT_2_X = 62;
    private static final int INPUT_SLOT_2_Y = 17;
    private static final int INPUT_SLOT_3_X = 44;
    private static final int INPUT_SLOT_3_Y = 35;
    private static final int INPUT_SLOT_4_X = 62;
    private static final int INPUT_SLOT_4_Y = 35;

    // 出力スロット位置
    private static final int OUTPUT_SLOT_X = 116;
    private static final int OUTPUT_SLOT_Y = 35;

    // 冷却スロット位置
    private static final int COOLING_SLOT_X = 8;
    private static final int COOLING_SLOT_Y = 62;

    // プレイヤーインベントリ位置
    private static final int PLAYER_INV_X = 8;
    private static final int PLAYER_INV_Y = 84;
    private static final int PLAYER_HOTBAR_Y = 142;

    public MixerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, 
             playerInventory.player.level().getBlockEntity(buf.readBlockPos()),
             new SimpleContainerData(10));
    }

    public MixerMenu(int containerId, Inventory playerInventory, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.MIXER.get(), containerId);
        
        checkContainerSize(playerInventory, MixerBlockEntity.SLOT_COUNT);
        this.blockEntity = (MixerBlockEntity) entity;
        this.level = playerInventory.player.level();
        this.data = data;

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);

        // 機械スロット
        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            // 入力スロット（2x2グリッド）
            this.addSlot(new SlotItemHandler(handler, MixerBlockEntity.INPUT_SLOT_1, INPUT_SLOT_1_X, INPUT_SLOT_1_Y));
            this.addSlot(new SlotItemHandler(handler, MixerBlockEntity.INPUT_SLOT_2, INPUT_SLOT_2_X, INPUT_SLOT_2_Y));
            this.addSlot(new SlotItemHandler(handler, MixerBlockEntity.INPUT_SLOT_3, INPUT_SLOT_3_X, INPUT_SLOT_3_Y));
            this.addSlot(new SlotItemHandler(handler, MixerBlockEntity.INPUT_SLOT_4, INPUT_SLOT_4_X, INPUT_SLOT_4_Y));
            // 出力スロット
            this.addSlot(new SlotItemHandler(handler, MixerBlockEntity.OUTPUT_SLOT, OUTPUT_SLOT_X, OUTPUT_SLOT_Y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
            // 冷却スロット
            this.addSlot(new SlotItemHandler(handler, MixerBlockEntity.COOLING_SLOT, COOLING_SLOT_X, COOLING_SLOT_Y));
        });

        addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, ModBlocks.MIXER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            // 機械スロット（36-41）からプレイヤーインベントリへ
            if (index >= 36) {
                if (!this.moveItemStackTo(slotStack, 0, 36, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // プレイヤーインベントリ（0-35）から機械スロットへ
            else {
                // 冷却アイテムは冷却スロットへ
                if (isCoolingItem(slotStack)) {
                    if (!this.moveItemStackTo(slotStack, 41, 42, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                // それ以外は入力スロットへ（4スロット）
                else if (!this.moveItemStackTo(slotStack, 36, 40, false)) {
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

    public MixerBlockEntity getBlockEntity() {
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

    public int getInputTank1Amount() {
        return data.get(6);
    }

    public int getInputTank2Amount() {
        return data.get(7);
    }

    public int getOutputTank1Amount() {
        return data.get(8);
    }

    public int getOutputTank2Amount() {
        return data.get(9);
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

    public float getInputTank1Percent() {
        return (float) getInputTank1Amount() / MixerBlockEntity.TANK_CAPACITY;
    }

    public float getInputTank2Percent() {
        return (float) getInputTank2Amount() / MixerBlockEntity.TANK_CAPACITY;
    }

    public float getOutputTank1Percent() {
        return (float) getOutputTank1Amount() / MixerBlockEntity.TANK_CAPACITY;
    }

    public float getOutputTank2Percent() {
        return (float) getOutputTank2Amount() / MixerBlockEntity.TANK_CAPACITY;
    }

    // 液体種類の取得（クライアント側で使用）
    public FluidStack getInputFluid1() {
        return blockEntity.getInputTank1().getFluid();
    }

    public FluidStack getInputFluid2() {
        return blockEntity.getInputTank2().getFluid();
    }

    public FluidStack getOutputFluid1() {
        return blockEntity.getOutputTank1().getFluid();
    }

    public FluidStack getOutputFluid2() {
        return blockEntity.getOutputTank2().getFluid();
    }
}
