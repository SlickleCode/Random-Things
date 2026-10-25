package lumien.randomthings.item;

import lumien.randomthings.entity.ModEntityTypes;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 1.12.2 registered a spawn egg for the Spirit (colours 0x000000 / 0x0009F6). 1.14.4's vanilla spawn egg needs the entity type at construction,
 * but entity types register after items, so this small item spawns the Spirit itself.
 */
public class SpiritSpawnEggItem extends Item
{
	public SpiritSpawnEggItem(Properties properties)
	{
		super(properties);
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context)
	{
		World world = context.getWorld();

		if (world.isRemote)
		{
			return ActionResultType.SUCCESS;
		}

		BlockPos pos = context.getPos().offset(context.getFace());

		if (ModEntityTypes.SPIRIT.spawn(world, context.getItem(), context.getPlayer(), pos, SpawnReason.SPAWN_EGG, true, false) != null && (context.getPlayer() == null || !context.getPlayer().abilities.isCreativeMode))
		{
			context.getItem().shrink(1);
		}

		return ActionResultType.SUCCESS;
	}
}
