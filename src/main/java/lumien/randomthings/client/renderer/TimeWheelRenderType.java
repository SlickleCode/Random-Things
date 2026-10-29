package lumien.randomthings.client.renderer;

import java.lang.reflect.Field;
import java.util.Map;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

/**
 * Unlit, untextured, per-vertex coloured quads with the standard alpha blend (SRC_ALPHA, ONE_MINUS_SRC_ALPHA), no face culling, depth test and depth write on:
 * the render state the Time Accelerator wheel (see {@link TimeWheel}) was drawn with in 1.12.2. Vanilla has no such type (lightning() blends additively).
 * <p>
 * The type gets its own vertex buffer in the main buffer source's {@code fixedBuffers}, otherwise it shares the fallback builder and ending another
 * layer's batch gives "BufferBuilder not started" (same as {@link TintedGlintRenderType}).
 */
public final class TimeWheelRenderType extends RenderType {
    private static RenderType instance;

    private TimeWheelRenderType() {
        super("rt_time_wheel", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, true, () -> {
        }, () -> {
        });
    }

    public static RenderType get() {
        if (instance == null) {
            instance = create("rt_time_wheel", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 4096, false, true,
                    CompositeState.builder()
                            .setShaderState(POSITION_COLOR_SHADER)
                            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                            .setCullState(NO_CULL)
                            .setLightmapState(NO_LIGHTMAP)
                            .setDepthTestState(LEQUAL_DEPTH_TEST)
                            .setWriteMaskState(COLOR_DEPTH_WRITE)
                            .createCompositeState(false));
            registerBuffer(instance);
        }

        return instance;
    }

    @SuppressWarnings("unchecked")
    private static void registerBuffer(RenderType type) {
        try {
            for (Field f : MultiBufferSource.BufferSource.class.getDeclaredFields()) {
                if (Map.class.isAssignableFrom(f.getType()) && f.getGenericType().toString().contains("BufferBuilder") && f.getGenericType().toString().contains("RenderType")) {
                    f.setAccessible(true);
                    ((Map<RenderType, BufferBuilder>) f.get(Minecraft.getInstance().renderBuffers().bufferSource())).put(type, new BufferBuilder(4096));
                    return;
                }
            }

            throw new IllegalStateException("fixedBuffers field not found");
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("cannot register the time wheel buffer", e);
        }
    }
}
