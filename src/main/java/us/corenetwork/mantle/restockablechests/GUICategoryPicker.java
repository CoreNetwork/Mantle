package us.corenetwork.mantle.restockablechests;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import us.core_network.cornel.common.Messages;
import us.core_network.cornel.custom.inventorygui.InventoryGUI;
import us.corenetwork.mantle.GriefPreventionHandler;
import us.corenetwork.mantle.IO;
import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.regeneration.RegenerationSettings;


public class GUICategoryPicker extends InventoryGUI
{

	private Map<Integer, Category> categoryPositions = new HashMap<Integer, Category>();
	
	
    public GUICategoryPicker(Player player)
    {
        
    	ItemStack okItem = RChestSettings.GUI_ITEM_CATEGORY_OK.itemStack();
    	ItemStack nopeItem = RChestSettings.GUI_ITEM_CATEGORY_NOPE.itemStack();
        
    	List<Category> basicCategories;
    	List<Category> rareCategories;
    	List<Category> applicableRareCategories;
    	
		basicCategories = RChestsModule.basicCategories;
		rareCategories = RChestsModule.rareCategories;
		applicableRareCategories = Category.filterRareCategories(rareCategories, player);

		List<Category> notApplicableRareCategories = new ArrayList<Category>(rareCategories);
		for(Category c : applicableRareCategories)
    	{
			notApplicableRareCategories.remove(c);
    	}
    	
		MLog.debug("---");
    	for(Category c : basicCategories)
    	{
    		MLog.debug(c.getLootTableName());
    	}
    	MLog.debug("---");
    	for(Category c : notApplicableRareCategories)
    	{
    		MLog.debug(c.getLootTableName());
    	}
    	MLog.debug("---");
    	for(Category c : applicableRareCategories)
    	{
    		MLog.debug(c.getLootTableName());
    	}
    	
    	int i = 9;
    	
    	for(Category c: basicCategories)
    	{
    		setItem(i-9, okItem);
    		setItem(i, c.getIcon());
    		categoryPositions.put(i, c);
    		setItem(i+9, okItem);
    		i++;
    	}
    	for(Category c: applicableRareCategories)
    	{
    		setItem(i-9, okItem);
    		setItem(i, c.getIcon());
    		categoryPositions.put(i, c);
    		setItem(i+9, okItem);
    		i++;
    	}
    	int j = 17;
		Collections.reverse(notApplicableRareCategories);
		for(Category c: notApplicableRareCategories)
    	{
    		if(j >= i)
    		{
    			setItem(j-9, nopeItem);
    			setItem(j, c.getIcon());
    			categoryPositions.put(i, c);
    			setItem(j+9, nopeItem);
    			j--;
    		}
    	}
    }

	@Override
	public void onClick(HumanEntity player, ClickType clickType, int slot)
	{
		Category selectedCategory = categoryPositions.get(slot);
		if(selectedCategory != null)
		{
			selectChestForPlayerCategory(player, selectedCategory);
			player.closeInventory();
		}
		
	}

	@Override
	public String getTitle()
	{
		return RChestSettings.GUI_TITLE_PICK_CATEGORY.string();
	}

	public static void selectChestForPlayerCategory(HumanEntity player, Category selectedCategory)
	{
		player.getUniqueId();
		
		RestockableChest rc = null;
		VillageInfoHelper selectedVillage = null;
		List<VillageInfoHelper> villageList = new ArrayList<VillageInfoHelper>();
		
		//pick a chest for this category
			//pick all villages with apprioprate distance
	
		Location location = player.getLocation();
		int x = location.getBlockX();
		int z = location.getBlockZ();
		int distanceRange = selectedCategory.getDistanceRange((Player) player);

		int minkw = selectedCategory.getMinDistance(distanceRange);
		minkw *= minkw;
		int maxkw = selectedCategory.getMaxDistance(distanceRange);
		maxkw *= maxkw;
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT ID, CornerX, CornerZ, SizeX, SizeZ, World, ((CornerX - ? + sizeX / 2) * (CornerX - ? + sizeX / 2) + (CornerZ - ? + sizeZ / 2) * (CornerZ - ? + sizeZ / 2)) as dist FROM regeneration_structures WHERE StructureName = 'Villages' AND LastCheck <= LastRestore AND dist > ? AND dist < ? ORDER BY dist ASC ");
			statement.setInt(1, x);
			statement.setInt(2, x);
			statement.setInt(3, z);
			statement.setInt(4, z);
			statement.setInt(5, minkw);
			statement.setInt(6, maxkw);

			ResultSet set = statement.executeQuery();
			if(!set.next())
			{
				MLog.warning("No village found for category " + selectedCategory.getLootTableName() + ". Please change min/max distance for compass.");
				
			}
			else
			{
				while(set.next())
				{
					int id = set.getInt("ID");
					final int villageX = set.getInt("CornerX");
					final int villageZ = set.getInt("CornerZ");
					final int xSize = set.getInt("SizeX");
					final int zSize = set.getInt("SizeZ");
					int distance = (int) Math.sqrt(set.getInt("dist"));
					String world = set.getString("World");
					
					VillageInfoHelper vih = new VillageInfoHelper(id, villageX, villageZ, xSize, zSize, distance, world);
					villageList.add(vih);
				}
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		
			//pick a random one from the set
		
		int padding = RegenerationSettings.RESORATION_VILLAGE_CHECK_PADDING.integer();
		boolean found = false;
		
		while(found == false && villageList.size() > 0)
		{
			int random = MantlePlugin.random.nextInt(villageList.size());
			VillageInfoHelper vih = villageList.get(random);
			villageList.remove(random);
			
			if (GriefPreventionHandler.containsClaim(vih.world, vih.villageX, vih.villageZ, vih.xSize, vih.zSize, padding, false, null))
			{
				continue;
			}
			
			for(RestockableChest rchest : RestockableChest.getChestsInStructure(vih.id))
			{
				if(rchest.chestExists() && !rchest.wasLootedByPlayer(player.getUniqueId()))
				{
					rc = rchest;
					found = true;
					selectedVillage = vih;
					break;
				}
			}
		}
		
		if(found == false)
		{
            Messages.send(RChestSettings.MESSAGE_COMPASS_CANT_FIND_VILLAGE.string(), (Player) player);
			MLog.warning("Compass - No viable village + chest found!");
			return;
		}
		
	
		//Check if table has a record for this player, if not, insert it with our values, if yes, update the record
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT diminishTotal FROM playerTotal WHERE PlayerUUID = ? LIMIT 1");
			
			statement.setString(1, player.getUniqueId().toString());
			
			ResultSet set = statement.executeQuery();
			if(set.next())
			{
				PreparedStatement statement2 = IO.getConnection().prepareStatement("UPDATE playerTotal SET CompassCategory = ?, CompassChestID = ? WHERE PlayerUUID = ?");
				statement2.setString(3, player.getUniqueId().toString());
				statement2.setString(1, selectedCategory.getLootTableName());
				statement2.setInt(2, rc.getID());
				statement2.executeUpdate();
				statement2.close();
			}
			else
			{
				PreparedStatement statement2 = IO.getConnection().prepareStatement("INSERT INTO playerTotal (PlayerUUID, diminishTotal, CompassCategory, CompassChestID) VALUES (?,?,?,?)");
				statement2.setString(1, player.getUniqueId().toString());
				statement2.setDouble(2, 1);
				statement2.setString(3, selectedCategory.getLootTableName());
				statement2.setInt(4, rc.getID());
				statement2.executeUpdate();
				statement2.close();
			}
			statement.close();
			IO.getConnection().commit();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		//do some magic with displaying the stuff to the player (?)
		MLog.debug("Picked category = " + selectedCategory.getLootTableName() + ".  ChestID = "+rc.getID()+". X " + rc.getBlock().getLocation().getBlockX() + ".   Z " + rc.getBlock().getLocation().getBlockZ());
		
		//add player destination to map
		CompassDestination.addDestination(player, new CompassDestination(rc.getBlock().getLocation().getBlockX(), rc.getBlock().getLocation().getBlockZ(), selectedVillage, selectedCategory));
	}
}
