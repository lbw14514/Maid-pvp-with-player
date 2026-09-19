package com.wuyulbw.maidpvpwithplayer;

import com.mojang.logging.LogUtils;
import com.wuyulbw.maidpvpwithplayer.config.AttackPlayerConfig;
import com.wuyulbw.maidpvpwithplayer.gamerule.MaidPvpGameRules;
import com.wuyulbw.maidpvpwithplayer.gamerule.MaidPvpRuleSync;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

/**
 * Entry point of the "Maid PvP with Player" mod.
 * <p>
 * The whole mod is server side logic, it only needs to inject a small hook into
 * {@code EntityMaid#canAttack(LivingEntity)} (see {@code maid_pvp_with_player.mixins.json}).
 * <p>
 * The three switches exist twice: as a config file, which is the value that survives, and as game
 * rules, which is the value the decision logic reads and which {@code /gamerule} can change live.
 * {@link MaidPvpRuleSync} keeps the two in sync.
 */
@Mod(MaidPvpWithPlayer.MOD_ID)
public final class MaidPvpWithPlayer {
    public static final String MOD_ID = "maid_pvp_with_player";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MaidPvpWithPlayer(IEventBus modEventBus, ModContainer modContainer) {
        // The rules must exist before the first world builds its GameRules instance.
        MaidPvpGameRules.register();
        modContainer.registerConfig(ModConfig.Type.SERVER, AttackPlayerConfig.SPEC);

        modEventBus.addListener(MaidPvpRuleSync::onConfigReloading);
        NeoForge.EVENT_BUS.addListener(MaidPvpRuleSync::onServerStarted);
    }
}
