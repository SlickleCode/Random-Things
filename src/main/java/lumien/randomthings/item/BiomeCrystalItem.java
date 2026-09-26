package lumien.randomthings.item;

import lumien.randomthings.lib.IRTItemColor;
import lumien.randomthings.util.BiomeColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

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
    public ITextComponent getDisplayName(ItemStack stack) {
        ITextComponent name = super.getDisplayName(stack);

        Biome biome = getBiome(stack);

        if (biome != null) {
            name = new StringTextComponent(name.getString() + " (" + biome.getDisplayName().getString() + ")");
        }

        return name;
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);

        Biome biome = getBiome(stack);

        if (biome != null) {
            tooltip.add(biome.getDisplayName());
        }
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        Biome biome = getBiome(stack);

        return biome != null && BiomeDictionary.hasType(biome, Type.MAGICAL);
    }

    @Override
    public int getColorFromItemstack(ItemStack stack, int tintIndex) {
        Biome biome = getBiome(stack);

        if (biome != null && Minecraft.getInstance().player != null) {
            return BiomeColorUtil.getBiomeColor(null, biome, Minecraft.getInstance().player.getPosition());
        }

        return 0xFFFFFF;
    }

    public static Biome getBiome(ItemStack stack) {
        CompoundNBT compound = stack.getTag();

        if (compound != null && compound.contains("biomeName")) {
            return ForgeRegistries.BIOMES.getValue(new ResourceLocation(compound.getString("biomeName")));
        }

        return null;
    }

    public static void setBiome(ItemStack stack, Biome biome) {
        CompoundNBT compound = stack.getOrCreateTag();

        compound.putString("biomeName", ForgeRegistries.BIOMES.getKey(biome).toString());
    }
}
