package lumien.randomthings.network.messages;

import lumien.randomthings.client.notifications.NotificationToast;
import lumien.randomthings.network.IRTMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

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
	public void read(PacketBuffer pb)
	{
		this.title = pb.readString();
		this.description = pb.readString();
		this.icon = pb.readItemStack();
	}

	@Override
	public void write(PacketBuffer pb)
	{
		pb.writeString(title);
		pb.writeString(description);
		pb.writeItemStack(icon);
	}

	@Override
	public void handle(Context context)
	{
		Minecraft.getInstance().getToastGui().add(new NotificationToast(title, description, icon));
	}
}
