package us.corenetwork.mantle.spellbooks;

import java.util.HashMap;

import us.corenetwork.mantle.spellbooks.books.GrowthBook;


public class SpellbookManager {
	private static HashMap<String, Spellbook> books = new HashMap<String, Spellbook>();
	
	public static void init()
	{
		addSpellbook(new GrowthBook());
	}
	
	public static Spellbook getBook(String name)
	{
		return books.get(name);
	}
	
	private static void addSpellbook(Spellbook book)
	{
		books.put(book.getName(), book);
	}
}
