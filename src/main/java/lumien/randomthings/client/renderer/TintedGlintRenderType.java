package lumien.randomthings.client.renderer;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import org.joml.Vector4f;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

/**
 * A copy of a vanilla glint {@link RenderType} (item, entity, armor glint, ...) that is drawn with a colour. 1.21.6+ gives every draw a colour modulator
 * uniform ({@code ColorModulator}, multiplied with the glint texture) that {@code RenderType.draw} always sets to white; a transformer lets
 * {@link #modulatorOf} replace it, so a tinted glint is the same pipeline and render setup under another render type instance that is registered here
 * with its colour. The colour is normalised to full brightness because the glint texture is already purple (the tint multiplies it).
 * <p>
 * Chosen by {@code ClientAsmHandler.tintGlint}, which the transformer of the {@code RenderTypes} glint getters calls. Every glint type needs its OWN vertex
 * buffer: an item draws its normal and glint layers through one delegating consumer, and a type without a dedicated buffer shares the buffer source's
 * single fallback builder, so asking for the second layer would end the first. Vanilla pre-registers its glint types in the main buffer source's
 * {@code fixedBuffers}; each tinted copy is added there when it is created.
 */
public final class TintedGlintRenderType
{
	private static final Map<String, RenderType> CACHE = new HashMap<>();
	private static final Map<RenderType, Vector4f> COLORS = new IdentityHashMap<>();
	private static Field setupField;
	private static Field fixedBuffersField;

	private TintedGlintRenderType()
	{
	}

	/** The colour modulator to draw {@code type} with: the tint of a tinted glint copy, else the given (white) one. */
	public static Vector4f modulatorOf(Vector4f white, RenderType type)
	{
		Vector4f tint = COLORS.get(type);
		if (tint != null)
		{
			lumien.randomthings.debug.HookProbe.hit("glint_modulated");
			lumien.randomthings.debug.HookProbe.hit("glint_modulated:" + type);
		}

		return tint == null ? white : tint;
	}

	public static RenderType of(RenderType base, int argb)
	{
		String key = System.identityHashCode(base) + "#" + (argb & 0xFFFFFF);

		synchronized (CACHE)
		{
			return CACHE.computeIfAbsent(key, k -> {
				float r = (argb >> 16 & 255) / 255.0F;
				float g = (argb >> 8 & 255) / 255.0F;
				float b = (argb & 255) / 255.0F;
				float max = Math.max(r, Math.max(g, b));

				if (max > 0.0F)
				{
					r /= max;
					g /= max;
					b /= max;
				}

				try
				{
					if (setupField == null)
					{
						setupField = RenderType.class.getDeclaredField("state");
						setupField.setAccessible(true);
					}

					RenderType type = RenderType.create("rt_glint_" + Integer.toHexString(argb & 0xFFFFFF) + "_" + Integer.toHexString(System.identityHashCode(base)), (RenderSetup) setupField.get(base));
					COLORS.put(type, new Vector4f(r, g, b, 1.0F));
					registerBuffer(type);
					return type;
				}
				catch (ReflectiveOperationException e)
				{
					throw new IllegalStateException("cannot create the tinted glint render type", e);
				}
			});
		}
	}

	@SuppressWarnings("unchecked")
	private static void registerBuffer(RenderType type) throws ReflectiveOperationException
	{
		if (fixedBuffersField == null)
		{
			fixedBuffersField = MultiBufferSource.BufferSource.class.getDeclaredField("fixedBuffers");
			fixedBuffersField.setAccessible(true);
		}

		Map<RenderType, ByteBufferBuilder> fixed = (Map<RenderType, ByteBufferBuilder>) fixedBuffersField.get(Minecraft.getInstance().renderBuffers().bufferSource());
		fixed.put(type, new ByteBufferBuilder(256));
	}
}
