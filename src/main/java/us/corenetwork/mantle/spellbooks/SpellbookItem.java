package us.corenetwork.mantle.spellbooks;

import java.text.ParseException;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.nanobot.NanobotUtil;

public class SpellbookItem {
	private ItemStack item;
	private Spellbook spellbook;
	private String owner;
	private int expireTime;

    private int expireTimeLoreLineId;
	
	private SpellbookItem()
	{
		owner = null;
	}
		
	public ItemStack getBookItem()
	{
		return item;
	}
	
	public Spellbook getSpellbook()
	{
		return spellbook;
	}
	
	public int getExpiringTime()
    {
        return expireTime;
    }
    public int getExpiringTimeLoreLineId()
    {
        return expireTimeLoreLineId;
    }

	public static SpellbookItem parseSpellbook(ItemStack itemStack)
	{
		if (itemStack.getType() != Material.ENCHANTED_BOOK && itemStack.getType() != Material.BOOK)
			return null;
		
		ItemMeta meta = itemStack.getItemMeta();
		
		if (!meta.hasDisplayName())
			return null;
		
		String name = ChatColor.stripColor(meta.getDisplayName());
		
		Spellbook book = SpellbookManager.getBook(name);
		if (book == null)
			return null;
		
		String owner = null;
        int expireTime = -1;
        int expireTimeLoreLineId = -1;
        String expireTimeStart = NanobotUtil.fixFormatting(SpellbooksSettings.DATE_STORE_BEGINNING.string());
		if (meta.getLore() != null)
		{
			for (int i = 0; i < meta.getLore().size(); i++)
			{
                String line = meta.getLore().get(i);

                String noColorLine = ChatColor.stripColor(line);
				if (noColorLine.startsWith("Soulbound to "))
				{
					owner = noColorLine.substring(13);
				}
                else if (line.startsWith(expireTimeStart))
                {
                    try
                    {
                        expireTime = (int) (SpellbooksSettings.expireDateStorageFormat.parse(line).getTime() / 1000 + SpellbooksSettings.EXPIRE_OFFSET_SECONDS.integer());
                        expireTimeLoreLineId = i;
                    }
                    catch (ParseException e)
                    {
                        MLog.severe("Invalid date format on book: " + line);

                        expireTime = -1;
                        expireTimeLoreLineId = -1;
                    }
                }
			}
		}
		
		SpellbookItem item = new SpellbookItem();
		item.item = itemStack;
		item.spellbook = book;
		item.owner = owner;
        item.expireTime = expireTime;
        item.expireTimeLoreLineId = expireTimeLoreLineId;
		
		return item;
	}	
}
