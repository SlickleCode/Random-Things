package lumien.randomthings.lib;

import net.neoforged.fml.common.EventBusSubscriber;
import lumien.randomthings.item.EnderBucketItem;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.ReinforcedEnderBucketItem;
import lumien.randomthings.tileentity.CreativePlayerInterfaceTileEntity;
import lumien.randomthings.tileentity.IronDropperTileEntity;
import lumien.randomthings.tileentity.ModTileEntityTypes;
import lumien.randomthings.tileentity.PlayerInterfaceTileEntity;
import lumien.randomthings.tileentity.SpectreEnergyInjectorTileEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStackSimple;

/** Capability providers (1.20.6: registered per block entity type / item instead of overriding {@code getCapability}). */
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class ModCapabilities
{
	@SubscribeEvent
	public static void register(RegisterCapabilitiesEvent event)
	{
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModTileEntityTypes.IRON_DROPPER, (IronDropperTileEntity be, net.minecraft.core.Direction side) -> be.itemHandler());
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModTileEntityTypes.PLAYER_INTERFACE, (PlayerInterfaceTileEntity be, net.minecraft.core.Direction side) -> be.getItemHandler(side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModTileEntityTypes.CREATIVE_PLAYER_INTERFACE, (CreativePlayerInterfaceTileEntity be, net.minecraft.core.Direction side) -> be.getItemHandler(side));
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModTileEntityTypes.SPECTRE_ENERGY_INJECTOR, (SpectreEnergyInjectorTileEntity be, net.minecraft.core.Direction side) -> be.getEnergyStorage());
		event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new FluidHandlerItemStackSimple(() -> ModDataComponents.FLUID, stack, EnderBucketItem.CAPACITY), ModItems.ENDER_BUCKET);
		event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new FluidHandlerItemStack(() -> ModDataComponents.FLUID, stack, ReinforcedEnderBucketItem.CAPACITY), ModItems.REINFORCED_ENDER_BUCKET);
	}
}
