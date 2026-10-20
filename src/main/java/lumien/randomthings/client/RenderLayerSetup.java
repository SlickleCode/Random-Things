package lumien.randomthings.client;

import net.minecraft.core.registries.Registries;

import lumien.randomthings.util.RegUtil;
import lumien.randomthings.block.StainedBrickBlock;
import lumien.randomthings.block.AdvancedItemCollectorBlock;
import lumien.randomthings.block.AdvancedRedstoneTorchBlock;
import lumien.randomthings.block.BeanStalkBlock;
import lumien.randomthings.block.BiomeRadarBlock;
import lumien.randomthings.block.ColoredGrassBlock;
import lumien.randomthings.block.DiaphanousBlock;
import lumien.randomthings.block.FlooBrickBlock;
import lumien.randomthings.block.FluidDisplayBlock;
import lumien.randomthings.block.GlowingMushroomBlock;
import lumien.randomthings.block.InventoryTesterBlock;
import lumien.randomthings.block.ItemCollectorBlock;
import lumien.randomthings.block.LapisGlassBlock;
import lumien.randomthings.block.LuminousTranslucentBlock;
import lumien.randomthings.block.PeaceCandleBlock;
import lumien.randomthings.block.PodBlock;
import lumien.randomthings.block.QuartzGlassBlock;
import lumien.randomthings.block.RainShieldBlock;
import lumien.randomthings.block.SlimeCubeBlock;
import lumien.randomthings.block.SpectreBlock;
import lumien.randomthings.block.SpectreCoreBlock;
import lumien.randomthings.block.SticksBlock;
import lumien.randomthings.block.SuperLubricentIceBlock;
import lumien.randomthings.block.SuperLubricentPlatformBlock;
import lumien.randomthings.block.TriggerGlassBlock;
import lumien.randomthings.block.spectretree.SpectreLeafBlock;
import lumien.randomthings.block.spectretree.SpectreLogBlock;
import lumien.randomthings.block.spectretree.SpectrePlankBlock;
import lumien.randomthings.block.spectretree.SpectreSaplingBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 1.16 removed {@code Block#getRenderLayer()}; a block's render layer is now registered on the client through
 * {@link RenderTypeLookup#setRenderLayer}. The layers below are exactly what the 1.14.4 overrides returned.
 */
public class RenderLayerSetup
{
    public static void apply()
    {
        for (Block block : ForgeRegistries.BLOCKS.getValues())
        {
            if (!"randomthings".equals(RegUtil.id(block).getNamespace()))
            {
                continue;
            }

            if (block instanceof StainedBrickBlock)
            {
                if (((StainedBrickBlock) block).isLuminous())
                {
                    ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutoutMipped());
                }
            }
            else if (block instanceof DiaphanousBlock ||
                block instanceof LuminousTranslucentBlock ||
                block instanceof SlimeCubeBlock ||
                block instanceof SpectreBlock ||
                block instanceof SpectreCoreBlock ||
                block instanceof SuperLubricentIceBlock ||
                block instanceof SuperLubricentPlatformBlock ||
                block instanceof TriggerGlassBlock ||
                block instanceof SpectreLeafBlock ||
                block instanceof SpectreLogBlock ||
                block instanceof SpectrePlankBlock ||
                block instanceof SpectreSaplingBlock)
            {
                ItemBlockRenderTypes.setRenderLayer(block, RenderType.translucent());
            }
            else if (block instanceof AdvancedItemCollectorBlock ||
                block instanceof AdvancedRedstoneTorchBlock ||
                block instanceof BeanStalkBlock ||
                block instanceof BiomeRadarBlock ||
                block instanceof ColoredGrassBlock ||
                block instanceof FlooBrickBlock ||
                block instanceof FluidDisplayBlock ||
                block instanceof GlowingMushroomBlock ||
                block instanceof InventoryTesterBlock ||
                block instanceof ItemCollectorBlock ||
                block instanceof LapisGlassBlock ||
                block instanceof PeaceCandleBlock ||
                block instanceof PodBlock ||
                block instanceof QuartzGlassBlock ||
                block instanceof RainShieldBlock ||
                block instanceof SticksBlock)
            {
                ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutout());
            }
        }
    }
}
