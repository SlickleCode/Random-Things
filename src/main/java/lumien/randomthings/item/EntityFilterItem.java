package lumien.randomthings.item;

import lumien.randomthings.lib.IEntityFilterItem;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

/**
 * Right-click (attack) any living entity to "capture" its exact type as this
 * item's filter target; it then matches only entities of that same type
 * wherever an {@link IEntityFilterItem} is accepted.
 * <p>
 * Simplification, disclosed here: 1.12.2 matched via
 * {@code Class.isAssignableFrom} on the raw entity class (so a filter could,
 * in principle, match a whole subclass hierarchy). 1.14.4 replaced ad-hoc
 * entity subclassing with the data-driven {@code EntityType} registry, so
 * this matches by exact {@code EntityType} instead - the natural 1.14.4
 * equivalent, and arguably more correct given how entities are structured
 * in this version.
 */
public class EntityFilterItem extends Item implements IEntityFilterItem {
    public EntityFilterItem(Item.Properties properties) {
        super(properties);
    }

    private static Optional<EntityType<?>> getFilterType(ItemStack filter) {
        CompoundNBT tag = filter.getTag();

        if (tag == null || !tag.contains("entityType")) {
            return Optional.empty();
        }

        return EntityType.byKey(tag.getString("entityType"));
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, PlayerEntity playerIn, LivingEntity target, Hand hand) {
        if (!playerIn.world.isRemote) {
            if (!stack.hasTag()) {
                stack.setTag(new CompoundNBT());
            }

            stack.getTag().putString("entityType", EntityType.getKey(target.getType()).toString());
        }

        return true;
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag advanced) {
        super.addInformation(stack, world, tooltip, advanced);

        Optional<EntityType<?>> filterType = getFilterType(stack);

        if (filterType.isPresent()) {
            tooltip.add(filterType.get().getName());
        } else {
            tooltip.add(new TranslationTextComponent("tooltip.randomthings.entity_filter.invalid_entity"));
        }
    }

    @Override
    public boolean apply(ItemStack me, Entity entity) {
        Optional<EntityType<?>> filterType = getFilterType(me);
        return !filterType.isPresent() || filterType.get() == entity.getType();
    }
}
