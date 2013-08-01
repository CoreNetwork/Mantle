package com.mcnsa.flatcore.restockablechests;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import net.minecraft.server.v1_6_R2.NBTTagCompound;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_6_R2.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import com.matejdro.bukkit.mcnsa.nanobot.commands.LoadCommand;
import com.mcnsa.flatcore.FCLog;
import com.mcnsa.flatcore.NodeParser;

public class LootTableNodeParser extends NodeParser {
	private String tableName;

	private static List<ItemStack> result;
	private static ItemStack curItemStack;
	private YamlConfiguration config;

	private LootTableNodeParser(String name, double chanceMultiplier, double chanceAdder, YamlConfiguration config)
	{
		super(chanceMultiplier, chanceAdder);
		this.tableName = name;
		this.config = config;
	}

	private List<ItemStack> parse()
	{
		List<?> node = (List<?>) config.getList("LootTables." + tableName + ".Items");
		result = new ArrayList<ItemStack>();

		if (node == null)
		{
			FCLog.warning("Invalid Loot tables config! Loot table " + tableName +" does not exists!");
			return result;	
		}

		parseNodeList(node);

		return result;
	}

	public static List<ItemStack> parseTable(String name, double chanceMultiplier, double chanceAdder, YamlConfiguration config)
	{
		return new LootTableNodeParser(name, chanceMultiplier, chanceAdder, config).parse();
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
			FCLog.warning("Invalid Loot tables config! Enchant ID is missing!");
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
			FCLog.warning("Invalid Loot tables config! Item ID is missing!");
			return;
		}

		Integer amount = (Integer) node.get("amount");
		if (amount == null) amount = 1;

		Integer damage = (Integer) node.get("damage");
		if (damage == null) damage = 0;

		ItemStack stack = new ItemStack(id, amount, damage.shortValue());
		curItemStack = stack;


		LinkedHashMap<?,?> yamlNbtTag = (LinkedHashMap<?,?>) node.get("nbt");
		if (yamlNbtTag != null)
		{
			NBTTagCompound newTag = LoadCommand.load(yamlNbtTag);

			net.minecraft.server.v1_6_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(curItemStack);
			nmsStack.tag = newTag;
			curItemStack = CraftItemStack.asBukkitCopy(nmsStack);
		}

		List<?> enchants = (List<?>) node.get("enchants");
		if (enchants != null)
			parseNodeList(enchants);



		result.add(curItemStack);
	}



}
