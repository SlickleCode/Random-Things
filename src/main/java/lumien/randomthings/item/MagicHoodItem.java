package lumien.randomthings.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.sounds.SoundEvent;

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
 * Direct port of 1.12.2's {@code ItemMagicHood}, with one requested
 * divergence: 1.12.2 explicitly overrode {@code isDamageable()}/{@code
 * getMaxDamage()} to make this indestructible, and this port originally
 * assumed leaving both unoverridden had the same effect - **wrong, found via
 * user testing and confirmed via {@code javap -c}: {@code ArmorItem}'s own
 * constructor unconditionally calls {@code properties.defaultMaxDamage
 * (material.getDurability(slot))}, and {@code Item#maxDamage} is {@code
 * private final} with only a {@code public final} getter - there is no
 * override point at all, so this was always taking plain Chain-helmet
 * durability (~66), never actually indestructible.** User asked for real
 * durability instead, specifically matching Leather's - since the only way
 * to change durability is the {@code IArmorMaterial} passed to the
 * constructor, and swapping to {@code ArmorMaterial.LEATHER} outright would
 * also change defense/toughness/enchantability/repair-item/sound,
 * {@link #DURABILITY_MATERIAL} delegates every other stat to Chain and
 * overrides only {@link IArmorMaterial#getDurability} with Leather's value.
 */
public class MagicHoodItem extends ArmorItem {
    private static final ArmorMaterial DURABILITY_MATERIAL = new ArmorMaterial() {
        @Override
        public int getDurabilityForSlot(EquipmentSlot slot) {
            return ArmorMaterials.LEATHER.getDurabilityForSlot(slot);
        }

        @Override
        public int getDefenseForSlot(EquipmentSlot slot) {
            return ArmorMaterials.CHAIN.getDefenseForSlot(slot);
        }

        @Override
        public int getEnchantmentValue() {
            return ArmorMaterials.CHAIN.getEnchantmentValue();
        }

        @Override
        public SoundEvent getEquipSound() {
            return ArmorMaterials.CHAIN.getEquipSound();
        }

        @Override
        public Ingredient getRepairIngredient() {
            return ArmorMaterials.CHAIN.getRepairIngredient();
        }

        @Override
        public float getKnockbackResistance() {
            return ArmorMaterials.CHAIN.getKnockbackResistance();
        }

        @Override
        public String getName() {
            return ArmorMaterials.CHAIN.getName();
        }

        @Override
        public float getToughness() {
            return ArmorMaterials.CHAIN.getToughness();
        }
    };

    public MagicHoodItem(Item.Properties properties) {
        super(DURABILITY_MATERIAL, EquipmentSlot.HEAD, properties);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "randomthings:textures/models/armor/magic_hood.png";
    }
}
