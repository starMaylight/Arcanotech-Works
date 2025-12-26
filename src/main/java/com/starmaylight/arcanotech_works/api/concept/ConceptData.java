package com.starmaylight.arcanotech_works.api.concept;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.*;

/**
 * アイテムに紐づく概念情報を保持するクラス。
 */
public class ConceptData implements INBTSerializable<CompoundTag> {

    private final Map<ConceptType, Integer> concepts = new EnumMap<>(ConceptType.class);

    public ConceptData() {}

    public void addConcept(ConceptType type, int intensity) {
        concepts.put(type, Math.max(1, Math.min(100, intensity)));
    }

    public void removeConcept(ConceptType type) {
        concepts.remove(type);
    }

    public boolean hasConcept(ConceptType type) {
        return concepts.containsKey(type);
    }

    public int getIntensity(ConceptType type) {
        return concepts.getOrDefault(type, 0);
    }

    public Set<ConceptType> getConcepts() {
        return Collections.unmodifiableSet(concepts.keySet());
    }

    public int getConceptCount() {
        return concepts.size();
    }

    public boolean isEmpty() {
        return concepts.isEmpty();
    }

    public void clear() {
        concepts.clear();
    }

    public List<ConceptType> getConceptsByDomain(ConceptDomain domain) {
        List<ConceptType> result = new ArrayList<>();
        for (ConceptType type : concepts.keySet()) {
            if (type.getDomain() == domain) result.add(type);
        }
        return result;
    }

    public int getDomainCount() {
        Set<ConceptDomain> domains = EnumSet.noneOf(ConceptDomain.class);
        for (ConceptType type : concepts.keySet()) {
            domains.add(type.getDomain());
        }
        return domains.size();
    }

    public boolean hasAllDomains() {
        return getDomainCount() == ConceptDomain.values().length;
    }

    public int getTotalIntensity() {
        return concepts.values().stream().mapToInt(Integer::intValue).sum();
    }

    public Optional<ConceptType> getDominantConcept() {
        return concepts.entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey);
    }

    public void merge(ConceptData other) {
        for (Map.Entry<ConceptType, Integer> entry : other.concepts.entrySet()) {
            ConceptType type = entry.getKey();
            int otherIntensity = entry.getValue();
            if (concepts.containsKey(type)) {
                concepts.put(type, (concepts.get(type) + otherIntensity) / 2);
            } else {
                concepts.put(type, otherIntensity);
            }
        }
    }

    public ConceptData copy() {
        ConceptData copy = new ConceptData();
        copy.concepts.putAll(this.concepts);
        return copy;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (Map.Entry<ConceptType, Integer> entry : concepts.entrySet()) {
            CompoundTag conceptTag = new CompoundTag();
            conceptTag.putString("Id", entry.getKey().getId());
            conceptTag.putInt("Intensity", entry.getValue());
            list.add(conceptTag);
        }
        tag.put("Concepts", list);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        concepts.clear();
        if (tag.contains("Concepts", Tag.TAG_LIST)) {
            ListTag list = tag.getList("Concepts", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag conceptTag = list.getCompound(i);
                ConceptType type = ConceptType.fromId(conceptTag.getString("Id"));
                if (type != null) {
                    concepts.put(type, conceptTag.getInt("Intensity"));
                }
            }
        }
    }

    @Override
    public String toString() {
        return "ConceptData{concepts=" + concepts.size() + "}";
    }
}
