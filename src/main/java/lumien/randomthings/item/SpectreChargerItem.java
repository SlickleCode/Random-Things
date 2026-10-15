package lumien.randomthings.item;

import java.util.List;

import lumien.randomthings.handler.spectrecoils.SpectreCoilHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * Ported from 1.12.2's {@code ItemSpectreCharger} - right-click toggles it on/off, then while on and
 * carried anywhere in the player's inventory (a Baubles belt item in 1.12.2; dropped project-wide, so
 * this uses the same "just has to be carried" fallback already established for Portable Sound
 * Dampener/Obsidian Skull/Lava Charm) it drains the owner's {@link SpectreCoilHandler} pool every tick
 * to top up any energy-capable item elsewhere in that inventory. {@code GENESIS} instantly fills
 * targets for free, matching 1.12.2 exactly.
 */
public class SpectreChargerItem extends Item {
    public enum Tier {
        NORMAL("normal", 1024),
        REDSTONE("redstone", 4096),
        ENDER("ender", 20480),
        GENESIS("genesis", 0);

        public final String suffix;
        public final int rate;

        Tier(String suffix, int rate) {
            this.suffix = suffix;
            this.rate = rate;
        }
    }

    private final Tier tier;

    public SpectreChargerItem(Item.Properties properties, Tier tier) {
        super(properties);
        this.tier = tier;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
        ItemStack stack = playerIn.getItemInHand(hand);

        if (!worldIn.isClientSide) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putBoolean("enabled", !tag.getBoolean("enabled"));
        }

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (worldIn.isClientSide || !(entityIn instanceof ServerPlayer) || !stack.hasTag() || !stack.getTag().getBoolean("enabled")) {
            return;
        }

        ServerPlayer player = (ServerPlayer) entityIn;
        IEnergyStorage pool = SpectreCoilHandler.get(worldIn).getStorageCoil(player.getGameProfile().getId());

        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (tier != Tier.GENESIS && pool.getEnergyStored() == 0) {
                break;
            }

            ItemStack targetStack = player.getInventory().getItem(slot);
            IEnergyStorage targetStorage = targetStack.getCapability(CapabilityEnergy.ENERGY, null).orElse(null);

            if (targetStorage == null || !targetStorage.canReceive()) {
                continue;
            }

            int missing = targetStorage.getMaxEnergyStored() - targetStorage.getEnergyStored();
            if (missing <= 0) {
                continue;
            }

            if (tier == Tier.GENESIS) {
                targetStorage.receiveEnergy(missing, false);
                continue;
            }

            int extracted = pool.extractEnergy(Math.min(tier.rate, missing), false);
            int remainder = extracted - targetStorage.receiveEnergy(extracted, false);

            if (remainder > 0) {
                pool.receiveEnergy(remainder, false);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        String display = tier == Tier.GENESIS ? "Infinite" : Integer.toString(tier.rate);
        tooltip.add(new TranslatableComponent("item.randomthings.spectre_charger.charge", display));
    }
}
