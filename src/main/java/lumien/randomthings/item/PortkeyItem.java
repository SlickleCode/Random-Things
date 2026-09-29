package lumien.randomthings.item;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.List;

/**
 * Right-click a block to bind this key to that spot (dimension + x/y/z),
 * same "remember where I clicked" idiom already proven by {@link
 * PositionFilterItem} in this port. Drop it on the ground; once it's sat
 * there undisturbed for 5 real seconds (100 ticks) it stops despawning and
 * glows to show it's primed. The next player to pick it up gets teleported
 * to the bound location instead of collecting it. Direct port of 1.12.2's
 * {@code ItemPortKey}.
 * <p>
 * Combine a Portkey with any other item in a crafting table ({@link
 * lumien.randomthings.recipes.PortkeyCamoRecipe}) to disguise it as that
 * item, matching 1.12.2's own "camo" system - see {@link
 * lumien.randomthings.client.renderer.PortkeyItemRenderer} for how this port
 * does it. 1.12.2 reached into its own internal item-model registry map via
 * reflection ({@code ReflectionUtil.getModelMap()}) to steal another item's
 * baked model; that internal structure doesn't carry over to 1.14.4's
 * different model-loading pipeline, but 1.14.4 has a clean, fully public
 * equivalent purpose-built for exactly this ({@code Item.Properties
 * #setTEISR} - the same mechanism vanilla itself uses for shulker boxes) -
 * no reflection needed, ground-truthed via {@code javap -c} before writing
 * it. {@link #getCamoStack(ItemStack)} is the shared read side both the
 * renderer and (if ever needed) anything else consults.
 * <p>
 * One remaining disclosed simplification: the original also rendered a
 * full-screen HUD directional beam pointing at the bound location while
 * held - dropped in favor of the same shift-to-reveal tooltip coordinates
 * {@link PositionFilterItem} already uses for an identical "where did I
 * bind this" need, rather than porting a whole custom overlay-rendering
 * system for one item. The core mechanic (bind, prime, pickup-teleport) is
 * otherwise a direct, faithful port, including the enchantment glow while
 * unprimed/unbound - just the vanilla glow color, not the original's custom
 * magenta tint (that needed its own ASM hook in 1.12.2, {@code AsmHandler
 * #enchantmentColorHook}; no equivalent exists yet in this port and it's
 * purely cosmetic, unlike the camo feature above).
 * <p>
 * The 5-second "still priming" counter lives on the item stack's own NBT
 * (not the dropped {@code ItemEntity}'s {@code getPersistentData()}, unlike
 * {@code StableEnderpearlItem}'s otherwise-identical pattern) specifically
 * so {@link #hasEffect(ItemStack)} - which only ever receives the stack,
 * never the entity - can see it regardless of render context. {@link
 * #inventoryTick} clears it the moment the stack is picked back up into an
 * inventory, matching 1.12.2's own {@code onUpdate} - dropping it again
 * starts the 5 seconds over, exactly like the original.
 */
public class PortkeyItem extends Item {
    private static final int PRIME_TICKS = 100;

    public PortkeyItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        CompoundNBT compound = stack.getTag();

        if (Screen.hasShiftDown()) {
            if (compound != null && compound.getBoolean("hasTarget")) {
                tooltip.add(new TranslationTextComponent("tooltip.randomthings.portkey.dimension", compound.getInt("dimension")));
                tooltip.add(new TranslationTextComponent("tooltip.randomthings.portkey.x", compound.getInt("targetX")));
                tooltip.add(new TranslationTextComponent("tooltip.randomthings.portkey.y", compound.getInt("targetY")));
                tooltip.add(new TranslationTextComponent("tooltip.randomthings.portkey.z", compound.getInt("targetZ")));
            } else {
                tooltip.add(new TranslationTextComponent("tooltip.randomthings.portkey.notset"));
            }
        } else {
            tooltip.add(new TranslationTextComponent("tooltip.randomthings.general.shift"));
        }

        super.addInformation(stack, world, tooltip, flag);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        if (!context.getWorld().isRemote) {
            CompoundNBT compound = context.getItem().getOrCreateTag();

            compound.putBoolean("hasTarget", true);
            compound.putInt("dimension", context.getWorld().getDimension().getType().getId());
            compound.putInt("targetX", context.getPos().getX());
            compound.putInt("targetY", context.getPos().getY());
            compound.putInt("targetZ", context.getPos().getZ());
        }

        return ActionResultType.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, net.minecraft.entity.Entity entityIn, int itemSlot, boolean isSelected) {
        if (!worldIn.isRemote && stack.hasTag() && stack.getTag().contains("dropCounter")) {
            stack.getTag().remove("dropCounter");
        }
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        CompoundNBT compound = stack.getTag();

        if (compound == null || !compound.getBoolean("hasTarget")) {
            return true;
        }

        return compound.getInt("dropCounter") < PRIME_TICKS;
    }

    /**
     * The item this Portkey is disguised as, or {@link ItemStack#EMPTY} if
     * it isn't (yet) camouflaged. Matches 1.12.2's own {@code "camo"} sub-
     * compound shape ({@code camo.stack} holding the full serialized donor
     * stack) - see {@link lumien.randomthings.recipes.PortkeyCamoRecipe} for
     * how it gets set.
     */
    public static ItemStack getCamoStack(ItemStack stack) {
        CompoundNBT compound = stack.getTag();

        if (compound == null || !compound.contains("camo")) {
            return ItemStack.EMPTY;
        }

        return ItemStack.read(compound.getCompound("camo").getCompound("stack"));
    }

    /**
     * Drives the "sitting on the ground, priming" countdown - see the
     * javadoc on {@link StableEnderpearlItem#tickDroppedPearl} for why this
     * lives here instead of on the item type directly ({@code
     * Item.onEntityItemUpdate} doesn't exist in this Forge build). Called
     * from a {@code WorldTickEvent} listener in {@code RandomThings}.
     * <p>
     * Real bug, found 2026-09-28 (reported by user): the glint never stopped
     * after priming, even once bound. Root cause: unlike {@code
     * StableEnderpearlItem}'s counter (stored in {@code
     * entityItem.getPersistentData()}, a server-only bookkeeping compound
     * that was never meant to reach the client), this counter has to be
     * client-visible - {@link #hasEffect(ItemStack)} only ever gets the
     * stack, never the entity, so it lives on the stack's own tag instead
     * (see this method's original 1.12.2 design too - same choice, same
     * place). But mutating that tag *in place* on the exact {@code
     * ItemStack} instance {@code ItemEntity}'s data manager already has
     * cached never actually re-syncs it: {@code EntityDataManager.set}
     * (ground-truthed via source) only marks its {@code ITEM} entry dirty
     * when the new value is a genuinely different object from what's
     * stored, and {@code ItemStack} doesn't override {@code equals()} - so
     * handing back the *same* (now-mutated) reference always reads as "no
     * change," and the client's original spawn-time snapshot (dropCounter
     * frozen at whatever it was) never updates. Fixed by mutating a copy and
     * calling {@code setItem} with that copy instead of the tag in place -
     * a genuinely different object reference is what actually trips the
     * dirty check and gets it sent.
     */
    public void tickDroppedPortkey(net.minecraft.entity.item.ItemEntity entityItem) {
        if (entityItem.world.isRemote) {
            return;
        }

        ItemStack original = entityItem.getItem();
        int counter = original.hasTag() ? original.getTag().getInt("dropCounter") : 0;

        if (counter == 0) {
            entityItem.setNoDespawn();
        }

        ItemStack updated = original.copy();
        updated.getOrCreateTag().putInt("dropCounter", counter + 1);
        entityItem.setItem(updated);
    }
}
