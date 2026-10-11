package lumien.randomthings.item;

/**
 * 1.16 moved item property overrides ({@code addPropertyOverride}) to the client-only
 * {@code ItemModelsProperties}; items that need one implement this and are registered from
 * {@code client/ClientSetup} once the client has started.
 */
public interface IClientItemProperties
{
	void registerClientProperties();
}
