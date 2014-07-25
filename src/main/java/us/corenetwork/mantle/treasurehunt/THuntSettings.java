package us.corenetwork.mantle.treasurehunt;

import java.util.List;


public enum THuntSettings {

	;
	protected String string;
	protected Object def;
	
	private THuntSettings(String string, Object def)
	{
		this.string = string;
		this.def = def;
	}
	
	
	public String string()
	{
		return (String) THuntModule.instance.config.get(string, def);
	}
	
	public List<String> stringList()
	{
		return (List<String>) THuntModule.instance.config.get(string, def);
	}
}
