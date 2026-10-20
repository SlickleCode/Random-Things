package lumien.randomthings.item;

import lumien.randomthings.lib.IEntityFilterItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;
import net.minecraft.world.InteractionResult;

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
        CompoundTag tag = filter.getTag();

        if (tag == null || !tag.contains("entityType")) {
            return Optional.empty();
        }

        return EntityType.byString(tag.getString("entityType"));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player playerIn, LivingEntity target, InteractionHand hand) {
        if (!playerIn.level().isClientSide) {
            if (!stack.hasTag()) {
                stack.setTag(new CompoundTag());
            }

            stack.getTag().putString("entityType", EntityType.getKey(target.getType()).toString());
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag advanced) {
        super.appendHoverText(stack, world, tooltip, advanced);

        Optional<EntityType<?>> filterType = getFilterType(stack);

        if (filterType.isPresent()) {
            tooltip.add(filterType.get().getDescription());
        } else {
            tooltip.add(Component.translatable("tooltip.randomthings.entity_filter.invalid_entity"));
        }
    }

    @Override
    public boolean apply(ItemStack me, Entity entity) {
        Optional<EntityType<?>> filterType = getFilterType(me);
        return !filterType.isPresent() || filterType.get() == entity.getType();
    }
}
