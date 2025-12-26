package com.starmaylight.arcanotech_works.menu;

import com.starmaylight.arcanotech_works.block.generator.ManaGeneratorBlockEntity;
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
 * 魔石燃焼炉のGUIメニュー
 */
public class ManaGeneratorMenu extends AbstractContainerMenu {

    private final ManaGeneratorBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    // スロット位置
    private static final int FUEL_SLOT_X = 80;
    private static final int FUEL_SLOT_Y = 35;

    public ManaGeneratorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, 
                playerInventory.player.level().getBlockEntity(extraData.readBlockPos()),
                new SimpleContainerData(5));
    }

    public ManaGeneratorMenu(int containerId, Inventory playerInventory, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.MANA_GENERATOR.get(), containerId);
        
        this.blockEntity = (ManaGeneratorBlockEntity) entity;
        this.level = playerInventory.player.level();
        this.data = data;

        addDataSlots(data);

        // 燃料スロット
        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            this.addSlot(new SlotItemHandler(handler, 0, FUEL_SLOT_X, FUEL_SLOT_Y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.getItem() instanceof ManaGemItem;
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

            // 燃料スロット(0)からプレイヤーインベントリへ
            if (index == 0) {
                if (!this.moveItemStackTo(slotStack, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // プレイヤーインベントリから燃料スロットへ
            else {
                if (slotStack.getItem() instanceof ManaGemItem) {
                    if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                // インベントリ↔ホットバー
                else if (index < 28) {
                    if (!this.moveItemStackTo(slotStack, 28, 37, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 37) {
                    if (!this.moveItemStackTo(slotStack, 1, 28, false)) {
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
                player, ModBlocks.MANA_GENERATOR.get());
    }

    // ========== データ取得 ==========

    public int getBurnTime() {
        return data.get(0);
    }

    public int getBurnTimeTotal() {
        return data.get(1);
    }

    public int getManaStored() {
        return data.get(2);
    }

    public int getMaxMana() {
        return data.get(3);
    }

    public int getCurrentManaGeneration() {
        return data.get(4);
    }

    public float getBurnProgress() {
        int total = getBurnTimeTotal();
        if (total == 0) return 0;
        return 1.0f - (getBurnTime() / (float) total);
    }

    public float getManaProgress() {
        int max = getMaxMana();
        if (max == 0) return 0;
        return getManaStored() / (float) max;
    }

    public boolean isBurning() {
        return getBurnTime() > 0;
    }

    public ManaGeneratorBlockEntity getBlockEntity() {
        return blockEntity;
    }
}
