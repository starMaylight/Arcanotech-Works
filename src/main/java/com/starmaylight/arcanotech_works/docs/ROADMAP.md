# Arcano Tech Works 開発ロードマップ

## 全体構成

```
Phase 0: 基盤 ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ [完了]
Phase 1: 魔力システム ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ [完了]
Phase 2: 概念システム基盤 ━━━━━━━━━━━━━━━━━━━━━━━━━━━━ [完了]
Phase 3: 序盤コンテンツ ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ [進行中 80%]
Phase 4: 中盤コンテンツ ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ [未着手]
Phase 5: 終盤コンテンツ ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ [未着手]
Phase 6: 自動化・統合 ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ [未着手]
```

---

## Phase 0: 基盤 ✅完了

- Forge MDK 1.20.1 セットアップ
- パッケージ構成
- DeferredRegister基盤

---

## Phase 1: 魔力システム ✅完了

- IManaStorage / ManaStorage
- ManaCapability（アイテム・BlockEntity対応）
- ManaStoneItem（4品質）
- 素材アイテム（魔導銀系）
- 鉱石ブロック

---

## Phase 2: 概念システム基盤 ✅完了

| タスク | 状態 |
|--------|------|
| ConceptDomain（5学域） | ✅ |
| ConceptType（40概念） | ✅ |
| ConceptData | ✅ |
| ConceptDefinition（JSON形式） | ✅ |
| ConceptRegistry（データパック読込） | ✅ |
| ConceptCapability | ✅ |
| ConceptAttachHandler | ✅ |
| ConceptTooltipHandler | ✅ |
| 言語ファイル（日英） | ✅ |
| サンプル概念定義JSON | ✅ |

---

## Phase 3: 序盤コンテンツ [進行中 80%]

| タスク | 内容 | 状態 |
|--------|------|------|
| 3-1 魔力ネットワーク | 魔導銀導線、魔力転送 | ✅完了 |
| 3-2 魔力生成 | 魔石燃焼炉、魔石精錬機 | ✅完了 |
| 3-3 ワールド生成 | 鉱石生成、魔力だまり | ✅完了 |
| 3-4 魔力だまり | 感知器、収集機、パーティクル | ✅完了 |
| 3-5 刻印システム | 魔導板＋彫刻刀、JSONレシピ | ✅完了 |
| 3-6 基礎機械群 | 粉砕機、圧縮機、混合機 | 🔲未着手 |
| 3-7 発熱/冷却 | 機械オーバーヒート、ファン | 🔲未着手 |
| 3-8 研究システム基盤 | ガイドブック、ロック機構 | 🔲未着手 |

### 3-1 魔力ネットワーク ✅完了

| 実装内容 | ファイル |
|----------|----------|
| ManaConduitBlock | block/ManaConduitBlock.java |
| ManaConduitBlockEntity | block/ManaConduitBlockEntity.java |
| 6方向接続 | 100 mana/tick転送 |

### 3-2 魔力生成 ✅完了

| 実装内容 | ファイル |
|----------|----------|
| ManaGeneratorBlock/BlockEntity | block/ManaGeneratorBlock.java |
| ManaGeneratorMenu/Screen | GUI付き |
| ManaRefineryBlock/BlockEntity | block/ManaRefineryBlock.java |
| ManaRefineryMenu/Screen | GUI付き |
| 魔石システム | ManaGemItem（クオリティ1-1000） |
| ルートテーブル | 幸運対応、SetManaGemQualityFunction |

### 3-3 ワールド生成 ✅完了

| 実装内容 | ファイル |
|----------|----------|
| 魔導銀鉱石 | mithril_ore, deepslate_mithril_ore |
| マナ結晶鉱石 | mana_crystal_ore |
| JSON形式 | worldgen/configured_feature, placed_feature |

### 3-4 魔力だまり ✅完了

| 実装内容 | ファイル |
|----------|----------|
| ManaPoolSavedData | 永続化（チャンク座標） |
| ManaPoolGenerator | 1/2000チャンク確率 |
| ManaPoolSyncPacket | S2C同期 |
| ManaPoolClientData | クライアント側保持 |
| ManaPoolParticleHandler | 暗い霧パーティクル |
| ManaSensorItem | 魔力だまり感知器 |
| ManaCollectorBlock/BlockEntity | 収集機（10 mana/tick） |

### 3-5 刻印システム ✅完了

| 実装内容 | ファイル |
|----------|----------|
| EngravingTableBlock | block/engraving/EngravingTableBlock.java |
| EngravingTableBlockEntity | 7×7グリッド、溶融魔導銀タンク |
| EngravingTableMenu | 5スロット（燃料、魔導銀、彫刻刀、魔導板、出力） |
| EngravingTableScreen | ホバー/クリック効果、ステータス表示 |
| EngravingPattern | パターン定義、マッチング |
| EngravingPatterns | デフォルトパターン5種 |
| EngravingPatternLoader | JSONレシピ読み込み |
| EngravingClickPacket | C2Sパケット |

**刻印レシピ（JSONベース）:**
```
data/arcanotech_works/engraving_patterns/
├── basic_circuit.json     # 十字パターン → 基礎魔導回路
├── conduit_circuit.json   # X字パターン → 導線回路
├── collector_circuit.json # 渦巻きパターン → 収集回路
├── refinery_circuit.json  # 菱形パターン → 精錬回路
└── sensor_circuit.json    # 円形パターン → 感知回路
```

**刻印システム仕様:**
| 項目 | 値 |
|------|-----|
| グリッドサイズ | 7×7（49マス） |
| タンク容量 | 630mb |
| 魔導銀1インゴット | 90mb |
| 1回の彫刻コスト | 1mb + 彫刻刀耐久1 |
| 彫刻刀耐久 | 250 |
| 失敗時出力 | 劣化魔導板（全マス彫刻） |

---

## Phase 4: 中盤コンテンツ

| タスク | 内容 |
|--------|------|
| 4-1 解放機 | アイテム→未整相星理 |
| 4-2 純化装置 | 未整相星理→規格体 |
| 4-3 空白概念/矛盾空間 | チャンク汚染システム |
| 4-4 概念儀式 | マルチブロック |
| 4-5 追加機械 | 圧延、注入、彫金、蒸留 |
| 4-6 刻印自動化 | 精密印刷機 |
| 4-7 研究パズル | パズルUI、論文 |

---

## Phase 5: 終盤コンテンツ

| タスク | 内容 |
|--------|------|
| 5-1 高度な概念操作 | 概念合成、分離 |
| 5-2 リスク/リターン | 矛盾空間利用 |
| 5-3 虚無生成 | 禁断の研究 |
| 5-4 アイテム複製 | 概念結合 |
| 5-5 魔道具 | 魔導回路刻印 |
| 5-6 魔法システム | 宝玉、杖 |

---

## Phase 6: 自動化・統合

| タスク | 内容 |
|--------|------|
| 6-1 パイプ輸送 | アイテム/魔力/概念 |
| 6-2 研究ゴーレム | 研究自動化 |
| 6-3 JEI完全連携 | 研究ロック反映 |
| 6-4 バランス調整 | 数値、進行 |
| 6-5 ドキュメント | ガイドブック |

---

## 登録済みアイテム/ブロック一覧

### ブロック
| ID | 名前 | 説明 |
|----|------|------|
| mana_conduit | 魔導銀導線 | 魔力転送（100/tick） |
| mana_generator | 魔石燃焼炉 | 魔石→魔力 |
| mana_refinery | 魔石精錬機 | 魔石品質向上 |
| mana_collector | 魔力収集機 | 魔力だまりから収集 |
| engraving_table | 刻印台 | 魔導回路作成 |
| mana_crystal_ore | マナ結晶鉱石 | 魔石ドロップ |
| mithril_ore | 魔導銀鉱石 | |
| deepslate_mithril_ore | 深層魔導銀鉱石 | |
| mithril_block | 魔導銀ブロック | |
| raw_mithril_block | 原石魔導銀ブロック | |

### アイテム
| ID | 名前 | 説明 |
|----|------|------|
| mana_gem | 魔石 | 品質1-1000 |
| mana_sensor | 魔力感知器 | 魔力だまり可視化 |
| mithril_ingot | 魔導銀インゴット | |
| mithril_nugget | 魔導銀ナゲット | |
| mithril_dust | 魔導銀の粉 | |
| raw_mithril | 原石魔導銀 | |
| arcane_plate | 魔導板 | 刻印の素材 |
| engraving_tool | 彫刻刀 | 耐久250 |
| degraded_plate | 劣化魔導板 | 刻印失敗時 |
| basic_circuit | 基礎魔導回路 | |
| conduit_circuit | 導線回路 | |
| collector_circuit | 収集回路 | |
| refinery_circuit | 精錬回路 | |
| sensor_circuit | 感知回路 | |

---

## 概念一覧

### 天象学域（Celestial）
- 黎明(dawn), 宵帳(vesper_veil), 薄明(liminal_light), 星屑(stardust)
- 星環(star_ring), 月痕(moon_mark), 朔望(syzygy), 彗尾(comet_tail)

### 境界学域（Liminal）
- 境環(boundary_ring), 門標(gate_sig), 影渡(shadow_pass), 鏡映(mirrimage)
- 反相(antiphase), 裏路(backpath), 封鍵(seal_key), 裂目(rift)

### 心象学域（Psyche）
- 誓詞(vow_script), 執念(obsession), 渇望(craving), 昂揚(exalt)
- 慈雨(kind_rain), 畏怖(awe), 想影(thought_shade), 夢導(oneiric_guide)

### 霊質学域（Aether）
- 残響(resonance), 断歌(cut_song), 静謐(stillness), 霊霞(ether_haze)
- 虚霧(void_mist), 泡影(mirage), 余燼(afterglow), 深淵(abyss)

### 生相学域（Vital）
- 芽吹(sprout), 胎動(quickening), 息吹(breath), 根脈(rootline)
- 群律(swarm_rhythm), 獣心(beast_core), 血潮(blood_tide), 羽音(wingbeat)

---

## ファイル構成

```
src/main/java/com/starmaylight/arcanotech_works/
├── Arcanotech_works.java
├── Config.java
├── api/
│   ├── mana/
│   │   ├── IManaStorage.java
│   │   └── ManaStorage.java
│   └── concept/
│       ├── ConceptDomain.java
│       ├── ConceptType.java
│       ├── ConceptData.java
│       ├── ConceptDefinition.java
│       └── ConceptRegistry.java
├── block/
│   ├── ManaConduitBlock.java
│   ├── ManaConduitBlockEntity.java
│   ├── ManaGeneratorBlock.java
│   ├── ManaGeneratorBlockEntity.java
│   ├── ManaGeneratorMenu.java
│   ├── ManaRefineryBlock.java
│   ├── ManaRefineryBlockEntity.java
│   ├── ManaRefineryMenu.java
│   ├── ManaCollectorBlock.java
│   ├── ManaCollectorBlockEntity.java
│   └── engraving/
│       ├── EngravingTableBlock.java
│       ├── EngravingTableBlockEntity.java
│       ├── EngravingTableMenu.java
│       ├── EngravingPattern.java
│       ├── EngravingPatterns.java
│       └── EngravingPatternLoader.java
├── blockentity/
│   └── ModBlockEntities.java
├── capability/
│   ├── ManaCapability.java
│   └── ConceptCapability.java
├── client/
│   └── screen/
│       ├── ManaGeneratorScreen.java
│       ├── ManaRefineryScreen.java
│       └── EngravingTableScreen.java
├── event/
│   ├── ConceptAttachHandler.java
│   └── ConceptTooltipHandler.java
├── item/
│   ├── ManaGemItem.java
│   ├── ManaStoneItem.java
│   └── ManaSensorItem.java
├── loot/
│   ├── ModLootFunctions.java
│   └── SetManaGemQualityFunction.java
├── manapool/
│   ├── ManaPoolSavedData.java
│   ├── ManaPoolGenerator.java
│   ├── ManaPoolSyncPacket.java
│   ├── ManaPoolClientData.java
│   └── ManaPoolParticleHandler.java
├── network/
│   ├── ModNetwork.java
│   └── EngravingClickPacket.java
└── registry/
    ├── ModItems.java
    ├── ModBlocks.java
    ├── ModMenuTypes.java
    └── ModCreativeTabs.java

src/main/resources/
├── assets/arcanotech_works/
│   ├── lang/
│   │   ├── ja_jp.json
│   │   └── en_us.json
│   ├── textures/
│   │   ├── block/
│   │   ├── item/
│   │   └── gui/
│   ├── blockstates/
│   └── models/
│       ├── block/
│       └── item/
└── data/arcanotech_works/
    ├── arcanotech_concepts/vanilla/
    ├── engraving_patterns/
    ├── loot_tables/blocks/
    └── worldgen/
        ├── configured_feature/
        └── placed_feature/
```

---

## 次のステップ（優先順）

1. **3-6 基礎機械群** - 粉砕機、圧縮機、混合機
2. **3-7 発熱/冷却システム** - オーバーヒート、ファン
3. **3-8 研究システム基盤** - ガイドブック、ロック機構
4. **レシピ実装** - クラフト、精錬、機械レシピ
