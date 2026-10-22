package lumien.randomthings.handler;


import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import lumien.randomthings.config.RTConfig;
import lumien.randomthings.enchantment.ModEnchantments;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.items.ItemHandlerHelper;

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
	protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context)
	{
		// block loot with a harvester has both parameters; entity / chest / fishing loot lacks the tool
		if (!context.hasParam(LootContextParams.TOOL) || !context.hasParam(LootContextParams.THIS_ENTITY) || !RTConfig.MAGNETIC_ENCHANTMENT.get())
		{
			return generatedLoot;
		}

		Entity harvester = context.getParamOrNull(LootContextParams.THIS_ENTITY);
		ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);

		if (!(harvester instanceof Player) || tool == null || EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.holder(context.getLevel().registryAccess(), ModEnchantments.MAGNETIC), tool) <= 0)
		{
			return generatedLoot;
		}

		for (ItemStack drop : generatedLoot)
		{
			ItemHandlerHelper.giveItemToPlayer((Player) harvester, drop);
		}

		return new ObjectArrayList<>();
	}

	public static final MapCodec<MagneticLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, MagneticLootModifier::new));

	@Override
	public MapCodec<? extends net.neoforged.neoforge.common.loot.IGlobalLootModifier> codec()
	{
		return CODEC;
	}
}
