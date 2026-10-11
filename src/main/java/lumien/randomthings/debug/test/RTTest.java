package lumien.randomthings.debug.test;

import java.util.ArrayList;
import java.util.List;

/**
 * One in-game test: a name plus a list of steps, each optionally followed by a wait of N server
 * ticks. Build with {@code RTTest.named("x").run(c -> setup).waitTicks(20).run(c -> verify)}.
 * A step fails by throwing (use the {@link TestCtx} assertion helpers).
 */
public final class RTTest
{
	public interface Body
	{
		void run(TestCtx c) throws Exception;
	}

	static final class Step
	{
		final Body body;
		int waitAfter;

		Step(Body body)
		{
			this.body = body;
		}
	}

	final String name;
	final List<Step> steps = new ArrayList<>();

	private RTTest(String name)
	{
		this.name = name;
	}

	public static RTTest named(String name)
	{
		return new RTTest(name);
	}

	public RTTest run(Body body)
	{
		steps.add(new Step(body));
		return this;
	}

	public RTTest waitTicks(int ticks)
	{
		steps.get(steps.size() - 1).waitAfter += ticks;
		return this;
	}
}
