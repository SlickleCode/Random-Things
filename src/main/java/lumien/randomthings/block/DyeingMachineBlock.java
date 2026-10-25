package lumien.randomthings.block;

import lumien.randomthings.container.DyeingMachineContainer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;

/**
 * Direct port of 1.12.2's {@code BlockDyeingMachine}: a static, non-tile-entity
 * block whose GUI ({@link DyeingMachineContainer}) does all the actual work -
 * no state lives on the block itself.
 */
public class DyeingMachineBlock extends Block {
    public DyeingMachineBlock() {
        super(lumien.randomthings.lib.RTIds.block().mapColor(net.minecraft.world.level.material.MapColor.WOOD).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASS).ignitedByLava().strength(0.7F).sound(SoundType.WOOD));
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player, BlockHitResult hit) {
        if (!worldIn.isClientSide()) {
            ((ServerPlayer) player).openMenu(new MenuProvider() {
                @Override
                public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player p) {
                    return new DyeingMachineContainer(windowId, inv, ContainerLevelAccess.create(worldIn, pos));
                }

                @Override
                public Component getDisplayName() {
                    return Component.translatable("block.randomthings.dyeing_machine");
                }
            }, pos);
        }

        return InteractionResult.SUCCESS;
    }
}
