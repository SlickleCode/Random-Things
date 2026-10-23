package lumien.randomthings.item;

import java.util.EnumMap;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;

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
    /**
     * Chain stats and Leather's durability factor (5), with the hood's own worn model: the equipment asset {@code assets/randomthings/equipment/magic_hood.json}
     * (1.21.2 replaced the {@code textures/models/armor/*_layer_1.png} lookup by equipment assets).
     */
    private static final ArmorMaterial MATERIAL;

    static {
        ArmorMaterial chain = ArmorMaterials.CHAINMAIL;
        MATERIAL = new ArmorMaterial(5, new EnumMap<>(chain.defense()), chain.enchantmentValue(), chain.equipSound(), chain.toughness(), chain.knockbackResistance(), chain.repairIngredient(),
                ResourceKey.create(EquipmentAssets.ROOT_ID, ResourceLocation.fromNamespaceAndPath("randomthings", "magic_hood")));
    }

    public MagicHoodItem(Item.Properties properties) {
        super(MATERIAL, ArmorType.HELMET, properties);
    }

}
