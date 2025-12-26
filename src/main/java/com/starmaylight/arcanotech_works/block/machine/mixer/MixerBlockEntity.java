package com.starmaylight.arcanotech_works.block.machine.mixer;

import com.starmaylight.arcanotech_works.block.machine.AbstractMachineBlockEntity;
import com.starmaylight.arcanotech_works.block.machine.MachineTier;
import com.starmaylight.arcanotech_works.blockentity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 混合機のBlockEntity
 * インベントリ構成:
 * - スロット0-3: アイテム入力（4スロット）
 * - スロット4: アイテム出力
 * - スロット5: ファン/冷却コアスロット
 * 
 * 液体タンク:
 * - 入力タンク1, 2（各4000mb）
 * - 出力タンク1, 2（各4000mb）
 */
public class MixerBlockEntity extends AbstractMachineBlockEntity {

    // スロット定義
    public static final int INPUT_SLOT_1 = 0;
    public static final int INPUT_SLOT_2 = 1;
    public static final int INPUT_SLOT_3 = 2;
    public static final int INPUT_SLOT_4 = 3;
    public static final int OUTPUT_SLOT = 4;
    public static final int COOLING_SLOT = 5;
    public static final int SLOT_COUNT = 6;

    // タンク容量
    public static final int TANK_CAPACITY = 4000;

    // 処理パラメータ
    private static final int BASE_PROCESS_TIME = 200;  // 10秒
    private static final int MANA_CAPACITY = 15000;
    private static final int MANA_MAX_INPUT = 150;
    private static final int MANA_PER_OPERATION = 60;
    private static final int HEAT_PER_OPERATION = 8;

    // インベントリ
    private final ItemStackHandler inventory = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case INPUT_SLOT_1, INPUT_SLOT_2, INPUT_SLOT_3, INPUT_SLOT_4 -> true;
                case OUTPUT_SLOT -> false;
                case COOLING_SLOT -> isFan(stack) || isCoolingCore(stack);
                default -> false;
            };
        }
    };

    // 液体タンク
    private final FluidTank inputTank1 = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    private final FluidTank inputTank2 = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    private final FluidTank outputTank1 = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    private final FluidTank outputTank2 = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    // Fluid Handler LazyOptional
    private final LazyOptional<IFluidHandler> inputFluidHandler1 = LazyOptional.of(() -> inputTank1);
    private final LazyOptional<IFluidHandler> inputFluidHandler2 = LazyOptional.of(() -> inputTank2);
    private final LazyOptional<IFluidHandler> outputFluidHandler1 = LazyOptional.of(() -> outputTank1);
    private final LazyOptional<IFluidHandler> outputFluidHandler2 = LazyOptional.of(() -> outputTank2);

    // GUI同期用データ（拡張版）
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
                // 液体タンクの量（上位/下位16ビットで分割）
                case 6 -> inputTank1.getFluidAmount();
                case 7 -> inputTank2.getFluidAmount();
                case 8 -> outputTank1.getFluidAmount();
                case 9 -> outputTank2.getFluidAmount();
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
            return 10;
        }
    };

    public MixerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MIXER.get(), pos, state);
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
        return Component.translatable("block.arcanotech_works.mixer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MixerMenu(containerId, playerInventory, this, data);
    }

    // ==================== 処理ロジック ====================

    @Override
    protected boolean canProcess() {
        if (!super.canProcess()) return false;
        
        // TODO: レシピマネージャーから有効なレシピをチェック
        MixerRecipeResult result = getMixingResult();
        if (result == null) return false;

        // 出力スペースチェック
        if (!result.outputItem.isEmpty()) {
            ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
            if (!output.isEmpty()) {
                if (!ItemStack.isSameItemSameTags(output, result.outputItem)) return false;
                if (output.getCount() + result.outputItem.getCount() > output.getMaxStackSize()) return false;
            }
        }

        // 液体出力スペースチェック
        if (!result.outputFluid1.isEmpty()) {
            if (outputTank1.fill(result.outputFluid1, IFluidHandler.FluidAction.SIMULATE) < result.outputFluid1.getAmount()) {
                return false;
            }
        }
        if (!result.outputFluid2.isEmpty()) {
            if (outputTank2.fill(result.outputFluid2, IFluidHandler.FluidAction.SIMULATE) < result.outputFluid2.getAmount()) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected boolean processRecipe() {
        MixerRecipeResult result = getMixingResult();
        if (result == null) {
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
            // 処理完了 - 入力消費
            for (int i = 0; i < result.inputItemCounts.length; i++) {
                if (result.inputItemCounts[i] > 0) {
                    inventory.extractItem(i, result.inputItemCounts[i], false);
                }
            }
            if (result.inputFluid1Amount > 0) {
                inputTank1.drain(result.inputFluid1Amount, IFluidHandler.FluidAction.EXECUTE);
            }
            if (result.inputFluid2Amount > 0) {
                inputTank2.drain(result.inputFluid2Amount, IFluidHandler.FluidAction.EXECUTE);
            }

            // 出力
            if (!result.outputItem.isEmpty()) {
                ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
                if (output.isEmpty()) {
                    inventory.setStackInSlot(OUTPUT_SLOT, result.outputItem.copy());
                } else {
                    output.grow(result.outputItem.getCount());
                }
            }
            if (!result.outputFluid1.isEmpty()) {
                outputTank1.fill(result.outputFluid1, IFluidHandler.FluidAction.EXECUTE);
            }
            if (!result.outputFluid2.isEmpty()) {
                outputTank2.fill(result.outputFluid2, IFluidHandler.FluidAction.EXECUTE);
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
     * 混合結果を取得
     * TODO: レシピマネージャーに置き換え
     */
    private MixerRecipeResult getMixingResult() {
        // 仮実装: 簡単なレシピ例
        
        // 水 + 溶岩 → 黒曜石
        if (!inputTank1.isEmpty() && !inputTank2.isEmpty()) {
            FluidStack fluid1 = inputTank1.getFluid();
            FluidStack fluid2 = inputTank2.getFluid();
            
            if ((fluid1.getFluid() == Fluids.WATER && fluid2.getFluid() == Fluids.LAVA) ||
                (fluid1.getFluid() == Fluids.LAVA && fluid2.getFluid() == Fluids.WATER)) {
                if (fluid1.getAmount() >= 1000 && fluid2.getAmount() >= 1000) {
                    return new MixerRecipeResult(
                            new int[]{0, 0, 0, 0},  // アイテム消費なし
                            1000, 1000,  // 液体消費
                            new ItemStack(Items.OBSIDIAN),  // 出力
                            FluidStack.EMPTY, FluidStack.EMPTY  // 液体出力なし
                    );
                }
            }
        }

        return null;
    }

    /**
     * 混合結果を保持する内部クラス
     */
    private record MixerRecipeResult(
            int[] inputItemCounts,
            int inputFluid1Amount,
            int inputFluid2Amount,
            ItemStack outputItem,
            FluidStack outputFluid1,
            FluidStack outputFluid2
    ) {}

    // ==================== タンクゲッター ====================

    public FluidTank getInputTank1() {
        return inputTank1;
    }

    public FluidTank getInputTank2() {
        return inputTank2;
    }

    public FluidTank getOutputTank1() {
        return outputTank1;
    }

    public FluidTank getOutputTank2() {
        return outputTank2;
    }

    public ContainerData getData() {
        return data;
    }

    // ==================== NBT ====================

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("InputTank1", inputTank1.writeToNBT(new CompoundTag()));
        tag.put("InputTank2", inputTank2.writeToNBT(new CompoundTag()));
        tag.put("OutputTank1", outputTank1.writeToNBT(new CompoundTag()));
        tag.put("OutputTank2", outputTank2.writeToNBT(new CompoundTag()));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("InputTank1")) {
            inputTank1.readFromNBT(tag.getCompound("InputTank1"));
        }
        if (tag.contains("InputTank2")) {
            inputTank2.readFromNBT(tag.getCompound("InputTank2"));
        }
        if (tag.contains("OutputTank1")) {
            outputTank1.readFromNBT(tag.getCompound("OutputTank1"));
        }
        if (tag.contains("OutputTank2")) {
            outputTank2.readFromNBT(tag.getCompound("OutputTank2"));
        }
    }

    // ==================== Capability ====================

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            // 方向に応じてタンクを返す（簡易実装）
            if (side == Direction.UP) {
                return inputFluidHandler1.cast();
            } else if (side == Direction.DOWN) {
                return outputFluidHandler1.cast();
            } else if (side == Direction.NORTH || side == Direction.SOUTH) {
                return inputFluidHandler2.cast();
            } else {
                return outputFluidHandler2.cast();
            }
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inputFluidHandler1.invalidate();
        inputFluidHandler2.invalidate();
        outputFluidHandler1.invalidate();
        outputFluidHandler2.invalidate();
    }
}
