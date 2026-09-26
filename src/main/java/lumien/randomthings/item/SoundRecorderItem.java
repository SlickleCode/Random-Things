package lumien.randomthings.item;

import lumien.randomthings.container.SoundRecorderContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.ArrayList;

/**
 * Sneak-right-click to toggle recording every sound you hear (up to 10, via
 * the client-side {@code PlaySoundEvent} listener in {@code RandomThings}
 * feeding {@link lumien.randomthings.network.messages.PlayedSoundMessage}
 * back to the server); plain right-click opens a GUI to stamp one of the
 * recorded sounds onto a blank {@link SoundPatternItem}. Direct port of
 * 1.12.2's {@code ItemSoundRecorder}.
 */
public class SoundRecorderItem extends Item implements INamedContainerProvider {
    public SoundRecorderItem(Item.Properties properties) {
        super(properties);

        this.addPropertyOverride(new ResourceLocation("recording"), new IItemPropertyGetter() {
            @Override
            public float call(ItemStack stack, World worldIn, net.minecraft.entity.LivingEntity entityIn) {
                return isRecording(stack) ? 1.0F : 0.0F;
            }
        });
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
        ItemStack stack = playerIn.getHeldItem(hand);

        if (!worldIn.isRemote) {
            if (playerIn.isSneaking()) {
                boolean nowRecording = !isRecording(stack);

                stack.getOrCreateTag().putBoolean("recording", nowRecording);

                if (nowRecording) {
                    stack.getTag().remove("recordList");
                }
            } else if (!isRecording(stack)) {
                NetworkHooks.openGui((ServerPlayerEntity) playerIn, this);
            }
        }

        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    public static boolean isRecording(ItemStack stack) {
        CompoundNBT compound = stack.getTag();

        return compound != null && compound.getBoolean("recording");
    }

    public static void recordSound(ItemStack stack, String soundName) {
        CompoundNBT compound = stack.getTag();

        if (compound == null || !compound.getBoolean("recording")) {
            return;
        }

        ListNBT recordList = compound.getList("recordList", 8);

        for (int i = 0; i < recordList.size(); i++) {
            if (recordList.getString(i).equals(soundName)) {
                return;
            }
        }

        recordList.add(new StringNBT(soundName));

        if (recordList.size() >= 10) {
            compound.putBoolean("recording", false);
        }

        compound.put("recordList", recordList);
    }

    public static ArrayList<String> getRecordedSounds(ItemStack stack) {
        ArrayList<String> list = new ArrayList<>();

        CompoundNBT compound = stack.getTag();

        if (compound != null) {
            ListNBT recordList = compound.getList("recordList", 8);

            for (int i = 0; i < recordList.size(); i++) {
                list.add(recordList.getString(i));
            }
        }

        return list;
    }

    @Override
    public net.minecraft.inventory.container.Container createMenu(int windowId, net.minecraft.entity.player.PlayerInventory playerInventory, PlayerEntity player) {
        return new SoundRecorderContainer(windowId, playerInventory);
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("item.randomthings.sound_recorder");
    }
}
