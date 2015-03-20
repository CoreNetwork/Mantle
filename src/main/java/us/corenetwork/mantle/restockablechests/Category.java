package us.corenetwork.mantle.restockablechests;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import us.corenetwork.mantle.IO;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.YamlUtils;
import us.corenetwork.mantle.nanobot.NanobotUtil;

public class Category {

	private String name;
	private int weight;
	private List<Double> chances;
	
	private int perPlayerTotalLimit;
	private List<String> preReqCategories;
	private ItemStack iconItem;
	private ArrayList<Integer> minDist;
	private ArrayList<Integer> maxDist;
	private String displayName;
	
	public Category(Map<?, ?> categoryMap)
	{
		this.name = (String) categoryMap.get("lootTable");
		this.weight = (Integer) categoryMap.get("weight");
		this.chances = (ArrayList<Double>) categoryMap.get("chances");
		this.preReqCategories = categoryMap.get("preReqCategories") == null ? new ArrayList<String>() : (ArrayList<String>) categoryMap.get("preReqCategories");
		this.perPlayerTotalLimit = categoryMap.get("perPlayerTotalLimit") == null ? -1 : (Integer) categoryMap.get("perPlayerTotalLimit");
		this.iconItem = YamlUtils.readItemStack((Map<String, Object>) categoryMap.get("icon"));
		this.minDist = (ArrayList<Integer>) categoryMap.get("compassMinDistance");
		this.maxDist = (ArrayList<Integer>) categoryMap.get("compassMaxDistance");
		this.displayName = categoryMap.get("displayName") == null ? name : (String) categoryMap.get("displayName");
	}

	public static List<Category> getCategories(List<Map<?, ?>> categoriesListMap)
	{
		List<Category> categories = new ArrayList<Category>();
		
		for(Map<?, ?> categoryMap : categoriesListMap)
		{
			categories.add(new Category(categoryMap));
		}
		
		return categories;
	}

	public static Category pickOne(List<Category> categories)
	{
		if (categories.size() == 0)
			return null;

		int weightSum = 0;
		for(Category cat : categories)
			weightSum += cat.weight;
		
		int randomInt = MantlePlugin.random.nextInt(weightSum);
		
		int currentWeight = 0;
		for(Category cat : categories)
		{
			currentWeight += cat.weight;
			if(currentWeight > randomInt)
				return cat;
		}
		return categories.get(categories.size()-1);
	}

	public String getLootTableName()
	{
		return name;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public int getDistanceRange(Player player)
	{
		float timesFoundPerc = (float)getTimesFound(player) / perPlayerTotalLimit;
		if(timesFoundPerc < 0.33)
			return 0;
		else if(timesFoundPerc <  0.66)
			return 1;
		else return 2;

	}

	public int getMinDistance(int range)
	{
		return minDist.get(range);
	}
	
	public int getMaxDistance(int range)
	{
		return maxDist.get(range);
	}
	
	public boolean isRare()
	{
		return perPlayerTotalLimit != -1;
	}
	
	public int howManyTimes(Player player, double diminishVillage, double diminishTotal)
	{
		int howMany = 0;
		
		
		for(Double chance : chances)
		{
			double finalChance = chance != 1.0 ? chance * diminishTotal * diminishVillage : 1.0;
		
			if(finalChance >= MantlePlugin.random.nextDouble())
				howMany++;
			else
				break;
		}
		
		int howManyLeft = howManyLeft(player);
		
		if(howManyLeft < howMany)
			howMany = howManyLeft;
		
		return howMany;
	}

	private int howManyLeft(Player player)
	{
		int leftTotal = 1000;
		if(perPlayerTotalLimit != -1)
		{
			leftTotal = perPlayerTotalLimit - getTimesFound(player);
		}
		
		return leftTotal;
	}


	public static List<Category> filterRareCategories(List<Category> categories, Player player)
	{
		List<Category> applicableCats = new ArrayList<Category>();
		
		for(Category cat : categories)
		{
			if(cat.gotAllPreReqs(player) && cat.isUnderTheLimit(player))
			{
				applicableCats.add(cat);
			}
		}
		
		return applicableCats;
	}



	private boolean isUnderTheLimit(Player player)
	{
		int timesFound = getTimesFound(player);
		
		return timesFound < perPlayerTotalLimit;
	}
	
	public int getTimesFound(Player player)
	{
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT TimesFound FROM playerCategory WHERE PlayerUUID = ? AND Category = ?");
			
			statement.setString(1, player.getUniqueId().toString());
			statement.setString(2, name);
			
			ResultSet set = statement.executeQuery();
			if(set.next())
			{
				return set.getInt("TimesFound");
			}
			statement.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	private boolean gotAllPreReqs(Player player)
	{
		if(preReqCategories.size() == 0)
		{
			return true;
		}
		
		try
		{
			StringBuilder sb = new StringBuilder();
			sb.append("(");
			for(String prereq : preReqCategories)
			{
				sb.append("'" + prereq + "',");
			}
			sb.deleteCharAt(sb.length()-1);
			sb.append(")");
			String stringStatement = "SELECT COUNT(*) FROM playerCategory WHERE PlayerUUID = ? AND Category in %ARR%";
			stringStatement = stringStatement.replace("%ARR%", sb.toString());
			PreparedStatement statement = IO.getConnection().prepareStatement(stringStatement);
			statement.setString(1, player.getUniqueId().toString());
			
			ResultSet set = statement.executeQuery();
			if(set.next())
			{
				int count = set.getInt(1);
				return count > 0;
			}
			statement.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public ItemStack getIconSimple()
	{
		return NanobotUtil.replaceStringInItem(iconItem, "<UsedMax>","");
	}

	public ItemStack getIcon(Player player)
	{
		if(getDistanceRange(player) > 0)
		{
			return NanobotUtil.replaceStringInItem(iconItem, "<UsedMax>", getTimesFound(player) + "/" + perPlayerTotalLimit);
		}

		return getIconSimple();
	}
}
