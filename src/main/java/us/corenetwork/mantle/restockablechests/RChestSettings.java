package us.corenetwork.mantle.restockablechests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import us.corenetwork.mantle.YamlUtils;
import us.corenetwork.mantle.beacons.BeaconsModule;

public enum RChestSettings {	
	
	USE_ONLY_CHEST_GUI("UseOnlyChestGUI", true),
	
	GUI_TITLE_PICK_CATEGORY("GuiCompassTitle", "Pick category"),
	
	GUI_ITEM_CATEGORY_OK("GuiItems.CategoryOk", new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.GREEN.getData())),
    GUI_ITEM_CATEGORY_NOPE("GuiItems.CategoryNope", new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.getData())),
	
	DUMMY_LOOT_TABLE_OW("DummyLootTableOW", "OWVill"),
	
	DIMINISH_CHECKER_INTERNVAL("Diminish.CheckInterval", 1200),
	
	DIMINISH_VILLAGE("Diminish.Village.DiminishPerChest", 0.07),
	DIMINISH_RESTORE_VILLAGE("Diminish.Village.RestoreBy", 0.07),
	DIMINISH_RESTORE_INTERVAL_VILLAGE("Diminish.Village.RestoreInterval", 86400),
	
	DIMINISH_TOTAL("Diminish.Total.DiminishPerChest", 0.01),
	DIMINISH_RESTORE_TOTAL("Diminish.Total.RestoreBy", 0.01),
	DIMINISH_RESTORE_INTERVAL_TOTAL("Diminish.Total.RestoreInterval", 7200),

	COMPASS_NBT_TAG("CompassNBTTag", "Compass"),
	COMPASS_OLD_DESTINATION_EXPIRATION_TIME_SECONDS("CompassOldDestinationExpirationTimeSeconds", 600),

	BASIC_CATEGORIES("BasicCategories",new ArrayList<Map<String, Object>>(){{
		add(new HashMap<String, Object>(){{
			put("lootTable", "Organic");
			put("weight",10);
			put("chances", new ArrayList<Double>(){{ add(1.0); add(0.05); add(0.01);}});
			put("icon", RChestSettings.iconHelper(6));
			put("compassMinDistance", new ArrayList<Integer>(){{add(200);add(200);add(200);}});
			put("compassMaxDistance", new ArrayList<Integer>(){{add(800);add(800);add(800);}});
		}});
		add(new HashMap<String, Object>(){{
			put("lootTable", "Tools");
			put("weight",10);
			put("chances", new ArrayList<Double>(){{ add(1.0); add(0.05); add(0.01);}});
			put("icon", RChestSettings.iconHelper(257));
			put("compassMinDistance", new ArrayList<Integer>(){{add(200);add(200);add(200);}});
			put("compassMaxDistance", new ArrayList<Integer>(){{add(800);add(800);add(800);}});
		}});
		add(new HashMap<String, Object>(){{
			put("lootTable", "CombatGear");
			put("displayName", "Combat Gear");
			put("weight",10);
			put("chances", new ArrayList<Double>(){{ add(1.0); add(0.05); add(0.01);}});
			put("icon", RChestSettings.iconHelper(307));
			put("compassMinDistance", new ArrayList<Integer>(){{add(200);add(200);add(200);}});
			put("compassMaxDistance", new ArrayList<Integer>(){{add(800);add(800);add(800);}});
		}});
	}}),
	RARE_CATEGORIES("RareCategories",new ArrayList<Map<String, Object>>(){{
		add(new HashMap<String, Object>(){{
			put("lootTable", "Obsidian");
			put("weight",20);
			put("chances", new ArrayList<Double>(){{ add(0.15); add(0.01); add(0.001);}});
			put("perPlayerTotalLimit", 50);
			put("icon", RChestSettings.iconHelper(49));
			put("compassMinDistance", new ArrayList<Integer>(){{add(2200);add(2200);add(2200);}});
			put("compassMaxDistance", new ArrayList<Integer>(){{add(4800);add(4800);add(4800);}});
		}});
		add(new HashMap<String, Object>(){{
			put("lootTable", "Gold");
			put("weight",10);
			put("chances", new ArrayList<Double>(){{ add(0.10); add(0.01); add(0.001);}});
			put("perPlayerTotalLimit", 50);
			put("icon", RChestSettings.iconHelper(266));
			put("compassMinDistance", new ArrayList<Integer>(){{add(2200);add(2200);add(2200);}});
			put("compassMaxDistance", new ArrayList<Integer>(){{add(4800);add(4800);add(4800);}});
		}});
		add(new HashMap<String, Object>(){{
			put("lootTable", "Diamond");
			put("weight",10);
			put("chances", new ArrayList<Double>(){{ add(0.10); add(0.01); add(0.001);}});
			put("perPlayerTotalLimit", 50);
			put("icon", RChestSettings.iconHelper(264));
			put("compassMinDistance", new ArrayList<Integer>(){{add(2200);add(2200);add(2200);}});
			put("compassMaxDistance", new ArrayList<Integer>(){{add(4800);add(4800);add(4800);}});
		}});
		add(new HashMap<String, Object>(){{
			put("lootTable", "EnchantedBook");
			put("displayName", "Enchanted Book");
			put("weight",15);
			put("chances", new ArrayList<Double>(){{ add(0.20); add(0.01); add(0.001);}});
			put("perPlayerTotalLimit", 50);
			put("icon", RChestSettings.iconHelper(403));
			put("compassMinDistance", new ArrayList<Integer>(){{add(2200);add(2200);add(2200);}});
			put("compassMaxDistance", new ArrayList<Integer>(){{add(4800);add(4800);add(4800);}});
		}});
		add(new HashMap<String, Object>(){{
			put("lootTable", "IronHorseArmor");
			put("displayName", "Iron Horse Armor");
			put("weight",10);
			put("chances", new ArrayList<Double>(){{ add(0.10); add(0.01); add(0.001);}});
			put("perPlayerTotalLimit", 50);
			put("preReqCategories", new ArrayList<String>(){{ add("Organic"); add("Tools");}});
			put("icon", RChestSettings.iconHelper(417));
			put("compassMinDistance", new ArrayList<Integer>(){{add(2200);add(2200);add(2200);}});
			put("compassMaxDistance", new ArrayList<Integer>(){{add(4800);add(4800);add(4800);}});
		}});
	}}),
	
	MESSAGE_RIGHT_CLICK_CHEST_WITH_ARM("Messages.RightClickChestWithArm", "Right click chest with your arm to finish creating it!"),
	MESSAGE_LOOTING_TABLE_DOES_NOT_EXIST("Messages.LootingTableDoesNotExist", "That looting table does not exist!"),
	MESSAGE_CHEST_CREATED("Messages.ChestCreated", "Restockable chest created."),
	MESSAGE_CHEST_EXISTS("Messages.ChestExists", "Chest already exists! Break it to delete it."),
	MESSAGE_CHEST_DELETED("Messages.ChestDeleted", "Restockable chest deleted."),
	MESSAGE_CHEST_DESTROYED("Messages.ChestDestroyed", "Poof! Loot chest will regenerate unless you claim a village."),
	MESSAGE_CHEST_INVISIBLE("Messages.ChestInvisible", "Sorry, you can't use this chest while invisible"),
	MESSAGE_CHEST_INVINCIBLE("Messages.ChestInvicible", "Sorry, you can't use this chest while invincible. Use /RiddleFillThisInIforgotCommandName to disable your invincibility."),
	MESSAGE_CHESTS_RESTOCKED("Messages.ChestsRestocked", "Chests Restocked"),
	MESSAGE_COMPASS_VILLAGE_CLAIMED("Messages.CompassVillageClaimed", "The village you are heading to got claimed! Recalculating new village"),
	MESSAGE_COMPASS_NAME_WITH_DISTANCE("Messages.CompassNameWithDistance", "&5[<Category>] <Distance> blocks away"),
	MESSAGE_COMPASS_NAME_WITHOUT_DISTANCE("Messages.CompassNameWithoutDistance", "&5Tracking [<Category>]"),
	MESSAGE_COMPASS_NAME_BLANK("Messages.CompassNameBlank", "Loot compass"),
	MESSAGE_COMPASS_CANT_FIND_VILLAGE("Messages.CompassCantFindVillage", "&cCouldn't find any villages close nearby, walk a bit and try again"),
	MESSAGE_COMPASS_VILLAGE_REGENRATED_WHILE_GONE("Messages.CompassVillageRegeneratedWhileGone", "&cTargeted village regenerated while you were gone. Please pick a new one."),
	MESSAGE_OPENING_CHEST_IN_CLAIMED_VILLAGE("Messages.OpeningChestInClaimedVillage", "&cThis chest won't give you loot as the village is claimed!");
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
	
	public ItemStack itemStack()
    {
        if (!RChestsModule.instance.config.contains(string))
            return null;

        ConfigurationSection cs = RChestsModule.instance.config.getConfigurationSection(string);
        
        return YamlUtils.readItemStack(cs.getValues(false));
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
	
	public static HashMap iconHelper(final int id)
	{
		return new HashMap<String, Integer>(){{put("ID", id);put("Amount", 1);put("Damage", 0);}};
	}
}
