package lumien.randomthings.client.renderer;

import lumien.randomthings.item.PortkeyItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * Draws whatever item a {@link PortkeyItem} is disguised as ("camo" NBT) in
 * its place, in every context ({@code Item.getTileEntityItemStackRenderer()}
 * is checked by {@code ItemRenderer.renderItem} itself - confirmed via
 * {@code javap -c} - so this fires for inventory slots, held-in-hand,
 * dropped-on-ground and item-frame rendering alike, the same mechanism
 * vanilla uses for shulker boxes). Direct port of 1.12.2's camo feature
 * ({@code PortKeyMesh}), which instead reached into 1.12.2's own internal
 * item-model registry map via reflection ({@code ReflectionUtil.getModelMap()})
 * - fragile even there, and 1.14.4's model-loading pipeline is different
 * enough that it wouldn't carry over. This is 1.14.4's own clean, fully
 * public equivalent: registering {@link PortkeyItem} with {@code
 * Item.Properties#setTEISR} instead of relying on the plain flat icon this
 * item shipped with initially.
 * <p>
 * The "no camo set" case can't just call {@code ItemRenderer.renderItem}
 * with the Portkey's own stack again - since this class's whole reason for
 * existing is that {@code randomthings:portkey}'s registered model is
 * {@code builtin/entity}, doing that would immediately recurse back into
 * this exact method. Instead it fetches a second, separate model resource
 * ({@code randomthings:portkey_base}, a plain flat-icon model that
 * nothing else references) via {@code ModelManager#getModel} and renders
 * that explicitly - registered once via a {@code ModelRegistryEvent}
 * listener in {@code RandomThings} ({@code ModelLoader#addSpecialModel}) so
 * it actually gets baked despite not being any item's primary model.
 * <p>
 * **2026-09-27, real bug found and fixed (reported by user): both the camo'd
 * and plain render showed oversized/offset.** Root cause: unlike this method,
 * {@code ItemRenderer.renderItem(ItemStack, IBakedModel)}/{@code
 * ItemStackTileEntityRenderer.renderByItem} itself takes no {@code
 * TransformType} in this version - the GUI/ground/hand/fixed scaling for a
 * TESR-backed item comes entirely from ITS OWN registered model's {@code
 * display} block, applied by the caller before {@code renderByItem} ever
 * runs (confirmed via {@code javap -c} and by extracting vanilla's real
 * {@code item/template_shulker_box.json}, the reference for every other
 * {@code builtin/entity} item). {@code portkey.json} had no {@code display}
 * block at all, so every context got an identity transform - fixed by giving
 * it the same transform numbers as {@code template_shulker_box.json}.
 */
public class PortkeyItemRenderer extends ItemStackTileEntityRenderer {
    private static final ModelResourceLocation BASE_MODEL = new ModelResourceLocation(new ResourceLocation("randomthings", "portkey_base"), "inventory");

    @Override
    public void renderByItem(ItemStack stack) {
        ItemStack camo = PortkeyItem.getCamoStack(stack);

        if (!camo.isEmpty()) {
            Minecraft.getInstance().getItemRenderer().renderItem(camo, ItemCameraTransforms.TransformType.NONE);
        } else {
            IBakedModel baseModel = Minecraft.getInstance().getModelManager().getModel(BASE_MODEL);

            Minecraft.getInstance().getItemRenderer().renderItem(stack, baseModel);
        }
    }
}
