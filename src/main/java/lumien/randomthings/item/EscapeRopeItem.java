package lumien.randomthings.item;

import lumien.randomthings.util.EscapeRopeHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

/**
 * Hold right-click under open sky (or where you could see it if the block
 * above were cleared) to start {@link EscapeRopeHandler} searching outward
 * from you for the nearest path to daylight, then teleport you there once
 * found. Direct port of 1.12.2's {@code ItemEscapeRope} - the actual search
 * algorithm lives in {@code EscapeRopeHandler}, not this class, matching the
 * original split.
 * <p>
 * Disclosed simplification: the original spawned a custom spiraling yellow
 * smoke particle while charging ({@code EntityColoredSmokeFX}, a bespoke
 * particle class); this port hasn't built custom-particle infrastructure yet
 * (nothing else in this project needs one), so it reuses vanilla's
 * {@code ParticleTypes.PORTAL} for the same "something is gathering" cue
 * instead of adding a new particle type just for this one cosmetic effect.
 */
public class EscapeRopeItem extends Item {
    public EscapeRopeItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, net.minecraft.world.entity.LivingEntity user) {
        return 20 * 60;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public InteractionResult use(Level worldIn, Player playerIn, InteractionHand hand) {
        ItemStack stack = playerIn.getItemInHand(hand);

        if (!worldIn.dimensionType().hasSkyLight() || worldIn.canSeeSkyFromBelowWater(playerIn.blockPosition()) || !worldIn.isEmptyBlock(playerIn.blockPosition())) {
            return InteractionResult.FAIL;
        }

        playerIn.startUsingItem(hand);

        if (!worldIn.isClientSide()) {
            EscapeRopeHandler.getInstance().addTask((ServerPlayer) playerIn);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return Minecraft.getInstance().player != null && Minecraft.getInstance().player.getUseItem() == stack;
    }

    @Override
    public void onUseTick(net.minecraft.world.level.Level level, LivingEntity entity, ItemStack stack, int count) {
        if (!entity.level().isClientSide()) {
            return;
        }

        float alpha = Math.min(1, (getUseDuration(stack, entity) - count) * (1 / 60F));

        for (int i = 0; i < 7; i += 1) {
            for (int c = 0; c < 20; c += 10) {
                double x = Math.sin((count + i * 20) / (10F + c));
                double z = Math.cos((count + i * 20) / (10F + c));
                double y = Math.sin((count + i * 20) / (15F + c));

                entity.level().addParticle(ParticleTypes.PORTAL, entity.getX() + x, entity.getY() + 1 + y, entity.getZ() + z, 0, alpha * 0.1D, 0);
            }
        }
    }
}
