package lumien.randomthings.item;

import lumien.randomthings.entity.FlooFireplaceEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;

/**
 * Drop it and leave it resting on the ground for a while (past tick 100) and
 * it spawns a temporary {@link FlooFireplaceEntity} at that spot, consuming
 * itself - a disposable, one-shot fireplace with no name (any Floo Sign can
 * still target it while it lasts, since {@code FlooNetworkHandler} doesn't
 * care where a fireplace's TE came from). Direct port of 1.12.2's {@code
 * ItemFlooToken}; see {@code StableEnderpearlItem.tickDroppedPearl}'s javadoc
 * for why this is driven from a {@code TickEvent.WorldTickEvent} listener in
 * {@code RandomThings.java} rather than an {@code Item.onEntityItemUpdate}
 * override - that hook doesn't exist on {@code Item} in this Forge build,
 * same finding as that item, and the same fix applies here.
 * <p>
 * Disclosed simplification: the original also spawned a small idle spark
 * particle client-side once the dropped token had aged past tick 30 (70
 * ticks before it actually becomes a fireplace). The replacement listener
 * driving this method only scans the server-side entity list (matching
 * {@code StableEnderpearlItem}'s equivalent listener), so there's no client-
 * side hook left to hang that brief pre-fireplace flicker on; dropped rather
 * than standing up a whole separate client-side entity scan for a ~1-second
 * cosmetic detail that's immediately followed by {@link FlooFireplaceEntity}'s
 * own, much longer particle effect once it actually spawns.
 */
public class FlooTokenItem extends Item {
    public FlooTokenItem(Item.Properties properties) {
        super(properties);
    }

    public void tickDroppedToken(ItemEntity entityItem) {
        int age = age(entityItem);

        if (age > 100 && entityItem.onGround) {
            boolean nearbyFireplace = !entityItem.world.getEntitiesWithinAABB(FlooFireplaceEntity.class, entityItem.getBoundingBox().grow(5)).isEmpty();

            if (!nearbyFireplace) {
                FlooFireplaceEntity toSpawn = new FlooFireplaceEntity(entityItem.world, entityItem.posX, entityItem.posY, entityItem.posZ);
                entityItem.world.addEntity(toSpawn);
                entityItem.remove();
            }
        }
    }

    private static int age(ItemEntity entityItem) {
        CompoundNBT data = entityItem.getPersistentData();
        int age = data.getInt("flooTokenAge") + 1;
        data.putInt("flooTokenAge", age);

        return age;
    }
}
