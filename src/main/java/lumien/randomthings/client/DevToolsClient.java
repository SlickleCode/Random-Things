package lumien.randomthings.client;

import net.neoforged.neoforge.common.NeoForge;

/** Client half of {@link lumien.randomthings.debug.DevTools}; only loaded in dev instances. */
public final class DevToolsClient
{
	private DevToolsClient()
	{
	}

	public static void init()
	{
		NeoForge.EVENT_BUS.register(GuiDebugScreenshots.class);
		NeoForge.EVENT_BUS.register(GuiOpenTriggerLog.class);
		NeoForge.EVENT_BUS.register(ParticleProbe.class);

		if (Boolean.getBoolean("randomthings.autotest"))
		{
			NeoForge.EVENT_BUS.register(AutoTest.class);
		}
	}
}
