package com.wuyulbw.maidpvpwithplayer.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Configuration of the mod.
 * <p>
 * This file holds the values that survive, the game rules are what the decision logic reads. The
 * two are kept in sync by {@code MaidPvpRuleSync}: on every server start, and every time NeoForge
 * reloads this file (editing it while the game runs counts), the three values below are copied into
 * the matching game rules.
 * <p>
 * That is what gives the mod two control surfaces: the file can be prepared while the game is
 * closed, and {@code /gamerule} can flip a switch live.
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
                "Synced to the 'maidPvpAttackPlayer' game rule on server start and on every config reload.",
                "总开关：女仆是否被允许攻击玩家。",
                "关闭时女仆保持车万女仆原版行为，永远不会把玩家当作攻击目标。",
                "开启后具体攻击哪些玩家由女仆攻击列表决定，见 respectAttackList。",
                "服务端启动时、以及每次配置重载时，该值会同步到游戏规则 maidPvpAttackPlayer。");
        ATTACK_PLAYER = builder.define("attackPlayer", DEFAULT_ATTACK_PLAYER);

        builder.comment(
                "Whether maids are allowed to attack their own owner.",
                "Only works while attackPlayer is true and the owner is a valid target.",
                "Synced to the 'maidPvpAttackOwner' game rule.",
                "女仆是否可以攻击自己的主人（仅在 attackPlayer 为 true 时生效）。",
                "会同步到游戏规则 maidPvpAttackOwner。");
        ATTACK_OWNER = builder.define("attackOwner", DEFAULT_ATTACK_OWNER);

        builder.comment(
                "Whether the maid attack list entry of 'minecraft:player' decides which players a maid attacks.",
                "true (recommended): only players that are explicitly set to HOSTILE or NEUTRAL in the maid",
                "      attack list are attacked. A maid without a 'minecraft:player' entry is treated as",
                "      FRIENDLY and never attacks players.",
                "false: ignore the attack list, every player is attacked (except the owner while attackOwner is false).",
                "Synced to the 'maidPvpRespectAttackList' game rule.",
                "是否由女仆攻击列表中 minecraft:player 这一项决定该女仆攻击哪些玩家。",
                "true（推荐）：只有被显式设为「敌对」或「中立」的玩家会被攻击；未配置该项的女仆按「友好」",
                "      处理，永远不会攻击玩家。",
                "false：忽略攻击列表，除主人外（attackOwner 为 false 时）所有玩家都会被攻击。",
                "会同步到游戏规则 maidPvpRespectAttackList。");
        RESPECT_ATTACK_LIST = builder.define("respectAttackList", DEFAULT_RESPECT_ATTACK_LIST);

        SPEC = builder.build();
    }

    private AttackPlayerConfig() {
    }

    /**
     * Server configs are only loaded once a world is running. The sync only asks for these values
     * while a server is up, but fall back to the defaults instead of crashing just in case.
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

    /**
     * Writes a value straight into the config file. Called when a player changes the matching game
     * rule, so that a {@code /gamerule} change survives a restart exactly like an edit of this file
     * does.
     * <p>
     * {@code save()} refires the config reloading event, which is harmless: the sync then pushes
     * the file value into the rule that the player just set, sees it is already there and stops.
     */
    public static void saveAttackPlayer(boolean value) {
        write(ATTACK_PLAYER, value);
    }

    public static void saveAttackOwner(boolean value) {
        write(ATTACK_OWNER, value);
    }

    public static void saveRespectAttackList(boolean value) {
        write(RESPECT_ATTACK_LIST, value);
    }

    private static void write(ModConfigSpec.BooleanValue value, boolean newValue) {
        // Both the value and the file write need a loaded config; a rule can only change while a
        // server runs, which is exactly when this one is loaded.
        if (!SPEC.isLoaded()) {
            return;
        }
        value.set(newValue);
        SPEC.save();
    }
}
