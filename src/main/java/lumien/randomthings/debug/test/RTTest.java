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

	public interface Condition
	{
		boolean met(TestCtx c) throws Exception;
	}

	static final class Step
	{
		final Body body;
		int waitAfter;
		/** When set, the step is a poll: it is retried every tick until the condition holds, failing after {@code maxTicks}. */
		Condition until;
		String what;
		int maxTicks;
		int stableTicks = 1;
		boolean skipOnTimeout;

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

	/**
	 * Waits (one tick at a time, at most {@code maxTicks}) until {@code condition} holds, instead of guessing a fixed delay for
	 * something asynchronous such as the lighting engine; fails the test with "timed out waiting for {@code what}" otherwise.
	 */
	public RTTest waitUntil(String what, Condition condition, int maxTicks)
	{
		return waitUntil(what, condition, maxTicks, 1);
	}

	/** Like {@link #waitUntil(String, Condition, int)}, but the condition must hold for {@code stableTicks} consecutive ticks. */
	public RTTest waitUntil(String what, Condition condition, int maxTicks, int stableTicks)
	{
		Step step = new Step(c -> {
		});
		step.until = condition;
		step.what = what;
		step.maxTicks = maxTicks;
		step.stableTicks = stableTicks;
		steps.add(step);
		return this;
	}

	/**
	 * Marks the preceding {@link #waitUntil} as an environment precondition: if it never holds, the test is skipped (and listed as
	 * such) instead of failed, because the thing it waits for is outside the code under test.
	 */
	public RTTest skipIfNeverMet()
	{
		steps.get(steps.size() - 1).skipOnTimeout = true;
		return this;
	}

	public RTTest waitTicks(int ticks)
	{
		steps.get(steps.size() - 1).waitAfter += ticks;
		return this;
	}
}
