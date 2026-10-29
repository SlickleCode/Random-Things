package lumien.randomthings.client.renderer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.IntFunction;

/**
 * The spinning "wheel" that 1.12.2's {@code RenderTimeAccelerator} drew on every face of an accelerated block (ported from its {@code MKRRenderUtil.renderCircleDec*}
 * helpers and {@code ColorFunctions}). No Minecraft classes in here, so the same file is used on every version branch: {@link #build} returns the triangles of the
 * six faces in the local space of a face (a flat figure in the XZ plane, y = 0), and each version's renderer only has to transform them and hand them to a
 * coloured, translucent, unlit vertex consumer.
 * <p>
 * The original issued OpenGL immediate-mode calls, where the colour set last stays current for the next vertex (also across primitives and faces). {@link Wheel}
 * keeps that state so strips and fans get the same per-vertex colours, then turns every primitive into plain triangles. Vertices are {@code x, y, z, r, g, b, a}
 * (colours 0..1), three vertices per triangle.
 */
public final class TimeWheel
{
	/** Floats per vertex in the arrays returned by {@link #build}. */
	public static final int STRIDE = 7;

	/** Distance of a wheel from the block centre, in blocks (1.12.2: {@code 1 / 1.9}). */
	public static final double OFFSET = 1 / 1.9D;

	@FunctionalInterface
	private interface ColorFunction
	{
		Color apply(float progress, int triangle, Color original);

		default ColorFunction next(ColorFunction other)
		{
			return (p, t, c) -> other.apply(p, t, apply(p, t, c));
		}

		default IntFunction<Color> tt(float progress)
		{
			return i -> apply(progress, i, Color.WHITE);
		}
	}

	private static ColorFunction constant(Color color)
	{
		return (p, t, c) -> color;
	}

	private static ColorFunction alternateN(Color c1, Color c2, int n, int offset)
	{
		return (p, t, c) -> (t + offset) % n == 0 ? c2 : c1;
	}

	private static ColorFunction flicker(long rngMod, int slow)
	{
		return (p, t, c) -> {
			Random rng = new Random((long) ((t * t * t * t + p + rngMod) / slow));

			if (rng.nextInt(3) == 0)
			{
				return new Color((int) Math.min(255, c.getRed() + 20f), (int) Math.min(255, c.getGreen() + 20f), (int) Math.min(255, c.getBlue() + 20f), c.getAlpha());
			}

			return c;
		};
	}

	private static ColorFunction limit(ColorFunction original, java.util.function.Predicate<Integer> when)
	{
		return (p, t, c) -> when.test(t) ? original.apply(p, t, c) : c;
	}

	private static final ColorFunction INNER = alternateN(new Color(100, 100, 100, 80), Color.LIGHT_GRAY, 6, 2);

	private static final ColorFunction OUTER_1 = constant(Color.LIGHT_GRAY).next(flicker(50, 100)).next(limit(constant(new Color(0, 0, 0, 0)), i -> (i + 2) % 6 != 0));

	private static final ColorFunction OUTER_2 = constant(Color.LIGHT_GRAY).next(limit(constant(new Color(0, 0, 0, 0)), i -> (i + 2) % 6 == 0 || (i + 4) % 6 == 0 || (i + 6) % 6 == 0)).next(flicker(400, 100));

	private static final ColorFunction OUTER_4 = alternateN(new Color(100, 100, 100, 80), Color.LIGHT_GRAY, 2, 5).next(flicker(800, 100)).next(limit(constant(new Color(100, 100, 100, 80)), i -> i % 2 == 0 || (i / 5) % 2 == 1));

	private static final ColorFunction CROSS = constant(new Color(100, 100, 100, 80)).next(limit(constant(Color.LIGHT_GRAY), i -> {
		i += 6;
		return i % 6 < 4 && i / 6 % 2 != 0;
	}));

	/**
	 * The triangles of the six wheels, in the order of {@code Direction.values()} of 1.12.2 / 1.14 (down, up, north, south, west, east) - the face index is the
	 * index into the result; the colour state carries over from one face to the next like it did there.
	 *
	 * @param timeRate  the accelerator's rate (2, 4, 8, ... 32 and up add rings)
	 * @param progress  {@code 2 * (animation ticks + partial tick)}, the same value the spin angle uses
	 */
	public static float[][] build(int timeRate, float progress)
	{
		Wheel wheel = new Wheel();
		float[][] faces = new float[6][];

		for (int face = 0; face < 6; face++)
		{
			wheel.start();

			wheel.inner(0.05, INNER.tt(progress), 33, i -> 3);

			if (timeRate >= 2)
			{
				wheel.cross(0.07, 0.05, CROSS.tt(progress), 60);
			}

			if (timeRate >= 4)
			{
				wheel.part3(0.1, 0.07, OUTER_1.tt(progress), 30);
			}

			if (timeRate >= 8)
			{
				wheel.part3(0.12, 0.09, OUTER_2.tt(progress), 30);
			}

			if (timeRate >= 16)
			{
				wheel.part5(0.15, 0.12, OUTER_4.tt(progress), 50);
			}

			if (timeRate >= 32)
			{
				wheel.part5(0.16, 0.15, i -> {
					Color c = Color.getHSBColor(0, 0, (float) Math.sin(((Math.PI * 4) / 50F) * i + progress / 10) / 2.5F + 0.6F);

					return new Color(c.getRed(), c.getGreen(), c.getBlue(), 255);
				}, 50);
			}

			faces[face] = wheel.finish();
		}

		return faces;
	}

	/** An OpenGL immediate mode look-alike: current colour state, primitives collected as triangles. */
	private static final class Wheel
	{
		private static final int TRIANGLES = 0;
		private static final int FAN = 1;
		private static final int STRIP = 2;

		private Color current = Color.WHITE;
		private List<float[]> triangles = new ArrayList<>();
		private final List<float[]> primitive = new ArrayList<>();
		private int mode;

		void start()
		{
			triangles = new ArrayList<>();
		}

		float[] finish()
		{
			float[] out = new float[triangles.size() * 3 * STRIDE];
			int pos = 0;

			for (float[] triangle : triangles)
			{
				System.arraycopy(triangle, 0, out, pos, triangle.length);
				pos += triangle.length;
			}

			return out;
		}

		private void begin(int mode)
		{
			this.mode = mode;
			primitive.clear();
		}

		private void color(Color color)
		{
			current = color;
		}

		private void vertex(double x, double z)
		{
			primitive.add(new float[] { (float) x, 0, (float) z, current.getRed() / 255F, current.getGreen() / 255F, current.getBlue() / 255F, current.getAlpha() / 255F });
		}

		private void end()
		{
			int n = primitive.size();

			switch (mode)
			{
				case TRIANGLES:
					for (int i = 0; i + 2 < n; i += 3)
					{
						add(primitive.get(i), primitive.get(i + 1), primitive.get(i + 2));
					}
					break;
				case FAN:
					for (int i = 1; i + 1 < n; i++)
					{
						add(primitive.get(0), primitive.get(i), primitive.get(i + 1));
					}
					break;
				default:
					for (int i = 0; i + 2 < n; i++)
					{
						if (i % 2 == 0)
						{
							add(primitive.get(i), primitive.get(i + 1), primitive.get(i + 2));
						}
						else
						{
							add(primitive.get(i + 1), primitive.get(i), primitive.get(i + 2));
						}
					}
					break;
			}
		}

		private void add(float[] a, float[] b, float[] c)
		{
			float[] triangle = new float[3 * STRIDE];
			System.arraycopy(a, 0, triangle, 0, STRIDE);
			System.arraycopy(b, 0, triangle, STRIDE, STRIDE);
			System.arraycopy(c, 0, triangle, 2 * STRIDE, STRIDE);
			triangles.add(triangle);
		}

		/** {@code renderCircleDecTriInner}: a fan of the ten corners of a decagon around the centre, each side split into {@code countFunction} parts. */
		void inner(double r, IntFunction<Color> triangleFunction, int triCount, IntFunction<Integer> countFunction)
		{
			begin(FAN);
			vertex(0, 0);

			int currentTriCount = 0;

			for (int c = 0; c < 10; c++)
			{
				int countForC = countFunction.apply(c);

				double winkel = Math.PI * 2 / 360 * c * 36;
				double winkel2 = Math.PI * 2 / 360 * (c + 1) * 36;

				double pX = Math.sin(winkel) * r;
				double pZ = Math.cos(winkel) * r;

				double pX2 = Math.sin(winkel2) * r;
				double pZ2 = Math.cos(winkel2) * r;

				double dX = pX2 - pX;
				double dZ = pZ2 - pZ;

				double length = Math.sqrt(dX * dX + dZ * dZ);
				double partLength = length / countForC;

				double nX = dX / length;
				double nZ = dZ / length;

				vertex(pX, pZ);

				for (int p = 1; p <= countForC; p++)
				{
					if (currentTriCount < triCount)
					{
						color(triangleFunction.apply(currentTriCount));
						vertex(pX + nX * partLength * p, pZ + nZ * partLength * p);
						currentTriCount++;
					}
				}
			}

			end();
		}

		/** {@code renderCircleDecTriPart3Tri}: a ring between {@code r2} and {@code r1}, three triangles per decagon side. */
		void part3(double r1, double r2, IntFunction<Color> triangleFunction, int triCount)
		{
			begin(STRIP);

			for (int c = 0; c < 10; c++)
			{
				double winkelRad1 = (Math.PI * 2) / 360D * (36 * c);
				double winkelRad2 = (Math.PI * 2) / 360D * (36 * (c + 1));

				int triIndex = c * 3;

				double pX1 = Math.sin(winkelRad1) * r1;
				double pZ1 = Math.cos(winkelRad1) * r1;
				double pX2 = Math.sin(winkelRad2) * r1;
				double pZ2 = Math.cos(winkelRad2) * r1;
				double pX1H = Math.sin(winkelRad1) * r2;
				double pZ1H = Math.cos(winkelRad1) * r2;
				double pX2H = Math.sin(winkelRad2) * r2;
				double pZ2H = Math.cos(winkelRad2) * r2;

				double cX = (pX2 + pX1) / 2;
				double cZ = (pZ2 + pZ1) / 2;

				if (triIndex < triCount)
				{
					color(triangleFunction.apply(triIndex));
					vertex(pX1, pZ1);
					vertex(pX1H, pZ1H);
					vertex(cX, cZ);
				}

				if (triIndex + 1 < triCount)
				{
					color(triangleFunction.apply(triIndex + 1));
					vertex(pX2H, pZ2H);
				}

				if (triIndex + 2 < triCount)
				{
					color(triangleFunction.apply(triIndex + 2));
					vertex(pX2, pZ2);
				}
			}

			end();
		}

		/** {@code renderCircleDecTriPart5Tri}: like {@link #part3} with five triangles per side. */
		void part5(double r1, double r2, IntFunction<Color> triangleFunction, int triCount)
		{
			begin(STRIP);

			for (int c = 0; c < 10; c++)
			{
				double winkelRad1 = (Math.PI * 2) / 360D * (36 * c);
				double winkelRad2 = (Math.PI * 2) / 360D * (36 * (c + 1));

				int triIndex = c * 5;

				double pX1 = Math.sin(winkelRad1) * r1;
				double pZ1 = Math.cos(winkelRad1) * r1;
				double pX2 = Math.sin(winkelRad2) * r1;
				double pZ2 = Math.cos(winkelRad2) * r1;
				double pX1H = Math.sin(winkelRad1) * r2;
				double pZ1H = Math.cos(winkelRad1) * r2;
				double pX2H = Math.sin(winkelRad2) * r2;
				double pZ2H = Math.cos(winkelRad2) * r2;

				double cXH = (pX2H + pX1H) / 2;
				double cZH = (pZ2H + pZ1H) / 2;

				double tX1 = pX1 + (pX2 - pX1) / 3;
				double tZ1 = pZ1 + (pZ2 - pZ1) / 3;
				double tX2 = pX1 + 2 * (pX2 - pX1) / 3;
				double tZ2 = pZ1 + 2 * (pZ2 - pZ1) / 3;

				if (triIndex < triCount)
				{
					color(triangleFunction.apply(triIndex));
					vertex(pX1, pZ1);
					vertex(pX1H, pZ1H);
					vertex(tX1, tZ1);
				}

				if (triIndex + 1 < triCount)
				{
					color(triangleFunction.apply(triIndex + 1));
					vertex(cXH, cZH);
				}

				if (triIndex + 2 < triCount)
				{
					color(triangleFunction.apply(triIndex + 2));
					vertex(tX2, tZ2);
				}

				if (triIndex + 3 < triCount)
				{
					color(triangleFunction.apply(triIndex + 3));
					vertex(pX2H, pZ2H);
				}

				if (triIndex + 4 < triCount)
				{
					color(triangleFunction.apply(triIndex + 4));
					vertex(pX2, pZ2);
				}
			}

			end();
		}

		/** {@code renderCircleDecTriPartCross}: per decagon side a fan of four triangles plus two single triangles that form a cross. */
		void cross(double r1, double r2, IntFunction<Color> triangleFunction, int triCount)
		{
			for (int c = 0; c < 10; c++)
			{
				double winkelRad1 = (Math.PI * 2) / 360D * (36 * c);
				double winkelRad2 = (Math.PI * 2) / 360D * (36 * (c + 1));

				int triIndex = c * 6;

				double pX1 = Math.sin(winkelRad1) * r1;
				double pZ1 = Math.cos(winkelRad1) * r1;
				double pX2 = Math.sin(winkelRad2) * r1;
				double pZ2 = Math.cos(winkelRad2) * r1;
				double pX1N = Math.sin(winkelRad1) * r2;
				double pZ1N = Math.cos(winkelRad1) * r2;
				double pX2N = Math.sin(winkelRad2) * r2;
				double pZ2N = Math.cos(winkelRad2) * r2;

				double cXF = (pX2 + pX1) / 2;
				double cZF = (pZ2 + pZ1) / 2;
				double cXN = (pX2N + pX1N) / 2;
				double cZN = (pZ2N + pZ1N) / 2;

				double cX = (cXF + cXN) / 2;
				double cZ = (cZF + cZN) / 2;

				double uX1 = cX + (cX - pX2N);
				double uZ1 = cZ + (cZ - pZ2N);
				double uX2 = cX + (cX - pX1N);
				double uZ2 = cZ + (cZ - pZ1N);

				begin(FAN);
				vertex(cX, cZ);

				if (triIndex < triCount)
				{
					color(triangleFunction.apply(triIndex));
					vertex(pX1N, pZ1N);
					vertex(uX1, uZ1);
				}

				if (triIndex + 1 < triCount)
				{
					color(triangleFunction.apply(triIndex + 1));
					vertex(uX2, uZ2);
				}

				if (triIndex + 2 < triCount)
				{
					color(triangleFunction.apply(triIndex + 2));
					vertex(pX2N, pZ2N);
				}

				if (triIndex + 3 < triCount)
				{
					color(triangleFunction.apply(triIndex + 3));
					vertex(pX1N, pZ1N);
				}

				end();

				begin(TRIANGLES);

				if (triIndex + 4 < triCount)
				{
					color(triangleFunction.apply(triIndex + 4));
					vertex(pX1, pZ1);
					vertex(uX1, uZ1);
					vertex(pX1N, pZ1N);
				}

				if (triIndex + 5 < triCount)
				{
					color(triangleFunction.apply(triIndex + 5));
					vertex(pX2, pZ2);
					vertex(pX2N, pZ2N);
					vertex(uX2, uZ2);
				}

				end();
			}
		}
	}

	private TimeWheel()
	{
	}
}
