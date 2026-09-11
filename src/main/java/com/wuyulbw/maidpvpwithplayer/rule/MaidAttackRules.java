package com.wuyulbw.maidpvpwithplayer.rule;

import com.wuyulbw.maidpvpwithplayer.compat.AttackListType;
import com.wuyulbw.maidpvpwithplayer.compat.MaidAttackList;
import com.wuyulbw.maidpvpwithplayer.config.AttackPlayerConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * The decision logic: "may this maid attack this player?".
 * <p>
 * Kept out of the mixin class on purpose, mixin classes should only contain injectors.
 */
public final class MaidAttackRules {
    private MaidAttackRules() {
    }

    /**
     * @param maidObject the maid entity (passed as {@link Object} so the mixin needs no maid types)
     * @param target     the entity the maid wants to attack
     * @return {@code null} to keep the Touhou Little Maid behaviour, otherwise the forced result
     */
    @Nullable
    public static Boolean canAttackPlayer(Object maidObject, LivingEntity target) {
        if (!(target instanceof Player player)) {
            return null;
        }
        if (!(maidObject instanceof TamableAnimal maid)) {
            return null;
        }
        // The master switch is off, keep vanilla / Touhou Little Maid behaviour.
        if (!AttackPlayerConfig.attackPlayer()) {
            return null;
        }

        AttackListType attackListType = null;
        if (AttackPlayerConfig.respectAttackList()) {
            attackListType = MaidAttackList.getAttackListType(maid, MaidAttackList.playerId());
            // "Automatic friendly" mode: a maid that has no 'minecraft:player' entry in its attack
            // list, or that has it set to "friendly", never attacks players.
            if (attackListType == null || attackListType == AttackListType.FRIENDLY) {
                return false;
            }
        }

        // The owner protection wins over everything else.
        if (maid.isOwnedBy(player) && !AttackPlayerConfig.attackOwner()) {
            return false;
        }

        // "neutral": only fight back when provoked.
        if (attackListType == AttackListType.NEUTRAL && !isProvoked(maid, player)) {
            return false;
        }

        // "hostile", or the attack list is not consulted at all: attack.
        return true;
    }

    /**
     * Same rules Touhou Little Maid uses for neutral mobs: the maid joins the fight when the
     * player hurt the owner, when the owner attacked the player, or when the player hurt the maid.
     */
    private static boolean isProvoked(TamableAnimal maid, Player player) {
        if (maid.getOwner() instanceof Player owner) {
            if (player.equals(owner.getLastHurtByMob())) {
                return true;
            }
            if (player.equals(owner.getLastHurtMob())) {
                return true;
            }
        }
        return player.equals(maid.getLastHurtByMob());
    }
}
