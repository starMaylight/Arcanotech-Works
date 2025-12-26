package com.starmaylight.arcanotech_works.block.machine;

import com.starmaylight.arcanotech_works.api.heat.HeatStorage;
import com.starmaylight.arcanotech_works.api.mana.IManaStorage;
import com.starmaylight.arcanotech_works.api.mana.ManaStorage;
import com.starmaylight.arcanotech_works.capability.ManaCapability;
import com.starmaylight.arcanotech_works.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 工業機械の基底BlockEntityクラス
 * 熱管理・冷却処理・魔力消費の共通処理を提供
 */
public abstract class AbstractMachineBlockEntity extends BlockEntity implements MenuProvider {

    // 熱管理
    protected final HeatStorage heatStorage;
    
    // 魔力管理
    protected final ManaStorage manaStorage;
    protected final LazyOptional<IManaStorage> manaHandler;

    // 冷却スロット用インデックス（サブクラスで定義）
    protected static final int FAN_SLOT = -1;  // サブクラスで上書き

    // ファンによる追加冷却
    protected static final int FAN_COOLING_BONUS = 1;  // 1 heat/sec追加

    // 処理関連
    protected int progress = 0;
    protected int maxProgress = 0;

    // 熱冷却遅延（処理完了後10秒 = 200tick）
    protected static final int COOLING_DELAY_TICKS = 200;
    protected int coolingDelayTimer = 0;  // 冷却開始までのタイマー
    protected int coolingTickCounter = 0;  // 1秒カウント用（20tickで1回冷却）

    public AbstractMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        
        MachineTier tier = getTier();
        this.heatStorage = new HeatStorage(tier.getMaxHeat());
        this.manaStorage = new ManaStorage(getManaCapacity(), getManaMaxInput(), 0);
        this.manaHandler = LazyOptional.of(() -> manaStorage);
    }

    // ==================== 抽象メソッド ====================

    /**
     * インベントリを取得
     */
    protected abstract ItemStackHandler getInventory();

    /**
     * ドロップ用のインベントリを取得
     */
    public abstract Container getDroppableInventory();

    /**
     * ファンスロットのインデックスを取得（-1の場合はファンスロットなし）
     */
    protected abstract int getFanSlotIndex();

    /**
     * 冷却コアスロットのインデックスを取得（-1の場合はスロットなし）
     */
    protected abstract int getCoolingCoreSlotIndex();

    /**
     * 機械固有の処理ロジック
     * @return 処理を行った場合true
     */
    protected abstract boolean processRecipe();

    /**
     * 魔力容量
     */
    protected abstract int getManaCapacity();

    /**
     * 魔力入力速度
     */
    protected abstract int getManaMaxInput();

    /**
     * 1回の処理で消費する魔力
     */
    protected abstract int getManaPerOperation();

    /**
     * 1回の処理で発生する熱
     */
    protected abstract int getHeatPerOperation();

    /**
     * GUIタイトル
     */
    @Override
    public abstract Component getDisplayName();

    // ==================== Tier関連 ====================

    /**
     * 現在のTierを取得
     */
    public MachineTier getTier() {
        if (level != null && worldPosition != null) {
            BlockState state = level.getBlockState(worldPosition);
            if (state.hasProperty(AbstractMachineBlock.TIER)) {
                return state.getValue(AbstractMachineBlock.TIER);
            }
        }
        return MachineTier.BASIC;
    }

    /**
     * Tierを設定
     */
    public void setTier(MachineTier tier) {
        if (level != null && !level.isClientSide()) {
            BlockState state = level.getBlockState(worldPosition);
            if (state.hasProperty(AbstractMachineBlock.TIER)) {
                level.setBlock(worldPosition, state.setValue(AbstractMachineBlock.TIER, tier), 3);
                heatStorage.setMaxHeat(tier.getMaxHeat());
            }
        }
    }

    // ==================== 熱管理 ====================

    /**
     * サーバーTick処理
     */
    public void serverTick() {
        if (level == null || level.isClientSide()) return;

        boolean wasActive = isActive();
        boolean didWork = false;
        boolean isProcessing = false;

        // 冷却コアがある場合は熱発生なし
        boolean hasCoolingCore = hasCoolingCore();

        // レシピ処理
        if (canProcess()) {
            didWork = processRecipe();
            isProcessing = progress > 0;  // 処理中かどうか
            
            // 熱発生（冷却コアがない場合のみ、処理完了時）
            if (didWork && !hasCoolingCore) {
                heatStorage.addHeat(getHeatPerOperation());
            }
        }

        // 冷却処理（処理中は冷却しない）
        if (isProcessing) {
            // 処理中は冷却遅延タイマーをリセット
            coolingDelayTimer = COOLING_DELAY_TICKS;
        } else {
            // 処理していない場合は冷却を試みる
            applyCooling();
        }

        // オーバーヒートチェック
        if (heatStorage.isOverheated()) {
            handleOverheat();
            return;  // ブロックが変化したので終了
        }

        // 状態更新
        boolean isActive = progress > 0;
        if (wasActive != isActive) {
            setActive(isActive);
        }

        if (didWork) {
            setChanged();
        }
    }

    /**
     * 冷却処理
     * - 処理終了後10秒（200tick）経過してから冷却開始
     * - 冷却速度は1heat/sec（20tickに1回）
     * - ファンがある場合は追加で1heat/sec
     */
    protected void applyCooling() {
        // 冷却遅延タイマーがある場合はカウントダウン
        if (coolingDelayTimer > 0) {
            coolingDelayTimer--;
            return;
        }

        // 熱がない場合は何もしない
        if (heatStorage.getHeat() <= 0) {
            return;
        }

        // 1秒（20tick）に1回冷却
        coolingTickCounter++;
        if (coolingTickCounter >= 20) {
            coolingTickCounter = 0;
            
            // 基本冷却: 1 heat/sec
            int cooling = 1;
            
            // ファンボーナス: +1 heat/sec
            if (hasFan()) {
                cooling += FAN_COOLING_BONUS;
            }
            
            heatStorage.removeHeat(cooling);
            setChanged();
        }
    }

    /**
     * オーバーヒート処理
     */
    protected void handleOverheat() {
        if (level == null || level.isClientSide()) return;

        // ファンがある場合は耐久を消費して回避
        int fanSlot = getFanSlotIndex();
        if (fanSlot >= 0) {
            ItemStack fanStack = getInventory().getStackInSlot(fanSlot);
            if (isFan(fanStack)) {
                // ファン耐久消費
                fanStack.setDamageValue(fanStack.getDamageValue() + 1);
                if (fanStack.getDamageValue() >= fanStack.getMaxDamage()) {
                    fanStack.shrink(1);
                }
                // 熱を80%にリセット
                heatStorage.resetToSafeLevel();
                setChanged();
                return;
            }
        }

        // ファンがない場合はブロック変化
        MachineTier tier = getTier();
        Block frameBlock = ((AbstractMachineBlock) getBlockState().getBlock()).getMachineFrameBlock(tier);
        
        // インベントリの中身をドロップ
        Container droppable = getDroppableInventory();
        for (int i = 0; i < droppable.getContainerSize(); i++) {
            ItemStack stack = droppable.getItem(i);
            if (!stack.isEmpty()) {
                net.minecraft.world.Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
            }
        }

        // 機械筐体に変化
        level.setBlock(worldPosition, frameBlock.defaultBlockState(), 3);
    }

    /**
     * 処理可能かどうか
     */
    protected boolean canProcess() {
        return manaStorage.getManaStored() >= getManaPerOperation();
    }

    // ==================== 冷却アイテム判定 ====================

    /**
     * ファンかどうか判定
     */
    protected boolean isFan(ItemStack stack) {
        return stack.is(ModItems.COOLING_FAN.get());
    }

    /**
     * 冷却コアかどうか判定
     */
    protected boolean isCoolingCore(ItemStack stack) {
        return stack.is(ModItems.COOLING_CORE.get());
    }

    /**
     * ファンを持っているか
     */
    protected boolean hasFan() {
        int fanSlot = getFanSlotIndex();
        if (fanSlot < 0) return false;
        return isFan(getInventory().getStackInSlot(fanSlot));
    }

    /**
     * 冷却コアを持っているか
     */
    protected boolean hasCoolingCore() {
        int coreSlot = getCoolingCoreSlotIndex();
        if (coreSlot < 0) return false;
        return isCoolingCore(getInventory().getStackInSlot(coreSlot));
    }

    // ==================== 状態管理 ====================

    /**
     * 稼働中かどうか
     */
    public boolean isActive() {
        return getBlockState().getValue(AbstractMachineBlock.ACTIVE);
    }

    /**
     * 稼働状態を設定
     */
    protected void setActive(boolean active) {
        if (level != null && !level.isClientSide()) {
            BlockState state = level.getBlockState(worldPosition);
            if (state.getValue(AbstractMachineBlock.ACTIVE) != active) {
                level.setBlock(worldPosition, state.setValue(AbstractMachineBlock.ACTIVE, active), 3);
            }
        }
    }

    // ==================== ゲッター ====================

    public HeatStorage getHeatStorage() {
        return heatStorage;
    }

    public ManaStorage getManaStorage() {
        return manaStorage;
    }

    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    // ==================== NBT ====================

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Heat", heatStorage.save());
        tag.put("Mana", manaStorage.serializeNBT());
        tag.put("Inventory", getInventory().serializeNBT());
        tag.putInt("Progress", progress);
        tag.putInt("MaxProgress", maxProgress);
        tag.putInt("CoolingDelayTimer", coolingDelayTimer);
        tag.putInt("CoolingTickCounter", coolingTickCounter);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Heat")) {
            heatStorage.load(tag.getCompound("Heat"));
        }
        if (tag.contains("Mana")) {
            manaStorage.deserializeNBT(tag.getCompound("Mana"));
        }
        if (tag.contains("Inventory")) {
            getInventory().deserializeNBT(tag.getCompound("Inventory"));
        }
        progress = tag.getInt("Progress");
        maxProgress = tag.getInt("MaxProgress");
        coolingDelayTimer = tag.getInt("CoolingDelayTimer");
        coolingTickCounter = tag.getInt("CoolingTickCounter");
    }

    // ==================== Capability ====================

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ManaCapability.MANA) {
            return manaHandler.cast();
        }
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return LazyOptional.of(this::getInventory).cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        manaHandler.invalidate();
    }
}
