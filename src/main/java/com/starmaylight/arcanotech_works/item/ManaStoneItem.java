package com.starmaylight.arcanotech_works.item;

import com.starmaylight.arcanotech_works.capability.ManaCapability;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 魔石アイテム - 魔力を蓄積・放出できるアイテム。
 */
public class ManaStoneItem extends Item {

    private final int capacity;
    private final int transferRate;

    public ManaStoneItem(Properties properties, int capacity, int transferRate) {
        super(properties);
        this.capacity = capacity;
        this.transferRate = transferRate;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ManaCapability.ItemManaProvider(capacity, transferRate);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        stack.getCapability(ManaCapability.MANA).ifPresent(mana -> {
            int stored = mana.getManaStored();
            int max = mana.getMaxManaStored();
            float ratio = mana.getFillRatio();

            ChatFormatting color = ratio >= 0.75f ? ChatFormatting.AQUA :
                    ratio >= 0.5f ? ChatFormatting.GREEN :
                    ratio >= 0.25f ? ChatFormatting.YELLOW :
                    ratio > 0f ? ChatFormatting.RED : ChatFormatting.DARK_GRAY;

            tooltip.add(Component.translatable("tooltip.arcanotech_works.mana_stored",
                    Component.literal(String.format("%,d", stored)).withStyle(color),
                    Component.literal(String.format("%,d", max)).withStyle(ChatFormatting.GRAY)));

            if (flag.isAdvanced()) {
                tooltip.add(Component.translatable("tooltip.arcanotech_works.mana_transfer",
                        Component.literal(String.valueOf(transferRate)).withStyle(ChatFormatting.AQUA))
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        });
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stack.getCapability(ManaCapability.MANA)
                .map(mana -> mana.getManaStored() < mana.getMaxManaStored())
                .orElse(false);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return stack.getCapability(ManaCapability.MANA)
                .map(mana -> Math.round(mana.getFillRatio() * 13f))
                .orElse(0);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return stack.getCapability(ManaCapability.MANA)
                .map(mana -> {
                    float ratio = mana.getFillRatio();
                    int r = (int) (ratio * 100);
                    int g = (int) (150 + ratio * 105);
                    return (r << 16) | (g << 8) | 255;
                }).orElse(0x0066FF);
    }

    @Nullable
    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        CompoundTag tag = super.getShareTag(stack);
        if (tag == null) tag = new CompoundTag();
        final CompoundTag finalTag = tag;
        stack.getCapability(ManaCapability.MANA).ifPresent(mana -> {
            if (mana instanceof net.minecraftforge.common.util.INBTSerializable<?> ser) {
                finalTag.put("ManaData", ((net.minecraftforge.common.util.INBTSerializable<CompoundTag>) ser).serializeNBT());
            }
        });
        return finalTag;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundTag tag) {
        super.readShareTag(stack, tag);
        if (tag != null && tag.contains("ManaData")) {
            stack.getCapability(ManaCapability.MANA).ifPresent(mana -> {
                if (mana instanceof net.minecraftforge.common.util.INBTSerializable<?>) {
                    ((net.minecraftforge.common.util.INBTSerializable<CompoundTag>) mana)
                            .deserializeNBT(tag.getCompound("ManaData"));
                }
            });
        }
    }

    public int getCapacity() { return capacity; }
    public int getTransferRate() { return transferRate; }
}
