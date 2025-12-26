package com.starmaylight.arcanotech_works.block.machine.rolling_mill;

import com.starmaylight.arcanotech_works.block.machine.AbstractMachineBlockEntity;
import com.starmaylight.arcanotech_works.block.machine.MachineTier;
import com.starmaylight.arcanotech_works.blockentity.ModBlockEntities;
import com.starmaylight.arcanotech_works.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/**
 * 圧延機のBlockEntity
 * インベントリ構成:
 * - スロット0: 入力（インゴット）
 * - スロット1: 出力（プレート）
 * - スロット2: ファン/冷却コアスロット
 */
public class RollingMillBlockEntity extends AbstractMachineBlockEntity {

    // スロット定義
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int COOLING_SLOT = 2;
    public static final int SLOT_COUNT = 3;

    // 処理パラメータ
    private static final int BASE_PROCESS_TIME = 100;  // 5秒
    private static final int MANA_CAPACITY = 10000;
    private static final int MANA_MAX_INPUT = 100;
    private static final int MANA_PER_OPERATION = 30;
    private static final int HEAT_PER_OPERATION = 12;

    // インベントリ
    private final ItemStackHandler inventory = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case INPUT_SLOT -> true;  // TODO: レシピで有効な入力かチェック
                case OUTPUT_SLOT -> false;
                case COOLING_SLOT -> isFan(stack) || isCoolingCore(stack);
                default -> false;
            };
        }
    };

    // GUI同期用データ
    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> maxProgress;
                case 2 -> heatStorage.getHeat();
                case 3 -> heatStorage.getMaxHeat();
                case 4 -> manaStorage.getManaStored();
                case 5 -> manaStorage.getMaxManaStored();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> maxProgress = value;
                case 2 -> heatStorage.setHeat(value);
                case 4 -> manaStorage.setMana(value);
            }
        }

        @Override
        public int getCount() {
            return 6;
        }
    };

    public RollingMillBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ROLLING_MILL.get(), pos, state);
    }

    // ==================== 抽象メソッド実装 ====================

    @Override
    protected ItemStackHandler getInventory() {
        return inventory;
    }

    @Override
    public Container getDroppableInventory() {
        SimpleContainer container = new SimpleContainer(SLOT_COUNT);
        for (int i = 0; i < SLOT_COUNT; i++) {
            container.setItem(i, inventory.getStackInSlot(i));
        }
        return container;
    }

    @Override
    protected int getFanSlotIndex() {
        return COOLING_SLOT;
    }

    @Override
    protected int getCoolingCoreSlotIndex() {
        return COOLING_SLOT;
    }

    @Override
    protected int getManaCapacity() {
        return MANA_CAPACITY;
    }

    @Override
    protected int getManaMaxInput() {
        return MANA_MAX_INPUT;
    }

    @Override
    protected int getManaPerOperation() {
        return MANA_PER_OPERATION;
    }

    @Override
    protected int getHeatPerOperation() {
        return HEAT_PER_OPERATION;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.arcanotech_works.rolling_mill");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new RollingMillMenu(containerId, playerInventory, this, data);
    }

    // ==================== 処理ロジック ====================

    @Override
    protected boolean canProcess() {
        if (!super.canProcess()) return false;
        
        ItemStack input = inventory.getStackInSlot(INPUT_SLOT);
        if (input.isEmpty()) return false;

        // TODO: レシピマネージャーから出力を取得
        ItemStack result = getRollingResult(input);
        if (result.isEmpty()) return false;

        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) return true;
        if (!ItemStack.isSameItemSameTags(output, result)) return false;
        return output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    @Override
    protected boolean processRecipe() {
        ItemStack input = inventory.getStackInSlot(INPUT_SLOT);
        if (input.isEmpty()) {
            progress = 0;
            return false;
        }

        ItemStack result = getRollingResult(input);
        if (result.isEmpty()) {
            progress = 0;
            return false;
        }

        // 処理時間を計算（Tierによる速度ボーナス）
        MachineTier tier = getTier();
        int processTime = tier.calculateProcessTime(BASE_PROCESS_TIME);

        if (maxProgress == 0) {
            maxProgress = processTime;
        }

        progress++;

        if (progress >= maxProgress) {
            // 処理完了
            input.shrink(1);
            
            ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
            if (output.isEmpty()) {
                inventory.setStackInSlot(OUTPUT_SLOT, result.copy());
            } else {
                output.grow(result.getCount());
            }

            // 魔力消費
            manaStorage.extractMana(getManaPerOperation(), false);

            progress = 0;
            maxProgress = 0;
            return true;
        }

        return false;
    }

    /**
     * 圧延結果を取得
     * TODO: レシピマネージャーに置き換え
     */
    private ItemStack getRollingResult(ItemStack input) {
        // 仮実装: インゴット→プレート
        
        // 鉄インゴット → 鉄プレート
        if (input.is(Items.IRON_INGOT)) {
            return new ItemStack(ModItems.IRON_PLATE.get());
        }
        
        // 金インゴット → 金プレート
        if (input.is(Items.GOLD_INGOT)) {
            return new ItemStack(ModItems.GOLD_PLATE.get());
        }
        
        // 銅インゴット → 銅プレート
        if (input.is(Items.COPPER_INGOT)) {
            return new ItemStack(ModItems.COPPER_PLATE.get());
        }
        
        // 魔導銀インゴット → 魔導銀プレート
        if (input.is(ModItems.MITHRIL_INGOT.get())) {
            return new ItemStack(ModItems.MITHRIL_PLATE.get());
        }

        return ItemStack.EMPTY;
    }

    // ==================== ゲッター ====================

    public ContainerData getData() {
        return data;
    }
}
