package us.corenetwork.mantle.nanobot.commands;

import net.minecraft.server.v1_7_R3.ItemStack;
import net.minecraft.server.v1_7_R3.NBTTagCompound;

import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;

import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.nanobot.NanobotUtil;

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
		if (itemName.startsWith("\"")) {
			itemName = itemName.substring(1);
			while (!itemName.endsWith("\"")) {
				i++;

				if (args.length - 2 < i) {
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

		if (i < args.length && Util.isInteger(args[i]))
			invSlot = Integer.parseInt(args[i]);

		org.bukkit.inventory.ItemStack stack = NanobotUtil
				.getMaterialFromItem(itemName);

		if (stack == null) {
			sender.sendMessage("Item name/id not recognized!");
			return;
		}

		Player player = (Player) sender;
		ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);

		NBTTagCompound newTag = LoadCommand.load(tag);

		if (newTag == null) {
			sender.sendMessage("Tag with that name was not found!");
			return;
		}

		nmsStack.tag = newTag;

		if (invSlot == null) {
			player.getInventory().addItem(
					CraftItemStack.asCraftMirror(nmsStack));
		} else {
			if (player.getInventory().getItem(invSlot) != null)
				player.getWorld().dropItem(player.getLocation(),
						player.getInventory().getItem(invSlot));

			player.getInventory().setItem(invSlot,
					CraftItemStack.asCraftMirror(nmsStack));
		}

		if (!(i < args.length && args[i].equals("silent"))
				&& !(i + 1 < args.length && args[i + 1].equals("silent")))
			sender.sendMessage("Item with tag was made sucessfully!");

	}
}
