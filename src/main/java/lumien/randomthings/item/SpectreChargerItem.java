package lumien.randomthings.item;

import java.util.List;

import lumien.randomthings.handler.spectrecoils.SpectreCoilHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
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
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
        ItemStack stack = playerIn.getHeldItem(hand);

        if (!worldIn.isRemote) {
            CompoundNBT tag = stack.getOrCreateTag();
            tag.putBoolean("enabled", !tag.getBoolean("enabled"));
        }

        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (worldIn.isRemote || !(entityIn instanceof ServerPlayerEntity) || !stack.hasTag() || !stack.getTag().getBoolean("enabled")) {
            return;
        }

        ServerPlayerEntity player = (ServerPlayerEntity) entityIn;
        IEnergyStorage pool = SpectreCoilHandler.get(worldIn).getStorageCoil(player.getGameProfile().getId());

        for (int slot = 0; slot < player.inventory.getSizeInventory(); slot++) {
            if (tier != Tier.GENESIS && pool.getEnergyStored() == 0) {
                break;
            }

            ItemStack targetStack = player.inventory.getStackInSlot(slot);
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
    public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, net.minecraft.client.util.ITooltipFlag flag) {
        String display = tier == Tier.GENESIS ? "Infinite" : Integer.toString(tier.rate);
        tooltip.add(new TranslationTextComponent("item.randomthings.spectre_charger.charge", display));
    }
}
