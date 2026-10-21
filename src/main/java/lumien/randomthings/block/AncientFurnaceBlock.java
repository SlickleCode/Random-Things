package lumien.randomthings.block;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.EntityBlock;

import lumien.randomthings.tileentity.ModTileEntityTypes;

import lumien.randomthings.util.Tickers;

import net.minecraft.world.level.block.entity.BlockEntityType;

import net.minecraft.world.level.block.entity.BlockEntityTicker;

import lumien.randomthings.tileentity.AncientFurnaceTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import java.util.Random;
import net.minecraft.world.InteractionResult;

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
public class AncientFurnaceBlock extends Block implements EntityBlock {
    public static final BooleanProperty HEATING = BooleanProperty.create("heating");

    public AncientFurnaceBlock() {
        super(Block.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.STONE).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASEDRUM).strength(50.0F, 10.0F));

        this.registerDefaultState(this.stateDefinition.any().setValue(HEATING, false));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(HEATING);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new AncientFurnaceTileEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        return Tickers.of(type, ModTileEntityTypes.ANCIENT_FURNACE, (l, p, s2, be) -> be.tick());
    }

    @Override
    public net.minecraft.world.ItemInteractionResult useItemOn(ItemStack heldStack, BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        InteractionResult result = legacyUse(state, worldIn, pos, player, handIn, hit);
        if (result == InteractionResult.SUCCESS) {
            return net.minecraft.world.ItemInteractionResult.SUCCESS;
        }
        if (result == InteractionResult.CONSUME) {
            return net.minecraft.world.ItemInteractionResult.CONSUME;
        }
        if (result == InteractionResult.FAIL) {
            return net.minecraft.world.ItemInteractionResult.FAIL;
        }
        return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private InteractionResult legacyUse(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (state.getValue(HEATING)) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getItemInHand(handIn);

        if (stack.getItem() != Items.NETHER_STAR) {
            return InteractionResult.PASS;
        }

        if (worldIn.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity te = worldIn.getBlockEntity(pos);

        if (!(te instanceof AncientFurnaceTileEntity)) {
            return InteractionResult.PASS;
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        worldIn.setBlock(pos, state.setValue(HEATING, true), 3);
        ((AncientFurnaceTileEntity) te).startHeating();
        worldIn.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, 0.6F);

        return InteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level worldIn, BlockPos pos, RandomSource rand) {
        if (!state.getValue(HEATING)) {
            return;
        }

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5;

        worldIn.addParticle(ParticleTypes.FLAME, x + rand.nextDouble() * 0.4 - 0.2, y, z + rand.nextDouble() * 0.4 - 0.2, 0, 0.02, 0);
        worldIn.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0.01, 0);
    }
}
