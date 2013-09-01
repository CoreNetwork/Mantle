package us.corenetwork.mantle.spellbooks;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.mantlecommands.BaseAdminCommand;


public class BindBookCommand extends BaseAdminCommand {	
	public BindBookCommand()
	{
		desc = "Bind book to player";
		needPlayer = true;
	}


	public Boolean run(final CommandSender sender, String[] args) {
		
		Player player = (Player) sender;
		
		ItemStack item = player.getItemInHand();
		
		if (item == null || item.getType() != Material.ENCHANTED_BOOK)
		{
			Util.Message("You need to have enchanted book in hand!", sender);
			return true;
		}
		
		ItemMeta meta = item.getItemMeta();

		if (!meta.hasLore())
		{
			Util.Message("Book needs to have lore!", sender);
			return true;
		}
			
		List<String> loreList = meta.getLore();
		
		for (int i = 0; i < loreList.size(); i++)
		{
			String lore = ChatColor.stripColor(loreList.get(i));
			if (lore.startsWith("Soulbound to "))
			{
				loreList.set(i, "");
			}
		}
		
		if (args.length > 0)
		{
			String owner = args[0];
			String line = ChatColor.COLOR_CHAR + "bSoulbound to " + owner;
			
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
						
			Util.Message("Book is now bound to " + owner + ".", sender);
		}
		else
		{
			Util.Message("Binding removed.", sender);
		}
		
		meta.setLore(loreList);
		item.setItemMeta(meta);
		player.setItemInHand(item);

		
		return true;
	}	
}
