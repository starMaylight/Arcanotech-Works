package com.starmaylight.arcanotech_works.api.concept;

import net.minecraft.network.chat.Component;

/**
 * 全40種の概念タイプ（各学域に8つ）
 */
public enum ConceptType {
    // 天象学域（Celestial）
    DAWN("dawn", "黎明", ConceptDomain.CELESTIAL, "開始・顕現・起動"),
    VESPER_VEIL("vesper_veil", "宵帳", ConceptDomain.CELESTIAL, "終端・収束・眠り"),
    LIMINAL_LIGHT("liminal_light", "薄明", ConceptDomain.CELESTIAL, "端境・遷移光"),
    STARDUST("stardust", "星屑", ConceptDomain.CELESTIAL, "微細化・分散・粉化"),
    STAR_RING("star_ring", "星環", ConceptDomain.CELESTIAL, "周回・整列・秩序"),
    MOON_MARK("moon_mark", "月痕", ConceptDomain.CELESTIAL, "周期・反復・位相差"),
    SYZYGY("syzygy", "朔望", ConceptDomain.CELESTIAL, "同調・合致・共振条件"),
    COMET_TAIL("comet_tail", "彗尾", ConceptDomain.CELESTIAL, "速度・貫通・軌道"),

    // 境界学域（Liminal）
    BOUNDARY_RING("boundary_ring", "境環", ConceptDomain.LIMINAL, "接続・境界面"),
    GATE_SIG("gate_sig", "門標", ConceptDomain.LIMINAL, "入口・座標・標識"),
    SHADOW_PASS("shadow_pass", "影渡", ConceptDomain.LIMINAL, "迂回・すり抜け"),
    MIRRIMAGE("mirrimage", "鏡映", ConceptDomain.LIMINAL, "反転・複写"),
    ANTIPHASE("antiphase", "反相", ConceptDomain.LIMINAL, "位相反転・矛盾の芽"),
    BACKPATH("backpath", "裏路", ConceptDomain.LIMINAL, "秘匿・隠匿経路"),
    SEAL_KEY("seal_key", "封鍵", ConceptDomain.LIMINAL, "ロック・アクセス制御"),
    RIFT("rift", "裂目", ConceptDomain.LIMINAL, "断絶・分割・切断"),

    // 心象学域（Psyche）
    VOW_SCRIPT("vow_script", "誓詞", ConceptDomain.PSYCHE, "契約・誓約・固定"),
    OBSESSION("obsession", "執念", ConceptDomain.PSYCHE, "固着・保持・継続意志"),
    CRAVING("craving", "渇望", ConceptDomain.PSYCHE, "吸収・収集・引力"),
    EXALT("exalt", "昂揚", ConceptDomain.PSYCHE, "増幅・臨界・励起"),
    KIND_RAIN("kind_rain", "慈雨", ConceptDomain.PSYCHE, "緩和・保護・回復"),
    AWE("awe", "畏怖", ConceptDomain.PSYCHE, "抑制・距離・萎縮"),
    THOUGHT_SHADE("thought_shade", "想影", ConceptDomain.PSYCHE, "記憶・イメージ・残像"),
    ONEIRIC_GUIDE("oneiric_guide", "夢導", ConceptDomain.PSYCHE, "夢・誘導・暗示"),

    // 霊質学域（Aether）
    RESONANCE("resonance", "残響", ConceptDomain.AETHER, "余波・遅延・持続"),
    CUT_SONG("cut_song", "断歌", ConceptDomain.AETHER, "ノイズ切断・分解"),
    STILLNESS("stillness", "静謐", ConceptDomain.AETHER, "安定・沈静・固定化"),
    ETHER_HAZE("ether_haze", "霊霞", ConceptDomain.AETHER, "霧・媒質・漂い"),
    VOID_MIST("void_mist", "虚霧", ConceptDomain.AETHER, "虚・不確定・欠落"),
    MIRAGE("mirage", "泡影", ConceptDomain.AETHER, "幻・仮置き・蜃気楼"),
    AFTERGLOW("afterglow", "余燼", ConceptDomain.AETHER, "代償・残熱・終わりの力"),
    ABYSS("abyss", "深淵", ConceptDomain.AETHER, "未知・侵食・重層"),

    // 生相学域（Vital）
    SPROUT("sprout", "芽吹", ConceptDomain.VITAL, "成長・生成"),
    QUICKENING("quickening", "胎動", ConceptDomain.VITAL, "発生・孵化・始原"),
    BREATH("breath", "息吹", ConceptDomain.VITAL, "活性・鼓動・生命駆動"),
    ROOTLINE("rootline", "根脈", ConceptDomain.VITAL, "供給網・循環"),
    SWARM_RHYTHM("swarm_rhythm", "群律", ConceptDomain.VITAL, "群れ・同期・分散協調"),
    BEAST_CORE("beast_core", "獣心", ConceptDomain.VITAL, "本能・攻勢"),
    BLOOD_TIDE("blood_tide", "血潮", ConceptDomain.VITAL, "代謝・交換・代償"),
    WINGBEAT("wingbeat", "羽音", ConceptDomain.VITAL, "回避・軽化・浮遊");

    private final String id;
    private final String japaneseName;
    private final ConceptDomain domain;
    private final String meaning;

    ConceptType(String id, String japaneseName, ConceptDomain domain, String meaning) {
        this.id = id;
        this.japaneseName = japaneseName;
        this.domain = domain;
        this.meaning = meaning;
    }

    public String getId() { return id; }
    public String getJapaneseName() { return japaneseName; }
    public ConceptDomain getDomain() { return domain; }
    public String getMeaning() { return meaning; }

    public String getTranslationKey() {
        return "concept.arcanotech_works." + id;
    }

    public Component getDisplayName() {
        return Component.translatable(getTranslationKey()).withStyle(domain.getColor());
    }

    public static ConceptType fromId(String id) {
        for (ConceptType type : values()) {
            if (type.id.equals(id)) return type;
        }
        return null;
    }

    public static ConceptType[] getByDomain(ConceptDomain domain) {
        return java.util.Arrays.stream(values())
                .filter(c -> c.domain == domain)
                .toArray(ConceptType[]::new);
    }
}
