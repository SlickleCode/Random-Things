package lumien.randomthings.block;

import net.minecraft.util.RandomSource;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import lumien.randomthings.item.ModItems;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.server.level.ServerLevel;

public class SticksBlock extends Block
{
	boolean returning;

	public SticksBlock(boolean returning)
	{
		super(Block.Properties.of(Material.WOOD, MaterialColor.WOOD).noOcclusion().strength(0.5F).sound(SoundType.WOOD));

		this.returning = returning;
	}

	@Override
	public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
	{
		super.setPlacedBy(worldIn, pos, state, placer, stack);
		if (!worldIn.isClientSide)
		{
			worldIn.scheduleTick(pos, this, 20 * 10);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public float func_220080_a(BlockState state, BlockGetter worldIn, BlockPos pos)
	{
		return 1.0F;
	}

	public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos)
	{
		return true;
	}

	public boolean causesSuffocation(BlockState state, BlockGetter worldIn, BlockPos pos)
	{
		return false;
	}

	public boolean isNormalCube(BlockState state, BlockGetter worldIn, BlockPos pos)
	{
		return false;
	}

	public boolean canEntitySpawn(BlockState state, BlockGetter worldIn, BlockPos pos, EntityType<?> type)
	{
		return false;
	}

	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random)
	{
		if (!worldIn.isClientSide)
		{
			worldIn.removeBlock(pos, false);

			if (returning)
			{
				worldIn.playSound(null, pos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 0.6f, 1.2f);

				List<Player> playerList = worldIn.getEntitiesOfClass(Player.class, new AABB(pos).inflate(50, 50, 50));

				if (!playerList.isEmpty())
				{
					Collections.sort(playerList, new Comparator<Player>()
					{
						@Override
						public int compare(Player o1, Player o2)
						{
							return o1.blockPosition().distSqr(pos) >= o2.blockPosition().distSqr(pos) ? 1 : -1;
						}
					});

					Player closes = playerList.get(0);

					if (!closes.isCreative())
					{
						closes.getInventory().add(new ItemStack(returning ? ModItems.BLOCK_OF_STICKS_RETURNING : ModItems.BLOCK_OF_STICKS));
					}
				}
			}
			else
			{
				worldIn.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.6f, 1.2f);
				worldIn.levelEvent(2001, pos, Block.getId(state));
			}
		}
	}
}
