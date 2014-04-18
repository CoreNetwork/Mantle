package us.corenetwork.mantle.spellbooks.commands;

import java.util.HashMap;

import net.minecraft.server.v1_7_R2.EntityItem;
import net.minecraft.server.v1_7_R2.NBTTagCompound;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R2.entity.CraftItem;
import org.bukkit.craftbukkit.v1_7_R2.inventory.CraftItemStack;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.mantlecommands.BaseMantleCommand;
import us.corenetwork.mantle.nanobot.commands.LoadCommand;


public class MakeBookCommand extends BaseMantleCommand {	
	public MakeBookCommand()
	{
		permission = "makebook";
		desc = "Make spellbook";
		needPlayer = true;
	}


	public void run(final CommandSender sender, String[] args) {
		if (args.length < 1) {
			sender.sendMessage("Syntax: /mantle makebook [nanobot tag] (number)");
			return;
		}

		String tag = args[0];
		int amount = 1;
		if (args.length > 1 && Util.isInteger(args[1]))
		{
			amount = Integer.parseInt(args[1]);
		}
		
		Player player = (Player) sender;

		NBTTagCompound newTag = LoadCommand.load(tag);
		
		if (newTag == null)
		{
			sender.sendMessage("Tag with that name was not found! Message moderators about that issue!");
			return;
		}
		do
		{
			int stackAmount = Math.min(amount, 64);
			ItemStack stack = new ItemStack(Material.BOOK, stackAmount);
			
			net.minecraft.server.v1_7_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
			nmsStack.tag = newTag;
			stack = CraftItemStack.asBukkitCopy(nmsStack);

			HashMap<Integer, ItemStack> overflowItems = player.getInventory().addItem(stack);
			if (overflowItems.size() > 0) //Drop books on the ground if inventory is full
			{
				for (ItemStack itemToDrop : overflowItems.values())
				{
					Item item = player.getWorld().dropItem(player.getLocation(), itemToDrop);
					EntityItem nmsItem = (EntityItem) ((CraftItem) item).getHandle();
					nmsItem.a(player.getName());
				}
			}

			
			amount -= stackAmount;
		}
		while (amount > 0);
	}		

}
