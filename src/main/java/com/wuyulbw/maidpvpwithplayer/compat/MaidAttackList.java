package com.wuyulbw.maidpvpwithplayer.compat;

import com.wuyulbw.maidpvpwithplayer.MaidPvpWithPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Reads the per maid attack list (hostile / neutral / friendly) from Touhou Little Maid.
 * <p>
 * Everything is accessed through reflection on purpose: this way the mod compiles against
 * NeoForge only, and a maid mod update that renames the internal classes will just disable the
 * attack list support instead of crashing the game.
 * <p>
 * The data lives in {@code EntityMaid#getData(InitTaskData.ATTACK_LIST)} and is a
 * {@code Map<ResourceLocation, MonsterType>}.
 */
public final class MaidAttackList {
    private static final String ENTITY_MAID_CLASS =
            "com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid";
    private static final String INIT_TASK_DATA_CLASS =
            "com.github.tartaricacid.touhoulittlemaid.init.InitTaskData";
    private static final String TASK_DATA_KEY_CLASS =
            "com.github.tartaricacid.touhoulittlemaid.api.entity.data.TaskDataKey";

    private static volatile boolean resolved;
    @Nullable
    private static Field attackListKeyField;
    @Nullable
    private static Method getDataMethod;

    private MaidAttackList() {
    }

    public static ResourceLocation playerId() {
        return BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.PLAYER);
    }

    /**
     * @return the attack list entry of the given entity type, or {@code null} when the maid has
     * no entry for it (or when the maid mod data could not be read).
     */
    @Nullable
    public static AttackListType getAttackListType(TamableAnimal maid, ResourceLocation entityId) {
        if (!resolve()) {
            return null;
        }
        try {
            Object key = attackListKeyField.get(null);
            if (key == null) {
                return null;
            }
            Object data = getDataMethod.invoke(maid, key);
            if (data == null) {
                return null;
            }
            Method attackGroups = data.getClass().getMethod("attackGroups");
            if (!(attackGroups.invoke(data) instanceof Map<?, ?> groups)) {
                return null;
            }
            if (groups.get(entityId) instanceof Enum<?> monsterType) {
                return AttackListType.byName(monsterType.name());
            }
        } catch (Exception e) {
            MaidPvpWithPlayer.LOGGER.debug("[Maid PvP with Player] Could not read the maid attack list", e);
        }
        return null;
    }

    private static boolean resolve() {
        if (resolved) {
            return attackListKeyField != null && getDataMethod != null;
        }
        synchronized (MaidAttackList.class) {
            if (resolved) {
                return attackListKeyField != null && getDataMethod != null;
            }
            try {
                Class<?> initTaskData = Class.forName(INIT_TASK_DATA_CLASS);
                Class<?> taskDataKey = Class.forName(TASK_DATA_KEY_CLASS);
                Class<?> entityMaid = Class.forName(ENTITY_MAID_CLASS);

                attackListKeyField = initTaskData.getField("ATTACK_LIST");
                getDataMethod = entityMaid.getMethod("getData", taskDataKey);
            } catch (Throwable t) {
                MaidPvpWithPlayer.LOGGER.warn(
                        "[Maid PvP with Player] Touhou Little Maid attack list is unavailable, "
                                + "the maid attack list will be ignored.", t);
            }
            resolved = true;
            return attackListKeyField != null && getDataMethod != null;
        }
    }
}
