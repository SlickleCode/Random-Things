package lumien.randomthings.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

/** 1.20.5+ replaced {@code PotionUtils} with the {@code minecraft:potion_contents} data component. */
public final class PotionHelper
{
	private PotionHelper()
	{
	}

	/** A potion item holding the given base potion. */
	public static ItemStack potion(Holder<Potion> potion)
	{
		ItemStack stack = new ItemStack(Items.POTION);
		stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
		return stack;
	}

	/** The effects of a potion item (base potion plus custom effects). */
	public static List<MobEffectInstance> effects(ItemStack stack)
	{
		List<MobEffectInstance> effects = new ArrayList<>();
		for (MobEffectInstance effect : stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getAllEffects())
		{
			effects.add(effect);
		}
		return effects;
	}
}
