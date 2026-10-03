package lumien.randomthings.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import java.util.Random;

import lumien.randomthings.config.RTConfig;
import lumien.randomthings.lib.ModConstants;
import lumien.randomthings.tileentity.RuneBaseTileEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.DyeColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

/**
 * Draws each placed rune as a small colored square, plus a thin connecting
 * strip toward any adjacent cell (in this same Rune Base, or across the edge
 * into a neighboring one) sharing its color - reading directly from the tile
 * entity's own data and, for edge connections, its loaded neighbors', at
 * render time. Replaces 1.12.2's {@code ModelRune}, a custom baked model
 * driven by {@code ExtendedBlockState}/{@code IUnlistedProperty} (removed in
 * 1.14.4) - this port's established fix for exactly that kind of per-instance
 * dynamic render data is a custom {@code TileEntityRenderer} instead (see
 * {@code BiomeRadarTileEntityRenderer}).
 * <p>
 * Like the original, each pixel/strip samples a random 2x2 (or 2x1) sub-region
 * of a 16x16 texture, tinted by the rune color, so no two pixels look quite
 * identical. The regions are drawn from a {@link Random} seeded by the block
 * position (consumed in the original's exact order) so they stay stable from
 * frame to frame. The {@code FlatRunes} config option swaps the noisy
 * {@code rune_base.png} for the plain {@code rune_base_flat.png}, as in 1.12.2.
 */
public class RuneBaseTileEntityRenderer extends TileEntityRenderer<RuneBaseTileEntity> {
    private static final float UNIT = 1F / 16F;
    private static final ResourceLocation NOISY = new ResourceLocation(ModConstants.MOD_ID, "textures/block/rune_base.png");
    private static final ResourceLocation FLAT = new ResourceLocation(ModConstants.MOD_ID, "textures/block/rune_base_flat.png");
    private static final Direction[] SIDE_ORDER = { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST };

    @Override
    public void render(RuneBaseTileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {
        DyeColor[][] runeData = te.getRuneData();

        if (te.isEmpty()) {
            return;
        }

        this.setLightmapDisabled(true);
        this.bindTexture(RTConfig.FLAT_RUNES.get() ? FLAT : NOISY);
        GlStateManager.enableTexture();
        // Real bug #2, found 2026-09-27 (the color4f reset below wasn't enough on its
        // own): TileEntityRendererDispatcher.render() calls RenderHelper
        // .enableStandardItemLighting() (real GL_LIGHTING, with fixed-function light
        // positions) right before invoking any TESR's own render() - confirmed by
        // reading the dispatcher's own decompiled source. That's fine for baked-model
        // TESRs (their quads carry proper normals), but this renderer's raw
        // POSITION_COLOR vertices carry no normal data at all, so the lighting math
        // ran against whatever normal happened to be left set globally, darkening
        // every pixel toward invisible on top of the alpha issue below.
        // RedstoneObserverLineRenderer - this project's other raw-colored-primitive
        // TESR-adjacent renderer - already disables lighting for exactly this reason;
        // missed here since it wasn't obviously needed for what looked like a plain
        // "flat color" draw.
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableCull();
        // Real bug #1, found 2026-09-27: every vertex color below is multiplied by
        // this global GL color state, which isn't reset between renderers - without
        // setting it to opaque white here, whatever alpha was left over from the last
        // thing drawn that frame (often near-zero) made every rune pixel invisible
        // despite the tile entity/data placing correctly. Every other custom TESR in
        // this project that draws its own colors already does this - missed here
        // specifically because this is the only one using per-vertex BufferBuilder.color(...) instead of a single
        // GlStateManager.color4f(...) call per shape.
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);

        Random rng = new Random(te.getPos().toLong());

        for (int cx = 0; cx < 4; cx++) {
            for (int cy = 0; cy < 4; cy++) {
                DyeColor color = runeData[cx][cy];

                float u = rng.nextInt(8) * 2;
                float v = rng.nextInt(8) * 2;

                if (color == null) {
                    // Keep the random sequence aligned with the original's, which
                    // still rolled the four side samples for empty cells.
                    for (int i = 0; i < 8; i++) {
                        rng.nextInt(8);
                    }

                    continue;
                }

                float[] rgb = color.getColorComponentValues();

                // The pixel itself: 2 units wide, centered in its 4-unit cell.
                addQuad(buffer, x, y, z, cx * 4 + 1, cx * 4 + 3, cy * 4 + 1, cy * 4 + 3, rgb, u, v);

                for (Direction dir : SIDE_ORDER) {
                    u = rng.nextInt(8) * 2;
                    v = rng.nextInt(8) * 2;

                    if (isConnected(te, runeData, cx, cy, color, dir)) {
                        addConnectionStrip(buffer, x, y, z, cx, cy, dir, rgb, u, v);
                    }
                }
            }
        }

        tessellator.draw();

        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture();
        this.setLightmapDisabled(false);
    }

    private boolean isConnected(RuneBaseTileEntity te, DyeColor[][] runeData, int cx, int cy, DyeColor color, Direction dir) {
        int nx = cx + dir.getXOffset();
        int ny = cy + dir.getZOffset();

        if (nx >= 0 && nx < 4 && ny >= 0 && ny < 4) {
            return runeData[nx][ny] == color;
        }

        TileEntity neighborTe = te.getWorld().getTileEntity(te.getPos().offset(dir));

        if (!(neighborTe instanceof RuneBaseTileEntity)) {
            return false;
        }

        int mirroredX = nx < 0 ? 3 : (nx >= 4 ? 0 : nx);
        int mirroredY = ny < 0 ? 3 : (ny >= 4 ? 0 : ny);

        return ((RuneBaseTileEntity) neighborTe).getRuneData()[mirroredX][mirroredY] == color;
    }

    private void addConnectionStrip(BufferBuilder buffer, double x, double y, double z, int cx, int cy, Direction dir, float[] rgb, float u, float v) {
        int baseX = cx * 4;
        int baseY = cy * 4;

        double x1, x2, y1, y2;

        switch (dir) {
            case NORTH:
                x1 = baseX + 1;
                x2 = baseX + 3;
                y1 = baseY;
                y2 = baseY + 1;
                break;
            case SOUTH:
                x1 = baseX + 1;
                x2 = baseX + 3;
                y1 = baseY + 3;
                y2 = baseY + 4;
                break;
            case WEST:
                x1 = baseX;
                x2 = baseX + 1;
                y1 = baseY + 1;
                y2 = baseY + 3;
                break;
            case EAST:
            default:
                x1 = baseX + 3;
                x2 = baseX + 4;
                y1 = baseY + 1;
                y2 = baseY + 3;
                break;
        }

        addQuad(buffer, x, y, z, x1, x2, y1, y2, rgb, u, v);
    }

    /** {@code u}/{@code v} pick the top-left texel of a region as big as the quad, in 1/16 texture units. */
    private void addQuad(BufferBuilder buffer, double x, double y, double z, double x1, double x2, double y1, double y2, float[] rgb, float u, float v) {
        double lx1 = x + x1 * UNIT;
        double lx2 = x + x2 * UNIT;
        double lz1 = z + y1 * UNIT;
        double lz2 = z + y2 * UNIT;
        double ly = y + 0.02;

        float u1 = u / 16F;
        float u2 = (u + (float) (x2 - x1)) / 16F;
        float v1 = v / 16F;
        float v2 = (v + (float) (y2 - y1)) / 16F;

        buffer.pos(lx1, ly, lz1).tex(u1, v1).color(rgb[0], rgb[1], rgb[2], 1.0F).endVertex();
        buffer.pos(lx1, ly, lz2).tex(u1, v2).color(rgb[0], rgb[1], rgb[2], 1.0F).endVertex();
        buffer.pos(lx2, ly, lz2).tex(u2, v2).color(rgb[0], rgb[1], rgb[2], 1.0F).endVertex();
        buffer.pos(lx2, ly, lz1).tex(u2, v1).color(rgb[0], rgb[1], rgb[2], 1.0F).endVertex();
    }
}
