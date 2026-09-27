package lumien.randomthings.item;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Worn as a helmet: hides your nametag from other players and hides your
 * potion-particle swirl, in both cases regardless of sneaking. Found in
 * dungeon chests in 1.12.2 - no crafting recipe there either. This item
 * class carries no logic of its own:
 * <ul>
 * <li>Nametag-hiding is implemented by {@code MagicHoodTransformer.js} (a
 * coremod, see {@code lumien.randomthings.asm.AsmHandler#overrideCanRenderName}),
 * which intercepts {@code LivingRenderer.canRenderName} globally - confirmed
 * via {@code javap -c} that no clean Forge event exists for this in this
 * Forge version ({@code RenderNameplateEvent} doesn't exist until a later
 * one).</li>
 * <li>Particle-hiding uses a real, already-existing Forge event instead -
 * {@code PotionColorCalculationEvent#shouldHideParticles}, listened for in
 * {@code RandomThings}'s constructor - no coremod needed for this half at
 * all.</li>
 * </ul>
 * Direct port of 1.12.2's {@code ItemMagicHood}. Indestructible (no override
 * needed - {@code Item.isDamageable()} already just checks {@code maxDamage
 * > 0}, and this item's {@code Properties} never sets one).
 */
public class MagicHoodItem extends ArmorItem {
    public MagicHoodItem(Item.Properties properties) {
        super(ArmorMaterial.CHAIN, EquipmentSlotType.HEAD, properties);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type) {
        return "randomthings:textures/models/armor/magic_hood.png";
    }
}
