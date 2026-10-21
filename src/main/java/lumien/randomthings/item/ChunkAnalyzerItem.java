package lumien.randomthings.item;

import lumien.randomthings.container.ChunkAnalyzerContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

/**
 * Right-click (main hand only, matching the original) to open a GUI that
 * scans your current chunk for block counts - see {@link ChunkAnalyzerContainer}
 * for the actual scan. Direct port of 1.12.2's {@code ItemChunkAnalyzer};
 * the old numeric {@code GuiIds}/{@code player.openGui(...)} system is
 * replaced with {@code NetworkHooks.openGui}, matching the pattern already
 * established for tile-entity GUIs in this port - this is the first
 * *item*-triggered one (no block/tile entity involved), so the
 * {@link INamedContainerProvider} is built inline here rather than
 * implemented by a tile entity.
 */
public class ChunkAnalyzerItem extends Item {
    public ChunkAnalyzerItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
        ItemStack stack = playerIn.getItemInHand(hand);

        if (!worldIn.isClientSide && hand == InteractionHand.MAIN_HAND) {
            ((ServerPlayer) playerIn).openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable("item.randomthings.chunk_analyzer");
                }

                @Override
                public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
                    return new ChunkAnalyzerContainer(windowId, playerInventory, null);
                }
            });

            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }

        return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
    }
}
