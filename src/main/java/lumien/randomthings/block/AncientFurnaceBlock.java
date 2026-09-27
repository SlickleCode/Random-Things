package lumien.randomthings.block;

import lumien.randomthings.tileentity.AncientFurnaceTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Direct port of 1.12.2's {@code BlockAncientFurnace}: a rare, naturally
 * embedded block (see {@link lumien.randomthings.worldgen.AncientFurnaceFeature})
 * that's right-clicked on top with a Nether Star to start heating up, then -
 * see {@link AncientFurnaceTileEntity#tick()} - warms the surrounding area
 * and explodes. {@code HEATING} is a real {@code BlockState} property (same
 * pattern as vanilla furnace's {@code LIT}) purely so {@link #animateTick}
 * can render the countdown client-side without needing any custom TE sync.
 * <p>
 * The wiki doesn't give exact numbers for hardness, heating duration, or
 * explosion strength, so this picks reasonable defaults (very hard to break,
 * a multi-minute heat-up, a TNT-strength blast that also removes the furnace
 * itself) rather than guessing at 1.12.2's originals.
 */
public class AncientFurnaceBlock extends Block {
    public static final BooleanProperty HEATING = BooleanProperty.create("heating");

    public AncientFurnaceBlock() {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(50.0F, 10.0F));

        this.setDefaultState(this.stateContainer.getBaseState().with(HEATING, false));
    }

    @Override
    protected void fillStateContainer(Builder<Block, BlockState> builder) {
        builder.add(HEATING);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new AncientFurnaceTileEntity();
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (state.get(HEATING)) {
            return false;
        }

        ItemStack stack = player.getHeldItem(handIn);

        if (stack.getItem() != Items.NETHER_STAR) {
            return false;
        }

        if (worldIn.isRemote) {
            return true;
        }

        TileEntity te = worldIn.getTileEntity(pos);

        if (!(te instanceof AncientFurnaceTileEntity)) {
            return false;
        }

        if (!player.abilities.isCreativeMode) {
            stack.shrink(1);
        }

        worldIn.setBlockState(pos, state.with(HEATING, true), 3);
        ((AncientFurnaceTileEntity) te).startHeating();
        worldIn.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0F, 0.6F);

        return true;
    }

    @Override
    public void animateTick(BlockState state, World worldIn, BlockPos pos, Random rand) {
        if (!state.get(HEATING)) {
            return;
        }

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5;

        worldIn.addParticle(ParticleTypes.FLAME, x + rand.nextDouble() * 0.4 - 0.2, y, z + rand.nextDouble() * 0.4 - 0.2, 0, 0.02, 0);
        worldIn.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0.01, 0);
    }
}
