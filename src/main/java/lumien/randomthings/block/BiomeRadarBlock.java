package lumien.randomthings.block;

import net.minecraft.world.level.block.EntityBlock;

import lumien.randomthings.tileentity.ModTileEntityTypes;

import lumien.randomthings.util.Tickers;

import net.minecraft.world.level.block.entity.BlockEntityType;

import net.minecraft.world.level.block.entity.BlockEntityTicker;

import lumien.randomthings.item.BiomeCrystalItem;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.lib.IRTBlockColor;
import lumien.randomthings.tileentity.BiomeRadarTileEntity;
import lumien.randomthings.tileentity.BiomeRadarTileEntity.STATE;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;

/**
 * Insert a {@link BiomeCrystalItem} and redstone-power it (with the iron-bars
 * "antenna" {@link BiomeRadarTileEntity#isValid()} checks for already built
 * above it) to start searching for that crystal's target biome. When it
 * finds one, right-click with paper to convert it into a
 * {@link lumien.randomthings.item.PositionFilterItem} pointing at that spot.
 * Direct port of 1.12.2's {@code BlockBiomeRadar}.
 */
public class BiomeRadarBlock extends Block implements IRTBlockColor, EntityBlock {
    public BiomeRadarBlock() {
        super(Block.Properties.of(Material.METAL).noOcclusion().strength(5.0F));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new BiomeRadarTileEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        return Tickers.of(type, ModTileEntityTypes.BIOME_RADAR, (l, p, s2, be) -> be.tick());
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity te = worldIn.getBlockEntity(pos);

            if (te instanceof BiomeRadarTileEntity) {
                ItemStack crystal = ((BiomeRadarTileEntity) te).getCurrentCrystal();

                if (!crystal.isEmpty()) {
                    Block.popResource(worldIn, pos, crystal);
                }
            }

            super.onRemove(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public int colorMultiplier(BlockState state, BlockAndTintGetter worldIn, BlockPos pos, int tintIndex) {
        if (pos == null) {
            return 0xFFFFFF;
        }

        int rgb = lumien.randomthings.util.BiomeColorUtil.getBiomeColor(worldIn, null, pos);
        return new java.awt.Color(rgb).brighter().getRGB();
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block neighborBlock, BlockPos changedPos, boolean isMoving) {
        BlockEntity te = worldIn.getBlockEntity(pos);

        if (te instanceof BiomeRadarTileEntity) {
            ((BiomeRadarTileEntity) te).neighborChanged(neighborBlock);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack equipped = player.getItemInHand(hand);
        BlockEntity te = worldIn.getBlockEntity(pos);

        if (!(te instanceof BiomeRadarTileEntity)) {
            return InteractionResult.PASS;
        }

        BiomeRadarTileEntity biomeRadar = (BiomeRadarTileEntity) te;

        if (biomeRadar.getState() == STATE.IDLE) {
            if (biomeRadar.getCurrentCrystal().isEmpty()) {
                if (!equipped.isEmpty() && equipped.getItem() == ModItems.BIOME_CRYSTAL) {
                    if (!worldIn.isClientSide) {
                        biomeRadar.setCrystal(equipped.copy());
                        equipped.shrink(1);
                        worldIn.blockEvent(pos, this, 1037, 0);
                    }

                    return InteractionResult.SUCCESS;
                }
            } else {
                if (equipped.isEmpty()) {
                    if (!worldIn.isClientSide) {
                        ItemStack currentCrystal = biomeRadar.getCurrentCrystal();

                        player.getInventory().setItem(player.getInventory().selected, currentCrystal);
                        biomeRadar.setCrystal(ItemStack.EMPTY);
                        worldIn.blockEvent(pos, this, 1036, 0);
                    }

                    return InteractionResult.SUCCESS;
                }
            }
        } else if (biomeRadar.getState() == STATE.FINISHED) {
            if (!equipped.isEmpty() && equipped.getItem() == Items.PAPER) {
                if (!worldIn.isClientSide) {
                    ItemStack positionFilter = biomeRadar.generatePositionFilter();

                    equipped.shrink(1);
                    player.getInventory().add(positionFilter);
                }

                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }
}
