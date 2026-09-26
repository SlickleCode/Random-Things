package lumien.randomthings.block;

import com.mojang.authlib.GameProfile;
import lumien.randomthings.handler.EnderLetterHandler;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.tileentity.EnderMailboxTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.items.IItemHandler;

import java.util.Random;

/**
 * A personal mailbox: whichever player placed it can right-click it to see
 * their own persistent inbox ({@link EnderLetterHandler}, keyed by their
 * UUID, not this specific block). Sneak-right-click any Ender Mailbox in the
 * world while holding an addressed, unsent {@link
 * lumien.randomthings.item.EnderLetterItem} to actually deliver it into the
 * named recipient's inbox. Direct port of 1.12.2's {@code BlockEnderMailbox}.
 */
public class EnderMailboxBlock extends Block {
    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private static final VoxelShape SHAPE_NORTH_SOUTH = Block.makeCuboidShape(5, 0, 1, 11, 22, 15);
    private static final VoxelShape SHAPE_EAST_WEST = Block.makeCuboidShape(1, 0, 5, 15, 22, 11);

    public EnderMailboxBlock() {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(1.5F, 10.0F).doesNotBlockMovement());

        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH).with(ACTIVE, false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        Direction facing = state.get(FACING);

        return facing.getAxis() == Direction.Axis.X ? SHAPE_EAST_WEST : SHAPE_NORTH_SOUTH;
    }

    @Override
    public void animateTick(BlockState state, World worldIn, BlockPos pos, Random rand) {
        if (!state.get(ACTIVE)) {
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
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        return worldIn.isAirBlock(pos.up());
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new EnderMailboxTileEntity();
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite()).with(ACTIVE, false);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!worldIn.isRemote && placer instanceof PlayerEntity) {
            TileEntity te = worldIn.getTileEntity(pos);

            if (te instanceof EnderMailboxTileEntity) {
                ((EnderMailboxTileEntity) te).setOwner(placer.getUniqueID());
            }
        }
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (worldIn.isRemote) {
            return true;
        }

        ItemStack heldItem = player.getHeldItemMainhand();

        if (player.isSneaking() && heldItem.getItem() == ModItems.ENDER_LETTER) {
            deliverLetter(worldIn, pos, player, heldItem);
            return true;
        }

        TileEntity te = worldIn.getTileEntity(pos);

        if (te instanceof EnderMailboxTileEntity) {
            EnderMailboxTileEntity mailbox = (EnderMailboxTileEntity) te;

            if (mailbox.getOwner() != null) {
                if (mailbox.getOwner().equals(player.getUniqueID())) {
                    net.minecraftforge.fml.network.NetworkHooks.openGui((net.minecraft.entity.player.ServerPlayerEntity) player, mailbox, pos);
                } else {
                    player.sendStatusMessage(new TranslationTextComponent("block.randomthings.ender_mailbox.owner").applyTextStyle(TextFormatting.RED), false);
                }
            }
        }

        return true;
    }

    private void deliverLetter(World worldIn, BlockPos pos, PlayerEntity player, ItemStack heldItem) {
        CompoundNBT compound = heldItem.getTag();

        if (compound == null || !compound.contains("receiver") || compound.getBoolean("received")) {
            return;
        }

        GameProfile profile = ((ServerWorld) worldIn).getServer().getPlayerProfileCache().getGameProfileForUsername(compound.getString("receiver"));

        if (profile == null || profile.getId() == null) {
            player.sendStatusMessage(new TranslationTextComponent("item.randomthings.ender_letter.no_player", compound.getString("receiver")).applyTextStyle(TextFormatting.DARK_PURPLE), false);
            return;
        }

        IItemHandler mailboxInventory = EnderLetterHandler.get(worldIn).getOrCreateInventoryForPlayer(profile.getId());

        for (int slot = 0; slot < mailboxInventory.getSlots(); slot++) {
            if (mailboxInventory.getStackInSlot(slot).isEmpty()) {
                ItemStack sendingLetter = heldItem.copy();
                sendingLetter.setCount(1);
                sendingLetter.getOrCreateTag().putBoolean("received", true);
                sendingLetter.getOrCreateTag().putString("sender", player.getGameProfile().getName());

                mailboxInventory.insertItem(slot, sendingLetter, false);
                heldItem.shrink(1);

                worldIn.playSound(null, pos, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 1, 1);
                return;
            }
        }

        player.sendStatusMessage(new TranslationTextComponent("item.randomthings.ender_letter.no_space").applyTextStyle(TextFormatting.DARK_PURPLE), false);
    }
}
