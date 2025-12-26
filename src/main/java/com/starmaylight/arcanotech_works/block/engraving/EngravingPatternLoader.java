package com.starmaylight.arcanotech_works.block.engraving;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.starmaylight.arcanotech_works.Arcanotech_works;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

/**
 * 刻印パターンのJSONレシピローダー
 * data/modid/engraving_patterns/*.json からパターンを読み込む
 */
public class EngravingPatternLoader extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public EngravingPatternLoader() {
        super(GSON, "engraving_patterns");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> recipes, ResourceManager manager, ProfilerFiller profiler) {
        // デフォルトパターンをクリアしてJSONから再読み込み
        EngravingPattern.clearPatterns();
        
        // まずデフォルトパターンを再登録
        EngravingPatterns.registerDefaults();

        int loaded = 0;
        for (Map.Entry<ResourceLocation, JsonElement> entry : recipes.entrySet()) {
            ResourceLocation id = entry.getKey();
            try {
                JsonObject json = entry.getValue().getAsJsonObject();
                EngravingPattern pattern = parsePattern(id, json);
                if (pattern != null) {
                    loaded++;
                }
            } catch (Exception e) {
                Arcanotech_works.LOGGER.error("Failed to load engraving pattern {}: {}", id, e.getMessage());
            }
        }

        Arcanotech_works.LOGGER.info("Loaded {} engraving patterns from JSON (Total: {})", 
                loaded, EngravingPattern.getPatternCount());
    }

    /**
     * JSONからパターンをパース
     */
    private EngravingPattern parsePattern(ResourceLocation id, JsonObject json) {
        // 出力アイテム
        String outputId = json.get("output").getAsString();
        ResourceLocation outputLoc = new ResourceLocation(outputId);
        Item outputItem = ForgeRegistries.ITEMS.getValue(outputLoc);
        
        if (outputItem == null) {
            Arcanotech_works.LOGGER.warn("Unknown output item {} for pattern {}", outputId, id);
            return null;
        }

        // パターン配列
        String[] patternLines = new String[7];
        var patternArray = json.getAsJsonArray("pattern");
        
        if (patternArray.size() != 7) {
            Arcanotech_works.LOGGER.warn("Pattern {} must have exactly 7 rows", id);
            return null;
        }
        
        for (int i = 0; i < 7; i++) {
            String line = patternArray.get(i).getAsString();
            if (line.length() != 7) {
                Arcanotech_works.LOGGER.warn("Pattern {} row {} must have exactly 7 characters", id, i);
                return null;
            }
            patternLines[i] = line;
        }

        // 名前（オプション）
        String name = json.has("name") ? json.get("name").getAsString() : id.getPath();

        return new EngravingPattern(id.toString(), name, patternLines, () -> outputItem);
    }
}
