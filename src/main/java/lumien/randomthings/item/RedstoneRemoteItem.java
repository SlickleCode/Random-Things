package lumien.randomthings.item;

import lumien.randomthings.container.RedstoneRemoteEditContainer;
import lumien.randomthings.container.RedstoneRemoteUseContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

/**
 * Sneak-right-click to edit up to 9 bound Position Filter targets (stored in
 * this item's own NBT, not a tile entity); plain right-click opens a row of
 * buttons, one per bound target, to fire a strength-15 pulse at it (via
 * {@link lumien.randomthings.network.messages.RedstoneRemoteActivateMessage}).
 * Direct port of 1.12.2's {@code ItemRedstoneRemote}, including the edit screen's second row of
 * ghost camo-icon slots (stored under {@code camo}) that override each button's icon.
 */
public class RedstoneRemoteItem extends Item {
    public RedstoneRemoteItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
        ItemStack stack = playerIn.getHeldItem(hand);

        if (!worldIn.isRemote) {
            if (playerIn.isSneaking()) {
                NetworkHooks.openGui((ServerPlayerEntity) playerIn, new INamedContainerProvider() {
                    @Override
                    public Container createMenu(int windowId, PlayerInventory inv, PlayerEntity p) {
                        return new RedstoneRemoteEditContainer(windowId, inv);
                    }

                    @Override
                    public ITextComponent getDisplayName() {
                        return new TranslationTextComponent("item.randomthings.redstone_remote");
                    }
                });
            } else {
                NetworkHooks.openGui((ServerPlayerEntity) playerIn, new INamedContainerProvider() {
                    @Override
                    public Container createMenu(int windowId, PlayerInventory inv, PlayerEntity p) {
                        return new RedstoneRemoteUseContainer(windowId, inv);
                    }

                    @Override
                    public ITextComponent getDisplayName() {
                        return new TranslationTextComponent("item.randomthings.redstone_remote");
                    }
                });
            }
        }

        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }
}
