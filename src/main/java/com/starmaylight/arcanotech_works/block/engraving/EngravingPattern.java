package com.starmaylight.arcanotech_works.block.engraving;

import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 刻印パターンの定義
 * 7x7グリッドでの彫刻パターンと出力アイテムを定義
 */
public class EngravingPattern {

    private final String id;
    private final String name;
    private final boolean[][] pattern; // 7x7 グリッド、trueは彫刻が必要な場所
    private final Supplier<Item> output;
    
    // 登録されたすべてのパターン
    private static final List<EngravingPattern> PATTERNS = new ArrayList<>();
    
    public EngravingPattern(String id, String name, String[] patternLines, Supplier<Item> output) {
        this.id = id;
        this.name = name;
        this.output = output;
        this.pattern = parsePattern(patternLines);
        PATTERNS.add(this);
    }
    
    /**
     * パターンリストをクリア（リロード時用）
     */
    public static void clearPatterns() {
        PATTERNS.clear();
    }
    
    /**
     * 文字列配列からパターンを解析
     * '#' = 彫刻が必要、'.' = 彫刻しない
     */
    private boolean[][] parsePattern(String[] lines) {
        boolean[][] result = new boolean[7][7];
        for (int y = 0; y < 7 && y < lines.length; y++) {
            String line = lines[y];
            for (int x = 0; x < 7 && x < line.length(); x++) {
                result[y][x] = line.charAt(x) == '#';
            }
        }
        return result;
    }
    
    /**
     * グリッド状態がこのパターンにマッチするかチェック
     * @param grid 7x7のグリッド状態（trueは彫刻済み）
     */
    public boolean matches(boolean[][] grid) {
        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 7; x++) {
                if (pattern[y][x] != grid[y][x]) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * グリッドがすべて彫刻済みかチェック（劣化判定用）
     */
    public static boolean isFullyEngraved(boolean[][] grid) {
        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 7; x++) {
                if (!grid[y][x]) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * マッチするパターンを検索
     */
    public static EngravingPattern findMatch(boolean[][] grid) {
        for (EngravingPattern pattern : PATTERNS) {
            if (pattern.matches(grid)) {
                return pattern;
            }
        }
        return null;
    }
    
    /**
     * 彫刻が必要なセルの数を取得
     */
    public int getRequiredEngravings() {
        int count = 0;
        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 7; x++) {
                if (pattern[y][x]) count++;
            }
        }
        return count;
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public boolean[][] getPattern() { return pattern; }
    public Item getOutput() { return output.get(); }
    
    public static List<EngravingPattern> getAllPatterns() {
        return new ArrayList<>(PATTERNS);
    }
    
    public static int getPatternCount() {
        return PATTERNS.size();
    }
    
    /**
     * パターンの特定位置が彫刻必要かどうか
     */
    public boolean requiresEngraving(int x, int y) {
        if (x < 0 || x >= 7 || y < 0 || y >= 7) return false;
        return pattern[y][x];
    }
}
