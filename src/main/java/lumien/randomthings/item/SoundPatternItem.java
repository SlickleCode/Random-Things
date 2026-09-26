package lumien.randomthings.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import java.util.List;

/**
 * A stamped-with-one-sound recording, produced by {@link SoundRecorderItem}'s
 * GUI. Inserted into a Sound Box (plays it on redstone) or a Sound Dampener /
 * Portable Sound Dampener (mutes it globally near the player). Sneak-right-
 * click clears it back to blank. Direct port of 1.12.2's {@code ItemSoundPattern}.
 * <p>
 * The empty/full model swap used {@code ItemMeshDefinition} in 1.12.2 (a
 * per-NBT model picker); this port's already-established replacement for
 * that (from {@link CompassItemBase}) is an {@code IItemPropertyGetter}
 * predicate driving the item model JSON's {@code overrides}.
 */
public class SoundPatternItem extends Item {
    public SoundPatternItem(Item.Properties properties) {
        super(properties);

        this.addPropertyOverride(new ResourceLocation("filled"), new IItemPropertyGetter() {
            @Override
            public float call(ItemStack stack, World worldIn, net.minecraft.entity.LivingEntity entityIn) {
                return getSoundLocation(stack) != null ? 1.0F : 0.0F;
            }
        });
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if (playerIn.isSneaking()) {
            ItemStack me = playerIn.getHeldItem(handIn);

            if (getSoundLocation(me) != null) {
                me.setTag(null);

                return new ActionResult<>(ActionResultType.SUCCESS, me);
            }
        }

        return new ActionResult<>(ActionResultType.PASS, playerIn.getHeldItem(handIn));
    }

    @Override
    public ITextComponent getDisplayName(ItemStack stack) {
        ResourceLocation sound = getSoundLocation(stack);

        if (sound != null) {
            return new StringTextComponent(super.getDisplayName(stack).getString() + " <" + sound.getPath() + ">");
        }

        return super.getDisplayName(stack);
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, net.minecraft.client.util.ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        ResourceLocation sound = getSoundLocation(stack);

        tooltip.add(new StringTextComponent(sound != null ? sound.toString() : "<Empty>"));
    }

    public static ResourceLocation getSoundLocation(ItemStack pattern) {
        CompoundNBT compound = pattern.getTag();

        if (compound != null && compound.contains("sound")) {
            return new ResourceLocation(compound.getString("sound"));
        }

        return null;
    }

    public static void setSoundLocation(ItemStack pattern, String sound) {
        pattern.getOrCreateTag().putString("sound", sound);
    }
}
