package us.corenetwork.mantle.nanobot.commands;

import java.io.IOException;
import net.minecraft.server.v1_8_R2.ItemStack;
import net.minecraft.server.v1_8_R2.NBTTagCompound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import us.core_network.cornel.common.MinecraftNames;
import us.core_network.cornel.items.NbtYaml;
import us.core_network.cornel.strings.NumberParsing;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.slimeballs.SlimeballItem;

public class MakeCommand extends NanobotBaseCommand {

	public MakeCommand() {
		needPlayer = true;
		adminCommand = true;
		desc = "Load and apply tag to item in hand";
		permission = "make";
	}

	@SuppressWarnings("unchecked")
	public void run(CommandSender sender, String[] args) {
		if (args.length < 2) {
			sender.sendMessage("Syntax: /nbt make [item name/id] [tag] (slot) (silent)");
			return;
		}

		int i = 0;
		String itemName = args[0];
		if (itemName.startsWith("\""))
		{
			itemName = itemName.substring(1);
			while (!itemName.endsWith("\""))
			{
				i++;
				
				if (args.length - 2 < i)
				{
					sender.sendMessage("Syntax: /nbt make [item name/id] [tag] (slot) (silent)");
					return;
				}
				
				itemName += " " + args[i];
			}
			itemName = itemName.substring(0, itemName.length() - 1);
		}
		
		i++;
		String tag = args[i];
		Integer invSlot = null;
		i++;
		
		if (i < args.length && NumberParsing.isInteger(args[i]))
			invSlot = Integer.parseInt(args[i]);

		boolean silent = (i < args.length && args[i].equals("silent")) && !(i + 1 < args.length && args[i + 1].equals("silent"));

		Integer itemId = MinecraftNames.getMaterialId(itemName.toLowerCase());

		if (itemId == null)
		{
			sender.sendMessage("Item id not recognized!");
			return;
		}

		org.bukkit.inventory.ItemStack stack = new org.bukkit.inventory.ItemStack(itemId, 1);


		Player player = (Player) sender;
		ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);

		NBTTagCompound newTag = null;
		try
		{
			newTag = NbtYaml.loadFromFile(tag);
		}
		catch (Exception e)
		{
			if (!silent)
				sender.sendMessage("Error while giving you the item!");
			e.printStackTrace();
			return;
		}

		if (newTag == null)
		{
			return;
		}
		
		nmsStack.setTag(newTag);

		if (invSlot == null)
		{
			player.getInventory().addItem(CraftItemStack.asCraftMirror(nmsStack));
		}
		else
		{			
			player.getInventory().setItem(invSlot, CraftItemStack.asCraftMirror(nmsStack));
		}
		
		if (!silent)
			sender.sendMessage("Item with tag was made sucessfully!");
		
	}
}
