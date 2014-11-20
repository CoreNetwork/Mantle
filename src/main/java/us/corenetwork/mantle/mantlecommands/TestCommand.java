package us.corenetwork.mantle.mantlecommands;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketOutputAdapter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.NBTTagList;
import net.minecraft.server.v1_7_R4.PacketPlayOutSetSlot;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.nanobot.NanobotUtil;
import us.corenetwork.mantle.spellbooks.SpellbookItem;
import us.corenetwork.mantle.spellbooks.SpellbooksSettings;


public class TestCommand extends BaseMantleCommand {
	public TestCommand()
	{
		permission = "test";
		desc = "test";
		needPlayer = true;

		new TestListener(PacketType.Play.Server.getInstance().values());
	}


	public void run(final CommandSender sender, String[] args) {
        final Player player = (Player) sender;

		final ItemStack item = new ItemStack(Material.STICK, 1);

		final ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("Item 1");
		item.setItemMeta(meta);
		item.setDurability((short) 32767);

		update(player, item);

		Bukkit.getScheduler().runTaskLater(MantlePlugin.instance, new Runnable()
		{
			@Override
			public void run()
			{
				meta.setDisplayName("Item 2");
				item.setItemMeta(meta);
				item.setDurability((short) 32767);

				//update(player, null);
				update(player, item);
			}
		}, 50L);
	}



	public class TestListener extends PacketAdapter
	{

		public TestListener(Iterable<? extends PacketType> packets) {
			super(MantlePlugin.instance, ListenerPriority.NORMAL, PacketType.Play.Server.SET_SLOT);
			ProtocolLibrary.getProtocolManager().addPacketListener(this);

		}

		@Override
		public void onPacketSending(PacketEvent event) {
			event.getNetworkMarker().addOutputHandler(new PacketOutputAdapter(MantlePlugin.instance, ListenerPriority.NORMAL)
			{
				@Override
				public byte[] handle(PacketEvent packetEvent, byte[] bytes)
				{
					Bukkit.broadcastMessage(Arrays.toString(bytes));
					return bytes;
				}
			});
		}
	}

	private static void update(Player player, ItemStack item)
	{
		Bukkit.broadcastMessage("Sent Packet");
		net.minecraft.server.v1_7_R4.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

		PacketPlayOutSetSlot packet = new PacketPlayOutSetSlot(0, 40, nmsItem);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
	}

}
