package lumien.randomthings.item;

import lumien.randomthings.handler.ModDimensions;
import lumien.randomthings.handler.spectre.SpectreHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

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
    public int getUseDuration(ItemStack stack) {
        return 100;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
        ItemStack stack = playerIn.getHeldItem(hand);
        playerIn.setActiveHand(hand);
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, LivingEntity livingEntity) {
        if (!worldIn.isRemote && livingEntity instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) livingEntity;
            SpectreHandler spectreHandler = SpectreHandler.getInstance(player.getServer());

            if (spectreHandler != null) {
                if (player.dimension != ModDimensions.SPECTRE_TYPE) {
                    spectreHandler.teleportPlayerToSpectreCube(player);
                } else {
                    spectreHandler.teleportPlayerBack(player);
                }
            }
        }

        return stack;
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return Minecraft.getInstance().player != null && Minecraft.getInstance().player.world.getDimension().getType() == ModDimensions.SPECTRE_TYPE;
    }

    @Override
    public void onUsingTick(ItemStack stack, LivingEntity entity, int count) {
        if (!entity.world.isRemote) {
            return;
        }

        for (int i = 0; i < (100 - count) / 5; i++) {
            double x = entity.posX + Math.random() * 1.8 - 0.9;
            double y = entity.posY + Math.random() * 1.8;
            double z = entity.posZ + Math.random() * 1.8 - 0.9;

            entity.world.addParticle(ParticleTypes.PORTAL, x, y, z, 0, 0, 0);
        }
    }
}
