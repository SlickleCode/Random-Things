package lumien.randomthings.client.renderer;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;

/**
 * A vanilla glint {@link RenderType} (item, entity, armor glint...) drawn with a colour. 1.16's glint types use the {@code POSITION_TEX} vertex format
 * (no per-vertex colour), so the only way to tint one is the GL colour state at draw time; as glint layers are batched per render type, each colour gets
 * its own wrapper type whose setup/clear hooks set and reset that colour around the original type's own state. The colour is normalised to full
 * brightness because the glint texture is already purple (the tint multiplies it).
 * Chosen by {@code ClientAsmHandler.tintGlint}, which the {@code RenderTypeTransformer} coremod calls from the glint getters.
 * <p>
 * Every glint type needs its OWN vertex buffer: an item draws its normal and glint layers through one delegating builder, and a type without a
 * dedicated buffer shares the buffer source's single fallback builder, so asking for the second layer ends the first ("BufferBuilder not started").
 * Vanilla pre-registers its glint types in the main buffer source's {@code fixedBuffers}; each tinted wrapper is added there when it is created.
 */
public final class TintedGlintRenderType extends RenderType {
    private static final Map<String, RenderType> CACHE = new HashMap<>();

    private TintedGlintRenderType(String name, RenderType base, float r, float g, float b) {
        super(name, base.getVertexFormat(), base.getDrawMode(), 256, false, false, () -> {
            base.setupRenderState();
            RenderSystem.color4f(r, g, b, 1.0F);
        }, () -> {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            base.clearRenderState();
        });
    }

    public static RenderType of(RenderType base, int argb) {
        String key = base + "#" + (argb & 0xFFFFFF);

        return CACHE.computeIfAbsent(key, k -> {
            float r = (argb >> 16 & 255) / 255.0F;
            float g = (argb >> 8 & 255) / 255.0F;
            float b = (argb & 255) / 255.0F;
            float max = Math.max(r, Math.max(g, b));

            if (max > 0.0F) {
                r /= max;
                g /= max;
                b /= max;
            }

            RenderType type = new TintedGlintRenderType("rt_glint_" + Integer.toHexString(argb & 0xFFFFFF) + "_" + Integer.toHexString(base.hashCode()), base, r, g, b);
            registerBuffer(type);

            return type;
        });
    }

    private static Field fixedBuffersField;

    @SuppressWarnings("unchecked")
    private static void registerBuffer(RenderType type) {
        try {
            if (fixedBuffersField == null) {
                // the map of render type -> dedicated builder; found by its type so the (obfuscated) field name does not matter
                for (Field f : IRenderTypeBuffer.Impl.class.getDeclaredFields()) {
                    if (Map.class.isAssignableFrom(f.getType()) && f.getGenericType().toString().contains("BufferBuilder") && f.getGenericType().toString().contains("RenderType")) {
                        f.setAccessible(true);
                        fixedBuffersField = f;
                        break;
                    }
                }
            }

            Map<RenderType, BufferBuilder> fixed = (Map<RenderType, BufferBuilder>) fixedBuffersField.get(Minecraft.getInstance().getRenderTypeBuffers().getBufferSource());
            fixed.put(type, new BufferBuilder(256));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("cannot register the tinted glint buffer", e);
        }
    }
}
