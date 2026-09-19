package com.wuyulbw.maidpvpwithplayer.gamerule;

import com.wuyulbw.maidpvpwithplayer.config.AttackPlayerConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * The three switches of the mod, exposed as game rules so that they can be read and changed in
 * game with {@code /gamerule <name> <value>}.
 * <p>
 * A rule changed by a player is written straight back into the config file, so both the command
 * and the file always hold the same value and either of them can be used to change a switch.
 * {@link MaidPvpRuleSync} owns the other direction, config file into game rules.
 */
public final class MaidPvpGameRules {
    /**
     * Master switch: whether maids are allowed to attack players at all. While false maids keep
     * the Touhou Little Maid behaviour and never target players.
     */
    public static final GameRules.Key<GameRules.BooleanValue> ATTACK_PLAYER =
            define("maidPvpAttackPlayer", false, value -> AttackPlayerConfig.saveAttackPlayer(value));

    /**
     * Whether maids are allowed to attack their own owner. Only useful while
     * {@link #ATTACK_PLAYER} is true.
     */
    public static final GameRules.Key<GameRules.BooleanValue> ATTACK_OWNER =
            define("maidPvpAttackOwner", false, value -> AttackPlayerConfig.saveAttackOwner(value));

    /**
     * Whether the maid attack list entry of {@code minecraft:player} decides which players a maid
     * attacks. While true a maid that has no such entry (or has it set to FRIENDLY) never attacks
     * players; while false every player is attacked, except the owner if {@link #ATTACK_OWNER} is
     * false.
     */
    public static final GameRules.Key<GameRules.BooleanValue> RESPECT_ATTACK_LIST =
            define("maidPvpRespectAttackList", true, value -> AttackPlayerConfig.saveRespectAttackList(value));

    private MaidPvpGameRules() {
    }

    /**
     * @param writeBack where a value set by a player is stored, see the field javadoc above
     */
    private static GameRules.Key<GameRules.BooleanValue> define(String name, boolean defaultValue,
            Consumer<Boolean> writeBack) {
        return GameRules.register(name, GameRules.Category.MOBS,
                GameRules.BooleanValue.create(defaultValue, (server, value) -> {
                    // A value that came out of the config file is already in the file, writing it
                    // back would only rewrite the file for nothing.
                    if (!MaidPvpRuleSync.isApplyingFromConfig()) {
                        writeBack.accept(value.get());
                    }
                }));
    }

    /**
     * Registers the three rules by loading this class, the actual work happens in the field
     * initializers above.
     * <p>
     * Called from the mod constructor on purpose: {@code GameRules} builds its rule values from
     * the registry when it is instantiated, so a rule registered after a world already created
     * its {@code GameRules} would be missing there and reading it would fail.
     */
    public static void register() {
    }

    public static boolean attackPlayer(Level level) {
        return level.getGameRules().getBoolean(ATTACK_PLAYER);
    }

    public static boolean attackOwner(Level level) {
        return level.getGameRules().getBoolean(ATTACK_OWNER);
    }

    public static boolean respectAttackList(Level level) {
        return level.getGameRules().getBoolean(RESPECT_ATTACK_LIST);
    }
}
