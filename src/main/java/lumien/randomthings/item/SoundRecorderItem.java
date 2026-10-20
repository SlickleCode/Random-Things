package lumien.randomthings.item;

import lumien.randomthings.container.SoundRecorderContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import java.util.ArrayList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Sneak-right-click to toggle recording every sound you hear (up to 10, via
 * the client-side {@code PlaySoundEvent} listener in {@code RandomThings}
 * feeding {@link lumien.randomthings.network.messages.PlayedSoundMessage}
 * back to the server); plain right-click opens a GUI to stamp one of the
 * recorded sounds onto a blank {@link SoundPatternItem}. Direct port of
 * 1.12.2's {@code ItemSoundRecorder}.
 */
public class SoundRecorderItem extends Item implements MenuProvider, IClientItemProperties{
    public SoundRecorderItem(Item.Properties properties) {
        super(properties);

    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
        ItemStack stack = playerIn.getItemInHand(hand);

        if (!worldIn.isClientSide) {
            if (playerIn.isShiftKeyDown()) {
                boolean nowRecording = !isRecording(stack);

                stack.getOrCreateTag().putBoolean("recording", nowRecording);

                if (nowRecording) {
                    stack.getTag().remove("recordList");
                }
            } else if (!isRecording(stack)) {
                NetworkHooks.openScreen((ServerPlayer) playerIn, this);
            }
        }

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    public static boolean isRecording(ItemStack stack) {
        CompoundTag compound = stack.getTag();

        return compound != null && compound.getBoolean("recording");
    }

    public static void recordSound(ItemStack stack, String soundName) {
        CompoundTag compound = stack.getTag();

        if (compound == null || !compound.getBoolean("recording")) {
            return;
        }

        ListTag recordList = compound.getList("recordList", 8);

        for (int i = 0; i < recordList.size(); i++) {
            if (recordList.getString(i).equals(soundName)) {
                return;
            }
        }

        recordList.add(StringTag.valueOf(soundName));

        if (recordList.size() >= 10) {
            compound.putBoolean("recording", false);
        }

        compound.put("recordList", recordList);
    }

    public static ArrayList<String> getRecordedSounds(ItemStack stack) {
        ArrayList<String> list = new ArrayList<>();

        CompoundTag compound = stack.getTag();

        if (compound != null) {
            ListTag recordList = compound.getList("recordList", 8);

            for (int i = 0; i < recordList.size(); i++) {
                list.add(recordList.getString(i));
            }
        }

        return list;
    }

    @Override
    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int windowId, net.minecraft.world.entity.player.Inventory playerInventory, Player player) {
        return new SoundRecorderContainer(windowId, playerInventory);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("item.randomthings.sound_recorder");
    }

	@Override
	@OnlyIn(Dist.CLIENT)
	public void registerClientProperties()
	{
		net.minecraft.client.renderer.item.ItemProperties.register(this, new ResourceLocation("recording"), new ItemPropertyFunction() {
            @Override
            public float call(ItemStack stack, net.minecraft.client.multiplayer.ClientLevel worldIn, net.minecraft.world.entity.LivingEntity entityIn, int seed) {
                return isRecording(stack) ? 1.0F : 0.0F;
            }
        });
	}
}
