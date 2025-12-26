package com.starmaylight.arcanotech_works.block.machine.crusher;

import com.starmaylight.arcanotech_works.block.machine.AbstractMachineBlockEntity;
import com.starmaylight.arcanotech_works.block.machine.MachineTier;
import com.starmaylight.arcanotech_works.blockentity.ModBlockEntities;
import com.starmaylight.arcanotech_works.recipe.CrusherRecipe;
import com.starmaylight.arcanotech_works.recipe.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * 粉砕機のBlockEntity
 * インベントリ構成:
 * - スロット0: 入力
 * - スロット1: 出力
 * - スロット2: ファン/冷却コアスロット
 */
public class CrusherBlockEntity extends AbstractMachineBlockEntity {

    // スロット定義
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int COOLING_SLOT = 2;
    public static final int SLOT_COUNT = 3;

    // 処理パラメータ
    private static final int MANA_CAPACITY = 10000;
    private static final int MANA_MAX_INPUT = 100;
    private static final int MANA_PER_OPERATION = 50;
    private static final int HEAT_PER_OPERATION = 10;

    // 現在のレシピキャッシュ
    private CrusherRecipe currentRecipe = null;

    // インベントリ
    private final ItemStackHandler inventory = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (slot == INPUT_SLOT) {
                currentRecipe = null;  // 入力変更時にレシピキャッシュをクリア
            }
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case INPUT_SLOT -> true;
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

    public CrusherBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRUSHER.get(), pos, state);
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
        return Component.translatable("block.arcanotech_works.crusher");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CrusherMenu(containerId, playerInventory, this, data);
    }

    // ==================== レシピ処理 ====================

    /**
     * 現在の入力に対するレシピを取得
     */
    private Optional<CrusherRecipe> getRecipe() {
        if (level == null) return Optional.empty();
        
        if (currentRecipe != null) {
            SimpleContainer container = new SimpleContainer(1);
            container.setItem(0, inventory.getStackInSlot(INPUT_SLOT));
            if (currentRecipe.matches(container, level)) {
                return Optional.of(currentRecipe);
            }
        }

        SimpleContainer container = new SimpleContainer(1);
        container.setItem(0, inventory.getStackInSlot(INPUT_SLOT));
        
        Optional<CrusherRecipe> recipe = level.getRecipeManager()
                .getRecipeFor(ModRecipes.CRUSHER_TYPE.get(), container, level);
        
        recipe.ifPresent(r -> currentRecipe = r);
        return recipe;
    }

    @Override
    protected boolean canProcess() {
        if (!super.canProcess()) return false;
        
        ItemStack input = inventory.getStackInSlot(INPUT_SLOT);
        if (input.isEmpty()) return false;

        Optional<CrusherRecipe> recipe = getRecipe();
        if (recipe.isEmpty()) return false;

        ItemStack result = recipe.get().getResult();
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        
        if (output.isEmpty()) return true;
        if (!ItemStack.isSameItemSameTags(output, result)) return false;
        return output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    @Override
    protected boolean processRecipe() {
        if (level == null) return false;
        
        Optional<CrusherRecipe> recipeOpt = getRecipe();
        if (recipeOpt.isEmpty()) {
            progress = 0;
            return false;
        }

        CrusherRecipe recipe = recipeOpt.get();

        // 処理時間を計算（レシピの処理時間 × Tierボーナス）
        MachineTier tier = getTier();
        int processTime = tier.calculateProcessTime(recipe.getProcessingTime());

        if (maxProgress == 0) {
            maxProgress = processTime;
        }

        progress++;

        if (progress >= maxProgress) {
            // 処理完了
            inventory.extractItem(INPUT_SLOT, 1, false);
            
            ItemStack result = recipe.getResult();
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
            currentRecipe = null;
            return true;
        }

        return false;
    }

    // ==================== ゲッター ====================

    public ContainerData getData() {
        return data;
    }
}
