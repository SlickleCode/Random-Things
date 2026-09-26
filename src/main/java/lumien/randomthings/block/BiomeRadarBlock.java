package lumien.randomthings.block;

import lumien.randomthings.item.BiomeCrystalItem;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.lib.IRTBlockColor;
import lumien.randomthings.tileentity.BiomeRadarTileEntity;
import lumien.randomthings.tileentity.BiomeRadarTileEntity.STATE;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.World;

/**
 * Insert a {@link BiomeCrystalItem} and redstone-power it (with the iron-bars
 * "antenna" {@link BiomeRadarTileEntity#isValid()} checks for already built
 * above it) to start searching for that crystal's target biome. When it
 * finds one, right-click with paper to convert it into a
 * {@link lumien.randomthings.item.PositionFilterItem} pointing at that spot.
 * Direct port of 1.12.2's {@code BlockBiomeRadar}.
 */
public class BiomeRadarBlock extends Block implements IRTBlockColor {
    public BiomeRadarBlock() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(5.0F));
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new BiomeRadarTileEntity();
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity te = worldIn.getTileEntity(pos);

            if (te instanceof BiomeRadarTileEntity) {
                ItemStack crystal = ((BiomeRadarTileEntity) te).getCurrentCrystal();

                if (!crystal.isEmpty()) {
                    Block.spawnAsEntity(worldIn, pos, crystal);
                }
            }

            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public int colorMultiplier(BlockState state, IEnviromentBlockReader worldIn, BlockPos pos, int tintIndex) {
        if (pos == null) {
            return 0xFFFFFF;
        }

        int rgb = lumien.randomthings.util.BiomeColorUtil.getBiomeColor(worldIn, worldIn.getBiome(pos), pos);
        return new java.awt.Color(rgb).brighter().getRGB();
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block neighborBlock, BlockPos changedPos, boolean isMoving) {
        TileEntity te = worldIn.getTileEntity(pos);

        if (te instanceof BiomeRadarTileEntity) {
            ((BiomeRadarTileEntity) te).neighborChanged(neighborBlock);
        }
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        ItemStack equipped = player.getHeldItem(hand);
        TileEntity te = worldIn.getTileEntity(pos);

        if (!(te instanceof BiomeRadarTileEntity)) {
            return false;
        }

        BiomeRadarTileEntity biomeRadar = (BiomeRadarTileEntity) te;

        if (biomeRadar.getState() == STATE.IDLE) {
            if (biomeRadar.getCurrentCrystal().isEmpty()) {
                if (!equipped.isEmpty() && equipped.getItem() == ModItems.BIOME_CRYSTAL) {
                    if (!worldIn.isRemote) {
                        biomeRadar.setCrystal(equipped.copy());
                        equipped.shrink(1);
                        worldIn.addBlockEvent(pos, this, 1037, 0);
                    }

                    return true;
                }
            } else {
                if (equipped.isEmpty()) {
                    if (!worldIn.isRemote) {
                        ItemStack currentCrystal = biomeRadar.getCurrentCrystal();

                        player.inventory.setInventorySlotContents(player.inventory.currentItem, currentCrystal);
                        biomeRadar.setCrystal(ItemStack.EMPTY);
                        worldIn.addBlockEvent(pos, this, 1036, 0);
                    }

                    return true;
                }
            }
        } else if (biomeRadar.getState() == STATE.FINISHED) {
            if (!equipped.isEmpty() && equipped.getItem() == Items.PAPER) {
                if (!worldIn.isRemote) {
                    ItemStack positionFilter = biomeRadar.generatePositionFilter();

                    equipped.shrink(1);
                    player.inventory.addItemStackToInventory(positionFilter);
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
