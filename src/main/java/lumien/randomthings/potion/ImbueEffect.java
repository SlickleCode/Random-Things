package lumien.randomthings.potion;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

/**
 * A passive marker effect applied by drinking an {@link lumien.randomthings.item.ImbueItem}
 * - carries no tick behavior of its own; combat code elsewhere checks
 * {@code LivingEntity.isPotionActive} for one of these four instances directly
 * (fire/poison/wither on the next hit dealt, experience on the next kill).
 * Direct port of 1.12.2's {@code ImbueFire}/{@code ImbuePoison}/
 * {@code ImbueExperience}/{@code ImbueWither} (all four were identical
 * one-liner subclasses of the shared {@code PotionBase}, so this port keeps
 * one class instantiated four times instead). The original's custom HUD/
 * inventory icon drawing has no equivalent needed here: 1.14.4 looks up a
 * modded effect's icon automatically from {@code textures/mob_effect/<name>.png}
 * (confirmed via javap on {@code PotionSpriteUploader}/{@code DisplayEffectsScreen} -
 * {@code IForgeEffect}'s render hooks are empty no-ops by default), so supplying
 * that texture is enough on its own.
 */
public class ImbueEffect extends Effect {
    public ImbueEffect(int liquidColor) {
        super(EffectType.NEUTRAL, liquidColor);
    }
}
