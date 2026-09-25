package lumien.randomthings.network.messages;

import lumien.randomthings.network.IRTMessage;
import lumien.randomthings.tileentity.BiomeRadarTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

/**
 * Server -> nearby clients: the up-to-4 distinct biomes a
 * {@link BiomeRadarTileEntity} has most recently sampled while searching, so
 * the client can render the antenna's colored particle feedback. Direct port
 * of 1.12.2's {@code MessageBiomeRadarAntenna}.
 */
public class BiomeRadarAntennaMessage implements IRTMessage
{
	private BlockPos pos;
	private String[] antennaBiomes = new String[4];

	public BiomeRadarAntennaMessage()
	{
	}

	public BiomeRadarAntennaMessage(BlockPos pos, String[] antennaBiomes)
	{
		this.pos = pos;
		this.antennaBiomes = antennaBiomes;
	}

	@Override
	public void read(PacketBuffer pb)
	{
		this.pos = pb.readBlockPos();

		int amount = pb.readInt();

		for (int i = 0; i < amount; i++)
		{
			antennaBiomes[i] = pb.readString();
		}
	}

	@Override
	public void write(PacketBuffer pb)
	{
		pb.writeBlockPos(pos);

		int amount = 0;

		for (String antennaBiome : antennaBiomes)
		{
			if (antennaBiome != null)
			{
				amount++;
			}
		}

		pb.writeInt(amount);

		for (String antennaBiome : antennaBiomes)
		{
			if (antennaBiome != null)
			{
				pb.writeString(antennaBiome);
			}
		}
	}

	@Override
	public void handle(Context ctx)
	{
		ctx.enqueueWork(() -> {
			if (Minecraft.getInstance().world == null)
			{
				return;
			}

			TileEntity te = Minecraft.getInstance().world.getTileEntity(pos);

			if (te instanceof BiomeRadarTileEntity)
			{
				((BiomeRadarTileEntity) te).setAntennaBiomes(antennaBiomes);
			}
		});
	}
}
