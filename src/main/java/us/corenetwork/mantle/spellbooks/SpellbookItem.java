package us.corenetwork.mantle.spellbooks;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SpellbookItem {	
	private ItemStack item;
	private Spellbook spellbook;
	private String owner;
	
	
	private SpellbookItem()
	{
		owner = null;
	}
	
	public boolean isSoulbound()
	{
		return owner != null;
	}
	
	public String getSoulboundOwner()
	{
		return owner;
	}
	
	public ItemStack getBookItem()
	{
		return item;
	}
	
	public Spellbook getSpellbook()
	{
		return spellbook;
	}
	
	public static SpellbookItem parseSpellbook(ItemStack itemStack)
	{
		if (itemStack.getType() != Material.ENCHANTED_BOOK)
			return null;
		
		ItemMeta meta = itemStack.getItemMeta();
		
		if (!meta.hasDisplayName())
			return null;
		
		String name = ChatColor.stripColor(meta.getDisplayName());
		
		Spellbook book = SpellbookManager.getBook(name);
		if (book == null)
			return null;
		
		String owner = null;
		for (String s : meta.getLore())
		{
			s = ChatColor.stripColor(s);
			if (s.startsWith("Soulbound to "))
			{
				owner = s.substring(13);
				break;
			}
		}
		
		SpellbookItem item = new SpellbookItem();
		item.item = itemStack;
		item.spellbook = book;
		item.owner = owner;
		
		return item;
	}
	
}
