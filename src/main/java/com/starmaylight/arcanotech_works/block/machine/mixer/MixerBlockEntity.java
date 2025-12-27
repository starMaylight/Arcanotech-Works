package com.starmaylight.arcanotech_works.block.machine.mixer;

import com.starmaylight.arcanotech_works.block.machine.AbstractMachineBlockEntity;
import com.starmaylight.arcanotech_works.block.machine.MachineTier;
import com.starmaylight.arcanotech_works.block.machine.SidedItemHandler;
import com.starmaylight.arcanotech_works.blockentity.ModBlockEntities;
import com.starmaylight.arcanotech_works.recipe.MixerRecipe;
import com.starmaylight.arcanotech_works.recipe.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
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
            syncToClient();
            currentRecipe = null;
        }
    };

    private final FluidTank inputTank2 = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            syncToClient();
            currentRecipe = null;
        }
    };

    private final FluidTank outputTank1 = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            syncToClient();
        }
    };

    private final FluidTank outputTank2 = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            syncToClient();
        }
    };

    // 方向ベースのアイテムハンドラー
    private LazyOptional<IItemHandler> topItemHandler = LazyOptional.empty();
    private LazyOptional<IItemHandler> bottomItemHandler = LazyOptional.empty();
    private LazyOptional<IItemHandler> sideItemHandler = LazyOptional.empty();

    // Fluid Handler LazyOptional
    private final LazyOptional<IFluidHandler> inputFluidHandler1 = LazyOptional.of(() -> inputTank1);
    private final LazyOptional<IFluidHandler> inputFluidHandler2 = LazyOptional.of(() -> inputTank2);
    private final LazyOptional<IFluidHandler> outputFluidHandler1 = LazyOptional.of(() -> outputTank1);
    private final LazyOptional<IFluidHandler> outputFluidHandler2 = LazyOptional.of(() -> outputTank2);
    
    // 全タンク統合ハンドラー（バケツ操作用）
    private final LazyOptional<IFluidHandler> combinedFluidHandler = LazyOptional.of(() -> new CombinedFluidHandler());

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
        initItemHandlers();
    }

    /**
     * 方向ベースのアイテムハンドラーを初期化
     */
    private void initItemHandlers() {
        topItemHandler = LazyOptional.of(() -> 
            SidedItemHandler.createInputHandler(inventory, INPUT_SLOT_1, INPUT_SLOT_2, INPUT_SLOT_3, INPUT_SLOT_4));
        
        bottomItemHandler = LazyOptional.of(() -> 
            SidedItemHandler.createOutputHandler(inventory, OUTPUT_SLOT));
        
        sideItemHandler = LazyOptional.of(() -> 
            new SidedItemHandler(inventory, new int[]{INPUT_SLOT_1, INPUT_SLOT_2, INPUT_SLOT_3, INPUT_SLOT_4, OUTPUT_SLOT, COOLING_SLOT}, true, true));
    }

    // ==================== クライアント同期 ====================

    /**
     * クライアントに同期を送信
     */
    private void syncToClient() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        // 液体データを追加
        tag.put("InputTank1", inputTank1.writeToNBT(new CompoundTag()));
        tag.put("InputTank2", inputTank2.writeToNBT(new CompoundTag()));
        tag.put("OutputTank1", outputTank1.writeToNBT(new CompoundTag()));
        tag.put("OutputTank2", outputTank2.writeToNBT(new CompoundTag()));
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
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

    // ==================== 液体バケツ操作 ====================

    /**
     * プレイヤーのバケツ操作を処理
     */
    public InteractionResult handleBucketInteraction(Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        
        if (heldItem.isEmpty()) {
            return InteractionResult.PASS;
        }

        Optional<IFluidHandlerItem> fluidHandlerItem = FluidUtil.getFluidHandler(heldItem).resolve();
        
        if (fluidHandlerItem.isEmpty()) {
            return InteractionResult.PASS;
        }

        IFluidHandlerItem itemHandler = fluidHandlerItem.get();
        FluidStack fluidInItem = itemHandler.getFluidInTank(0);
        
        if (!fluidInItem.isEmpty()) {
            if (tryFillInputTank(player, hand, heldItem, fluidInItem)) {
                return InteractionResult.SUCCESS;
            }
        } else {
            if (tryDrainToContainer(player, hand, heldItem)) {
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    /**
     * 入力タンクへの注入を試みる
     * 異なる液体は異なるタンクに入れる
     */
    private boolean tryFillInputTank(Player player, InteractionHand hand, ItemStack container, FluidStack fluid) {
        FluidTank targetTank = null;
        
        // まず空のタンクを探す（異なる液体用）
        if (inputTank1.isEmpty()) {
            targetTank = inputTank1;
        } else if (inputTank2.isEmpty()) {
            // tank1と同じ液体の場合はtank1に追加、異なる液体はtank2に
            if (inputTank1.getFluid().isFluidEqual(fluid)) {
                if (inputTank1.getFluidAmount() + 1000 <= TANK_CAPACITY) {
                    targetTank = inputTank1;
                }
            } else {
                targetTank = inputTank2;
            }
        } else {
            // 両方のタンクに液体がある場合、同じ液体のタンクに追加
            if (inputTank1.getFluid().isFluidEqual(fluid) && inputTank1.getFluidAmount() + 1000 <= TANK_CAPACITY) {
                targetTank = inputTank1;
            } else if (inputTank2.getFluid().isFluidEqual(fluid) && inputTank2.getFluidAmount() + 1000 <= TANK_CAPACITY) {
                targetTank = inputTank2;
            }
        }
        
        if (targetTank == null) {
            return false;
        }

        // 液体を注入
        int filled = targetTank.fill(new FluidStack(fluid.getFluid(), 1000), IFluidHandler.FluidAction.EXECUTE);
        
        if (filled > 0) {
            if (!player.isCreative()) {
                player.setItemInHand(hand, new ItemStack(Items.BUCKET));
            }
            
            if (level != null) {
                level.playSound(null, worldPosition, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return true;
        }
        
        return false;
    }

    /**
     * タンクから空のバケツへ抽出を試みる
     */
    private boolean tryDrainToContainer(Player player, InteractionHand hand, ItemStack container) {
        FluidTank sourceTank = null;
        
        // 出力タンクから優先
        if (!outputTank1.isEmpty() && outputTank1.getFluidAmount() >= 1000) {
            sourceTank = outputTank1;
        } else if (!outputTank2.isEmpty() && outputTank2.getFluidAmount() >= 1000) {
            sourceTank = outputTank2;
        } else if (!inputTank1.isEmpty() && inputTank1.getFluidAmount() >= 1000) {
            sourceTank = inputTank1;
        } else if (!inputTank2.isEmpty() && inputTank2.getFluidAmount() >= 1000) {
            sourceTank = inputTank2;
        }
        
        if (sourceTank == null) {
            return false;
        }

        FluidStack inTank = sourceTank.getFluid();
        ItemStack filledBucket = ItemStack.EMPTY;
        
        if (inTank.getFluid() == Fluids.WATER) {
            filledBucket = new ItemStack(Items.WATER_BUCKET);
        } else if (inTank.getFluid() == Fluids.LAVA) {
            filledBucket = new ItemStack(Items.LAVA_BUCKET);
        }
        
        if (!filledBucket.isEmpty()) {
            sourceTank.drain(1000, IFluidHandler.FluidAction.EXECUTE);
            if (!player.isCreative()) {
                player.setItemInHand(hand, filledBucket);
            }
            if (level != null) {
                level.playSound(null, worldPosition, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return true;
        }
        
        return false;
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

        if (!r.getResult().isEmpty()) {
            ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
            if (!output.isEmpty()) {
                if (!ItemStack.isSameItemSameTags(output, r.getResult())) return false;
                if (output.getCount() + r.getResult().getCount() > output.getMaxStackSize()) return false;
            }
        }

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
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null) {
                return LazyOptional.of(() -> inventory).cast();
            }
            return switch (side) {
                case UP -> topItemHandler.cast();
                case DOWN -> bottomItemHandler.cast();
                default -> sideItemHandler.cast();
            };
        }
        
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (side == null) {
                return combinedFluidHandler.cast();
            }
            return switch (side) {
                case UP -> inputFluidHandler1.cast();
                case DOWN -> outputFluidHandler1.cast();
                case NORTH, SOUTH -> inputFluidHandler2.cast();
                default -> outputFluidHandler2.cast();
            };
        }
        
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        topItemHandler.invalidate();
        bottomItemHandler.invalidate();
        sideItemHandler.invalidate();
        inputFluidHandler1.invalidate();
        inputFluidHandler2.invalidate();
        outputFluidHandler1.invalidate();
        outputFluidHandler2.invalidate();
        combinedFluidHandler.invalidate();
    }

    // ==================== 統合液体ハンドラー ====================

    private class CombinedFluidHandler implements IFluidHandler {
        
        @Override
        public int getTanks() {
            return 4;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return switch (tank) {
                case 0 -> inputTank1.getFluid();
                case 1 -> inputTank2.getFluid();
                case 2 -> outputTank1.getFluid();
                case 3 -> outputTank2.getFluid();
                default -> FluidStack.EMPTY;
            };
        }

        @Override
        public int getTankCapacity(int tank) {
            return TANK_CAPACITY;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return true;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            // 空のタンクまたは同じ液体のタンクを探す
            if (inputTank1.isEmpty() || inputTank1.getFluid().isFluidEqual(resource)) {
                int filled = inputTank1.fill(resource, action);
                if (filled > 0) return filled;
            }
            if (inputTank2.isEmpty() || inputTank2.getFluid().isFluidEqual(resource)) {
                return inputTank2.fill(resource, action);
            }
            return 0;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            FluidStack drained = outputTank1.drain(resource, action);
            if (drained.isEmpty()) {
                drained = outputTank2.drain(resource, action);
            }
            return drained;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            FluidStack drained = outputTank1.drain(maxDrain, action);
            if (drained.isEmpty()) {
                drained = outputTank2.drain(maxDrain, action);
            }
            if (drained.isEmpty()) {
                drained = inputTank1.drain(maxDrain, action);
            }
            if (drained.isEmpty()) {
                drained = inputTank2.drain(maxDrain, action);
            }
            return drained;
        }
    }
}
