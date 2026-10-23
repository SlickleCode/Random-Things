package lumien.randomthings.client;

import net.minecraft.core.registries.Registries;

import lumien.randomthings.util.RegUtil;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lumien.randomthings.debug.RTDebug;
import lumien.randomthings.lib.ModConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * Static audit of the mod's registered content against its assets and data, run by
 * {@link AutoTest} (or directly via {@link #run()}):
 * <ul>
 * <li><b>models</b> - every block has a blockstate file whose models exist (parent chain included);
 * every item has an item model; item model {@code overrides} resolve too</li>
 * <li><b>textures</b> - every texture a model resolves to exists as a png, and no {@code #variable}
 * is left unresolved</li>
 * <li><b>recipes</b> - every recipe file under {@code data/randomthings/recipe} actually loaded,
 * outputs and ingredients are non-empty, and every item/block has at least one recipe that
 * produces it unless it is in {@link #NO_RECIPE_EXPECTED}</li>
 * </ul>
 * Results go to the {@code [RT-DEBUG]} log and to {@code run/randomthings_audit.txt}.
 */
public class ResourceAudit
{
	/** Items/blocks that are intentionally not craftable (loot, creative-only, spawn eggs, placed-only, ...). */
	private static final Set<String> NO_RECIPE_EXPECTED = new HashSet<>();

	static
	{
		// none of these has a crafting recipe in 1.12.2 either (checked against ModRecipes + assets/recipes):
		// loot / worldgen / mob drops / grown / imbuing-station results / ritual or creative-only items
		for (String s : new String[] { "beans", "magic_bean", "lotus_blossom", "lotus_seeds", "ectoplasm", "bottle_of_air", "compressed_slime_block", "spectre_block", "spectre_log", "spectre_leaf", "spectre_sapling", "glowing_mushroom", "colored_grass", "biome_crystal", "rune_pattern", "imbue_fire", "imbue_poison", "imbue_experience", "imbue_wither", "summoning_pendulum", "golden_egg", "creative_player_interface", "magic_hood", "peace_candle", "ancient_furnace", "spectre_coil_genesis", "spectre_charger_genesis" })
		{
			NO_RECIPE_EXPECTED.add(s);
		}
	}

	public final List<String> failures = new ArrayList<>();
	public final List<String> warnings = new ArrayList<>();
	public int blocks, items, recipes;

	private final ResourceManager assets;
	private final ResourceManager data;
	private final Map<ResourceLocation, JsonObject> modelCache = new HashMap<>();

	private ResourceAudit(ResourceManager assets, ResourceManager data)
	{
		this.assets = assets;
		this.data = data;
	}

	public static ResourceAudit run()
	{
		Minecraft mc = Minecraft.getInstance();
		ResourceAudit audit = new ResourceAudit(mc.getResourceManager(), mc.getSingleplayerServer() != null ? mc.getSingleplayerServer().getServerResources().resourceManager() : mc.getResourceManager());

		audit.auditBlocks();
		audit.auditItems();
		audit.auditRecipes();
		audit.auditInjectedLootTables();
		audit.report();
		return audit;
	}

	// ---------------------------------------------------------------- models / textures

	private void auditBlocks()
	{
		for (Block block : BuiltInRegistries.BLOCK)
		{
			ResourceLocation id = RegUtil.id(block);

			if (id == null || !ModConstants.MOD_ID.equals(id.getNamespace()))
			{
				continue;
			}

			blocks++;
			ResourceLocation stateFile = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "blockstates/" + id.getPath() + ".json");
			JsonObject state = readJson(assets, stateFile);

			if (state == null)
			{
				failures.add("[model] block " + id + ": missing/unreadable " + stateFile);
				continue;
			}

			Set<String> models = new TreeSet<>();

			if (state.has("variants"))
			{
				for (Map.Entry<String, JsonElement> e : state.getAsJsonObject("variants").entrySet())
				{
					collectVariantModels(e.getValue(), models);
				}
			}

			if (state.has("multipart"))
			{
				for (JsonElement part : state.getAsJsonArray("multipart"))
				{
					collectVariantModels(part.getAsJsonObject().get("apply"), models);
				}
			}

			if (models.isEmpty())
			{
				failures.add("[model] block " + id + ": blockstate references no models");
			}

			for (String model : models)
			{
				checkModel("block " + id, model);
			}
		}
	}

	private void auditItems()
	{
		for (Item item : BuiltInRegistries.ITEM)
		{
			ResourceLocation id = RegUtil.id(item);

			if (id == null || !ModConstants.MOD_ID.equals(id.getNamespace()))
			{
				continue;
			}

			items++;
			checkModel("item " + id, id.getNamespace() + ":item/" + id.getPath());
		}
	}

	private void collectVariantModels(JsonElement variant, Set<String> out)
	{
		if (variant == null)
		{
			return;
		}

		if (variant.isJsonArray())
		{
			for (JsonElement e : variant.getAsJsonArray())
			{
				collectVariantModels(e, out);
			}
		}
		else if (variant.isJsonObject() && variant.getAsJsonObject().has("model"))
		{
			out.add(variant.getAsJsonObject().get("model").getAsString());
		}
	}

	/** Checks the model exists, its parent chain exists, all textures resolve and exist, and recurses into overrides. */
	private void checkModel(String owner, String model)
	{
		checkModel(owner, model, new HashSet<>());
	}

	private void checkModel(String owner, String model, Set<String> seen)
	{
		if (!seen.add(model))
		{
			return;
		}

		ResourceLocation modelId = ResourceLocation.parse(model);
		Map<String, String> textures = new HashMap<>();
		Set<String> usedVars = new HashSet<>();
		boolean builtinEntity = false;
		boolean generated = false;
		List<String> overrides = new ArrayList<>();

		// walk child -> parent; child texture entries win
		ResourceLocation current = modelId;
		boolean first = true;

		for (int depth = 0; current != null && depth < 20; depth++)
		{
			String path = current.getPath();

			if (path.equals("builtin/generated"))
			{
				generated = true;
				break;
			}
			else if (path.equals("builtin/entity"))
			{
				builtinEntity = true;
				break;
			}
			else if (path.equals("builtin/missing"))
			{
				break;
			}

			JsonObject json = modelJson(current);

			if (json == null)
			{
				failures.add("[model] " + owner + ": " + (first ? "model" : "parent model") + " " + current + " not found (referenced via " + model + ")");
				return;
			}

			if (json.has("textures"))
			{
				for (Map.Entry<String, JsonElement> e : json.getAsJsonObject("textures").entrySet())
				{
					textures.putIfAbsent(e.getKey(), e.getValue().getAsString());
				}
			}

			if (json.has("elements"))
			{
				for (JsonElement el : json.getAsJsonArray("elements"))
				{
					JsonObject faces = el.getAsJsonObject().has("faces") ? el.getAsJsonObject().getAsJsonObject("faces") : new JsonObject();

					for (Map.Entry<String, JsonElement> face : faces.entrySet())
					{
						JsonObject f = face.getValue().getAsJsonObject();

						if (f.has("texture"))
						{
							String t = f.get("texture").getAsString();

							if (t.startsWith("#"))
							{
								usedVars.add(t.substring(1));
							}
							else
							{
								textures.putIfAbsent("$literal:" + t, t);
							}
						}
					}
				}
			}

			if (first && json.has("overrides"))
			{
				for (JsonElement o : json.getAsJsonArray("overrides"))
				{
					overrides.add(o.getAsJsonObject().get("model").getAsString());
				}
			}

			current = json.has("parent") ? ResourceLocation.parse(json.get("parent").getAsString()) : null;
			first = false;
		}

		if (builtinEntity)
		{
			// rendered by code (TEISR); there is no texture list to verify
			return;
		}

		Set<String> toCheck = new TreeSet<>();

		for (Map.Entry<String, String> e : textures.entrySet())
		{
			String value = e.getValue();
			int hops = 0;

			while (value.startsWith("#") && hops++ < 10)
			{
				value = textures.getOrDefault(value.substring(1), value);

				if (value.startsWith("#") && !textures.containsKey(value.substring(1)))
				{
					break;
				}
			}

			if (value.startsWith("#"))
			{
				// an unresolved variable only matters if some face / layer actually uses it
				if (usedVars.contains(e.getKey()) || generated)
				{
					failures.add("[texture] " + owner + " (" + model + "): texture variable '" + e.getKey() + "' unresolved (" + e.getValue() + ")");
				}
			}
			else
			{
				toCheck.add(value);
			}
		}

		for (String var : usedVars)
		{
			if (!textures.containsKey(var))
			{
				failures.add("[texture] " + owner + " (" + model + "): face uses undefined texture variable '#" + var + "'");
			}
		}

		for (String tex : toCheck)
		{
			ResourceLocation tid = ResourceLocation.parse(tex);

			if (assets.getResource(ResourceLocation.fromNamespaceAndPath(tid.getNamespace(), "textures/" + tid.getPath() + ".png")).isEmpty())
			{
				failures.add("[texture] " + owner + " (" + model + "): missing texture " + tex);
			}
		}

		for (String override : overrides)
		{
			checkModel(owner + " override", override, seen);
		}
	}

	private JsonObject modelJson(ResourceLocation id)
	{
		return modelCache.computeIfAbsent(id, k -> {
			ResourceLocation file = ResourceLocation.fromNamespaceAndPath(k.getNamespace(), "models/" + k.getPath() + ".json");

			if (ModConstants.MOD_ID.equals(k.getNamespace()))
			{
				verifyParses(file);
			}

			return readJson(assets, file);
		});
	}

	/** Runs the game's own model deserializer: catches schema mistakes (e.g. scalar where an array is required) that valid-JSON checks miss. */
	private void verifyParses(ResourceLocation file)
	{
		try (BufferedReader reader = assets.getResource(file).orElseThrow().openAsReader())
		{
			BlockModel.fromStream(reader);
		}
		catch (java.io.FileNotFoundException e)
		{
			// reported as "model not found" by the caller
		}
		catch (Exception e)
		{
			failures.add("[model] " + file + ": rejected by the game's model parser: " + e.getMessage());
		}
	}

	// ---------------------------------------------------------------- recipes

	private void auditRecipes()
	{
		Minecraft mc = Minecraft.getInstance();

		if (mc.getSingleplayerServer() == null)
		{
			warnings.add("[recipe] no integrated server, recipe audit skipped");
			return;
		}

		Collection<net.minecraft.world.item.crafting.RecipeHolder<?>> all = mc.getSingleplayerServer().getRecipeManager().getRecipes();
		Set<ResourceLocation> loaded = new HashSet<>();
		Set<Item> produced = new HashSet<>();

		for (net.minecraft.world.item.crafting.RecipeHolder<?> holder : all)
		{
			Recipe<?> recipe = holder.value();
			ResourceLocation id = holder.id().location();
			ItemStack out = lumien.randomthings.debug.test.RecipeProbe.resultOf(recipe, mc.level.registryAccess());

			if (!out.isEmpty())
			{
				produced.add(out.getItem());
			}

			if (!ModConstants.MOD_ID.equals(id.getNamespace()))
			{
				continue;
			}

			loaded.add(id);
			recipes++;

			if (recipe.isSpecial())
			{
				continue;
			}

			if (out.isEmpty())
			{
				failures.add("[recipe] " + id + ": empty output");
			}

			for (java.util.Optional<Ingredient> optional : lumien.randomthings.debug.test.RecipeProbe.ingredientsOf(recipe))
			{
				if (optional.isPresent() && optional.get().items().findAny().isEmpty())
				{
					failures.add("[recipe] " + id + ": ingredient matches no items (bad item/tag?)");
					break;
				}
			}
		}

		// every recipe file must have been accepted by the RecipeManager (a parse failure silently drops it)
		for (ResourceLocation file : data.listResources("recipe", s -> s.getPath().endsWith(".json")).keySet())
		{
			if (!ModConstants.MOD_ID.equals(file.getNamespace()))
			{
				continue;
			}

			String path = file.getPath();
			ResourceLocation id = ResourceLocation.fromNamespaceAndPath(file.getNamespace(), path.substring("recipe/".length(), path.length() - ".json".length()));

			if (!loaded.contains(id))
			{
				failures.add("[recipe] " + id + ": file present but recipe did not load (see log for parse error)");
			}
		}

		for (Item item : BuiltInRegistries.ITEM)
		{
			ResourceLocation id = RegUtil.id(item);

			if (id != null && ModConstants.MOD_ID.equals(id.getNamespace()) && !produced.contains(item) && !NO_RECIPE_EXPECTED.contains(id.getPath()))
			{
				failures.add("[recipe] " + id + ": no recipe produces this item (add it to ResourceAudit.NO_RECIPE_EXPECTED if intentional)");
			}
		}
	}

	/** The vanilla chest tables LootHandler injects into must exist, or the injection silently never happens. */
	private void auditInjectedLootTables()
	{
		Minecraft mc = Minecraft.getInstance();

		if (mc.getSingleplayerServer() == null)
		{
			return;
		}

		Set<ResourceLocation> known = new HashSet<>(mc.getSingleplayerServer().reloadableRegistries().getKeys(net.minecraft.core.registries.Registries.LOOT_TABLE));

		for (ResourceLocation table : new ResourceLocation[] { lumien.randomthings.handler.LootHandler.SIMPLE_DUNGEON, lumien.randomthings.handler.LootHandler.VILLAGE_WEAPONSMITH, lumien.randomthings.handler.LootHandler.STRONGHOLD_CORRIDOR, lumien.randomthings.handler.LootHandler.JUNGLE_TEMPLE })
		{
			if (!known.contains(table))
			{
				failures.add("[loot] LootHandler injects into " + table + " but no such loot table exists");
			}
		}
	}

	// ---------------------------------------------------------------- util

	private static JsonObject readJson(ResourceManager manager, ResourceLocation file)
	{
		try (BufferedReader reader = manager.getResource(file).orElseThrow().openAsReader())
		{
			return new JsonParser().parse(reader).getAsJsonObject();
		}
		catch (Exception e)
		{
			return null;
		}
	}

	private void report()
	{
		List<String> lines = new ArrayList<>();
		lines.add("Random Things resource audit: " + blocks + " blocks, " + items + " items, " + recipes + " recipes");
		lines.add(failures.size() + " failure(s), " + warnings.size() + " warning(s)");
		failures.forEach(f -> lines.add("FAIL " + f));
		warnings.forEach(w -> lines.add("WARN " + w));

		lines.forEach(l -> RTDebug.log("Audit: {}", l));

		try
		{
			java.nio.file.Files.write(java.nio.file.Paths.get("randomthings_audit.txt"), lines, StandardCharsets.UTF_8);
		}
		catch (Exception e)
		{
			RTDebug.log("Audit: could not write report file: {}", e.toString());
		}
	}
}
