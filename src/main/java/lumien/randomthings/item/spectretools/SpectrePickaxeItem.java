package lumien.randomthings.item.spectretools;

import com.google.common.collect.Multimap;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import com.google.common.collect.ArrayListMultimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.common.ForgeMod;

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
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
        Multimap<Attribute, AttributeModifier> multimap = ArrayListMultimap.create(super.getDefaultAttributeModifiers(equipmentSlot));

        if (equipmentSlot == EquipmentSlot.MAINHAND) {
            multimap.put(ForgeMod.REACH_DISTANCE.get(), new AttributeModifier(SpectreItemTier.REACH_MODIFIER_UUID, "Spectre Range Modifier", 3, AttributeModifier.Operation.ADDITION));
        }

        return multimap;
    }
}
