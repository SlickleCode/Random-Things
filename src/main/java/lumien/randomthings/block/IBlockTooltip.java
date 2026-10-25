package lumien.randomthings.block;

import java.util.function.Consumer;

import net.minecraft.network.chat.Component;

/** A block whose item shows extra tooltip lines (Block.appendHoverText is gone since 1.21.5; {@code TooltipBlockItem} asks the block instead). */
public interface IBlockTooltip
{
	void addTooltip(Consumer<Component> tooltip);
}
