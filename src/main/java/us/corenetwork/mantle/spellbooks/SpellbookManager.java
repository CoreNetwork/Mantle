package us.corenetwork.mantle.spellbooks;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.spellbooks.books.AllureBook;
import us.corenetwork.mantle.spellbooks.books.DeadweightBook;
import us.corenetwork.mantle.spellbooks.books.DecayBook;
import us.corenetwork.mantle.spellbooks.books.ForgingBook;
import us.corenetwork.mantle.spellbooks.books.FusingBook;
import us.corenetwork.mantle.spellbooks.books.GrazingBook;
import us.corenetwork.mantle.spellbooks.books.GrowthBook;
import us.corenetwork.mantle.spellbooks.books.PeddlingBook;
import us.corenetwork.mantle.spellbooks.books.PruningBook;
import us.corenetwork.mantle.spellbooks.books.SmithingBook;
import us.corenetwork.mantle.spellbooks.books.TimeBook;
import us.corenetwork.mantle.spellbooks.books.TimeTravelBook;
import us.corenetwork.mantle.spellbooks.books.UnslimingBook;
import us.corenetwork.mantle.spellbooks.books.WindBook;


public class SpellbookManager {
	private static HashMap<String, Spellbook> books = new HashMap<String, Spellbook>();

	public static void init()
	{
		addSpellbook(new GrowthBook());
		addSpellbook(new TimeTravelBook()); //Duplicate book used for backwards compatibility. Remove on next map
		addSpellbook(new TimeBook());
		addSpellbook(new DeadweightBook());
		addSpellbook(new WindBook());
		addSpellbook(new DecayBook());
		addSpellbook(new FusingBook());
		addSpellbook(new UnslimingBook());
		addSpellbook(new PeddlingBook());
		addSpellbook(new ForgingBook());
        addSpellbook(new SmithingBook());
        addSpellbook(new AllureBook());
        addSpellbook(new PruningBook());
        addSpellbook(new GrazingBook());
    }
	
	public static Spellbook getBook(String name)
	{		
		return books.get(name.toLowerCase());
	}
		
	private static void addSpellbook(Spellbook book)
	{
		books.put(book.getName().toLowerCase(), book);
		books.put(("Spell of " + book.getName()).toLowerCase(), book);
		books.put(("Spellbook of " + book.getName()).toLowerCase(), book); //Compatibility reasons, remove on new map

		if (book instanceof Listener)
			Bukkit.getPluginManager().registerEvents((Listener) book, MantlePlugin.instance);
	}
}
