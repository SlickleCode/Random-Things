package lumien.randomthings.handler;

import java.util.Collections;
import java.util.List;

import com.google.gson.JsonObject;

import lumien.randomthings.config.RTConfig;
import lumien.randomthings.enchantment.ModEnchantments;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * Magnetic enchantment: block drops produced for a harvester holding a Magnetic tool go straight
 * into that harvester's inventory. 1.12.2 did this with an ASM "ItemCatcher" patch; 1.14.4 Forge
 * (28.2.26) never fires {@code BlockEvent.HarvestDropsEvent} at all, so the equivalent is a global
 * loot modifier (registered in {@code data/forge/loot_modifiers/global_loot_modifiers.json}), which
 * sees every block loot roll together with the harvesting entity and the tool used.
 * Block Breaker relies on this too: its fake player carries a Magnetic pickaxe.
 */
public class MagneticLootModifier extends LootModifier
{
	public MagneticLootModifier(LootItemCondition[] conditions)
	{
		super(conditions);
	}

	@Override
	protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context)
	{
		// block loot with a harvester has both parameters; entity / chest / fishing loot lacks the tool
		if (!context.hasParam(LootContextParams.TOOL) || !context.hasParam(LootContextParams.THIS_ENTITY) || !RTConfig.MAGNETIC_ENCHANTMENT.get())
		{
			return generatedLoot;
		}

		Entity harvester = context.getParamOrNull(LootContextParams.THIS_ENTITY);
		ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);

		if (!(harvester instanceof Player) || tool == null || EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.MAGNETIC, tool) <= 0)
		{
			return generatedLoot;
		}

		for (ItemStack drop : generatedLoot)
		{
			ItemHandlerHelper.giveItemToPlayer((Player) harvester, drop);
		}

		return Collections.emptyList();
	}

	public static class Serializer extends GlobalLootModifierSerializer<MagneticLootModifier>
	{
		@Override
		public MagneticLootModifier read(ResourceLocation location, JsonObject object, LootItemCondition[] conditions)
		{
			return new MagneticLootModifier(conditions);
		}

		@Override
		public JsonObject write(MagneticLootModifier instance)
		{
			return this.makeConditions(instance.conditions);
		}
	}
}
