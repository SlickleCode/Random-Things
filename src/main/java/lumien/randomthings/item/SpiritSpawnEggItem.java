package lumien.randomthings.item;

import java.util.Objects;

import lumien.randomthings.entity.ModEntityTypes;
import lumien.randomthings.lib.IRTItemColor;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

/**
 * 1.12.2 registered a spawn egg for the Spirit (colours 0x000000 / 0x0009F6). 1.14.4's vanilla spawn egg needs the entity type at construction,
 * but entity types register after items, so this small item does what the vanilla egg does itself: it spawns the Spirit on a block, on water,
 * switches a spawner to Spirits, is fired out of a dispenser, and tints the egg template black / blue.
 */
public class SpiritSpawnEggItem extends Item implements IRTItemColor
{
	public SpiritSpawnEggItem(Properties properties)
	{
		super(properties);

		DispenserBlock.registerDispenseBehavior(this, new DefaultDispenseItemBehavior()
		{
			@Override
			protected ItemStack dispenseStack(IBlockSource source, ItemStack stack)
			{
				Direction facing = source.getBlockState().get(DispenserBlock.FACING);

				ModEntityTypes.SPIRIT.spawn(source.getWorld(), stack, null, source.getBlockPos().offset(facing), SpawnReason.DISPENSER, facing != Direction.UP, false);
				stack.shrink(1);

				return stack;
			}
		});
	}

	@Override
	public int getColorFromItemstack(ItemStack stack, int tintIndex)
	{
		return tintIndex == 0 ? 0x000000 : 0x0009F6;
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context)
	{
		World world = context.getWorld();

		if (world.isRemote)
		{
			return ActionResultType.SUCCESS;
		}

		ItemStack stack = context.getItem();
		BlockPos pos = context.getPos();
		Direction face = context.getFace();
		BlockState state = world.getBlockState(pos);
		boolean creative = context.getPlayer() != null && context.getPlayer().abilities.isCreativeMode;

		if (state.getBlock() == Blocks.SPAWNER)
		{
			TileEntity tile = world.getTileEntity(pos);

			if (tile instanceof MobSpawnerTileEntity)
			{
				((MobSpawnerTileEntity) tile).getSpawnerBaseLogic().setEntityType(ModEntityTypes.SPIRIT);
				tile.markDirty();
				world.notifyBlockUpdate(pos, state, state, 3);

				if (!creative)
				{
					stack.shrink(1);
				}

				return ActionResultType.SUCCESS;
			}
		}

		BlockPos target = state.getCollisionShape(world, pos).isEmpty() ? pos : pos.offset(face);

		if (ModEntityTypes.SPIRIT.spawn(world, stack, context.getPlayer(), target, SpawnReason.SPAWN_EGG, true, !Objects.equals(pos, target) && face == Direction.UP) != null && !creative)
		{
			stack.shrink(1);
		}

		return ActionResultType.SUCCESS;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		RayTraceResult ray = rayTrace(world, player, RayTraceContext.FluidMode.SOURCE_ONLY);

		if (ray.getType() != RayTraceResult.Type.BLOCK)
		{
			return new ActionResult<>(ActionResultType.PASS, stack);
		}

		if (world.isRemote)
		{
			return new ActionResult<>(ActionResultType.SUCCESS, stack);
		}

		BlockRayTraceResult blockRay = (BlockRayTraceResult) ray;
		BlockPos pos = blockRay.getPos();

		if (!(world.getBlockState(pos).getBlock() instanceof FlowingFluidBlock))
		{
			return new ActionResult<>(ActionResultType.PASS, stack);
		}

		if (world.isBlockModifiable(player, pos) && player.canPlayerEdit(pos, blockRay.getFace(), stack))
		{
			if (ModEntityTypes.SPIRIT.spawn(world, stack, player, pos, SpawnReason.SPAWN_EGG, false, false) == null)
			{
				return new ActionResult<>(ActionResultType.PASS, stack);
			}

			if (!player.abilities.isCreativeMode)
			{
				stack.shrink(1);
			}

			player.addStat(Stats.ITEM_USED.get(this));

			return new ActionResult<>(ActionResultType.SUCCESS, stack);
		}

		return new ActionResult<>(ActionResultType.FAIL, stack);
	}
}
