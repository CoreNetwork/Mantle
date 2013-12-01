package us.corenetwork.mantle.spellbooks;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.mantlecommands.BaseMantleCommand;


public class BindBookCommand extends BaseMantleCommand {	
	public BindBookCommand()
	{
		permission = "bindbook";
		desc = "Bind book to player";
		needPlayer = true;
	}


	public void run(final CommandSender sender, String[] args) {
		
		Player player = (Player) sender;
		
		String owner = null;
		if (args.length > 0 && !args[0].equals("all"))
		{
			owner = args[0];
		}
		
		if ((args.length > 0 && args[0].equals("all") || (args.length > 1 && args[1].equals("all")) ))
		{
			for (int i = 0; i < player.getInventory().getSize(); i++)
			{
				ItemStack item = player.getInventory().getItem(i);
				if (item == null || item.getType() != Material.ENCHANTED_BOOK)
				{
					continue;
				}

				ItemMeta meta = item.getItemMeta();

				if (!meta.hasLore())
				{
					continue;
				}
				
				bind(meta, owner);
				
				item.setItemMeta(meta);
				player.getInventory().setItem(i, item);

			}
			
			if (owner != null)
			{
				Util.Message("All books are now bound to " + owner + ".", sender);
			}
			else
			{
				Util.Message("Binding removed from all books.", sender);
			}
		}
		else
		{
			ItemStack item = player.getItemInHand();
			if (item == null || item.getType() != Material.ENCHANTED_BOOK)
			{
				Util.Message("You need to have enchanted book in hand!", sender);
				return;
			}

			ItemMeta meta = item.getItemMeta();

			if (!meta.hasLore())
			{
				Util.Message("Book needs to have lore!", sender);
				return;
			}
			
			bind(meta, owner);
			
			item.setItemMeta(meta);
			player.setItemInHand(item);
			
			if (owner != null)
			{
				Util.Message("Book is now bound to " + owner + ".", sender);
			}
			else
			{
				Util.Message("Binding removed.", sender);
			}
		}
				
	}	
	
	private static void bind(ItemMeta meta, String owner)
	{
		List<String> loreList = meta.getLore();
		
		for (int i = 0; i < loreList.size(); i++)
		{
			String lore = ChatColor.stripColor(loreList.get(i));
			if (lore.startsWith("Soulbound to "))
			{
				loreList.set(i, "");
			}
		}
		
		if (owner != null)
		{
			String line = ChatColor.COLOR_CHAR + "8Soulbound to " + owner;
			
			boolean ownershipSet = false;
			
			for (int i = loreList.size() - 1; i >= 0; i--)
			{
				if (loreList.get(i).trim().equals(""))
				{
					loreList.set(i, line);
					ownershipSet = true;
					break;
				}
			}
			
			if (!ownershipSet)
				loreList.add(line);
						
		}
		
		meta.setLore(loreList);
	}
}
