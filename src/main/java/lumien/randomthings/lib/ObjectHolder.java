package lumien.randomthings.lib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Stand-in for Forge's removed {@code @ObjectHolder}: a static field filled by {@link ObjectHolderInjector} right after
 * the registry named by {@link #registryName()} has received this mod's entries.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ObjectHolder
{
	String registryName();

	String value();
}
