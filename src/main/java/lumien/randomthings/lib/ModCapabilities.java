package lumien.randomthings.lib;

import net.neoforged.fml.common.EventBusSubscriber;
import lumien.randomthings.item.EnderBucketItem;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.ReinforcedEnderBucketItem;
import lumien.randomthings.itemhandler.ItemHandlerAdapter;
import lumien.randomthings.tileentity.CreativePlayerInterfaceTileEntity;
import lumien.randomthings.tileentity.IronDropperTileEntity;
import lumien.randomthings.tileentity.ModTileEntityTypes;
import lumien.randomthings.tileentity.PlayerInterfaceTileEntity;
import lumien.randomthings.tileentity.SpectreEnergyInjectorTileEntity;
import lumien.randomthings.util.EnergyStorageHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.fluid.ItemAccessFluidHandler;

/** Capability providers (registered per block entity type / item; NeoForge 21.9+ uses the transaction based resource handlers). */
@EventBusSubscriber
public class ModCapabilities
{
	@SubscribeEvent
	public static void register(RegisterCapabilitiesEvent event)
	{
		event.registerBlockEntity(Capabilities.Item.BLOCK, ModTileEntityTypes.IRON_DROPPER, (IronDropperTileEntity be, net.minecraft.core.Direction side) -> (lumien.randomthings.itemhandler.ItemStackHandler) be.itemHandler());
		event.registerBlockEntity(Capabilities.Item.BLOCK, ModTileEntityTypes.PLAYER_INTERFACE, (PlayerInterfaceTileEntity be, net.minecraft.core.Direction side) -> new ItemHandlerAdapter(be.getItemHandler(side)));
		event.registerBlockEntity(Capabilities.Item.BLOCK, ModTileEntityTypes.CREATIVE_PLAYER_INTERFACE, (CreativePlayerInterfaceTileEntity be, net.minecraft.core.Direction side) -> new ItemHandlerAdapter(be.getItemHandler(side)));
		event.registerBlockEntity(Capabilities.Energy.BLOCK, ModTileEntityTypes.SPECTRE_ENERGY_INJECTOR, (SpectreEnergyInjectorTileEntity be, net.minecraft.core.Direction side) -> new EnergyStorageHandler(be.getEnergyStorage()));
		event.registerItem(Capabilities.Fluid.ITEM, (stack, access) -> new ItemAccessFluidHandler(access, ModDataComponents.FLUID, EnderBucketItem.CAPACITY), ModItems.ENDER_BUCKET);
		event.registerItem(Capabilities.Fluid.ITEM, (stack, access) -> new ItemAccessFluidHandler(access, ModDataComponents.FLUID, ReinforcedEnderBucketItem.CAPACITY), ModItems.REINFORCED_ENDER_BUCKET);
	}
}
