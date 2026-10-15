package lumien.randomthings.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import java.util.List;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
public class SoundPatternItem extends Item implements IClientItemProperties{
    public SoundPatternItem(Item.Properties properties) {
        super(properties);

    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        if (playerIn.isShiftKeyDown()) {
            ItemStack me = playerIn.getItemInHand(handIn);

            if (getSoundLocation(me) != null) {
                me.setTag(null);

                return new InteractionResultHolder<>(InteractionResult.SUCCESS, me);
            }
        }

        return new InteractionResultHolder<>(InteractionResult.PASS, playerIn.getItemInHand(handIn));
    }

    @Override
    public Component getName(ItemStack stack) {
        ResourceLocation sound = getSoundLocation(stack);

        if (sound != null) {
            return Component.literal(super.getName(stack).getString() + " <" + sound.getPath() + ">");
        }

        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, net.minecraft.world.item.TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        ResourceLocation sound = getSoundLocation(stack);

        tooltip.add(Component.literal(sound != null ? sound.toString() : "<Empty>"));
    }

    public static ResourceLocation getSoundLocation(ItemStack pattern) {
        CompoundTag compound = pattern.getTag();

        if (compound != null && compound.contains("sound")) {
            return new ResourceLocation(compound.getString("sound"));
        }

        return null;
    }

    public static void setSoundLocation(ItemStack pattern, String sound) {
        pattern.getOrCreateTag().putString("sound", sound);
    }

	@Override
	@OnlyIn(Dist.CLIENT)
	public void registerClientProperties()
	{
		net.minecraft.client.renderer.item.ItemProperties.register(this, new ResourceLocation("filled"), new ItemPropertyFunction() {
            @Override
            public float call(ItemStack stack, net.minecraft.client.multiplayer.ClientLevel worldIn, net.minecraft.world.entity.LivingEntity entityIn, int seed) {
                return getSoundLocation(stack) != null ? 1.0F : 0.0F;
            }
        });
	}
}
