package lumien.randomthings.item;

import lumien.randomthings.entity.TimeAcceleratorEntity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;

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
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        int storedSeconds = getStoredTime(stack) / 20;
        int hours = storedSeconds / 3600;
        int minutes = (storedSeconds % 3600) / 60;
        int seconds = storedSeconds % 60;

        tooltip.add(Component.translatable("tooltip.randomthings.time_in_a_bottle", hours, minutes, seconds));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !ItemStack.isSameItem(oldStack, newStack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (worldIn.isClientSide) {
            return;
        }

        if (worldIn.getGameTime() % SECOND_WORTH == 0) {
            CompoundTag timeData = stack.getOrCreateTagElement("timeData");

            if (timeData.getInt("storedTime") < MAX_STORED) {
                timeData.putInt("storedTime", timeData.getInt("storedTime") + 20);
            }
        }

        if (worldIn.getGameTime() % 60 == 0 && entityIn instanceof Player) {
            Player player = (Player) entityIn;

            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack invStack = player.getInventory().getItem(i);

                if (invStack.getItem() == this && invStack != stack) {
                    CompoundTag otherTimeData = invStack.getOrCreateTagElement("timeData");
                    CompoundTag myTimeData = stack.getOrCreateTagElement("timeData");

                    if (myTimeData.getInt("storedTime") < otherTimeData.getInt("storedTime")) {
                        myTimeData.putInt("storedTime", 0);
                    }
                }
            }
        }
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack me, UseOnContext context) {
        Level world = context.getLevel();

        if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();

        java.util.Optional<TimeAcceleratorEntity> existing = world.getEntitiesOfClass(TimeAcceleratorEntity.class, new AABB(pos).inflate(1.0)).stream().filter(e -> pos.equals(e.getTarget())).findFirst();

        if (existing.isPresent()) {
            TimeAcceleratorEntity eta = existing.get();
            int currentRate = eta.getTimeRate();
            int usedUpTime = 20 * 30 - eta.getRemainingTime();

            if (currentRate < 32) {
                int nextRate = currentRate * 2;
                int timeRequired = nextRate / 2 * 20 * 30;

                CompoundTag timeData = me.getTagElement("timeData");
                int timeAvailable = timeData != null ? timeData.getInt("storedTime") : 0;

                if (timeAvailable >= timeRequired || (player != null && player.getAbilities().instabuild)) {
                    int timeAdded = (nextRate * usedUpTime - currentRate * usedUpTime) / nextRate;

                    if (player == null || !player.getAbilities().instabuild) {
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

                    world.playSound(null, pos, SoundEvents.NOTE_BLOCK_HARP.value(), SoundSource.BLOCKS, 0.5F, pitch);
                } else if (player != null) {
                    player.displayClientMessage(Component.translatable("item.randomthings.time_in_a_bottle.not_enough_time").withStyle(ChatFormatting.RED), false);
                }
            }
        } else {
            CompoundTag timeData = me.getOrCreateTagElement("timeData");
            int timeAvailable = timeData.getInt("storedTime");

            if (timeAvailable >= 20 * 30 || (player != null && player.getAbilities().instabuild)) {
                if (player == null || !player.getAbilities().instabuild) {
                    timeData.putInt("storedTime", timeAvailable - 20 * 30);
                }

                TimeAcceleratorEntity n = new TimeAcceleratorEntity(world, pos, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                n.setTimeRate(1);
                n.setRemainingTime(20 * 30);

                world.playSound(null, pos, SoundEvents.NOTE_BLOCK_HARP.value(), SoundSource.BLOCKS, 0.5F, 0.749154F);
                world.addFreshEntity(n);
            } else if (player != null) {
                player.displayClientMessage(Component.translatable("item.randomthings.time_in_a_bottle.not_enough_time").withStyle(ChatFormatting.RED), false);
            }
        }

        return InteractionResult.SUCCESS;
    }

    public static int getStoredTime(ItemStack stack) {
        CompoundTag timeData = stack.getTagElement("timeData");
        return timeData != null ? timeData.getInt("storedTime") : 0;
    }

    public static void setStoredTime(ItemStack stack, int time) {
        stack.getOrCreateTagElement("timeData").putInt("storedTime", time);
    }
}
