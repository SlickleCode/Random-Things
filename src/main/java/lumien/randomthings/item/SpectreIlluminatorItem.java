package lumien.randomthings.item;

import lumien.randomthings.entity.SpectreIlluminatorEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Right-click a block to place a {@link SpectreIlluminatorEntity}, which
 * lights up its whole chunk once it settles into place. Refuses if that
 * chunk already has one. Direct port of 1.12.2's {@code ItemSpectreIlluminator}.
 */
public class SpectreIlluminatorItem extends Item {
    private static final Logger LOGGER = LogManager.getLogger();

    public SpectreIlluminatorItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        PlayerEntity player = context.getPlayer();

        if (!world.isRemote) {
            ChunkPos cp = new ChunkPos(pos);

            boolean alreadyLit = false;

            for (SpectreIlluminatorEntity illuminator : SpectreIlluminatorEntity.ILLUMINATORS) {
                if (illuminator.isInChunk(world, cp)) {
                    alreadyLit = true;
                    break;
                }
            }

            if (alreadyLit) {
                LOGGER.info("[SpectreIlluminator] placement at {} refused: chunk {} already has an illuminated orb", pos, cp);
            } else {
                AxisAlignedBB scanBox = new AxisAlignedBB(new BlockPos(cp.getXStart() - 2, 0, cp.getZStart() - 2), new BlockPos(cp.getXEnd() + 2, 255, cp.getZEnd() + 2));
                java.util.List<SpectreIlluminatorEntity> nearby = world.getEntitiesWithinAABB(SpectreIlluminatorEntity.class, scanBox);

                if (!nearby.isEmpty()) {
                    LOGGER.info("[SpectreIlluminator] placement at {} refused: {} existing orb(s) within 2 chunks (e.g. {} at {}, illuminated={})", pos, nearby.size(), nearby.get(0), nearby.get(0).getPosition(), nearby.get(0).isIlluminated());
                    return ActionResultType.FAIL;
                }

                SpectreIlluminatorEntity illuminator = new SpectreIlluminatorEntity(world, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5);
                world.addEntity(illuminator);

                LOGGER.info("[SpectreIlluminator] placed at {} (entity {})", pos, illuminator.getEntityId());

                if (player == null || !player.abilities.isCreativeMode) {
                    ItemStack stack = context.getItem();
                    stack.shrink(1);
                }
            }
        }

        return ActionResultType.SUCCESS;
    }
}
