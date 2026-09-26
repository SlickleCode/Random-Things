package lumien.randomthings.item;

import com.mojang.authlib.GameProfile;
import lumien.randomthings.lib.IEntityFilterItem;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

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
    public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        CompoundNBT tag = stack.getTag();

        if (tag != null && tag.contains("name")) {
            tooltip.add(new StringTextComponent(tag.getString("name")));
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        GameProfile profile = playerIn.getGameProfile();

        if (!worldIn.isRemote) {
            if (!stack.hasTag()) {
                stack.setTag(new CompoundNBT());
            }

            stack.getTag().putString("uuid", profile.getId().toString());
            stack.getTag().putString("name", profile.getName() != null ? profile.getName() : "Anonymous");
        }

        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    public static UUID getUUID(ItemStack card) {
        CompoundNBT tag = card.getTag();
        return tag != null && tag.contains("uuid") ? UUID.fromString(tag.getString("uuid")) : null;
    }

    @Override
    public boolean apply(ItemStack me, Entity entity) {
        if (!(entity instanceof PlayerEntity)) {
            return false;
        }

        UUID cardUUID = getUUID(me);
        return cardUUID == null || cardUUID.equals(((PlayerEntity) entity).getGameProfile().getId());
    }
}
