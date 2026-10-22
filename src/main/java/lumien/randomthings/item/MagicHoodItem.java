package lumien.randomthings.item;

import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;

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
    /** Chain stats, but the hood's own armor texture ({@code textures/models/armor/magic_hood_layer_1.png}). */
    private static final Holder<ArmorMaterial> MATERIAL;

    static {
        ArmorMaterial chain = ArmorMaterials.CHAIN.value();
        MATERIAL = Holder.direct(new ArmorMaterial(chain.defense(), chain.enchantmentValue(), chain.equipSound(), chain.repairIngredient(),
                List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath("randomthings", "magic_hood"))), chain.toughness(), chain.knockbackResistance()));
    }

    public MagicHoodItem(Item.Properties properties) {
        super(MATERIAL, ArmorItem.Type.HELMET, properties.durability(ArmorItem.Type.HELMET.getDurability(5)));
    }

}
