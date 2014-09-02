package us.corenetwork.mantle.spellbooks;

import java.text.SimpleDateFormat;
import java.util.List;

public enum SpellbooksSettings {
    EXPIRE_OFFSET_SECONDS("ExpireOffsetSeconds", 43200),

    LORE_DATE_STORE("Lores.DateStore", "'Expires on' yyyy-MM-dd 'at 12:00 GMT+0'"),
    DATE_STORE_BEGINNING("DateStoreBeginning", "Expires on "),
    LORE_BOOK_DAYS_LEFT("Lores.BookDaysLeft", "This book will expire in around <Days> day<PluralS>!"),
    LORE_BOOK_EXPIRED("Lores.BookExpired", "Book expired! Feel free to throw it into lava."),
    LORE_BOOK_LESS_THAN_DAY("Lores.BookLessThanDayLeft", "This book will expire very shortly! Use it quickly!"),

    MESSAGE_RESLIME_SUCCESS("Messages.ReslimeSuccess", "No-slime spell was successfully lifted from this chunk!"),
    MESSAGE_RESLIME_FAIL("Messages.ReslimeFail", "This chunk is not under unsliming spell!"),

    MESSAGE_BOOK_EXPIRED("Messages.BookExpired", "Sorry, your book has expired!"),
    MESSAGE_NO_PERMISSION("Messages.NoPermission", "Sorry, you can't use spellbook on territory owned by somebody else!");

	protected String string;
	protected Object def;
	
	private SpellbooksSettings(String string, Object def)
	{
		this.string = string;
		this.def = def;
	}

	public double doubleNumber()
	{
		return ((Number) SpellbooksModule.instance.config.get(string, def)).doubleValue();
	}
	
	public Integer integer()
	{
		return (Integer) SpellbooksModule.instance.config.get(string, def);
	}
	
	public String string()
	{
		return (String) SpellbooksModule.instance.config.get(string, def);
	}
		
	public List<String> stringList()
	{
		return (List<String>) SpellbooksModule.instance.config.get(string, def);
	}

    public static SimpleDateFormat expireDateStorageFormat;
}
