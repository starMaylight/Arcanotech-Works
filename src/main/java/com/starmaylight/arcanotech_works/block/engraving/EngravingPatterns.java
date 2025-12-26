package com.starmaylight.arcanotech_works.block.engraving;

import com.starmaylight.arcanotech_works.Arcanotech_works;
import com.starmaylight.arcanotech_works.registry.ModItems;

/**
 * 刻印パターンの登録
 * デフォルトパターンはここで定義され、JSONで上書き可能
 */
public class EngravingPatterns {

    private static boolean initialized = false;

    /**
     * デフォルトパターンを登録
     */
    public static void registerDefaults() {
        // 基礎魔導回路 - 単純な十字パターン
        new EngravingPattern(
                "arcanotech_works:basic_circuit",
                "基礎魔導回路",
                new String[] {
                        "...#...",
                        "...#...",
                        "...#...",
                        "#######",
                        "...#...",
                        "...#...",
                        "...#..."
                },
                ModItems.BASIC_CIRCUIT::get
        );
        
        // 導線回路 - X字パターン
        new EngravingPattern(
                "arcanotech_works:conduit_circuit",
                "導線回路",
                new String[] {
                        "#.....#",
                        ".#...#.",
                        "..#.#..",
                        "...#...",
                        "..#.#..",
                        ".#...#.",
                        "#.....#"
                },
                ModItems.CONDUIT_CIRCUIT::get
        );
        
        // 収集回路 - 渦巻きパターン
        new EngravingPattern(
                "arcanotech_works:collector_circuit",
                "収集回路",
                new String[] {
                        ".#####.",
                        ".#.....",
                        ".#.###.",
                        ".#.#.#.",
                        ".###.#.",
                        ".....#.",
                        ".#####."
                },
                ModItems.COLLECTOR_CIRCUIT::get
        );
        
        // 精錬回路 - 菱形パターン
        new EngravingPattern(
                "arcanotech_works:refinery_circuit",
                "精錬回路",
                new String[] {
                        "...#...",
                        "..#.#..",
                        ".#...#.",
                        "#.....#",
                        ".#...#.",
                        "..#.#..",
                        "...#..."
                },
                ModItems.REFINERY_CIRCUIT::get
        );
        
        // 感知回路 - 円形パターン
        new EngravingPattern(
                "arcanotech_works:sensor_circuit",
                "感知回路",
                new String[] {
                        "..###..",
                        ".#...#.",
                        "#.....#",
                        "#..#..#",
                        "#.....#",
                        ".#...#.",
                        "..###.."
                },
                ModItems.SENSOR_CIRCUIT::get
        );
        
        Arcanotech_works.LOGGER.info("Registered {} default engraving patterns", EngravingPattern.getPatternCount());
    }
    
    /**
     * 初期化（commonSetupで呼ばれる）
     */
    public static void init() {
        if (!initialized) {
            registerDefaults();
            initialized = true;
        }
    }
}
