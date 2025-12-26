package com.starmaylight.arcanotech_works.api.concept;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.starmaylight.arcanotech_works.Arcanotech_works;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.*;

/**
 * 概念定義のレジストリ。
 * データパス: data/<namespace>/arcanotech_concepts/<path>.json
 */
public class ConceptRegistry extends SimpleJsonResourceReloadListener {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ConceptRegistry INSTANCE;

    private Map<ResourceLocation, ConceptDefinition> definitions = new HashMap<>();

    public ConceptRegistry() {
        super(GSON, "arcanotech_concepts");
        INSTANCE = this;
    }

    public static ConceptRegistry getInstance() { return INSTANCE; }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager resourceManager, ProfilerFiller profiler) {
        definitions.clear();
        int loaded = 0, failed = 0;

        for (Map.Entry<ResourceLocation, JsonElement> entry : jsonMap.entrySet()) {
            ResourceLocation id = entry.getKey();
            try {
                JsonObject json = entry.getValue().getAsJsonObject();
                ConceptDefinition definition = ConceptDefinition.fromJson(id, json);
                definitions.put(definition.getTargetItem(), definition);
                loaded++;
                LOGGER.debug("Loaded concept: {} -> {}", id, definition.getTargetItem());
            } catch (Exception e) {
                LOGGER.error("Failed to load concept: {}", id, e);
                failed++;
            }
        }
        LOGGER.info("Loaded {} concept definitions ({} failed)", loaded, failed);
    }

    @Nullable
    public ConceptDefinition getDefinition(ResourceLocation itemId) {
        return definitions.get(itemId);
    }

    @Nullable
    public ConceptDefinition getDefinition(Item item) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        return id != null ? definitions.get(id) : null;
    }

    @Nullable
    public ConceptDefinition getDefinition(ItemStack stack) {
        return stack.isEmpty() ? null : getDefinition(stack.getItem());
    }

    public boolean hasConcepts(Item item) {
        return getDefinition(item) != null;
    }

    public ConceptData getConceptData(Item item) {
        ConceptDefinition def = getDefinition(item);
        return def != null ? def.toConceptData() : new ConceptData();
    }

    public ConceptData getConceptData(ItemStack stack) {
        return stack.isEmpty() ? new ConceptData() : getConceptData(stack.getItem());
    }

    public Collection<ConceptDefinition> getAllDefinitions() {
        return Collections.unmodifiableCollection(definitions.values());
    }

    public List<ResourceLocation> findItemsWithConcept(ConceptType type) {
        List<ResourceLocation> result = new ArrayList<>();
        for (Map.Entry<ResourceLocation, ConceptDefinition> entry : definitions.entrySet()) {
            if (entry.getValue().getConcepts().containsKey(type)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public List<ResourceLocation> findItemsWithDomain(ConceptDomain domain) {
        List<ResourceLocation> result = new ArrayList<>();
        for (Map.Entry<ResourceLocation, ConceptDefinition> entry : definitions.entrySet()) {
            for (ConceptType type : entry.getValue().getConcepts().keySet()) {
                if (type.getDomain() == domain) {
                    result.add(entry.getKey());
                    break;
                }
            }
        }
        return result;
    }

    public void registerDefinition(ConceptDefinition definition) {
        definitions.put(definition.getTargetItem(), definition);
    }

    public void unregisterDefinition(ResourceLocation itemId) {
        definitions.remove(itemId);
    }
}
