package lumien.randomthings.item;

import lumien.randomthings.entity.TimeAcceleratorEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.List;

/**
 * Passively stores up 1 tick of charge per real-time second held anywhere in
 * a player's inventory (up to ~360 days' worth). Right-click empty space to
 * spend 30 seconds of charge planting a new {@link TimeAcceleratorEntity}
 * targeting that block (1x tick rate); right-click an existing one to double
 * its rate (up to 32x), each doubling costing progressively more stored
 * charge. Also works with {@link
 * lumien.randomthings.entity.EclipsedClockEntity} to fast-forward the world
 * clock - see that class. Direct port of 1.12.2's {@code ItemTimeInABottle}.
 * <p>
 * Both "not enough stored time" cases (placing a new accelerator, upgrading
 * an existing one) send the player an action-bar-adjacent status message
 * instead of silently doing nothing - per user request, 2026-09-27, not
 * something 1.12.2 did either, but matching this port's own established
 * feedback convention ({@code EnderMailboxBlock}/{@code EnderLetterItem}'s
 * {@code sendStatusMessage} calls).
 * <p>
 * Real bug found and fixed, 2026-09-27 (reported by user, after the
 * {@link TimeAcceleratorEntity} billboard was added): the new marker spawned
 * at the clicked block's exact center - fine while invisible, but the
 * targeted block is very often a full solid block (a hopper, a furnace, ...)
 * once the billboard was added, so the icon (and the particles that
 * originally followed it) rendered clipped inside solid geometry. First
 * fixed by offsetting toward whichever face was actually clicked; per a
 * follow-up user request the same day, the marker is back at the block
 * center and {@link lumien.randomthings.client.renderer.TimeAcceleratorEntityRenderer}
 * now draws the icon on all 6 faces instead of just one, close to each
 * face's own surface - see that class and {@link TimeAcceleratorEntity
 * #spawnParticles()} for the rest of that redesign. The "is there already
 * one here" detection matches by {@link TimeAcceleratorEntity#getTarget()}
 * (the block whose tile entity actually gets force-ticked) rather than
 * scanning by position, so none of this visual back-and-forth needed to
 * touch that logic again.
 */
public class TimeInABottleItem extends Item {
    private static final int SECOND_WORTH = 20;
    private static final int MAX_STORED = 622080000;

    public TimeInABottleItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        int storedSeconds = getStoredTime(stack) / 20;
        int hours = storedSeconds / 3600;
        int minutes = (storedSeconds % 3600) / 60;
        int seconds = storedSeconds % 60;

        tooltip.add(new TranslationTextComponent("tooltip.randomthings.time_in_a_bottle", hours, minutes, seconds));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !ItemStack.areItemsEqual(oldStack, newStack);
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (worldIn.isRemote) {
            return;
        }

        if (worldIn.getGameTime() % SECOND_WORTH == 0) {
            CompoundNBT timeData = stack.getOrCreateChildTag("timeData");

            if (timeData.getInt("storedTime") < MAX_STORED) {
                timeData.putInt("storedTime", timeData.getInt("storedTime") + 20);
            }
        }

        if (worldIn.getGameTime() % 60 == 0 && entityIn instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entityIn;

            for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                ItemStack invStack = player.inventory.getStackInSlot(i);

                if (invStack.getItem() == this && invStack != stack) {
                    CompoundNBT otherTimeData = invStack.getOrCreateChildTag("timeData");
                    CompoundNBT myTimeData = stack.getOrCreateChildTag("timeData");

                    if (myTimeData.getInt("storedTime") < otherTimeData.getInt("storedTime")) {
                        myTimeData.putInt("storedTime", 0);
                    }
                }
            }
        }
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack me, ItemUseContext context) {
        World world = context.getWorld();

        if (world.isRemote) {
            return ActionResultType.SUCCESS;
        }

        BlockPos pos = context.getPos();
        PlayerEntity player = context.getPlayer();

        java.util.Optional<TimeAcceleratorEntity> existing = world.getEntitiesWithinAABB(TimeAcceleratorEntity.class, new AxisAlignedBB(pos).grow(1.0)).stream().filter(e -> pos.equals(e.getTarget())).findFirst();

        if (existing.isPresent()) {
            TimeAcceleratorEntity eta = existing.get();
            int currentRate = eta.getTimeRate();
            int usedUpTime = 20 * 30 - eta.getRemainingTime();

            if (currentRate < 32) {
                int nextRate = currentRate * 2;
                int timeRequired = nextRate / 2 * 20 * 30;

                CompoundNBT timeData = me.getChildTag("timeData");
                int timeAvailable = timeData != null ? timeData.getInt("storedTime") : 0;

                if (timeAvailable >= timeRequired || (player != null && player.abilities.isCreativeMode)) {
                    int timeAdded = (nextRate * usedUpTime - currentRate * usedUpTime) / nextRate;

                    if (player == null || !player.abilities.isCreativeMode) {
                        timeData.putInt("storedTime", timeAvailable - timeRequired);
                    }

                    eta.setTimeRate(nextRate);
                    eta.setRemainingTime(eta.getRemainingTime() + timeAdded);

                    float pitch;

                    switch (nextRate) {
                        case 2:
                            pitch = 0.793701F;
                            break;
                        case 4:
                            pitch = 0.890899F;
                            break;
                        case 8:
                            pitch = 1.059463F;
                            break;
                        case 16:
                            pitch = 0.943874F;
                            break;
                        case 32:
                        default:
                            pitch = 0.890899F;
                            break;
                    }

                    world.playSound(null, pos, SoundEvents.BLOCK_NOTE_BLOCK_HARP, SoundCategory.BLOCKS, 0.5F, pitch);
                } else if (player != null) {
                    player.sendStatusMessage(new TranslationTextComponent("item.randomthings.time_in_a_bottle.not_enough_time").applyTextStyle(TextFormatting.RED), false);
                }
            }
        } else {
            CompoundNBT timeData = me.getOrCreateChildTag("timeData");
            int timeAvailable = timeData.getInt("storedTime");

            if (timeAvailable >= 20 * 30 || (player != null && player.abilities.isCreativeMode)) {
                if (player == null || !player.abilities.isCreativeMode) {
                    timeData.putInt("storedTime", timeAvailable - 20 * 30);
                }

                TimeAcceleratorEntity n = new TimeAcceleratorEntity(world, pos, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                n.setTimeRate(1);
                n.setRemainingTime(20 * 30);

                world.playSound(null, pos, SoundEvents.BLOCK_NOTE_BLOCK_HARP, SoundCategory.BLOCKS, 0.5F, 0.749154F);
                world.addEntity(n);
            } else if (player != null) {
                player.sendStatusMessage(new TranslationTextComponent("item.randomthings.time_in_a_bottle.not_enough_time").applyTextStyle(TextFormatting.RED), false);
            }
        }

        return ActionResultType.SUCCESS;
    }

    public static int getStoredTime(ItemStack stack) {
        CompoundNBT timeData = stack.getChildTag("timeData");
        return timeData != null ? timeData.getInt("storedTime") : 0;
    }

    public static void setStoredTime(ItemStack stack, int time) {
        stack.getOrCreateChildTag("timeData").putInt("storedTime", time);
    }
}
