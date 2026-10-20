package lumien.randomthings.item.spectretools;

import com.google.common.collect.Multimap;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShovelItem;
import com.google.common.collect.ArrayListMultimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.common.ForgeMod;

/**
 * Ported from 1.12.2's {@code ItemSpectreShovel} - plain {@code ItemSpade} on the spectre material
 * (same 1.5F/-3.0F damage/speed modifiers vanilla's own shovels use, matching 1.12.2 never overriding
 * them either), plus the same +3 block reach while held in the mainhand.
 */
public class SpectreShovelItem extends ShovelItem {
    public SpectreShovelItem(Item.Properties properties) {
        super(SpectreItemTier.INSTANCE, 1.5F, -3.0F, properties);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
        Multimap<Attribute, AttributeModifier> multimap = ArrayListMultimap.create(super.getDefaultAttributeModifiers(equipmentSlot));

        if (equipmentSlot == EquipmentSlot.MAINHAND) {
            multimap.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(SpectreItemTier.REACH_MODIFIER_UUID, "Spectre Range Modifier", 3, AttributeModifier.Operation.ADDITION));
        }

        return multimap;
    }
}
