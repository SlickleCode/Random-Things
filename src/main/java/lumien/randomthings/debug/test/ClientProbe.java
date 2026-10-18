package lumien.randomthings.debug.test;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

/** Reads what the real client sees; only ever called when the dist is CLIENT, so it is kept out of the server-safe test classes. */
final class ClientProbe
{
	private ClientProbe()
	{
	}

	/** The biome id the client world reports at {@code pos}, or null when the client has no such chunk. */
	static ResourceLocation biomeAt(BlockPos pos)
	{
		Minecraft mc = Minecraft.getInstance();
		return mc.supplyAsync(() -> mc.world == null || !mc.world.isBlockLoaded(pos) ? null : mc.world.getBiome(pos).getRegistryName()).join();
	}
}
