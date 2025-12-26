package com.starmaylight.arcanotech_works.capability;

import com.starmaylight.arcanotech_works.Arcanotech_works;
import com.starmaylight.arcanotech_works.api.mana.IManaStorage;
import com.starmaylight.arcanotech_works.api.mana.ManaStorage;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 魔力Capabilityの登録とアタッチを管理するクラス。
 */
public class ManaCapability {

    public static final Capability<IManaStorage> MANA = CapabilityManager.get(new CapabilityToken<>() {});
    public static final ResourceLocation MANA_CAP_ID = new ResourceLocation(Arcanotech_works.MODID, "mana");

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IManaStorage.class);
    }

    public static class ItemManaProvider implements ICapabilitySerializable<CompoundTag> {
        private final ManaStorage storage;
        private final LazyOptional<IManaStorage> optional;

        public ItemManaProvider(int capacity, int maxTransfer) {
            this.storage = new ManaStorage(capacity, maxTransfer);
            this.optional = LazyOptional.of(() -> storage);
        }

        public ItemManaProvider(int capacity, int maxReceive, int maxExtract) {
            this.storage = new ManaStorage(capacity, maxReceive, maxExtract);
            this.optional = LazyOptional.of(() -> storage);
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return cap == MANA ? optional.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() { return storage.serializeNBT(); }

        @Override
        public void deserializeNBT(CompoundTag nbt) { storage.deserializeNBT(nbt); }

        public void invalidate() { optional.invalidate(); }
    }

    public static class BlockEntityManaProvider implements ICapabilitySerializable<CompoundTag> {
        private final ManaStorage storage;
        private final LazyOptional<IManaStorage> optional;

        public BlockEntityManaProvider(int capacity, int maxTransfer) {
            this.storage = new ManaStorage(capacity, maxTransfer);
            this.optional = LazyOptional.of(() -> storage);
        }

        public BlockEntityManaProvider(BlockEntity be, int capacity, int maxReceive, int maxExtract) {
            this.storage = new ManaStorage(capacity, maxReceive, maxExtract) {
                @Override
                protected void onManaChanged() { be.setChanged(); }
            };
            this.optional = LazyOptional.of(() -> storage);
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return cap == MANA ? optional.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() { return storage.serializeNBT(); }

        @Override
        public void deserializeNBT(CompoundTag nbt) { storage.deserializeNBT(nbt); }

        public void invalidate() { optional.invalidate(); }
        public ManaStorage getStorage() { return storage; }
    }

    public static LazyOptional<IManaStorage> getMana(ItemStack stack) {
        return stack.getCapability(MANA);
    }

    public static LazyOptional<IManaStorage> getMana(BlockEntity blockEntity) {
        return blockEntity.getCapability(MANA);
    }

    public static LazyOptional<IManaStorage> getMana(BlockEntity blockEntity, @Nullable Direction side) {
        return blockEntity.getCapability(MANA, side);
    }
}
