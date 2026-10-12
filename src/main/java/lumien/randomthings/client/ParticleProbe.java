package lumien.randomthings.client;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Queue;

import lumien.randomthings.debug.RTDebug;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.SpellParticle;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Dev-only: while a file named {@code rt_probe} exists in the game directory, logs once a second how many potion-swirl particles
 * ({@link SpellParticle}) are within 3 blocks of the player ("[RT-DEBUG] swirl particles near player: N"). The scripted playtest reads that line to check
 * that the Magic Hood really hides the swirl. Registered from {@link DevToolsClient} only.
 */
public final class ParticleProbe
{
	private static Field byTypeField;
	private static int tick;

	private ParticleProbe()
	{
	}

	@SubscribeEvent
	@SuppressWarnings("unchecked")
	public static void onClientTick(TickEvent.ClientTickEvent event)
	{
		Minecraft mc = Minecraft.getInstance();

		if (event.phase != TickEvent.Phase.END || mc.player == null || ++tick % 20 != 0 || !new java.io.File(mc.gameDir, "rt_probe").exists())
		{
			return;
		}

		try
		{
			if (byTypeField == null)
			{
				// the map of render type -> particle queue; found by its type so the (obfuscated) field name does not matter
				for (Field f : ParticleManager.class.getDeclaredFields())
				{
					Type t = f.getGenericType();

					if (Map.class.isAssignableFrom(f.getType()) && t.toString().contains("Queue") && t.toString().contains("IParticleRenderType"))
					{
						f.setAccessible(true);
						byTypeField = f;
						break;
					}
				}
			}

			int count = 0;
			Vec3d p = mc.player.getPositionVec();

			for (Queue<Particle> queue : ((Map<?, Queue<Particle>>) byTypeField.get(mc.particles)).values())
			{
				for (Particle particle : queue)
				{
					if (particle instanceof SpellParticle && particle.getBoundingBox().getCenter().distanceTo(p) < 3.0)
					{
						count++;
					}
				}
			}

			RTDebug.log("swirl particles near player: {}", count);
		}
		catch (Exception e)
		{
			RTDebug.log("particle probe failed: {}", e.toString());
		}
	}
}
