package com.mcnsa.flatcore;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.gadberry.utility.expression.ArgumentCastException;
import com.gadberry.utility.expression.Expression;
import com.gadberry.utility.expression.InvalidExpressionException;

public class NodeParser {
	private static List<ItemStack> result;
	private static ItemStack curItemStack;
	private static Random random = new Random();

	private static EntityDamageEvent event;

	private static double chanceMultiplier;
	private static double chanceAdder;

	public static void parseDamageEvent(EntityDamageEvent event, String eventName)
	{
		
		NodeParser.event = event;
		NodeParser.chanceMultiplier = 1;
		NodeParser.chanceAdder = 0;

		List<?> node = (List<?>) IO.config.getList("DamageModifiers." + eventName);
		if (node == null)
			return;

		parseNodeList(node);
	}
	
	public static void parseDamageEvent(EntityDamageEvent event)
	{
		parseDamageEvent(event, event.getCause().toString());
	}

	public static List<ItemStack> parseTable(String name, double chanceMultiplier, double chanceAdder)
	{
		List<?> node = (List<?>) IO.config.getList("LootTables." + name + ".Items");
		result = new ArrayList<ItemStack>();
		NodeParser.chanceMultiplier = chanceMultiplier;
		NodeParser.chanceAdder = chanceAdder;

		if (node == null)
		{
			FCLog.warning("Invalid Loot tables config! Loot table " + name +" does not exists!");
			return result;			
		}

		parseNodeList(node);

		return result;
	}

	private static void parseNodeList(List<?> node)
	{
		for (Object setObject : node)
		{
			LinkedHashMap<?,?> hashMap = (LinkedHashMap<?,?>) setObject;
			Entry<?,?> firstEntry = hashMap.entrySet().toArray(new Entry<?,?>[0])[0];

			String type = (String) firstEntry.getKey();

			parseNodeObject(type, firstEntry.getValue());
		}
	}

	private static void parseNodeObject(String type, Object node)
	{
		if (node instanceof List)
		{
			if (type.toLowerCase().startsWith("pick"))
				parsePickList(type, (List<?>) node);
			else
				parseNodeList((List<?>) node);
		}
		else if (node instanceof LinkedHashMap)
			parseNode(type, (LinkedHashMap<?,?>) node);

	}

	private static void parsePickList(String params, List<?> node)
	{
		int count = getNumberOfRolls(node, false);

		int childCount = 0;
		for (Object o : node)
			if (o instanceof LinkedHashMap) childCount++;

		int[] weights = new int[childCount];
		for (int i = 0; i < childCount; i++)
			weights[i] = 1;

		int pickCount = 1;
		String paramSplit[] = params.split(" ");
		if (paramSplit.length > 1)
			pickCount = Integer.parseInt(paramSplit[1]);

		if (pickCount > childCount)
		{
			FCLog.warning("Invalid Loot tables config! Amount of items to pick must be smaller or equal to amount of items!");
			return;
		}

		for (Object o : node)
		{
			if (o instanceof String)
			{
				String text = (String) o;
				if (text.startsWith("weights "))
				{
					String[] textSplit = text.split(" ");
					for (int i = 0; i < childCount; i++)
					{
						weights[i] = Integer.parseInt(textSplit[i + 1]);
					}
				}

			}
		}


		int weightsSum = 0;
		for (int i = 0; i < childCount; i++)
			weightsSum += weights[i];


		for (int a = 0; a < count; a++)
		{
			List<Integer> pickedItems = new ArrayList<Integer>();
			for (int b = 0; b < pickCount; b++)
			{
				int selection = 0;
				do
				{
					int pickedNumber = random.nextInt(weightsSum);
					int sum = 0;
					for (int i = 0; i < childCount; i++)
					{
						sum += weights[i];
						if (pickedNumber < sum)
						{
							selection = i;
							break;
						}
					}
				}
				while (pickedItems.contains(selection));

				pickedItems.add(selection);

				int counter = -1;
				for (int i = 0; i < node.size(); i++)
				{
					Object o = node.get(i);
					if (o instanceof LinkedHashMap) 
						counter++;
					else
						continue;

					if (counter == selection)
					{
						LinkedHashMap<?,?> hashMap = (LinkedHashMap<?,?>) o;
						Entry<?,?> firstEntry = hashMap.entrySet().toArray(new Entry<?,?>[0])[0];
						parseNodeObject((String) firstEntry.getKey(), firstEntry.getValue());
						break;
					}

				}

			}
		}
	}

	private static void parseNode(String type, LinkedHashMap<?,?> node)
	{
		int count = getNumberOfRolls(node, true);
		for (int i = 0; i < count; i++)
		{
			if (type.equalsIgnoreCase("item"))
				parseItem(node);
			else if (type.equalsIgnoreCase("enchant"))
				parseEnchant(node);
			else if (type.equalsIgnoreCase("setdamage"))
				parseSetDamage(node);
			else if (type.equalsIgnoreCase("adddamage"))
				parseAddDamage(node);
			else if (type.equalsIgnoreCase("multiplydamage"))
				parseMultiplyDamage(node);
			else if (type.equalsIgnoreCase("addpotioneffect"))
				parseAddPotionEffect(node);

		}

	}

	private static void parseItem(LinkedHashMap<?,?> node)
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

		List<?> enchants = (List<?>) node.get("enchants");
		if (enchants != null)
			parseNodeList(enchants);

		result.add(curItemStack);
	}

	private static void parseEnchant(LinkedHashMap<?,?> node)
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

	private static void parseSetDamage(LinkedHashMap<?,?> node)
	{
		Number amount = (Number) node.get("amount");

		if (amount == null)
		{
			FCLog.warning("Invalid Damage modifiers config! Set amount is missing!");
			return;

		}

		event.setDamage(amount.intValue());
	}

	private static void parseAddDamage(LinkedHashMap<?,?> node)
	{
		Number amount = (Number) node.get("amount");

		if (amount == null)
		{
			FCLog.warning("Invalid Damage modifiers config! Add amount is missing!");
			return;

		}

		event.setDamage(event.getDamage() + amount.intValue());
	}


	private static void parseMultiplyDamage(LinkedHashMap<?,?> node)
	{
		Number amount = (Number) node.get("amount");

		if (amount == null)
		{
			FCLog.warning("Invalid Damage modifiers config! Multiply amount is missing!");
			return;
		}

		event.setDamage((int) (event.getDamage() * amount.doubleValue()));
	}

	private static void parseAddPotionEffect(LinkedHashMap<?,?> node)
	{
		//Protect admins against evil features
		if (event.getEntity() instanceof Player && ((Player) event.getEntity()).getGameMode() == GameMode.CREATIVE )
		{
			return;
		}

		final Integer id = (Integer) node.get("id");
		if (id == null)
		{
			FCLog.warning("Invalid Damage modifiers config! Effect id is missing!");
			return;
		}

		
		Object durationNode = node.get("duration");
		int duration = 0;
		if (durationNode == null)
		{
			FCLog.warning("Invalid Damage modifiers config! Effect duration is missing!");
			return;
		}
		else if (durationNode instanceof Integer)
		{
			duration = ((Integer) durationNode).intValue();
		}
		else if (durationNode instanceof String)
		{
			String expression = (String) durationNode;
			expression = expression.replace("damage", Integer.toString(event.getDamage()));
						
			try {
				duration = Expression.evaluate(expression).toInteger();
			} catch (ArgumentCastException e) {
				e.printStackTrace();
			} catch (InvalidExpressionException e) {
				FCLog.warning("Invalid Damage modifiers config! Effect duration expression is invalid!");
				return;
			}
		}
		else
		{
			FCLog.warning("Invalid Damage modifiers config! Effect duration is invalid!");
			return;
		}
		

		Object amplifierNode = node.get("amplifier");
		int amplifier = 0;
		if (amplifierNode == null)
		{
			FCLog.warning("Invalid Damage modifiers config! Effect duration is missing!");
			return;
		}
		else if (amplifierNode instanceof Integer)
		{
			amplifier = ((Integer) amplifierNode).intValue();
		}
		else if (amplifierNode instanceof String)
		{
			String expression = (String) amplifierNode;
			expression = expression.replace("damage", Integer.toString(event.getDamage()));
						
			try {
				amplifier = Expression.evaluate(expression).toInteger();
			} catch (ArgumentCastException e) {
				e.printStackTrace();
			} catch (InvalidExpressionException e) {
				FCLog.warning("Invalid Damage modifiers config! Effect amplifier expression is invalid!");
				return;
			}			
		}
		else
		{
			FCLog.warning("Invalid Damage modifiers config! Effect amplifier is invalid!");
			return;
		}
		
		final int fAmplifier = amplifier;
		final int fDuration = duration;

		final Boolean ambient = (Boolean) node.get("ambient");

		Bukkit.getScheduler().scheduleSyncDelayedTask(MCNSAFlatcore.instance, new Runnable() {
			@Override
			public void run() {
				((LivingEntity) event.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.getById(id), fDuration, fAmplifier, ambient == null ? false : ambient));
			}
		});

	}

	private static int getNumberOfRolls(Object node, boolean lowLevel)
	{
		int rolls = 1;
		double chance = 1;

		if (node instanceof List)
		{
			for (Object o : (List<?>) node)
			{
				if (o instanceof String)
				{
					String textSplit[] = ((String) o).split(" ");
					if (textSplit.length > 1 && textSplit[0].equalsIgnoreCase("rolls") && Util.isInteger(textSplit[1]))
					{
						rolls = Integer.parseInt(textSplit[1]);
					}
					else if (textSplit.length > 1 && textSplit[0].equalsIgnoreCase("chance") && Util.isDouble(textSplit[1]))
					{
						chance = Double.parseDouble(textSplit[1]);
					}
				}
			}
		}
		else if (node instanceof LinkedHashMap<?,?>)
		{
			LinkedHashMap<?,?> mapNode = (LinkedHashMap<?,?>) node;
			Integer rollsObject = (Integer) mapNode.get("rolls");
			Number chanceObject = (Number) mapNode.get("chance");

			if (rollsObject != null)
				rolls = rollsObject;
			if (chanceObject != null)
				chance = chanceObject.doubleValue();
		}

		if (lowLevel)
		{
			chance += chanceAdder;
			chance *= chanceMultiplier;			
		}

		if (chance < 0.01)
			return 0;

		int num = 0;
		for (int i = 0; i < rolls; i++)
		{
			num += Math.floor(chance / 1.0);
			double newChance = chance % 1;

			double rand = random.nextDouble();
			if (rand < newChance)
				num++;
		}

		return num;
	}	
}
