package lumien.randomthings.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lumien.randomthings.debug.HookProbe;
import lumien.randomthings.debug.RTDebug;

/**
 * Developer-only: during the render scene run ({@code RT_AUTOTEST_SCENES=true}) asserts that the coremod hooks a scene exists to
 * show were really hit while it rendered (see {@link HookProbe}); results go to {@code randomthings_scenes.txt} in the game directory.
 */
final class SceneHookChecks
{
	private static final Map<String, String[]> EXPECT = new LinkedHashMap<>();
	private static final List<String> LINES = new ArrayList<>();
	private static int passed;
	private static int failed;

	static
	{
		EXPECT.put("armor_and_items", new String[] { "armor_dye_applied", "item_dye_applied", "glint_tint_applied", "glint_tint_armor", "glint_modulated" });
		EXPECT.put("armor_glint", new String[] { "glint_tint_armor" });
		EXPECT.put("light_redirector", new String[] { "light_redirect_applied" });
		EXPECT.put("magic_hood", new String[] { "hood_name_hidden" });
		EXPECT.put("rain_with_shield", new String[] { "rain_shield_render" });
	}

	private SceneHookChecks()
	{
	}

	static void check(String scene, Map<String, Integer> before)
	{
		String[] hooks = EXPECT.get(scene);

		if (hooks == null || before == null)
		{
			return;
		}

		for (String hook : hooks)
		{
			int delta = HookProbe.count(hook) - before.getOrDefault(hook, 0);

			if (delta > 0)
			{
				passed++;
				LINES.add("ok   " + scene + ": " + hook + " x" + delta);
			}
			else
			{
				failed++;
				LINES.add("FAIL " + scene + ": the coremod hook " + hook + " was never hit while the scene rendered");
			}
		}

		for (Map.Entry<String, Integer> e : HookProbe.snapshot().entrySet())
		{
			if (e.getKey().startsWith("glint_modulated:"))
			{
				LINES.add("info " + scene + ": " + e.getKey() + " x" + e.getValue());
			}
		}

		List<String> out = new ArrayList<>();
		out.add("Random Things render hook checks: " + passed + " passed, " + failed + " failed");
		out.addAll(LINES);

		try
		{
			Files.write(Paths.get("randomthings_scenes.txt"), out, StandardCharsets.UTF_8);
		}
		catch (IOException e)
		{
			RTDebug.log("SceneHookChecks: cannot write the report: {}", e);
		}

		RTDebug.log("SceneHookChecks: {} passed, {} failed", passed, failed);
	}
}
