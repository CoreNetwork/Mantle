package us.corenetwork.mantle.spellbooks;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.spellbooks.books.DeadweightBook;
import us.corenetwork.mantle.spellbooks.books.DecayBook;
import us.corenetwork.mantle.spellbooks.books.FusingBook;
import us.corenetwork.mantle.spellbooks.books.GrowthBook;
import us.corenetwork.mantle.spellbooks.books.TimeTravelBook;
import us.corenetwork.mantle.spellbooks.books.UnslimingBook;
import us.corenetwork.mantle.spellbooks.books.WindBook;


public class SpellbookManager {
	private static HashMap<String, Spellbook> books = new HashMap<String, Spellbook>();
	
	public static void init()
	{
		addSpellbook(new GrowthBook());
		addSpellbook(new TimeTravelBook());
		addSpellbook(new DeadweightBook());
		addSpellbook(new WindBook());
		addSpellbook(new DecayBook());
		addSpellbook(new FusingBook());
		addSpellbook(new UnslimingBook());
	}	

	
	public static Spellbook getBook(Class<? extends Spellbook> type)
	{
		for (Spellbook book : books.values())
		{
			if (book.getClass() == type)
				return book;
		}
		
		return null;
	}
	
	public static Spellbook getBook(String name)
	{
		return books.get(name);
	}
	
	private static void addSpellbook(Spellbook book)
	{
		books.put(book.getName(), book);
		
		if (book instanceof Listener)
			Bukkit.getPluginManager().registerEvents((Listener) book, MantlePlugin.instance);
	}
}
