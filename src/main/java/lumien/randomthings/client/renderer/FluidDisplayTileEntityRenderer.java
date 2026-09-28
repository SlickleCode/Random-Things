package lumien.randomthings.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import lumien.randomthings.tileentity.FluidDisplayTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fluids.FluidStack;

/**
 * Draws the displayed fluid as a full unit cube textured with its still/
 * flowing sprite (read straight off the main block atlas, exactly like
 * vanilla's own fluid rendering does), tinted with the fluid's own color, and
 * spun around Y in 90-degree steps for the rotation property. Replaces
 * 1.12.2's {@code ModelFluidDisplay}, a custom baked model driven by {@code
 * ExtendedBlockState} (not a thing in 1.14.4) - this port's established
 * substitute for that kind of per-instance dynamic render data is a custom
 * {@code TileEntityRenderer} instead (see {@code RuneBaseTileEntityRenderer}).
 * <p>
 * Disclosed simplification: the original's rotation property remapped which
 * baked quad's UVs faced which direction on an otherwise-static cube; this
 * instead physically rotates the whole rendered cube in world space each
 * 90-degree step, which looks equivalent for the fully-opaque single-texture
 * cube case but isn't byte-for-byte the same transform.
 */
public class FluidDisplayTileEntityRenderer extends TileEntityRenderer<FluidDisplayTileEntity> {
    @Override
    public void render(FluidDisplayTileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {
        FluidStack fluidStack = te.getFluidStack();

        if (fluidStack == null || fluidStack.isEmpty()) {
            return;
        }

        net.minecraftforge.fluids.FluidAttributes attributes = fluidStack.getFluid().getAttributes();
        net.minecraft.util.ResourceLocation spriteLocation = te.flowing() ? attributes.getFlowingTexture() : attributes.getStillTexture();

        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureMap().getSprite(spriteLocation);
        int color = attributes.getColor(fluidStack);
        float r = (color >> 16 & 0xFF) / 255F;
        float g = (color >> 8 & 0xFF) / 255F;
        float b = (color & 0xFF) / 255F;

        this.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.enableBlend();
        // Real bug, found 2026-09-27 (same category as RuneBaseTileEntityRenderer's
        // fix): TileEntityRendererDispatcher.render() enables real GL_LIGHTING right
        // before calling any TESR's own render(), but this renderer's POSITION_TEX_COLOR
        // vertices carry no normal data, so the lighting math darkened the whole cube
        // toward black regardless of the fluid's actual tint color.
        GlStateManager.disableLighting();

        GlStateManager.pushMatrix();
        GlStateManager.translated(x + 0.5, y, z + 0.5);
        GlStateManager.rotatef(te.getRotation().ordinal() * 90F, 0, 1, 0);
        GlStateManager.translated(-0.5, 0, -0.5);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);

        float minU = sprite.getMinU();
        float maxU = sprite.getMaxU();
        float minV = sprite.getMinV();
        float maxV = sprite.getMaxV();

        // Real bug, found 2026-09-27: the -Y and +Y faces both had their winding
        // order backwards relative to the other 4 faces (confirmed by computing each
        // face's normal from its vertex order via the right-hand rule: every side face
        // pointed outward as expected, but -Y and +Y both pointed inward). With
        // back-face culling on (never disabled here, matching how a real opaque cube
        // should render), a backwards-wound face's visible outward side is exactly the
        // side that gets culled - invisible/"transparent" from the normal viewing
        // angle, which is exactly what made the top face never show a texture. Fixed
        // by reversing each one's corner order (keeping the same 4 corners, same
        // plane) - the corner lists below are the corrected ones.
        //
        // Also real bug, found the same day: every face used the exact same, flat
        // (r, g, b) tint with no per-face brightness - unlike vanilla blocks, which
        // bake a direction-based diffuse-lighting multiplier into each face's vertex
        // color (ground-truthed from BlockModelRenderer's own per-direction constants:
        // UP 1.0, DOWN 0.5, NORTH/SOUTH 0.8, WEST/EAST 0.6 - GL_LIGHTING was disabled
        // above specifically because it can't replicate this per-face-direction model
        // using real normals/lights, so it has to be baked into the vertex color by
        // hand instead, the same way vanilla's own block renderer does it). Without
        // this, every face of the cube looked identically bright/flat, unlike every
        // other block in the game.
        // -Y (bottom), corrected winding, DOWN brightness (0.5)
        quad(buffer, 0, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 1, minU, maxU, minV, maxV, r, g, b, 0.5F);
        // +Y (top), corrected winding, UP brightness (1.0)
        quad(buffer, 0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, minU, maxU, minV, maxV, r, g, b, 1.0F);
        // -Z (north), NORTH brightness (0.8)
        quad(buffer, 0, 1, 0, 1, 1, 0, 1, 0, 0, 0, 0, 0, minU, maxU, minV, maxV, r, g, b, 0.8F);
        // +Z (south), SOUTH brightness (0.8)
        quad(buffer, 0, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, minU, maxU, minV, maxV, r, g, b, 0.8F);
        // -X (west), WEST brightness (0.6)
        quad(buffer, 0, 0, 0, 0, 0, 1, 0, 1, 1, 0, 1, 0, minU, maxU, minV, maxV, r, g, b, 0.6F);
        // +X (east), EAST brightness (0.6)
        quad(buffer, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 0, 0, minU, maxU, minV, maxV, r, g, b, 0.6F);

        tessellator.draw();

        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
    }

    private static void quad(BufferBuilder buffer, double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4, float minU, float maxU, float minV, float maxV, float r, float g, float b, float brightness) {
        float fr = r * brightness;
        float fg = g * brightness;
        float fb = b * brightness;

        buffer.pos(x1, y1, z1).tex(minU, minV).color(fr, fg, fb, 1.0F).endVertex();
        buffer.pos(x2, y2, z2).tex(minU, maxV).color(fr, fg, fb, 1.0F).endVertex();
        buffer.pos(x3, y3, z3).tex(maxU, maxV).color(fr, fg, fb, 1.0F).endVertex();
        buffer.pos(x4, y4, z4).tex(maxU, minV).color(fr, fg, fb, 1.0F).endVertex();
    }
}
