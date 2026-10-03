package lumien.randomthings.block;

import lumien.randomthings.container.DyeingMachineContainer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

/**
 * Direct port of 1.12.2's {@code BlockDyeingMachine}: a static, non-tile-entity
 * block whose GUI ({@link DyeingMachineContainer}) does all the actual work -
 * no state lives on the block itself.
 */
public class DyeingMachineBlock extends Block {
    public DyeingMachineBlock() {
        super(Block.Properties.create(Material.WOOD).hardnessAndResistance(0.7F).sound(SoundType.WOOD));
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (!worldIn.isRemote) {
            NetworkHooks.openGui((ServerPlayerEntity) player, new INamedContainerProvider() {
                @Override
                public Container createMenu(int windowId, PlayerInventory inv, PlayerEntity p) {
                    return new DyeingMachineContainer(windowId, inv, IWorldPosCallable.of(worldIn, pos));
                }

                @Override
                public ITextComponent getDisplayName() {
                    return new TranslationTextComponent("block.randomthings.dyeing_machine");
                }
            }, pos);
        }

        return true;
    }
}
