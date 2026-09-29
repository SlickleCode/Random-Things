package lumien.randomthings.block;

import lumien.randomthings.tileentity.DiaphanousBlockTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

/**
 * Direct port of 1.12.2's {@code BlockBlockDiaphanous}: a non-solid block
 * that displays another block's appearance (real rendering in {@link
 * lumien.randomthings.client.renderer.DiaphanousBlockTileEntityRenderer}, a
 * {@code TileEntityRenderer} - this port's established substitute for
 * 1.12.2's custom {@code TileEntitySpecialRenderer}), fading from mostly
 * invisible up close to fully opaque-looking at range so it's still usable as
 * a real block up close, with an "inverted" mode ({@link
 * lumien.randomthings.recipes.DiaphanousInvertRecipe}) that flips both the
 * fade curve and whether it actually has a collision box. {@code
 * BlockRenderType.INVISIBLE} (not 1.12.2's {@code ENTITYBLOCK_ANIMATED} -
 * this port's own established convention for "100% TESR-rendered, no static
 * chunk-mesh contribution at all," see {@code FluidDisplayBlock}/{@code
 * RuneBaseBlock}) means the chunk mesh builder never touches this block at
 * all; {@link #getRenderLayer} still has to be non-{@code SOLID} regardless
 * (real bug already found and fixed once in this port for exactly this
 * reason - see {@code FluidDisplayBlock}'s own javadoc - {@code isSolid()},
 * which gates neighbor face culling, doesn't care about render *type* at
 * all).
 * <p>
 * Disclosed simplification: 1.12.2's item form was itself {@code
 * builtin/entity} + a shared {@code TileEntityItemStackRenderer}, pulsing a
 * translucent ghost-preview of the tagged block in hand/inventory. This port
 * gives it a plain flat icon (reusing {@code block/quartz_glass}'s texture -
 * translucent, thematically close) instead of porting a second full custom
 * item renderer on top of the placed-block one - the placed block's real
 * ghost-fade behavior (the actual gameplay-relevant part) is unaffected.
 * {@code addHitEffects} (per-mine-tap particles matching the displayed
 * block) is dropped for the same reason - purely cosmetic, and this version's
 * {@code ParticleManager} convenience method for it reads the *real* (empty/
 * invisible) block at this position rather than accepting an explicit state
 * the way {@link #addDestroyEffects}/{@link #addLandingEffects} below do, so
 * reproducing it faithfully would need hand-rolled particle positioning like
 * 1.12.2's own version - not worth it for a detail this minor.
 */
public class DiaphanousBlock extends Block {
    public DiaphanousBlock() {
        super(Block.Properties.create(Material.GLASS).sound(net.minecraft.block.SoundType.GLASS).hardnessAndResistance(0.3F).doesNotBlockMovement());
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new DiaphanousBlockTileEntity();
    }

    private static boolean isInverted(IBlockReader world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        return te instanceof DiaphanousBlockTileEntity && ((DiaphanousBlockTileEntity) te).isInverted();
    }

    /**
     * Real bug, found 2026-09-28 (reported by user): non-inverted blocks
     * couldn't be mined at all. Root cause: this was wrongly gated on {@code
     * inverted} the same as {@link #getCollisionShape} below - but {@code
     * getShape} is also what {@code World}'s block-targeting raytrace uses to
     * decide what the player is looking at (confirmed via source; 1.14.4 has
     * no separate "collision box" vs. "selection/interaction box" split the
     * way 1.12.2's {@code getCollisionBoundingBox}/base {@code
     * getBoundingBox} did - {@code BlockBlockDiaphanous} never touched the
     * latter at all, so it stayed a normal full cube for targeting purposes
     * regardless of collision). Always a full cube now, so the block is
     * always targetable/mineable/right-clickable; only {@link
     * #getCollisionShape} - actual physical walk-through-or-not - stays
     * gated on {@code inverted}.
     */
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return VoxelShapes.fullCube();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return isInverted(worldIn, pos) ? VoxelShapes.fullCube() : VoxelShapes.empty();
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

        if (worldIn.isRemote) {
            return;
        }

        TileEntity te = worldIn.getTileEntity(pos);

        if (!(te instanceof DiaphanousBlockTileEntity) || !stack.hasTag()) {
            return;
        }

        CompoundNBT tag = stack.getTag();

        if (!tag.contains("block")) {
            return;
        }

        net.minecraft.block.Block displayBlock = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(new net.minecraft.util.ResourceLocation(tag.getString("block")));

        if (displayBlock != null) {
            ((DiaphanousBlockTileEntity) te).setDisplayState(displayBlock.getDefaultState(), tag.getBoolean("inverted"));
        }
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.getBlock() != this && !worldIn.isRemote) {
            TileEntity te = worldIn.getTileEntity(pos);

            if (te instanceof DiaphanousBlockTileEntity) {
                DiaphanousBlockTileEntity diaphanous = (DiaphanousBlockTileEntity) te;

                ItemStack drop = new ItemStack(this);
                CompoundNBT tag = new CompoundNBT();
                tag.putString("block", diaphanous.getDisplayState().getBlock().getRegistryName().toString());
                tag.putBoolean("inverted", diaphanous.isInverted());
                drop.setTag(tag);

                Block.spawnAsEntity(worldIn, pos, drop);
            }
        }

        super.onReplaced(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public boolean addLandingEffects(BlockState state, ServerWorld worldObj, BlockPos pos, BlockState iblockstate, LivingEntity entity, int numberOfParticles) {
        TileEntity te = worldObj.getTileEntity(pos);

        if (!(te instanceof DiaphanousBlockTileEntity)) {
            return false;
        }

        BlockState display = ((DiaphanousBlockTileEntity) te).getDisplayState();
        worldObj.spawnParticle(new net.minecraft.particles.BlockParticleData(net.minecraft.particles.ParticleTypes.BLOCK, display), pos.getX(), pos.getY(), pos.getZ(), numberOfParticles, 0.0D, 0.0D, 0.0D, 0.15000000596046448D);

        return true;
    }

    @Override
    public boolean addDestroyEffects(BlockState state, World world, BlockPos pos, ParticleManager manager) {
        TileEntity te = world.getTileEntity(pos);

        if (!(te instanceof DiaphanousBlockTileEntity)) {
            return false;
        }

        BlockState display = ((DiaphanousBlockTileEntity) te).getDisplayState();
        manager.addBlockDestroyEffects(pos, display);

        return true;
    }
}
