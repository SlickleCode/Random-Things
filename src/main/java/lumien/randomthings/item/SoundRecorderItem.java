package lumien.randomthings.item;

import lumien.randomthings.util.StackTag;
import lumien.randomthings.container.SoundRecorderContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import net.neoforged.api.distmarker.Dist;


/**
 * Sneak-right-click to toggle recording every sound you hear (up to 10, via
 * the client-side {@code PlaySoundEvent} listener in {@code RandomThings}
 * feeding {@link lumien.randomthings.network.messages.PlayedSoundMessage}
 * back to the server); plain right-click opens a GUI to stamp one of the
 * recorded sounds onto a blank {@link SoundPatternItem}. Direct port of
 * 1.12.2's {@code ItemSoundRecorder}.
 */
public class SoundRecorderItem extends Item implements MenuProvider{
    public SoundRecorderItem(Item.Properties properties) {
        super(properties);

    }

    @Override
    public InteractionResult use(Level worldIn, Player playerIn, InteractionHand hand) {
        ItemStack stack = playerIn.getItemInHand(hand);

        if (!worldIn.isClientSide()) {
            if (playerIn.isShiftKeyDown()) {
                boolean nowRecording = !isRecording(stack);

                StackTag.edit(stack, t -> t.putBoolean("recording", nowRecording));
                if (nowRecording) {
                    StackTag.remove(stack, "recordList");
                }
            } else if (!isRecording(stack)) {
                ((ServerPlayer) playerIn).openMenu(this);
            }
        }

        return InteractionResult.SUCCESS;
    }

    public static boolean isRecording(ItemStack stack) {
        CompoundTag compound = StackTag.get(stack);

        return compound != null && compound.getBooleanOr("recording", false);
    }

    public static void recordSound(ItemStack stack, String soundName) {
        CompoundTag compound = StackTag.get(stack);

        if (compound == null || !compound.getBooleanOr("recording", false)) {
            return;
        }

        ListTag recordList = compound.getListOrEmpty("recordList");

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
        StackTag.set(stack, compound);
    }

    public static ArrayList<String> getRecordedSounds(ItemStack stack) {
        ArrayList<String> list = new ArrayList<>();

        CompoundTag compound = StackTag.get(stack);

        if (compound != null) {
            ListTag recordList = compound.getListOrEmpty("recordList");

            for (int i = 0; i < recordList.size(); i++) {
                list.add(recordList.getStringOr(i, ""));
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
}
