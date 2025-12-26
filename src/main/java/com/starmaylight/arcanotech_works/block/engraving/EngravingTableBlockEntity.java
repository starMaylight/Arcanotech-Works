package com.starmaylight.arcanotech_works.block.engraving;

import com.starmaylight.arcanotech_works.blockentity.ModBlockEntities;
import com.starmaylight.arcanotech_works.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 刻印台のBlockEntity
 */
public class EngravingTableBlockEntity extends BlockEntity implements MenuProvider {

    // スロット番号
    public static final int FUEL_SLOT = 0;      // 燃料
    public static final int MITHRIL_SLOT = 1;   // 魔導銀インゴット
    public static final int CHISEL_SLOT = 2;    // 彫刻刀
    public static final int PLATE_SLOT = 3;     // 魔導板入力
    public static final int OUTPUT_SLOT = 4;    // 出力
    public static final int SLOT_COUNT = 5;
    
    // 溶融魔導銀タンク
    private int moltenMithril = 0;
    private static final int TANK_CAPACITY = 630;
    private static final int MELT_AMOUNT = 90;    // 1インゴット = 90mb
    private static final int ENGRAVE_COST = 1;    // 1回の彫刻 = 1mb
    
    // 7x7刻印グリッド（trueは彫刻済み）
    private final boolean[][] engravingGrid = new boolean[7][7];
    
    // 作業中かどうか（魔導板が投入済み）
    private boolean hasPlate = false;
    
    // インベントリ
    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            // 魔導板スロットに魔導板が入った場合、作業開始
            if (slot == PLATE_SLOT && !getStackInSlot(PLATE_SLOT).isEmpty()) {
                tryInsertPlate();
            }
        }
        
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case FUEL_SLOT -> isFuel(stack);
                case MITHRIL_SLOT -> stack.is(ModItems.MITHRIL_INGOT.get());
                case CHISEL_SLOT -> stack.is(ModItems.ENGRAVING_TOOL.get());
                case PLATE_SLOT -> stack.is(ModItems.ARCANE_PLATE.get());
                case OUTPUT_SLOT -> false; // 出力専用
                default -> false;
            };
        }
    };
    
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    
    // GUI同期用データ（拡張版：グリッド状態を含む）
    // index 0: moltenMithril
    // index 1: TANK_CAPACITY
    // index 2: hasPlate
    // index 3-9: gridRow0-6（各行のビットマスク）
    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> moltenMithril;
                case 1 -> TANK_CAPACITY;
                case 2 -> hasPlate ? 1 : 0;
                case 3, 4, 5, 6, 7, 8, 9 -> getGridRowBits(index - 3);
                default -> 0;
            };
        }
        
        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> moltenMithril = value;
                case 2 -> hasPlate = value == 1;
                case 3, 4, 5, 6, 7, 8, 9 -> setGridRowBits(index - 3, value);
            }
        }
        
        @Override
        public int getCount() {
            return 10; // 3 + 7 rows
        }
    };
    
    /**
     * 指定行のビットマスクを取得（7ビット）
     */
    private int getGridRowBits(int row) {
        if (row < 0 || row >= 7) return 0;
        int bits = 0;
        for (int x = 0; x < 7; x++) {
            if (engravingGrid[row][x]) {
                bits |= (1 << x);
            }
        }
        return bits;
    }
    
    /**
     * 指定行のビットマスクを設定
     */
    private void setGridRowBits(int row, int bits) {
        if (row < 0 || row >= 7) return;
        for (int x = 0; x < 7; x++) {
            engravingGrid[row][x] = (bits & (1 << x)) != 0;
        }
    }
    
    public EngravingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENGRAVING_TABLE.get(), pos, state);
    }
    
    // ========== 刻印ロジック ==========
    
    /**
     * 魔導板スロットから魔導板を投入
     */
    private void tryInsertPlate() {
        if (hasPlate) return;
        
        ItemStack plateStack = itemHandler.getStackInSlot(PLATE_SLOT);
        if (!plateStack.isEmpty() && plateStack.is(ModItems.ARCANE_PLATE.get())) {
            hasPlate = true;
            plateStack.shrink(1);
            clearGrid();
            setChanged();
        }
    }
    
    /**
     * グリッドをクリア
     */
    private void clearGrid() {
        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 7; x++) {
                engravingGrid[y][x] = false;
            }
        }
    }
    
    /**
     * 指定位置を彫刻
     */
    public boolean engrave(int x, int y, Player player) {
        if (!hasPlate || x < 0 || x >= 7 || y < 0 || y >= 7) {
            return false;
        }
        
        // 既に彫刻済み
        if (engravingGrid[y][x]) {
            return false;
        }
        
        // 溶融魔導銀チェック
        if (moltenMithril < ENGRAVE_COST) {
            return false;
        }
        
        // 彫刻刀チェックと消費
        ItemStack chisel = itemHandler.getStackInSlot(CHISEL_SLOT);
        if (chisel.isEmpty() || !chisel.is(ModItems.ENGRAVING_TOOL.get())) {
            return false;
        }
        
        // 彫刻実行
        engravingGrid[y][x] = true;
        moltenMithril -= ENGRAVE_COST;
        
        // 彫刻刀の耐久消費
        if (chisel.isDamageableItem()) {
            int newDamage = chisel.getDamageValue() + 1;
            if (newDamage >= chisel.getMaxDamage()) {
                // 彫刻刀が壊れた
                itemHandler.setStackInSlot(CHISEL_SLOT, ItemStack.EMPTY);
                level.playSound(null, worldPosition, net.minecraft.sounds.SoundEvents.ITEM_BREAK, 
                        net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
            } else {
                chisel.setDamageValue(newDamage);
            }
        }
        
        setChanged();
        
        // パターンチェック
        checkPattern();
        
        return true;
    }
    
    /**
     * パターンをチェックして出力を生成
     */
    private void checkPattern() {
        // 出力スロットが空でない場合はスキップ
        if (!itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty()) {
            return;
        }
        
        // デバッグ: 現在のグリッド状態をログ
        int engravedCount = 0;
        StringBuilder gridStr = new StringBuilder("\nGrid state:");
        for (int y = 0; y < 7; y++) {
            gridStr.append("\n");
            for (int x = 0; x < 7; x++) {
                gridStr.append(engravingGrid[y][x] ? "#" : ".");
                if (engravingGrid[y][x]) engravedCount++;
            }
        }
        com.starmaylight.arcanotech_works.Arcanotech_works.LOGGER.info(
                "Checking pattern - Engraved: {}/49, Patterns available: {}{}",
                engravedCount, EngravingPattern.getPatternCount(), gridStr);
        
        // すべて彫刻済み → 劣化した魔導板
        if (EngravingPattern.isFullyEngraved(engravingGrid)) {
            itemHandler.setStackInSlot(OUTPUT_SLOT, new ItemStack(ModItems.DEGRADED_PLATE.get()));
            hasPlate = false;
            clearGrid();
            setChanged();
            return;
        }
        
        // パターンマッチング
        EngravingPattern matched = EngravingPattern.findMatch(engravingGrid);
        if (matched != null) {
            com.starmaylight.arcanotech_works.Arcanotech_works.LOGGER.info(
                    "Pattern matched: {} -> {}", matched.getId(), matched.getOutput());
            itemHandler.setStackInSlot(OUTPUT_SLOT, new ItemStack(matched.getOutput()));
            hasPlate = false;
            clearGrid();
            setChanged();
        }
    }
    
    /**
     * 魔導銀を溶融
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, EngravingTableBlockEntity entity) {
        if (level.isClientSide) return;
        
        // 燃料と魔導銀があれば溶融
        ItemStack fuel = entity.itemHandler.getStackInSlot(FUEL_SLOT);
        ItemStack mithril = entity.itemHandler.getStackInSlot(MITHRIL_SLOT);
        
        if (!fuel.isEmpty() && isFuel(fuel) && !mithril.isEmpty() 
                && mithril.is(ModItems.MITHRIL_INGOT.get())
                && entity.moltenMithril + MELT_AMOUNT <= TANK_CAPACITY) {
            
            // 消費して溶融
            fuel.shrink(1);
            mithril.shrink(1);
            entity.moltenMithril += MELT_AMOUNT;
            entity.setChanged();
        }
    }
    
    /**
     * 燃料かどうかチェック
     */
    private static boolean isFuel(ItemStack stack) {
        return stack.is(Items.COAL) || stack.is(Items.CHARCOAL) 
                || stack.is(Items.COAL_BLOCK) || stack.is(Items.LAVA_BUCKET);
    }
    
    // ========== Getters ==========
    
    public boolean[][] getEngravingGrid() {
        return engravingGrid;
    }
    
    public boolean isEngraved(int x, int y) {
        if (x < 0 || x >= 7 || y < 0 || y >= 7) return false;
        return engravingGrid[y][x];
    }
    
    public boolean hasPlate() {
        return hasPlate;
    }
    
    public int getMoltenMithril() {
        return moltenMithril;
    }
    
    public int getTankCapacity() {
        return TANK_CAPACITY;
    }
    
    // ========== NBT ==========
    
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", itemHandler.serializeNBT());
        tag.putInt("MoltenMithril", moltenMithril);
        tag.putBoolean("HasPlate", hasPlate);
        
        // グリッド保存
        int[] gridData = new int[49];
        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 7; x++) {
                gridData[y * 7 + x] = engravingGrid[y][x] ? 1 : 0;
            }
        }
        tag.putIntArray("EngravingGrid", gridData);
    }
    
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        
        // インベントリを読み込み（古いデータ対応）
        CompoundTag invTag = tag.getCompound("Inventory");
        if (invTag.contains("Items")) {
            net.minecraft.nbt.ListTag items = invTag.getList("Items", 10);
            for (int i = 0; i < items.size(); i++) {
                CompoundTag itemTag = items.getCompound(i);
                int slot = itemTag.getInt("Slot");
                if (slot >= 0 && slot < SLOT_COUNT) {
                    itemHandler.setStackInSlot(slot, ItemStack.of(itemTag));
                }
            }
        }
        
        moltenMithril = tag.getInt("MoltenMithril");
        hasPlate = tag.getBoolean("HasPlate");
        
        // グリッド読み込み
        int[] gridData = tag.getIntArray("EngravingGrid");
        if (gridData.length == 49) {
            for (int y = 0; y < 7; y++) {
                for (int x = 0; x < 7; x++) {
                    engravingGrid[y][x] = gridData[y * 7 + x] == 1;
                }
            }
        }
    }
    
    // ========== Capability ==========
    
    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }
    
    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }
    
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }
    
    // ========== Menu ==========
    
    @Override
    public Component getDisplayName() {
        return Component.translatable("block.arcanotech_works.engraving_table");
    }
    
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new EngravingTableMenu(containerId, playerInventory, this, this.data);
    }
    
    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }
    
    public IItemHandler getItemHandler() {
        return itemHandler;
    }
}
