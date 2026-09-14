package com.wuyulbw.maidpvpwithplayer.gamerule;

import com.wuyulbw.maidpvpwithplayer.config.AttackPlayerConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * Pushes the three config file values into the matching game rules.
 * <p>
 * The config file is the value that survives and the game rules are what the decision logic reads,
 * so this is the direction that makes the file work as a switch: editing it while the game is
 * closed, or while it runs, ends up in the rules. The other direction lives in
 * {@link MaidPvpGameRules}, where a rule changed with {@code /gamerule} is written back into the
 * file, which makes the two always agree.
 */
public final class MaidPvpRuleSync {
    /**
     * True while the config file values are being pushed into the game rules, so that the rule
     * callbacks can tell a value coming from the file apart from a value a player just set.
     * <p>
     * Only ever touched on the server thread: a rule is set either by a command or by
     * {@link #onConfigReloading}, which hands the work to the server thread first.
     */
    private static boolean applyingFromConfig;

    private MaidPvpRuleSync() {
    }

    public static boolean isApplyingFromConfig() {
        return applyingFromConfig;
    }

    public static void onServerStarted(ServerStartedEvent event) {
        apply(event.getServer());
    }

    public static void onConfigReloading(ModConfigEvent.Reloading event) {
        ModConfig config = event.getConfig();
        if (config.getType() != ModConfig.Type.SERVER || config.getSpec() != AttackPlayerConfig.SPEC) {
            return;
        }
        // A server config is also synced to clients when they connect, and there is no server to
        // push the values into on the client side.
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null || !server.isRunning()) {
            return;
        }
        // Reloads arrive from the file watcher thread and from the command that wrote the file,
        // but game rules belong to the server thread.
        server.execute(() -> apply(server));
    }

    private static void apply(MinecraftServer server) {
        applyingFromConfig = true;
        try {
            GameRules rules = server.getGameRules();
            set(rules, MaidPvpGameRules.ATTACK_PLAYER, AttackPlayerConfig.attackPlayer(), server);
            set(rules, MaidPvpGameRules.ATTACK_OWNER, AttackPlayerConfig.attackOwner(), server);
            set(rules, MaidPvpGameRules.RESPECT_ATTACK_LIST, AttackPlayerConfig.respectAttackList(), server);
        } finally {
            applyingFromConfig = false;
        }
    }

    private static void set(GameRules rules, GameRules.Key<GameRules.BooleanValue> key,
            boolean value, MinecraftServer server) {
        GameRules.BooleanValue rule = rules.getRule(key);
        if (rule.get() != value) {
            rule.set(value, server);
        }
    }
}
