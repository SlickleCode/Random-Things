package lumien.randomthings.item;

import lumien.randomthings.util.WeakSets;
import lumien.randomthings.debug.RTDebug;
import lumien.randomthings.entity.SpectreIlluminatorEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

/**
 * Right-click a block to place a {@link SpectreIlluminatorEntity}, which
 * lights up its whole chunk once it settles into place. Refuses if that
 * chunk already has one. Direct port of 1.12.2's {@code ItemSpectreIlluminator}.
 */
public class SpectreIlluminatorItem extends Item {

    public SpectreIlluminatorItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();

        if (!world.isClientSide()) {
            ChunkPos cp = new ChunkPos(pos);

            boolean alreadyLit = false;

            for (SpectreIlluminatorEntity illuminator : WeakSets.snapshot(SpectreIlluminatorEntity.ILLUMINATORS)) {
                if (illuminator.isInChunk(world, cp)) {
                    alreadyLit = true;
                    break;
                }
            }

            if (alreadyLit) {
                RTDebug.log("[SpectreIlluminator] placement at {} refused: chunk {} already has an illuminated orb", pos, cp);
            } else {
                AABB scanBox = lumien.randomthings.util.AABBs.of(new BlockPos(cp.getMinBlockX() - 2, world.getMinY(), cp.getMinBlockZ() - 2), new BlockPos(cp.getMaxBlockX() + 2, world.getMaxY() + 1 - 1, cp.getMaxBlockZ() + 2));
                java.util.List<SpectreIlluminatorEntity> nearby = world.getEntitiesOfClass(SpectreIlluminatorEntity.class, scanBox);

                if (!nearby.isEmpty()) {
                    RTDebug.log("[SpectreIlluminator] placement at {} refused: {} existing orb(s) within 2 chunks (e.g. {} at {}, illuminated={})", pos, nearby.size(), nearby.get(0), nearby.get(0).blockPosition(), nearby.get(0).isIlluminated());
                    return InteractionResult.FAIL;
                }

                SpectreIlluminatorEntity illuminator = new SpectreIlluminatorEntity(world, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5);
                world.addFreshEntity(illuminator);

                RTDebug.log("[SpectreIlluminator] placed at {} (entity {})", pos, illuminator.getId());

                if (player == null || !player.getAbilities().instabuild) {
                    ItemStack stack = context.getItemInHand();
                    stack.shrink(1);
                }
            }
        }

        return InteractionResult.SUCCESS;
    }
}
