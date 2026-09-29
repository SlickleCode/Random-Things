package lumien.randomthings.item.spectretools;

import com.google.common.collect.Multimap;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;

/**
 * Ported from 1.12.2's {@code ItemSpectreAxe} - 8.0 attack damage / -3.0 attack speed hardcoded
 * directly onto the item there (bypassing the material's own damage bonus), reproduced here as a
 * {@code 5.0F} damage modifier since 1.14.4's {@code AxeItem} constructor adds the tier's own
 * {@link SpectreItemTier#getAttackDamage() 3.0F} on top (5 + 3 = 8, matching exactly); attack speed
 * isn't tier-additive in this version's {@code ToolItem}, so {@code -3.0F} carries over unchanged.
 * Plus the same +3 block reach while held in the mainhand.
 *
 * <p>1.12.2's {@code getDestroySpeed} override (full efficiency on wood/plant/vine materials
 * regardless of the block-specific {@code EFFECTIVE_ON} list) needs no equivalent here - vanilla's own
 * {@code AxeItem#getDestroySpeed} already does exactly that check (material != WOOD/PLANTS/
 * TALL_PLANTS/BAMBOO -&gt; full efficiency), confirmed from the real 1.14.4 source before concluding no
 * override was needed.
 */
public class SpectreAxeItem extends AxeItem {
    public SpectreAxeItem(Item.Properties properties) {
        super(SpectreItemTier.INSTANCE, 5.0F, -3.0F, properties);
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot) {
        Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(equipmentSlot);

        if (equipmentSlot == EquipmentSlotType.MAINHAND) {
            multimap.put(PlayerEntity.REACH_DISTANCE.getName(), new AttributeModifier(SpectreItemTier.REACH_MODIFIER_UUID, "Spectre Range Modifier", 3, AttributeModifier.Operation.ADDITION));
        }

        return multimap;
    }
}
