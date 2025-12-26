package com.starmaylight.arcanotech_works.api.concept;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * 概念の学域（5つの大分類）
 */
public enum ConceptDomain {
    CELESTIAL("celestial", "天象学域", ChatFormatting.GOLD),
    LIMINAL("liminal", "境界学域", ChatFormatting.DARK_PURPLE),
    PSYCHE("psyche", "心象学域", ChatFormatting.LIGHT_PURPLE),
    AETHER("aether", "霊質学域", ChatFormatting.AQUA),
    VITAL("vital", "生相学域", ChatFormatting.GREEN);

    private final String id;
    private final String japaneseName;
    private final ChatFormatting color;

    ConceptDomain(String id, String japaneseName, ChatFormatting color) {
        this.id = id;
        this.japaneseName = japaneseName;
        this.color = color;
    }

    public String getId() { return id; }
    public String getJapaneseName() { return japaneseName; }
    public ChatFormatting getColor() { return color; }

    public String getTranslationKey() {
        return "concept.arcanotech_works.domain." + id;
    }

    public Component getDisplayName() {
        return Component.translatable(getTranslationKey()).withStyle(color);
    }

    public static ConceptDomain fromId(String id) {
        for (ConceptDomain domain : values()) {
            if (domain.id.equals(id)) return domain;
        }
        return null;
    }
}
