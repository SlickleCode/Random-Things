package lumien.randomthings.item;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.Rarity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
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
    public int getBarColor(ItemStack stack) {
        return lumien.randomthings.util.DyeUtil.rgb(DyeColor.PURPLE);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * (1.0F - (float) (1 - 1F / 5F * getEntityCount(stack))));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return getEntityCount(stack) == 5;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getEntityCount(stack) != 5;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag advanced) {
        int entityCount = getEntityCount(stack);

        if (Screen.hasShiftDown() && entityCount != 0) {
            ListTag tagList = stack.getTag().getList("entitys", 10);

            for (int i = 0; i < tagList.size(); i++) {
                ResourceLocation entityLocation = new ResourceLocation(tagList.getCompound(i).getString("id"));
                EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(entityLocation);

                if (type != null) {
                    tooltip.add(Component.literal("- ").append(type.getDescription()));
                }
            }
            return;
        }

        tooltip.add(Component.translatable(entityCount == 1 ? "tooltip.randomthings.summoning_pendulum.entity_count.singular" : "tooltip.randomthings.summoning_pendulum.entity_count.plural", entityCount));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if (entity.level.isClientSide) {
            return InteractionResult.PASS;
        }

        if (!(entity instanceof Enemy || entity instanceof Player)) {
            ItemStack heldStack = player.getMainHandItem();
            CompoundTag compound = heldStack.hasTag() ? heldStack.getTag() : new CompoundTag();

            ListTag tagList = compound.getList("entitys", 10);
            if (tagList.size() < 5) {
                CompoundTag entityNBT = new CompoundTag();
                entity.save(entityNBT);
                tagList.add(entityNBT);
                entity.discard();
                entity.level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5f, 1.5F);
            } else {
                entity.level.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.5f, 1.5F);
            }

            compound.put("entitys", tagList);
            heldStack.setTag(compound);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        Level world = context.getLevel();
        Player player = context.getPlayer();

        if (!world.isClientSide && player != null) {
            CompoundTag compound = stack.getTag();
            if (compound != null) {
                ListTag tagList = compound.getList("entitys", 10);
                if (tagList.size() > 0) {
                    CompoundTag entityNBT = tagList.getCompound(0);
                    tagList.remove(0);


                    EntityType.create(entityNBT, world).ifPresent(entity -> {
                        entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                        world.addFreshEntity(entity);
                        player.level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5f, 0.5F);
                    });
                } else {
                    player.level.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.5f, 0.2F);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.RARE;
    }
}
