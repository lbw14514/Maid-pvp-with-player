package com.wuyulbw.maidpvpwithplayer.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Configuration of the mod.
 * <p>
 * The values are read while a maid picks an attack target, so they are stored in a
 * server config and can be changed per world / per server.
 */
public final class AttackPlayerConfig {
    private static final boolean DEFAULT_ATTACK_PLAYER = false;
    private static final boolean DEFAULT_ATTACK_OWNER = false;
    private static final boolean DEFAULT_RESPECT_ATTACK_LIST = true;

    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.BooleanValue ATTACK_PLAYER;
    private static final ModConfigSpec.BooleanValue ATTACK_OWNER;
    private static final ModConfigSpec.BooleanValue RESPECT_ATTACK_LIST;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment(
                "Master switch: whether maids are allowed to attack players at all.",
                "While false, maids keep the Touhou Little Maid behaviour and never target players.",
                "While true, which players get attacked is decided by the maid attack list, see respectAttackList.",
                "总开关：女仆是否被允许攻击玩家。",
                "关闭时女仆保持车万女仆原版行为，永远不会把玩家当作攻击目标。",
                "开启后具体攻击哪些玩家由女仆攻击列表决定，见 respectAttackList。")
                .translation("maid_pvp_with_player.config.attackPlayer");
        ATTACK_PLAYER = builder.define("attackPlayer", DEFAULT_ATTACK_PLAYER);

        builder.comment(
                "Whether maids are allowed to attack their own owner.",
                "Only works while attackPlayer is true and the owner is a valid target.",
                "女仆是否可以攻击自己的主人（仅在 attackPlayer 为 true 时生效）。")
                .translation("maid_pvp_with_player.config.attackOwner");
        ATTACK_OWNER = builder.define("attackOwner", DEFAULT_ATTACK_OWNER);

        builder.comment(
                "Whether the maid attack list entry of 'minecraft:player' decides which players a maid attacks.",
                "true (recommended): only players that are explicitly set to HOSTILE or NEUTRAL in the maid",
                "      attack list are attacked. A maid without a 'minecraft:player' entry is treated as",
                "      FRIENDLY and never attacks players.",
                "false: ignore the attack list, every player is attacked (except the owner while attackOwner is false).",
                "是否由女仆攻击列表中 minecraft:player 这一项决定该女仆攻击哪些玩家。",
                "true（推荐）：只有被显式设为「敌对」或「中立」的玩家会被攻击；未配置该项的女仆按「友好」",
                "      处理，永远不会攻击玩家。",
                "false：忽略攻击列表，除主人外（attackOwner 为 false 时）所有玩家都会被攻击。")
                .translation("maid_pvp_with_player.config.respectAttackList");
        RESPECT_ATTACK_LIST = builder.define("respectAttackList", DEFAULT_RESPECT_ATTACK_LIST);

        SPEC = builder.build();
    }

    private AttackPlayerConfig() {
    }

    /**
     * Server configs are only loaded once a world is running. The AI only asks for these
     * values in world, but fall back to the defaults instead of crashing just in case.
     */
    private static boolean read(ModConfigSpec.BooleanValue value, boolean fallback) {
        try {
            return value.get();
        } catch (IllegalStateException e) {
            return fallback;
        }
    }

    public static boolean attackPlayer() {
        return read(ATTACK_PLAYER, DEFAULT_ATTACK_PLAYER);
    }

    public static boolean attackOwner() {
        return read(ATTACK_OWNER, DEFAULT_ATTACK_OWNER);
    }

    public static boolean respectAttackList() {
        return read(RESPECT_ATTACK_LIST, DEFAULT_RESPECT_ATTACK_LIST);
    }
}
