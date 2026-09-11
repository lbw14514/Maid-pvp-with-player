package com.wuyulbw.maidpvpwithplayer.mixin;

import com.wuyulbw.maidpvpwithplayer.rule.MaidAttackRules;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Touhou Little Maid's {@code EntityMaid#canAttack(LivingEntity)} delegates to the current
 * attack task, and {@code IAttackTask#canAttack} always returns {@code false} for players.
 * <p>
 * Injecting at the head of {@code EntityMaid#canAttack} is the single choke point every
 * target search ({@code IAttackTask#findFirstValidAttackTarget}) goes through, so overriding
 * the result here is enough to let maids attack players.
 * <p>
 * The target is referenced by name on purpose, so this mod does not need the maid mod as a
 * compile dependency.
 */
@Mixin(targets = "com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid")
public abstract class EntityMaidCanAttackMixin {
    @Inject(
            method = "canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void maidPvpWithPlayer$canAttackPlayer(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        Boolean result = MaidAttackRules.canAttackPlayer(this, target);
        if (result != null) {
            cir.setReturnValue(result);
        }
    }
}
