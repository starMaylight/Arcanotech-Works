package com.starmaylight.arcanotech_works.block.machine.mixer;

import com.starmaylight.arcanotech_works.block.machine.AbstractMachineBlockEntity;
import com.starmaylight.arcanotech_works.block.machine.MachineTier;
import com.starmaylight.arcanotech_works.blockentity.ModBlockEntities;
import com.starmaylight.arcanotech_works.recipe.MixerRecipe;
import com.starmaylight.arcanotech_works.recipe.ModRecipes;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

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
    private static final int MANA_CAPACITY = 15000;
    private static final int MANA_MAX_INPUT = 150;
    private static final int MANA_PER_OPERATION = 60;
    private static final int HEAT_PER_OPERATION = 8;

    // 現在のレシピキャッシュ
    private MixerRecipe currentRecipe = null;

    // インベントリ
    private final ItemStackHandler inventory = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (slot >= INPUT_SLOT_1 && slot <= INPUT_SLOT_4) {
                currentRecipe = null;
            }
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
            currentRecipe = null;
        }
    };

    private final FluidTank inputTank2 = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            currentRecipe = null;
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

    // ==================== レシピ処理 ====================

    private SimpleContainer createItemContainer() {
        SimpleContainer container = new SimpleContainer(4);
        for (int i = 0; i < 4; i++) {
            container.setItem(i, inventory.getStackInSlot(i));
        }
        return container;
    }

    private Optional<MixerRecipe> getRecipe() {
        if (level == null) return Optional.empty();
        
        SimpleContainer container = createItemContainer();
        
        if (currentRecipe != null) {
            if (currentRecipe.matchesWithFluids(container, inputTank1.getFluid(), inputTank2.getFluid(), level)) {
                return Optional.of(currentRecipe);
            }
        }

        // レシピを検索
        Optional<MixerRecipe> recipe = level.getRecipeManager()
                .getAllRecipesFor(ModRecipes.MIXER_TYPE.get())
                .stream()
                .filter(r -> r.matchesWithFluids(container, inputTank1.getFluid(), inputTank2.getFluid(), level))
                .findFirst();
        
        recipe.ifPresent(r -> currentRecipe = r);
        return recipe;
    }

    @Override
    protected boolean canProcess() {
        if (!super.canProcess()) return false;

        Optional<MixerRecipe> recipe = getRecipe();
        if (recipe.isEmpty()) return false;

        MixerRecipe r = recipe.get();

        // 出力スペースチェック
        if (!r.getResult().isEmpty()) {
            ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
            if (!output.isEmpty()) {
                if (!ItemStack.isSameItemSameTags(output, r.getResult())) return false;
                if (output.getCount() + r.getResult().getCount() > output.getMaxStackSize()) return false;
            }
        }

        // 液体出力スペースチェック
        if (!r.getFluidOutput1().isEmpty()) {
            if (outputTank1.fill(r.getFluidOutput1(), IFluidHandler.FluidAction.SIMULATE) < r.getFluidOutput1().getAmount()) {
                return false;
            }
        }
        if (!r.getFluidOutput2().isEmpty()) {
            if (outputTank2.fill(r.getFluidOutput2(), IFluidHandler.FluidAction.SIMULATE) < r.getFluidOutput2().getAmount()) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected boolean processRecipe() {
        if (level == null) return false;
        
        Optional<MixerRecipe> recipeOpt = getRecipe();
        if (recipeOpt.isEmpty()) {
            progress = 0;
            return false;
        }

        MixerRecipe recipe = recipeOpt.get();

        MachineTier tier = getTier();
        int processTime = tier.calculateProcessTime(recipe.getProcessingTime());

        if (maxProgress == 0) {
            maxProgress = processTime;
        }

        progress++;

        if (progress >= maxProgress) {
            // アイテム入力消費
            for (var ingredient : recipe.getIngredientList()) {
                for (int i = 0; i < 4; i++) {
                    ItemStack slot = inventory.getStackInSlot(i);
                    if (ingredient.test(slot)) {
                        inventory.extractItem(i, 1, false);
                        break;
                    }
                }
            }

            // 液体入力消費
            FluidStack fluidIn1 = recipe.getFluidInput1();
            FluidStack fluidIn2 = recipe.getFluidInput2();
            
            if (!fluidIn1.isEmpty()) {
                if (inputTank1.getFluid().isFluidEqual(fluidIn1)) {
                    inputTank1.drain(fluidIn1.getAmount(), IFluidHandler.FluidAction.EXECUTE);
                } else if (inputTank2.getFluid().isFluidEqual(fluidIn1)) {
                    inputTank2.drain(fluidIn1.getAmount(), IFluidHandler.FluidAction.EXECUTE);
                }
            }
            if (!fluidIn2.isEmpty()) {
                if (inputTank2.getFluid().isFluidEqual(fluidIn2)) {
                    inputTank2.drain(fluidIn2.getAmount(), IFluidHandler.FluidAction.EXECUTE);
                } else if (inputTank1.getFluid().isFluidEqual(fluidIn2)) {
                    inputTank1.drain(fluidIn2.getAmount(), IFluidHandler.FluidAction.EXECUTE);
                }
            }

            // アイテム出力
            if (!recipe.getResult().isEmpty()) {
                ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
                if (output.isEmpty()) {
                    inventory.setStackInSlot(OUTPUT_SLOT, recipe.getResult().copy());
                } else {
                    output.grow(recipe.getResult().getCount());
                }
            }

            // 液体出力
            if (!recipe.getFluidOutput1().isEmpty()) {
                outputTank1.fill(recipe.getFluidOutput1(), IFluidHandler.FluidAction.EXECUTE);
            }
            if (!recipe.getFluidOutput2().isEmpty()) {
                outputTank2.fill(recipe.getFluidOutput2(), IFluidHandler.FluidAction.EXECUTE);
            }

            manaStorage.extractMana(getManaPerOperation(), false);

            progress = 0;
            maxProgress = 0;
            currentRecipe = null;
            return true;
        }

        return false;
    }

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
