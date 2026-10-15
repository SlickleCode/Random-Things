package lumien.randomthings.network.messages;

import lumien.randomthings.client.notifications.NotificationToast;
import lumien.randomthings.network.IRTMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

/**
 * NotificationMessage - tells the receiving client to pop a toast, mirroring
 * 1.12.2's MessageNotification.
 */
public class NotificationMessage implements IRTMessage
{
	private String title;
	private String description;
	private ItemStack icon;

	public NotificationMessage(String title, String description, ItemStack icon)
	{
		this.title = title;
		this.description = description;
		this.icon = icon;
	}

	public NotificationMessage()
	{
	}

	@Override
	public void read(FriendlyByteBuf pb)
	{
		this.title = pb.readUtf(32767);
		this.description = pb.readUtf(32767);
		this.icon = pb.readItem();
	}

	@Override
	public void write(FriendlyByteBuf pb)
	{
		pb.writeUtf(title);
		pb.writeUtf(description);
		pb.writeItem(icon);
	}

	@Override
	public void handle(Context context)
	{
		Minecraft.getInstance().getToasts().addToast(new NotificationToast(title, description, icon));
	}
}
