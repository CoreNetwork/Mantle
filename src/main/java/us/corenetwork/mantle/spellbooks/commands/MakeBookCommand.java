package us.corenetwork.mantle.spellbooks.commands;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import net.minecraft.server.v1_8_R1.EntityItem;
import net.minecraft.server.v1_8_R1.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_8_R1.inventory.CraftItemStack;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import us.core_network.cornel.items.NbtYaml;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.mantlecommands.BaseMantleCommand;
import us.corenetwork.mantle.nanobot.commands.LoadCommand;
import us.corenetwork.mantle.spellbooks.Spellbook;
import us.corenetwork.mantle.spellbooks.SpellbookManager;


public class MakeBookCommand extends BaseMantleCommand {	
	public MakeBookCommand()
	{
		permission = "makebook";
		desc = "Make spellbook";
		needPlayer = true;
	}


	public void run(final CommandSender sender, String[] args) {
		if (args.length < 1) {
			sender.sendMessage("Syntax: /mantle makebook [spell name] (number)");
			return;
		}

		String spellName = args[0];
		Spellbook spell = SpellbookManager.getBook(spellName);
		if (spell == null)
		{
			sender.sendMessage("Spell with that name was not found! Message moderators about that issue!");
			return;

		}
		
		int amount = 1;
		if (args.length > 1 && Util.isInteger(args[1]))
		{
			amount = Integer.parseInt(args[1]);
		}
		
		Player player = (Player) sender;

		String tag = spell.settings.getString(Spellbook.SETTING_TEMPLATE);
		NBTTagCompound newTag = null;
		try
		{
			newTag = NbtYaml.loadFromFile(tag);
		}
		catch (FileNotFoundException e)
		{
			sender.sendMessage("Tag with that name could not be found! Message moderators about that issue!");
			return;
		}
		catch (IOException e)
		{
			sender.sendMessage("Tag with that name could not be loaded! Message moderators about that issue!");
			e.printStackTrace();
			return;
		}
		catch (InvalidConfigurationException e)
		{
			sender.sendMessage("Tag with that name was not yaml file! Message moderators about that issue!");
			return;

		}

		do
		{
			int stackAmount = Math.min(amount, 64);
			ItemStack stack = new ItemStack(Material.BOOK, stackAmount);
			
			net.minecraft.server.v1_8_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
			nmsStack.setTag(newTag);
			stack = CraftItemStack.asCraftMirror(nmsStack);

			HashMap<Integer, ItemStack> overflowItems = player.getInventory().addItem(stack);
			if (overflowItems.size() > 0) //Drop books on the ground if inventory is full
			{
				for (ItemStack itemToDrop : overflowItems.values())
				{
					Item item = player.getWorld().dropItem(player.getLocation(), itemToDrop);
					EntityItem nmsItem = (EntityItem) ((CraftItem) item).getHandle();
					nmsItem.b(player.getName()); //Prevent other players from picking up this item
				}
			}

			
			amount -= stackAmount;
		}
		while (amount > 0);
	}		

}
