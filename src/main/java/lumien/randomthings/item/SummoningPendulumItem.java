package lumien.randomthings.item;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * Right-click a passive (non-hostile, non-player) mob to capture it into the
 * pendulum's NBT (up to 5, FIFO); right-click a block to release the oldest
 * captured entity there. Shows capture count as a purple durability bar and
 * glows once full. Direct port of 1.12.2's {@code ItemSummoningPendulum}.
 */
public class SummoningPendulumItem extends Item {
    public SummoningPendulumItem(Item.Properties properties) {
        super(properties);
    }

    private static int getEntityCount(ItemStack stack) {
        if (!stack.hasTag()) {
            return 0;
        }

        return stack.getTag().getList("entitys", 10).size();
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return DyeColor.PURPLE.getColorValue();
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1 - 1F / 5F * getEntityCount(stack);
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return getEntityCount(stack) == 5;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return getEntityCount(stack) != 5;
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag advanced) {
        int entityCount = getEntityCount(stack);

        if (Screen.hasShiftDown() && entityCount != 0) {
            ListNBT tagList = stack.getTag().getList("entitys", 10);

            for (int i = 0; i < tagList.size(); i++) {
                ResourceLocation entityLocation = new ResourceLocation(tagList.getCompound(i).getString("id"));
                EntityType<?> type = ForgeRegistries.ENTITIES.getValue(entityLocation);

                if (type != null) {
                    tooltip.add(new StringTextComponent("- ").appendSibling(type.getName()));
                }
            }
            return;
        }

        tooltip.add(new TranslationTextComponent(entityCount == 1 ? "tooltip.randomthings.summoning_pendulum.entity_count.singular" : "tooltip.randomthings.summoning_pendulum.entity_count.plural", entityCount));
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
        if (entity.world.isRemote) {
            return false;
        }

        if (!(entity instanceof IMob || entity instanceof PlayerEntity)) {
            ItemStack heldStack = player.getHeldItemMainhand();
            CompoundNBT compound = heldStack.hasTag() ? heldStack.getTag() : new CompoundNBT();

            ListNBT tagList = compound.getList("entitys", 10);
            if (tagList.size() < 5) {
                CompoundNBT entityNBT = new CompoundNBT();
                entity.writeUnlessPassenger(entityNBT);
                tagList.add(entityNBT);
                entity.remove();
                entity.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.5f, 1.5F);
            } else {
                entity.world.playSound(null, player.getPosition(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 0.5f, 1.5F);
            }

            compound.put("entitys", tagList);
            heldStack.setTag(compound);
            return true;
        }
        return true;
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        ItemStack stack = context.getItem();
        BlockPos pos = context.getPos().offset(context.getFace());
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();

        if (!world.isRemote && player != null) {
            CompoundNBT compound = stack.getTag();
            if (compound != null) {
                ListNBT tagList = compound.getList("entitys", 10);
                if (tagList.size() > 0) {
                    CompoundNBT entityNBT = tagList.getCompound(0);
                    tagList.remove(0);

                    entityNBT.putInt("Dimension", world.getDimension().getType().getId());

                    EntityType.loadEntityUnchecked(entityNBT, world).ifPresent(entity -> {
                        entity.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                        world.addEntity(entity);
                        player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.5f, 0.5F);
                    });
                } else {
                    player.world.playSound(null, player.getPosition(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 0.5f, 0.2F);
                }
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.RARE;
    }
}
