package com.starmaylight.arcanotech_works.api.heat;

/**
 * 熱量管理インターフェース
 * 工業機械の発熱/冷却システムで使用
 */
public interface IHeatStorage {

    /**
     * 現在の熱量を取得
     */
    int getHeat();

    /**
     * 最大熱量を取得
     */
    int getMaxHeat();

    /**
     * 熱を追加
     * @param amount 追加する熱量
     * @return 実際に追加された熱量
     */
    int addHeat(int amount);

    /**
     * 熱を除去（冷却）
     * @param amount 除去する熱量
     * @return 実際に除去された熱量
     */
    int removeHeat(int amount);

    /**
     * 熱量を直接設定
     */
    void setHeat(int heat);

    /**
     * オーバーヒート状態かどうか
     */
    default boolean isOverheated() {
        return getHeat() >= getMaxHeat();
    }

    /**
     * 熱量の割合（0.0-1.0）
     */
    default float getHeatPercentage() {
        return getMaxHeat() > 0 ? (float) getHeat() / getMaxHeat() : 0f;
    }

    /**
     * 危険域かどうか（80%以上）
     */
    default boolean isDangerous() {
        return getHeatPercentage() >= 0.8f;
    }

    /**
     * 警告域かどうか（60%以上）
     */
    default boolean isWarning() {
        return getHeatPercentage() >= 0.6f;
    }
}
