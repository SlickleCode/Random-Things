package lumien.randomthings.item;

import lumien.randomthings.handler.ModDimensions;
import lumien.randomthings.handler.spectre.SpectreHandler;
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
 * Direct port of 1.12.2's {@code ItemSpectreKey}: hold right-click for 5
 * seconds (100 ticks, matching the original's {@code getMaxItemUseDuration})
 * to teleport into your own private {@link lumien.randomthings.handler.spectre.SpectreCube}
 * room, or back out again if already there. Disclosed simplification: the
 * original's charge-up effect was a bespoke tinted smoke particle
 * ({@code EntityColoredSmokeFX}); this port hasn't built custom-particle
 * infrastructure (see {@link EscapeRopeItem}'s own javadoc for the same
 * precedent/reasoning), so it reuses vanilla's {@code ParticleTypes.PORTAL}
 * instead.
 */
public class SpectreKeyItem extends Item {
    public SpectreKeyItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, net.minecraft.world.entity.LivingEntity user) {
        return 100;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public InteractionResult use(Level worldIn, Player playerIn, InteractionHand hand) {
        ItemStack stack = playerIn.getItemInHand(hand);
        playerIn.startUsingItem(hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity livingEntity) {
        if (!worldIn.isClientSide() && livingEntity instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) livingEntity;
            SpectreHandler spectreHandler = SpectreHandler.getInstance(player.level().getServer());

            if (spectreHandler != null) {
                if (!ModDimensions.isSpectre(player.level())) {
                    spectreHandler.teleportPlayerToSpectreCube(player);
                } else {
                    spectreHandler.teleportPlayerBack(player);
                }
            }
        }

        return stack;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return Minecraft.getInstance().player != null && ModDimensions.isSpectre(Minecraft.getInstance().player.level());
    }

    @Override
    public void onUseTick(net.minecraft.world.level.Level level, LivingEntity entity, ItemStack stack, int count) {
        if (!entity.level().isClientSide()) {
            return;
        }

        for (int i = 0; i < (100 - count) / 5; i++) {
            double x = entity.getX() + Math.random() * 1.8 - 0.9;
            double y = entity.getY() + Math.random() * 1.8;
            double z = entity.getZ() + Math.random() * 1.8 - 0.9;

            entity.level().addParticle(ParticleTypes.PORTAL, x, y, z, 0, 0, 0);
        }
    }
}
