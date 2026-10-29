package lumien.randomthings.item;

import lumien.randomthings.entity.SpectreIlluminatorEntity;
import lumien.randomthings.util.WeakSets;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

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
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        PlayerEntity player = context.getPlayer();

        if (!world.isRemote) {
            ChunkPos cp = new ChunkPos(pos);

            for (SpectreIlluminatorEntity illuminator : WeakSets.snapshot(SpectreIlluminatorEntity.ILLUMINATORS)) {
                if (illuminator.isInChunk(world, cp)) {
                    illuminator.remove();

                    BlockPos spawnPos = pos.offset(context.getFace());
                    world.addEntity(new ItemEntity(world, spawnPos.getX() + 0.5, spawnPos.getY() + 0.5, spawnPos.getZ() + 0.5, new ItemStack(ModItems.SPECTRE_ILLUMINATOR)));

                    if (player == null || !player.abilities.isCreativeMode) {
                        context.getItem().shrink(1);
                    }

                    return ActionResultType.SUCCESS;
                }
            }

            return ActionResultType.PASS;
        }

        return ActionResultType.PASS;
    }
}
