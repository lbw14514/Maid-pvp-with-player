package com.wuyulbw.maidpvpwithplayer.compat;

import org.jetbrains.annotations.Nullable;

/**
 * Mirror of Touhou Little Maid's {@code MonsterType} enum, so this mod does not need a
 * compile time dependency on the maid mod.
 */
public enum AttackListType {
    /**
     * The maid always attacks this entity type.
     */
    HOSTILE,
    /**
     * The maid only attacks this entity type after being provoked.
     */
    NEUTRAL,
    /**
     * The maid never attacks this entity type.
     */
    FRIENDLY;

    @Nullable
    public static AttackListType byName(String name) {
        for (AttackListType type : values()) {
            if (type.name().equals(name)) {
                return type;
            }
        }
        return null;
    }
}
