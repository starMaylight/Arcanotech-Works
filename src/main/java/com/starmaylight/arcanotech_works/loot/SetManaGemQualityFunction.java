package com.starmaylight.arcanotech_works.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.starmaylight.arcanotech_works.item.ManaGemItem;
import com.starmaylight.arcanotech_works.registry.ModItems;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * 魔石にランダムなクオリティを設定するルートファンクション
 * 幸運エンチャントでクオリティ上限が上昇
 */
public class SetManaGemQualityFunction extends LootItemConditionalFunction {

    private final int minQuality;
    private final int maxQuality;
    private final int fortuneBonus; // 幸運1レベルあたりのボーナス

    protected SetManaGemQualityFunction(LootItemCondition[] conditions, int minQuality, int maxQuality, int fortuneBonus) {
        super(conditions);
        this.minQuality = minQuality;
        this.maxQuality = maxQuality;
        this.fortuneBonus = fortuneBonus;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        if (!(stack.getItem() instanceof ManaGemItem)) {
            return stack;
        }

        RandomSource random = context.getRandom();

        // 幸運レベルを取得
        int fortuneLevel = 0;
        if (context.hasParam(LootContextParams.TOOL)) {
            ItemStack tool = context.getParam(LootContextParams.TOOL);
            fortuneLevel = tool.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE);
        }

        // クオリティ計算
        int adjustedMax = Math.min(maxQuality + (fortuneLevel * fortuneBonus), ManaGemItem.MAX_QUALITY_REFINED);
        int quality = random.nextIntBetweenInclusive(minQuality, adjustedMax);

        ManaGemItem.setQuality(stack, quality);
        return stack;
    }

    @Override
    public LootItemFunctionType getType() {
        return ModLootFunctions.SET_MANA_GEM_QUALITY.get();
    }

    public static Builder<?> builder(int minQuality, int maxQuality) {
        return simpleBuilder((conditions) -> new SetManaGemQualityFunction(conditions, minQuality, maxQuality, 50));
    }

    public static Builder<?> builder(int minQuality, int maxQuality, int fortuneBonus) {
        return simpleBuilder((conditions) -> new SetManaGemQualityFunction(conditions, minQuality, maxQuality, fortuneBonus));
    }

    /**
     * シリアライザー
     */
    public static class Serializer extends LootItemConditionalFunction.Serializer<SetManaGemQualityFunction> {
        @Override
        public void serialize(JsonObject json, SetManaGemQualityFunction function, JsonSerializationContext context) {
            super.serialize(json, function, context);
            json.addProperty("min_quality", function.minQuality);
            json.addProperty("max_quality", function.maxQuality);
            json.addProperty("fortune_bonus", function.fortuneBonus);
        }

        @Override
        public SetManaGemQualityFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            int minQuality = GsonHelper.getAsInt(json, "min_quality", 1);
            int maxQuality = GsonHelper.getAsInt(json, "max_quality", 500);
            int fortuneBonus = GsonHelper.getAsInt(json, "fortune_bonus", 50);
            return new SetManaGemQualityFunction(conditions, minQuality, maxQuality, fortuneBonus);
        }
    }
}
