package us.corenetwork.mantle.restockablechests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.server.v1_8_R1.NBTTagCompound;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import us.core_network.cornel.items.NbtYaml;
import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.NodeParser;
import us.corenetwork.mantle.nanobot.commands.LoadCommand;
import us.corenetwork.mantle.util.MinecraftNames;


public class LootTableNodeParser extends NodeParser {
	private String tableName;

	private static List<ItemStack> result;
	private static ItemStack curItemStack;
	private YamlConfiguration config;

	public LootTableNodeParser(String name, double chanceMultiplier, double chanceAdder, YamlConfiguration config)
	{
		super(chanceMultiplier, chanceAdder);
		this.tableName = name;
		this.config = config;
	}

	public List<ItemStack> parse()
	{
		List<?> node = (List<?>) config.getList("LootTables." + tableName + ".Items");
		result = new ArrayList<ItemStack>();

		if (node == null)
		{
			MLog.warning("Invalid Loot tables config! Loot table " + tableName +" does not exists!");
			return result;	
		}

		parseNodeList(node);

		return result;
	}

	@Override
	protected void parseNode(String type, LinkedHashMap<?, ?> node) {
		if (type.equalsIgnoreCase("item"))
			parseItem(node);
		else if (type.equalsIgnoreCase("enchant"))
			parseEnchant(node);	}


	private void parseEnchant(LinkedHashMap<?,?> node)
	{
		Integer id = (Integer) node.get("id");
		if (id == null)
		{
			MLog.warning("Invalid Loot tables config! Enchant ID is missing!");
			return;
		}

		Integer level = (Integer) node.get("level");
		if (level == null) level = 1;

		int curLevel = curItemStack.getEnchantmentLevel(Enchantment.getById(id));

		curItemStack.addUnsafeEnchantment(Enchantment.getById(id), level + curLevel);
	}


	private void parseItem(LinkedHashMap<?,?> node)
	{
		Integer id = (Integer) node.get("id");
		if (id == null)
		{
			if (node.containsKey("name")) {
				String name = (String) node.get("name");
				Integer material = MinecraftNames.getMaterialId(name);
				if (material != null) {
					id = material;
				} else {
					MLog.warning("Can't find material for name " + name);
					return;
				}
			}
			else
			{
				MLog.warning("Invalid Loot tables config! Item ID is missing!");
			}
		}

		Integer amount = (Integer) node.get("amount");
		if (amount == null) amount = 1;

		Integer damage = (Integer) node.get("damage");
		if (damage == null) damage = 0;

		ItemStack stack = new ItemStack(id, amount, damage.shortValue());
		curItemStack = stack;


		Object yamlNbtTag = node.get("nbt");
		if (yamlNbtTag != null)
		{
			NBTTagCompound newTag;
			if (yamlNbtTag instanceof String)
			{
				try
				{
					newTag = NbtYaml.loadFromFile((String) yamlNbtTag);
				}
				catch (IOException e)
				{
					MLog.warning("Invalid Loot tables config! Nanobot file " + ((String) yamlNbtTag) + ".yml failed loading!");
					return;
				}
				catch (InvalidConfigurationException e)
				{
					MLog.warning("Invalid Loot tables config! Nanobot file " + ((String) yamlNbtTag) + ".yml is invalid YAML file!");
					return;
				}
			}
			else 
			{
				newTag = NbtYaml.loadFromNodes((Map<?, ?>) yamlNbtTag);
			}
			
			if (newTag != null)
			{
				net.minecraft.server.v1_8_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(curItemStack);
				nmsStack.setTag(newTag);
				curItemStack = CraftItemStack.asCraftMirror(nmsStack);
			}
			
		}

		List<?> enchants = (List<?>) node.get("enchants");
		if (enchants != null)
			parseNodeList(enchants);



		result.add(curItemStack);
	}
}
