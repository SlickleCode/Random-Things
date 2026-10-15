package lumien.randomthings.item;

import lumien.randomthings.util.RegUtil;
import lumien.randomthings.lib.IRTItemColor;
import lumien.randomthings.util.BiomeColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import lumien.randomthings.util.BiomeUtil;

/**
 * A crystal tuned to one target biome (its registry name, stored in NBT).
 * Inserted into a {@link lumien.randomthings.block.BiomeRadarBlock} to make
 * it search for that biome. Not craftable, same as the 1.12.2 original - it
 * was loot-table/command obtained there too, neither of which this port has
 * built yet, so for now it's creative-menu/give-command only.
 */
public class BiomeCrystalItem extends Item implements IRTItemColor {
    public BiomeCrystalItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        Component name = super.getName(stack);

        Biome biome = getBiome(stack);

        if (biome != null) {
            name = Component.literal(name.getString() + " (" + BiomeUtil.displayName(biome).getString() + ")");
        }

        return name;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);

        Biome biome = getBiome(stack);

        if (biome != null) {
            tooltip.add(BiomeUtil.displayName(biome));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        Biome biome = getBiome(stack);

        return biome != null && BiomeUtil.hasType(biome, "MAGICAL");
    }

    @Override
    public int getColorFromItemstack(ItemStack stack, int tintIndex) {
        Biome biome = getBiome(stack);

        if (biome != null && Minecraft.getInstance().player != null) {
            return BiomeColorUtil.getBiomeColor(null, biome, Minecraft.getInstance().player.blockPosition());
        }

        return 0xFFFFFF;
    }

    public static Biome getBiome(ItemStack stack) {
        CompoundTag compound = stack.getTag();

        if (compound != null && compound.contains("biomeName")) {
            return ForgeRegistries.BIOMES.getValue(new ResourceLocation(compound.getString("biomeName")));
        }

        return null;
    }

    public static void setBiome(ItemStack stack, Biome biome) {
        CompoundTag compound = stack.getOrCreateTag();

        compound.putString("biomeName", BiomeUtil.id(biome).toString());
    }
}
