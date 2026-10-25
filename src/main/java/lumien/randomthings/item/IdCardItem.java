package lumien.randomthings.item;

import lumien.randomthings.util.StackTag;
import com.mojang.authlib.GameProfile;
import lumien.randomthings.lib.IEntityFilterItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
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
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldInCtx, net.minecraft.world.item.component.TooltipDisplay rtDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag flagIn) {
        Level worldIn = lumien.randomthings.client.ClientHooks.level();
        super.appendHoverText(stack, worldInCtx, rtDisplay, tooltip, flagIn);

        CompoundTag tag = StackTag.get(stack);

        if (tag != null && tag.contains("name")) {
            tooltip.accept(Component.literal(tag.getStringOr("name", "")));
        }
    }

    @Override
    public InteractionResult use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        GameProfile profile = playerIn.getGameProfile();

        if (!worldIn.isClientSide()) {
            StackTag.edit(stack, t -> {
                t.putString("uuid", profile.id().toString());
                t.putString("name", profile.name() != null ? profile.name() : "Anonymous");
            });
        }

        return InteractionResult.SUCCESS;
    }

    public static UUID getUUID(ItemStack card) {
        CompoundTag tag = StackTag.get(card);
        return tag != null && tag.contains("uuid") ? UUID.fromString(tag.getStringOr("uuid", "")) : null;
    }

    @Override
    public boolean apply(ItemStack me, Entity entity) {
        if (!(entity instanceof Player)) {
            return false;
        }

        UUID cardUUID = getUUID(me);
        return cardUUID == null || cardUUID.equals(((Player) entity).getGameProfile().id());
    }
}
