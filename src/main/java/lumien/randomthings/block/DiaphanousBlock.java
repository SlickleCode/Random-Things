package lumien.randomthings.block;

import lumien.randomthings.util.StackTag;
import net.minecraft.core.registries.Registries;

import lumien.randomthings.util.RegUtil;
import net.minecraft.world.level.block.EntityBlock;

import lumien.randomthings.tileentity.DiaphanousBlockTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

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
public class DiaphanousBlock extends Block implements EntityBlock {
    public DiaphanousBlock() {
        super(lumien.randomthings.lib.RTIds.block().mapColor(net.minecraft.world.level.material.MapColor.NONE).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.HAT).noOcclusion().sound(net.minecraft.world.level.block.SoundType.GLASS).strength(0.3F).noCollision());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new DiaphanousBlockTileEntity(pos, state);
    }

    private static boolean isInverted(BlockGetter world, BlockPos pos) {
        BlockEntity te = world.getBlockEntity(pos);
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
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return isInverted(worldIn, pos) ? Shapes.block() : Shapes.empty();
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(worldIn, pos, state, placer, stack);

        if (worldIn.isClientSide()) {
            return;
        }

        BlockEntity te = worldIn.getBlockEntity(pos);

        if (!(te instanceof DiaphanousBlockTileEntity) || !StackTag.has(stack)) {
            return;
        }

        CompoundTag tag = StackTag.get(stack);

        if (!tag.contains("block")) {
            return;
        }

        net.minecraft.world.level.block.Block displayBlock = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.parse(tag.getStringOr("block", "")));

        if (displayBlock != null) {
            ((DiaphanousBlockTileEntity) te).setDisplayState(displayBlock.defaultBlockState(), tag.getBooleanOr("inverted", false));
        }
    }

    public void rtOnRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.getBlock() != this && !worldIn.isClientSide()) {
            BlockEntity te = worldIn.getBlockEntity(pos);

            if (te instanceof DiaphanousBlockTileEntity) {
                DiaphanousBlockTileEntity diaphanous = (DiaphanousBlockTileEntity) te;

                ItemStack drop = new ItemStack(this);
                CompoundTag tag = new CompoundTag();
                tag.putString("block", RegUtil.id(diaphanous.getDisplayState().getBlock()).toString());
                tag.putBoolean("inverted", diaphanous.isInverted());
                StackTag.set(drop, tag);

                Block.popResource(worldIn, pos, drop);
            }
        }

    }

    @Override
    public boolean addLandingEffects(BlockState state, ServerLevel worldObj, BlockPos pos, BlockState iblockstate, LivingEntity entity, int numberOfParticles) {
        BlockEntity te = worldObj.getBlockEntity(pos);

        if (!(te instanceof DiaphanousBlockTileEntity)) {
            return false;
        }

        BlockState display = ((DiaphanousBlockTileEntity) te).getDisplayState();
        worldObj.sendParticles(new net.minecraft.core.particles.BlockParticleOption(net.minecraft.core.particles.ParticleTypes.BLOCK, display), pos.getX(), pos.getY(), pos.getZ(), numberOfParticles, 0.0D, 0.0D, 0.0D, 0.15000000596046448D);

        return true;
    }

    /** Client side of the break particles (registered through RegisterClientExtensionsEvent in ClientSetup, 1.21.4 moved it out of the block). */
    public static final class ClientExtensions implements net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions {
        @Override
        public boolean addDestroyEffects(BlockState state, Level world, BlockPos pos, ParticleEngine manager) {
            BlockEntity te = world.getBlockEntity(pos);

            if (!(te instanceof DiaphanousBlockTileEntity)) {
                return false;
            }

            BlockState display = ((DiaphanousBlockTileEntity) te).getDisplayState();
            world.addDestroyBlockEffect(pos, display);

            return true;
        }
    }
}
