package com.wuyulbw.maidpvpwithplayer;

import com.mojang.logging.LogUtils;
import com.wuyulbw.maidpvpwithplayer.config.AttackPlayerConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

/**
 * Entry point of the "Maid PvP with Player" mod.
 * <p>
 * The whole mod is server side logic, it only needs to inject a small hook into
 * {@code EntityMaid#canAttack(LivingEntity)} (see {@code maid_pvp_with_player.mixins.json}).
 */
@Mod(MaidPvpWithPlayer.MOD_ID)
public final class MaidPvpWithPlayer {
    public static final String MOD_ID = "maid_pvp_with_player";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MaidPvpWithPlayer(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, AttackPlayerConfig.SPEC);
    }
}
