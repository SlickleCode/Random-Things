package lumien.randomthings.util;

import net.minecraft.entity.LivingEntity;

import java.lang.reflect.Field;

/**
 * Forces {@link LivingEntity} to recompute its synced potion-particle
 * metadata (color + hidden flag) on its very next tick - see {@link
 * lumien.randomthings.RandomThings}'s {@code LivingEquipmentChangeEvent}
 * listener for why Magic Hood needs this. {@code LivingEntity
 * .potionsNeedUpdate} is private with no public setter and no event fills
 * the gap (confirmed via {@code javap -p}/{@code javap -c}), so this
 * reflects it once and caches the {@link Field} - the same flag vanilla's
 * own effect-added/removed/expired paths already flip, just triggered from
 * outside the class for a case those paths don't cover.
 */
public class PotionMetadataUtil {
    private static Field potionsNeedUpdateField;
    private static boolean lookupFailed;

    public static void forceRecalculation(LivingEntity entity) {
        Field field = getField();

        if (field == null) {
            return;
        }

        try {
            field.set(entity, true);
        } catch (IllegalAccessException ignored) {
        }
    }

    private static Field getField() {
        if (potionsNeedUpdateField != null || lookupFailed) {
            return potionsNeedUpdateField;
        }

        try {
            Field field = LivingEntity.class.getDeclaredField("potionsNeedUpdate");
            field.setAccessible(true);
            potionsNeedUpdateField = field;
        } catch (NoSuchFieldException e) {
            lookupFailed = true;
        }

        return potionsNeedUpdateField;
    }
}
