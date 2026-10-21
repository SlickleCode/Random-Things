package lumien.randomthings.debug;

import lumien.randomthings.debug.test.RTTestRunner;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.DistExecutor;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

/** Registers all developer-only hooks. Only ever called when {@link DevMode#ENABLED}. */
public final class DevTools
{
	private DevTools()
	{
	}

	public static void init()
	{
		RTDebug.log("Developer tools enabled (-Drandomthings.debug=true)");

		NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.RegisterCommandsEvent event) -> RTDebugCommand.register(event.getDispatcher()));
		NeoForge.EVENT_BUS.addListener(RTTestRunner::onServerTick);
		lumien.randomthings.debug.test.ServerAutoTest.init();

		DistExecutor.runWhenOn(Dist.CLIENT, () -> lumien.randomthings.client.DevToolsClient::init);
	}
}
