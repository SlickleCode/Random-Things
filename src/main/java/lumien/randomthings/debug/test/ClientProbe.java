package lumien.randomthings.debug.test;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

/** Reads what the real client sees; only ever called when the dist is CLIENT, so it is kept out of the server-safe test classes. */
final class ClientProbe
{
	private ClientProbe()
	{
	}

	/** The biome the client level reports at {@code pos}, or null when the client has no such chunk. */
	static ResourceKey<Biome> biomeAt(BlockPos pos)
	{
		Minecraft mc = Minecraft.getInstance();
		return mc.submit(() -> mc.level == null || !mc.level.hasChunkAt(pos) ? null : mc.level.getBiome(pos).unwrapKey().orElse(null)).join();
	}
}
