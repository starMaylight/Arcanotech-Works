package com.starmaylight.arcanotech_works.item;

import com.starmaylight.arcanotech_works.Arcanotech_works;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 魔石アイテム - クオリティ値を持つ魔力生成用素材
 * 
 * クオリティ:
 * - 採掘時: 1-500のランダム
 * - 精錬後: 最大1000
 * - 魔力生成量 = クオリティ × 2
 */
public class ManaGemItem extends Item {

    public static final String TAG_QUALITY = "Quality";
    public static final int MIN_QUALITY = 1;
    public static final int MAX_QUALITY_NATURAL = 500;  // 採掘時の最大
    public static final int MAX_QUALITY_REFINED = 1000; // 精錬後の最大

    public ManaGemItem(Properties properties) {
        super(properties);
    }

    /**
     * クオリティを取得
     */
    public static int getQuality(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ManaGemItem)) {
            return 0;
        }
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_QUALITY)) {
            return tag.getInt(TAG_QUALITY);
        }
        return MIN_QUALITY; // デフォルト値
    }

    /**
     * クオリティを設定
     */
    public static void setQuality(ItemStack stack, int quality) {
        if (stack.isEmpty()) return;
        quality = Mth.clamp(quality, MIN_QUALITY, MAX_QUALITY_REFINED);
        stack.getOrCreateTag().putInt(TAG_QUALITY, quality);
    }

    /**
     * ランダムなクオリティで魔石を作成（採掘用）
     */
    public static ItemStack createWithRandomQuality(Item item, Level level) {
        ItemStack stack = new ItemStack(item);
        int quality = level.getRandom().nextIntBetweenInclusive(MIN_QUALITY, MAX_QUALITY_NATURAL);
        setQuality(stack, quality);
        return stack;
    }

    /**
     * 指定クオリティで魔石を作成
     */
    public static ItemStack createWithQuality(Item item, int quality) {
        ItemStack stack = new ItemStack(item);
        setQuality(stack, quality);
        return stack;
    }

    /**
     * 魔力生成量を計算
     */
    public static int getManaOutput(ItemStack stack) {
        return getQuality(stack) * 2;
    }

    /**
     * クオリティに応じたランク名を取得
     */
    public static String getQualityRank(int quality) {
        if (quality >= 900) return "quality.arcanotech_works.legendary";
        if (quality >= 700) return "quality.arcanotech_works.exceptional";
        if (quality >= 500) return "quality.arcanotech_works.superior";
        if (quality >= 300) return "quality.arcanotech_works.fine";
        if (quality >= 100) return "quality.arcanotech_works.common";
        return "quality.arcanotech_works.crude";
    }

    /**
     * クオリティに応じた色を取得
     */
    public static ChatFormatting getQualityColor(int quality) {
        if (quality >= 900) return ChatFormatting.LIGHT_PURPLE;
        if (quality >= 700) return ChatFormatting.GOLD;
        if (quality >= 500) return ChatFormatting.AQUA;
        if (quality >= 300) return ChatFormatting.GREEN;
        if (quality >= 100) return ChatFormatting.WHITE;
        return ChatFormatting.GRAY;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        int quality = getQuality(stack);
        ChatFormatting color = getQualityColor(quality);
        String rankKey = getQualityRank(quality);

        // クオリティ表示
        tooltip.add(Component.translatable("tooltip.arcanotech_works.quality",
                Component.literal(String.valueOf(quality)).withStyle(color),
                Component.translatable(rankKey).withStyle(color)));

        // 魔力生成量
        int manaOutput = getManaOutput(stack);
        tooltip.add(Component.translatable("tooltip.arcanotech_works.mana_output",
                Component.literal(String.valueOf(manaOutput)).withStyle(ChatFormatting.AQUA))
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // 高クオリティはエンチャントのような光沢
        return getQuality(stack) >= 700;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int quality = getQuality(stack);
        return Math.round((quality / (float) MAX_QUALITY_REFINED) * 13f);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        int quality = getQuality(stack);
        float ratio = quality / (float) MAX_QUALITY_REFINED;
        
        // 低→高: 赤→黄→緑→水色→紫
        if (ratio < 0.25f) {
            return Mth.hsvToRgb(0.0f, 1.0f, 1.0f); // 赤
        } else if (ratio < 0.5f) {
            return Mth.hsvToRgb(0.15f, 1.0f, 1.0f); // オレンジ/黄
        } else if (ratio < 0.75f) {
            return Mth.hsvToRgb(0.35f, 1.0f, 1.0f); // 緑
        } else if (ratio < 0.9f) {
            return Mth.hsvToRgb(0.5f, 1.0f, 1.0f); // 水色
        } else {
            return Mth.hsvToRgb(0.8f, 1.0f, 1.0f); // 紫
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true; // 常にクオリティバーを表示
    }
}
