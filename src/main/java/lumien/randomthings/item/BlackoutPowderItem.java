package lumien.randomthings.item;

import lumien.randomthings.entity.SpectreIlluminatorEntity;
import lumien.randomthings.util.WeakSets;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

/**
 * Port of 1.12.2's {@code ItemIngredient.BLACKOUT_POWDER}: right-clicking a block in a chunk lit by a
 * {@link SpectreIlluminatorEntity} removes the orb (which relights the chunk, exactly like picking it up) and
 * drops the Spectre Illuminator item next to the clicked block. Unlike 1.12.2, which never used the powder up,
 * one powder is consumed (not in creative), as the wiki describes.
 */
public class BlackoutPowderItem extends Item {

    public BlackoutPowderItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();

        if (!world.isClientSide) {
            ChunkPos cp = new ChunkPos(pos);

            for (SpectreIlluminatorEntity illuminator : WeakSets.snapshot(SpectreIlluminatorEntity.ILLUMINATORS)) {
                if (illuminator.isInChunk(world, cp)) {
                    illuminator.discard();

                    BlockPos spawnPos = pos.relative(context.getClickedFace());
                    world.addFreshEntity(new ItemEntity(world, spawnPos.getX() + 0.5, spawnPos.getY() + 0.5, spawnPos.getZ() + 0.5, new ItemStack(ModItems.SPECTRE_ILLUMINATOR)));

                    if (player == null || !player.getAbilities().instabuild) {
                        context.getItemInHand().shrink(1);
                    }

                    return InteractionResult.SUCCESS;
                }
            }

            return InteractionResult.PASS;
        }

        return InteractionResult.PASS;
    }
}
