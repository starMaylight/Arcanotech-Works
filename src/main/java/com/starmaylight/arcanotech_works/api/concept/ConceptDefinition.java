package com.starmaylight.arcanotech_works.api.concept;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * JSONから読み込む概念定義データ。
 */
public class ConceptDefinition {

    private final ResourceLocation targetItem;
    private final Map<ConceptType, Integer> concepts;
    private final boolean inherited;

    public ConceptDefinition(ResourceLocation targetItem, Map<ConceptType, Integer> concepts, boolean inherited) {
        this.targetItem = targetItem;
        this.concepts = concepts;
        this.inherited = inherited;
    }

    public ResourceLocation getTargetItem() { return targetItem; }
    public Map<ConceptType, Integer> getConcepts() { return concepts; }
    public boolean isInherited() { return inherited; }

    public ConceptData toConceptData() {
        ConceptData data = new ConceptData();
        for (Map.Entry<ConceptType, Integer> entry : concepts.entrySet()) {
            data.addConcept(entry.getKey(), entry.getValue());
        }
        return data;
    }

    /**
     * JSONからパース
     * {
     *   "item": "minecraft:diamond",
     *   "inherited": true,
     *   "concepts": [
     *     { "type": "stillness", "intensity": 80 }
     *   ]
     * }
     */
    public static ConceptDefinition fromJson(ResourceLocation id, JsonObject json) {
        String itemId = GsonHelper.getAsString(json, "item");
        ResourceLocation targetItem = new ResourceLocation(itemId);
        boolean inherited = GsonHelper.getAsBoolean(json, "inherited", true);

        Map<ConceptType, Integer> concepts = new HashMap<>();
        JsonArray conceptArray = GsonHelper.getAsJsonArray(json, "concepts");

        for (JsonElement element : conceptArray) {
            JsonObject conceptObj = element.getAsJsonObject();
            String typeId = GsonHelper.getAsString(conceptObj, "type");
            int intensity = GsonHelper.getAsInt(conceptObj, "intensity", 50);

            ConceptType type = ConceptType.fromId(typeId);
            if (type != null) {
                concepts.put(type, Math.max(1, Math.min(100, intensity)));
            }
        }

        return new ConceptDefinition(targetItem, concepts, inherited);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("item", targetItem.toString());
        json.addProperty("inherited", inherited);

        JsonArray conceptArray = new JsonArray();
        for (Map.Entry<ConceptType, Integer> entry : concepts.entrySet()) {
            JsonObject conceptObj = new JsonObject();
            conceptObj.addProperty("type", entry.getKey().getId());
            conceptObj.addProperty("intensity", entry.getValue());
            conceptArray.add(conceptObj);
        }
        json.add("concepts", conceptArray);

        return json;
    }
}
