package lumien.randomthings.item;

import com.mojang.authlib.GameProfile;
import lumien.randomthings.lib.IEntityFilterItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

/**
 * Right-click to bind this card to yourself (stores your own UUID/name);
 * usable wherever an {@link IEntityFilterItem} is accepted to mean "only
 * this specific player".
 */
public class IdCardItem extends Item implements IEntityFilterItem {
    public IdCardItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        CompoundTag tag = stack.getTag();

        if (tag != null && tag.contains("name")) {
            tooltip.add(new TextComponent(tag.getString("name")));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        GameProfile profile = playerIn.getGameProfile();

        if (!worldIn.isClientSide) {
            if (!stack.hasTag()) {
                stack.setTag(new CompoundTag());
            }

            stack.getTag().putString("uuid", profile.getId().toString());
            stack.getTag().putString("name", profile.getName() != null ? profile.getName() : "Anonymous");
        }

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    public static UUID getUUID(ItemStack card) {
        CompoundTag tag = card.getTag();
        return tag != null && tag.contains("uuid") ? UUID.fromString(tag.getString("uuid")) : null;
    }

    @Override
    public boolean apply(ItemStack me, Entity entity) {
        if (!(entity instanceof Player)) {
            return false;
        }

        UUID cardUUID = getUUID(me);
        return cardUUID == null || cardUUID.equals(((Player) entity).getGameProfile().getId());
    }
}
