package lumien.randomthings.item.spectretools;

import com.google.common.collect.Multimap;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.PickaxeItem;

/**
 * Ported from 1.12.2's {@code ItemSpectrePickaxe} - plain {@code ItemPickaxe} on the spectre material
 * (same 1/-2.8F damage/speed modifiers vanilla's own pickaxes use, matching 1.12.2 never overriding
 * them either), plus the same +3 block reach while held in the mainhand.
 */
public class SpectrePickaxeItem extends PickaxeItem {
    public SpectrePickaxeItem(Item.Properties properties) {
        super(SpectreItemTier.INSTANCE, 1, -2.8F, properties);
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
