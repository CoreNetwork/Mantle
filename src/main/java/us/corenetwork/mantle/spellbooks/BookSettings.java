package us.corenetwork.mantle.spellbooks;

import java.util.List;
import us.corenetwork.mantle.MLog;


public class BookSettings {
	private String book;
	public BookSettings(String book)
	{
		this.book = book;
	}
	
	public void setDefault(String setting, Object value)
	{
		String fullPath = "Spells.".concat(book).concat(".").concat(setting);
		Object property = SpellbooksModule.instance.config.get(fullPath);
		if (property == null)
		{
			SpellbooksModule.instance.config.set(fullPath, value);
			SpellbooksModule.instance.saveConfig();
		}
	}
	
	public Object getProperty(String setting)
	{
		return getProperty(setting, true);
	}

	
	public Object getProperty(String setting, boolean warnIfNull)
	{
		String fullPath = "Spells.".concat(book).concat(".").concat(setting);
		Object property = SpellbooksModule.instance.config.get(fullPath);
		if (property == null && warnIfNull)
		{
			MLog.warning("[Mantle][Spellbooks] Configuration entry missing: " + fullPath);	
		}
		
		return property;
	}
	
	public Boolean getBoolean(String setting)
	{
		return 	(Boolean) getProperty(setting);
	}
	
	public long getLong(String setting)
	{
		return 	((Number) getProperty(setting)).longValue();
	}
	
	public int getInt(String setting)
	{
		return 	((Number) getProperty(setting)).intValue();
	}
	
	public double getDouble(String setting)
	{
		return 	((Number) getProperty(setting)).doubleValue();
	}

	public String getString(String setting)
	{
		return 	(String) getProperty(setting);
	}	
	
	public List<String> getStringList(String setting)
	{
		return 	(List<String>) getProperty(setting);
	}
	
	public List<?> getList(String setting)
	{
		return 	(List<?>) getProperty(setting);
	}
}
