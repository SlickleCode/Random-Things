package lumien.randomthings.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import lumien.randomthings.tileentity.RuneBaseTileEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.DyeColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;

/**
 * Draws each placed rune as a small colored square, plus a thin connecting
 * strip toward any adjacent cell (in this same Rune Base, or across the edge
 * into a neighboring one) sharing its color - reading directly from the tile
 * entity's own data and, for edge connections, its loaded neighbors', at
 * render time. Replaces 1.12.2's {@code ModelRune}, a custom baked model
 * driven by {@code ExtendedBlockState}/{@code IUnlistedProperty} (removed in
 * 1.14.4) - this port's established fix for exactly that kind of per-instance
 * dynamic render data is a custom {@code TileEntityRenderer} instead (see
 * {@code SpecialChestTileEntityRenderer}/{@code BiomeRadarTileEntityRenderer}).
 * <p>
 * Disclosed simplification: the original additionally sampled a random 2x2
 * sub-region out of an 8x8 noise-variation grid baked into its texture, so no
 * two rune pixels looked quite identical. This renders each pixel as a flat
 * tinted color instead (no texture sampling at all) - same size/shape/
 * connection logic, just without that per-pixel texture noise.
 */
public class RuneBaseTileEntityRenderer extends TileEntityRenderer<RuneBaseTileEntity> {
    private static final float UNIT = 1F / 16F;

    @Override
    public void render(RuneBaseTileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {
        DyeColor[][] runeData = te.getRuneData();

        if (te.isEmpty()) {
            return;
        }

        this.setLightmapDisabled(true);
        GlStateManager.disableTexture();
        GlStateManager.enableBlend();
        GlStateManager.disableCull();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);

        for (int cx = 0; cx < 4; cx++) {
            for (int cy = 0; cy < 4; cy++) {
                DyeColor color = runeData[cx][cy];

                if (color == null) {
                    continue;
                }

                float[] rgb = color.getColorComponentValues();

                // The pixel itself: 2 units wide, centered in its 4-unit cell.
                addQuad(buffer, x, y, z, cx * 4 + 1, cx * 4 + 3, cy * 4 + 1, cy * 4 + 3, rgb);

                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    if (isConnected(te, runeData, cx, cy, color, dir)) {
                        addConnectionStrip(buffer, x, y, z, cx, cy, dir, rgb);
                    }
                }
            }
        }

        tessellator.draw();

        GlStateManager.enableCull();
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

    private void addConnectionStrip(BufferBuilder buffer, double x, double y, double z, int cx, int cy, Direction dir, float[] rgb) {
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

        addQuad(buffer, x, y, z, x1, x2, y1, y2, rgb);
    }

    private void addQuad(BufferBuilder buffer, double x, double y, double z, double x1, double x2, double y1, double y2, float[] rgb) {
        double lx1 = x + x1 * UNIT;
        double lx2 = x + x2 * UNIT;
        double lz1 = z + y1 * UNIT;
        double lz2 = z + y2 * UNIT;
        double ly = y + 0.02;

        buffer.pos(lx1, ly, lz1).color(rgb[0], rgb[1], rgb[2], 1.0F).endVertex();
        buffer.pos(lx1, ly, lz2).color(rgb[0], rgb[1], rgb[2], 1.0F).endVertex();
        buffer.pos(lx2, ly, lz2).color(rgb[0], rgb[1], rgb[2], 1.0F).endVertex();
        buffer.pos(lx2, ly, lz1).color(rgb[0], rgb[1], rgb[2], 1.0F).endVertex();
    }
}
