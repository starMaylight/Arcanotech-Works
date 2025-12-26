package com.starmaylight.arcanotech_works.block.engraving;

import com.starmaylight.arcanotech_works.registry.ModBlocks;
import com.starmaylight.arcanotech_works.registry.ModItems;
import com.starmaylight.arcanotech_works.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

/**
 * 刻印台のGUIメニュー
 */
public class EngravingTableMenu extends AbstractContainerMenu {

    private final EngravingTableBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    // スロット位置定数（調整済み）
    private static final int FUEL_X = 8;
    private static final int FUEL_Y = 20;
    private static final int MITHRIL_X = 8;
    private static final int MITHRIL_Y = 42;
    private static final int CHISEL_X = 8;
    private static final int CHISEL_Y = 64;
    private static final int PLATE_X = 8;
    private static final int PLATE_Y = 86;
    private static final int OUTPUT_X = 178;
    private static final int OUTPUT_Y = 70;
    
    // プレイヤーインベントリ（調整済み）
    private static final int PLAYER_INV_X = 30;
    private static final int PLAYER_INV_Y = 140;
    private static final int HOTBAR_Y = 198;

    public EngravingTableMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory,
                playerInventory.player.level().getBlockEntity(extraData.readBlockPos()),
                new SimpleContainerData(10)); // 3 + 7 rows
    }

    public EngravingTableMenu(int containerId, Inventory playerInventory, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.ENGRAVING_TABLE.get(), containerId);

        this.blockEntity = (EngravingTableBlockEntity) entity;
        this.level = playerInventory.player.level();
        this.data = data;

        addDataSlots(data);

        // 機械スロット
        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            // 燃料スロット
            this.addSlot(new SlotItemHandler(handler, EngravingTableBlockEntity.FUEL_SLOT, FUEL_X, FUEL_Y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.is(Items.COAL) || stack.is(Items.CHARCOAL) 
                            || stack.is(Items.COAL_BLOCK) || stack.is(Items.LAVA_BUCKET);
                }
            });

            // 魔導銀スロット
            this.addSlot(new SlotItemHandler(handler, EngravingTableBlockEntity.MITHRIL_SLOT, MITHRIL_X, MITHRIL_Y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.is(ModItems.MITHRIL_INGOT.get());
                }
            });

            // 彫刻刀スロット
            this.addSlot(new SlotItemHandler(handler, EngravingTableBlockEntity.CHISEL_SLOT, CHISEL_X, CHISEL_Y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.is(ModItems.ENGRAVING_TOOL.get());
                }
            });

            // 魔導板スロット
            this.addSlot(new SlotItemHandler(handler, EngravingTableBlockEntity.PLATE_SLOT, PLATE_X, PLATE_Y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.is(ModItems.ARCANE_PLATE.get());
                }
            });

            // 出力スロット
            this.addSlot(new SlotItemHandler(handler, EngravingTableBlockEntity.OUTPUT_SLOT, OUTPUT_X, OUTPUT_Y) {
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
                        PLAYER_INV_X + col * 18, PLAYER_INV_Y + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, PLAYER_INV_X + col * 18, HOTBAR_Y));
        }
    }

    /**
     * グリッドをクリックして彫刻
     */
    public boolean clickGrid(int gridX, int gridY, Player player) {
        return blockEntity.engrave(gridX, gridY, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            // 機械スロット(0-4)からプレイヤーインベントリへ
            if (index < 5) {
                if (!this.moveItemStackTo(slotStack, 5, 41, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // プレイヤーインベントリから機械スロットへ
            else {
                // 燃料
                if (slotStack.is(Items.COAL) || slotStack.is(Items.CHARCOAL) 
                        || slotStack.is(Items.COAL_BLOCK) || slotStack.is(Items.LAVA_BUCKET)) {
                    if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                // 魔導銀
                else if (slotStack.is(ModItems.MITHRIL_INGOT.get())) {
                    if (!this.moveItemStackTo(slotStack, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                // 彫刻刀
                else if (slotStack.is(ModItems.ENGRAVING_TOOL.get())) {
                    if (!this.moveItemStackTo(slotStack, 2, 3, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                // 魔導板
                else if (slotStack.is(ModItems.ARCANE_PLATE.get())) {
                    if (!this.moveItemStackTo(slotStack, 3, 4, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                // インベントリ↔ホットバー
                else if (index < 32) {
                    if (!this.moveItemStackTo(slotStack, 32, 41, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 41) {
                    if (!this.moveItemStackTo(slotStack, 5, 32, false)) {
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
                player, ModBlocks.ENGRAVING_TABLE.get());
    }

    // ========== データ取得（ContainerDataから） ==========

    public int getMoltenMithril() {
        return data.get(0);
    }

    public int getTankCapacity() {
        return data.get(1);
    }

    public boolean hasPlate() {
        return data.get(2) == 1;
    }

    /**
     * グリッドが彫刻済みかどうか（ContainerDataから取得）
     */
    public boolean isEngraved(int x, int y) {
        if (x < 0 || x >= 7 || y < 0 || y >= 7) return false;
        int rowBits = data.get(3 + y); // index 3-9が各行
        return (rowBits & (1 << x)) != 0;
    }

    public float getTankFillPercent() {
        int capacity = getTankCapacity();
        if (capacity == 0) return 0;
        return getMoltenMithril() / (float) capacity;
    }

    public EngravingTableBlockEntity getBlockEntity() {
        return blockEntity;
    }
}
