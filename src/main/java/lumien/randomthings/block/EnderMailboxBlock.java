package lumien.randomthings.block;

import lumien.randomthings.util.StackTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.EntityBlock;

import lumien.randomthings.tileentity.ModTileEntityTypes;

import lumien.randomthings.util.Tickers;

import net.minecraft.world.level.block.entity.BlockEntityType;

import net.minecraft.world.level.block.entity.BlockEntityTicker;

import com.mojang.authlib.GameProfile;
import lumien.randomthings.handler.EnderLetterHandler;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.tileentity.EnderMailboxTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.Random;
import net.minecraft.world.InteractionResult;

/**
 * A personal mailbox: whichever player placed it can right-click it to see
 * their own persistent inbox ({@link EnderLetterHandler}, keyed by their
 * UUID, not this specific block). Sneak-right-click any Ender Mailbox in the
 * world while holding an addressed, unsent {@link
 * lumien.randomthings.item.EnderLetterItem} to actually deliver it into the
 * named recipient's inbox. Direct port of 1.12.2's {@code BlockEnderMailbox}.
 */
public class EnderMailboxBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = EnumProperty.create("facing", Direction.class, Direction.Plane.HORIZONTAL);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private static final VoxelShape SHAPE_NORTH_SOUTH = Block.box(5, 0, 1, 11, 22, 15);
    private static final VoxelShape SHAPE_EAST_WEST = Block.box(1, 0, 5, 15, 22, 11);

    public EnderMailboxBlock() {
        super(lumien.randomthings.lib.RTIds.block().mapColor(net.minecraft.world.level.material.MapColor.STONE).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASEDRUM).strength(1.5F, 10.0F).noCollission());

        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);

        return facing.getAxis() == Direction.Axis.X ? SHAPE_EAST_WEST : SHAPE_NORTH_SOUTH;
    }

    @Override
    public void animateTick(BlockState state, Level worldIn, BlockPos pos, RandomSource rand) {
        if (!state.getValue(ACTIVE)) {
            return;
        }

        for (int i = 0; i < 4; ++i) {
            double x = pos.getX() + rand.nextFloat();
            double y = pos.getY() + rand.nextFloat();
            double z = pos.getZ() + rand.nextFloat();
            double vx = (rand.nextFloat() - 0.5D) * 0.5D;
            double vy = (rand.nextFloat() - 0.5D) * 0.5D;
            double vz = (rand.nextFloat() - 0.5D) * 0.5D;
            int j = rand.nextInt(2) * 2 - 1;

            if (worldIn.getBlockState(pos.west()).getBlock() != this && worldIn.getBlockState(pos.east()).getBlock() != this) {
                x = pos.getX() + 0.5D + 0.25D * j;
                vx = rand.nextFloat() * 2.0F * j;
            } else {
                z = pos.getZ() + 0.5D + 0.25D * j;
                vz = rand.nextFloat() * 2.0F * j;
            }

            worldIn.addParticle(ParticleTypes.PORTAL, x, y, z, vx, vy, vz);
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        return worldIn.isEmptyBlock(pos.above());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new EnderMailboxTileEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        return Tickers.of(type, ModTileEntityTypes.ENDER_MAILBOX, (l, p, s2, be) -> be.tick());
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(ACTIVE, false);
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!worldIn.isClientSide && placer instanceof Player) {
            BlockEntity te = worldIn.getBlockEntity(pos);

            if (te instanceof EnderMailboxTileEntity) {
                ((EnderMailboxTileEntity) te).setOwner(placer.getUUID());
            }
        }
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player, BlockHitResult hit) {
        if (worldIn.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ItemStack heldItem = player.getMainHandItem();

        if (player.isShiftKeyDown() && heldItem.getItem() == ModItems.ENDER_LETTER) {
            deliverLetter(worldIn, pos, player, heldItem);
            return InteractionResult.SUCCESS;
        }

        BlockEntity te = worldIn.getBlockEntity(pos);

        if (te instanceof EnderMailboxTileEntity) {
            EnderMailboxTileEntity mailbox = (EnderMailboxTileEntity) te;

            if (mailbox.getOwner() != null) {
                if (mailbox.getOwner().equals(player.getUUID())) {
                    ((net.minecraft.server.level.ServerPlayer) player).openMenu(mailbox, pos);
                } else {
                    player.displayClientMessage(Component.translatable("block.randomthings.ender_mailbox.owner").withStyle(ChatFormatting.RED), false);
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    private void deliverLetter(Level worldIn, BlockPos pos, Player player, ItemStack heldItem) {
        CompoundTag compound = StackTag.get(heldItem);

        if (compound == null || !compound.contains("receiver") || compound.getBoolean("received")) {
            return;
        }

        GameProfile profile = ((ServerLevel) worldIn).getServer().getProfileCache().get(compound.getString("receiver")).orElse(null);

        if (profile == null || profile.getId() == null) {
            player.displayClientMessage(Component.translatable("item.randomthings.ender_letter.no_player", compound.getString("receiver")).withStyle(ChatFormatting.DARK_PURPLE), false);
            return;
        }

        IItemHandler mailboxInventory = EnderLetterHandler.get(worldIn).getOrCreateInventoryForPlayer(profile.getId());

        for (int slot = 0; slot < mailboxInventory.getSlots(); slot++) {
            if (mailboxInventory.getStackInSlot(slot).isEmpty()) {
                ItemStack sendingLetter = heldItem.copy();
                sendingLetter.setCount(1);
                StackTag.edit(sendingLetter, t -> t.putBoolean("received", true));
                StackTag.edit(sendingLetter, t -> t.putString("sender", player.getGameProfile().getName()));
                mailboxInventory.insertItem(slot, sendingLetter, false);
                heldItem.shrink(1);

                worldIn.playSound(null, pos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1, 1);
                return;
            }
        }

        player.displayClientMessage(Component.translatable("item.randomthings.ender_letter.no_space").withStyle(ChatFormatting.DARK_PURPLE), false);
    }
}
