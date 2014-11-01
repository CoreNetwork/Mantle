package us.corenetwork.mantle.restockablechests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public enum RChestSettings {	
	
	USE_ONLY_CHEST_GUI("UseOnlyChestGUI", true),
	
	DUMMY_LOOT_TABLE_OW("DummyLootTableOW", "OWVill"),
	
	DIMINISH_CHECKER_INTERNVAL("Diminish.CheckInterval", 1200),
	
	DIMINISH_VILLAGE("Diminish.Village.DiminishPerChest", 0.07),
	DIMINISH_RESTORE_VILLAGE("Diminish.Village.RestoreBy", 0.07),
	DIMINISH_RESTORE_INTERVAL_VILLAGE("Diminish.Village.RestoreInterval", 86400),
	
	DIMINISH_TOTAL("Diminish.Total.DiminishPerChest", 0.01),
	DIMINISH_RESTORE_TOTAL("Diminish.Total.RestoreBy", 0.01),
	DIMINISH_RESTORE_INTERVAL_TOTAL("Diminish.Total.RestoreInterval", 7200),
	
	BASIC_CATEGORIES("BasicCategories",new ArrayList<Map<String, Object>>(){{
		add(new HashMap<String, Object>(){{
			put("lootTable", "Organic");
			put("weight",10);
			put("chances", new ArrayList<Double>(){{ add(1.0); add(0.05); add(0.01);}});
		}});
		add(new HashMap<String, Object>(){{
			put("lootTable", "Tools");
			put("weight",10);
			put("chances", new ArrayList<Double>(){{ add(1.0); add(0.05); add(0.01);}});
		}});
		add(new HashMap<String, Object>(){{
			put("lootTable", "CombatGear");
			put("weight",10);
			put("chances", new ArrayList<Double>(){{ add(1.0); add(0.05); add(0.01);}});
		}});
	}}),
	RARE_CATEGORIES("RareCategories",new ArrayList<Map<String, Object>>(){{
		add(new HashMap<String, Object>(){{
			put("lootTable", "Obsidian");
			put("weight",20);
			put("chances", new ArrayList<Double>(){{ add(0.15); add(0.01); add(0.001);}});
			put("perPlayerTotalLimit", 50);
			put("perPlayerDayLimit", 5);
		}});
		add(new HashMap<String, Object>(){{
			put("lootTable", "Gold");
			put("weight",10);
			put("chances", new ArrayList<Double>(){{ add(0.10); add(0.01); add(0.001);}});
			put("perPlayerTotalLimit", 50);
			put("perPlayerDayLimit", 5);
		}});
		add(new HashMap<String, Object>(){{
			put("lootTable", "Diamond");
			put("weight",10);
			put("chances", new ArrayList<Double>(){{ add(0.10); add(0.01); add(0.001);}});
			put("perPlayerTotalLimit", 50);
			put("perPlayerDayLimit", 5);
		}});
		add(new HashMap<String, Object>(){{
			put("lootTable", "EnchantedBook");
			put("weight",15);
			put("chances", new ArrayList<Double>(){{ add(0.20); add(0.01); add(0.001);}});
			put("perPlayerTotalLimit", 50);
			put("perPlayerDayLimit", 5);
		}});
		add(new HashMap<String, Object>(){{
			put("lootTable", "IronHorseArmor");
			put("weight",10);
			put("chances", new ArrayList<Double>(){{ add(0.10); add(0.01); add(0.001);}});
			put("perPlayerTotalLimit", 50);
			put("perPlayerDayLimit", 5);
			put("preReqCategories", new ArrayList<String>(){{ add("Organic"); add("Tools");}});
		}});
	}}),
	
	MESSAGE_RIGHT_CLICK_CHEST_WITH_ARM("Messages.RightClickChestWithArm", "Right click chest with your arm to finish creating it!"),
	MESSAGE_LOOTING_TABLE_DOES_NOT_EXIST("Messages.LootingTableDoesNotExist", "That looting table does not exist!"),
	MESSAGE_CHEST_CREATED("Messages.ChestCreated", "Restockable chest created."),
	MESSAGE_CHEST_EXISTS("Messages.ChestExists", "Chest already exists! Break it to delete it."),
	MESSAGE_CHEST_DELETED("Messages.ChestDeleted", "Restockable chest deleted."),
	MESSAGE_CHEST_INVISIBLE("Messages.ChestInvisible", "Sorry, you can't use this chest while invisible"),
	MESSAGE_CHEST_INVINCIBLE("Messages.ChestInvicible", "Sorry, you can't use this chest while invincible. Use /RiddleFillThisInIforgotCommandName to disable your invincibility."),
	MESSAGE_CHESTS_RESTOCKED("Messages.ChestsRestocked", "Chests Restocked");

	
	protected String string;
	protected Object def;
	
	private RChestSettings(String string, Object def)
	{
		this.string = string;
		this.def = def;
	}

	public double doubleNumber()
	{
		return ((Number) RChestsModule.instance.config.get(string, def)).doubleValue();
	}
	
	public Integer integer()
	{
		return (Integer) RChestsModule.instance.config.get(string, def);
	}
	
	public Boolean bool()
	{
		return (Boolean) RChestsModule.instance.config.get(string, def);
	}
	
	public String string()
	{
		return (String) RChestsModule.instance.config.get(string, def);
	}
	
	public static String getCommandDescription(String cmd, String def)
	{
		String path = "CommandDescriptions." + cmd;
		
		Object descO = RChestsModule.instance.config.get(path);
		if (descO == null)
		{
			RChestsModule.instance.config.set(path, "&a/chp " + cmd + " &8-&f " + def);
			RChestsModule.instance.saveConfig();
			descO = RChestsModule.instance.config.get(path);
		}
		
		return (String) descO;
		
	}
}
