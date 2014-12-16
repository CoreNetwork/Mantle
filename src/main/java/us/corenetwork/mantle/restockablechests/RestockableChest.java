package us.corenetwork.mantle.restockablechests;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.server.v1_8_R1.BlockPosition;
import net.minecraft.server.v1_8_R1.EnumParticle;
import net.minecraft.server.v1_8_R1.PacketPlayOutBlockAction;
import net.minecraft.server.v1_8_R1.TileEntity;
import net.minecraft.server.v1_8_R1.TileEntityBeacon;
import net.minecraft.server.v1_8_R1.TileEntityBrewingStand;
import net.minecraft.server.v1_8_R1.TileEntityChest;
import net.minecraft.server.v1_8_R1.TileEntityDispenser;
import net.minecraft.server.v1_8_R1.TileEntityDropper;
import net.minecraft.server.v1_8_R1.TileEntityFurnace;
import net.minecraft.server.v1_8_R1.TileEntityHopper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.configuration.MemorySection;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_8_R1.inventory.CraftInventoryCustom;
import org.bukkit.craftbukkit.v1_8_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import us.corenetwork.core.respawn.ProtectTimer;
import us.corenetwork.mantle.IO;
import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.ParticleLibrary;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.hardmode.HardmodeModule;
import us.corenetwork.mantle.nanobot.NanobotUtil;


public class RestockableChest {
	private static HashMap<Player, RestockableChest> openedInventories = new HashMap<Player, RestockableChest>();
	private HashMap<Player, Inventory> inventoryCache = new HashMap<Player, Inventory>();
	private static HashMap<Block, Integer> openedNumber = new HashMap<Block, Integer>();
	private Block chestBlock;
	private InventoryHolder inventoryHolder;
	private int id;
	private int interval;
	private boolean perPlayer;
	private String lootTable;
	private Integer structureID;
	
	private RestockableChest()
	{

	}

	public static RestockableChest getChest(Block chest)
	{
		Block alternativeChest = chest;
		boolean checkBoth = false;
		if (!Util.isInventoryContainer(chest.getTypeId()))
			return null;

		Inventory inventory = ((InventoryHolder) chest.getState()).getInventory();
		if (inventory instanceof DoubleChestInventory)
		{
			DoubleChestInventory doubleChestInventory = (DoubleChestInventory) inventory;
			Chest leftChest = (Chest) doubleChestInventory.getLeftSide().getHolder();
			Chest rightChest = (Chest) doubleChestInventory.getRightSide().getHolder();
			chest = leftChest.getBlock();
			alternativeChest = rightChest.getBlock();
			checkBoth = true;
		}

		try
		{
			
			
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT ID,Interval,PerPlayer,LootTable, StructureID FROM chests WHERE World = ? AND X = ? AND Y = ? AND Z = ? LIMIT 1");
			statement.setString(1, chest.getWorld().getName());
			statement.setInt(2, chest.getX());
			statement.setInt(3, chest.getY());
			statement.setInt(4, chest.getZ());
			ResultSet set = statement.executeQuery();

			if (!set.next())
			{
				
				if(checkBoth)
				{
					statement = IO.getConnection().prepareStatement("SELECT ID,Interval,PerPlayer,LootTable, StructureID FROM chests WHERE World = ? AND X = ? AND Y = ? AND Z = ? LIMIT 1");
					statement.setString(1, alternativeChest.getWorld().getName());
					statement.setInt(2, alternativeChest.getX());
					statement.setInt(3, alternativeChest.getY());
					statement.setInt(4, alternativeChest.getZ());
					set = statement.executeQuery();
					
					if (!set.next())
					{
						return null;
					}
				}
				else
				{
					return null;
				}
			}

			RestockableChest rChest = new RestockableChest();

			rChest.id = set.getInt("ID");
			rChest.interval = set.getInt("Interval");
			rChest.perPlayer = set.getInt("perPlayer") == 1;
			rChest.lootTable = set.getString("LootTable");
			rChest.chestBlock = chest;
			rChest.inventoryHolder = (InventoryHolder) chest.getState();
			rChest.structureID = set.getInt("StructureID");
			statement.close();
			return rChest;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static List<RestockableChest> getChestsInStructure(int structureID)
	{
		List<RestockableChest> chests = new ArrayList<RestockableChest>();
		
		
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT ID,Interval,PerPlayer,LootTable,World,X,Y,Z FROM chests WHERE StructureID = ?");
			statement.setInt(1, structureID);
			ResultSet set = statement.executeQuery();
			
			while(set.next())
			{
				RestockableChest rChest = new RestockableChest();
				rChest.id = set.getInt("ID");
				rChest.interval = set.getInt("Interval");
				rChest.perPlayer = set.getInt("perPlayer") == 1;
				rChest.lootTable = set.getString("LootTable");
				rChest.chestBlock = MantlePlugin.instance.getServer().getWorld(set.getString("World")).getBlockAt(set.getInt("X"),set.getInt("Y"),set.getInt("Z"));
				BlockState blockState = rChest.chestBlock.getState();
				rChest.inventoryHolder = blockState instanceof InventoryHolder ? (InventoryHolder) blockState : null;
				rChest.structureID = structureID;
				chests.add(rChest);
			}
			
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return chests;
	}
	
	public int getStructureID()
	{
		return structureID;
	}
	
	public int getID()
	{
		return id;
	}
	
	public boolean chestExists()
	{
		return chestBlock.getType() == Material.CHEST;
	}
	
	public static void createChest(Block chest, String lootTable, int interval, boolean perPlayer, Integer structureID)
	{
		Inventory inventory = ((InventoryHolder) chest.getState()).getInventory();
		if (inventory instanceof DoubleChestInventory)
		{
			DoubleChestInventory doubleChestInventory = (DoubleChestInventory) inventory;
			Chest leftChest = (Chest) doubleChestInventory.getLeftSide().getHolder();
			chest = leftChest.getBlock();
		}

		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("INSERT INTO chests (Interval, LootTable, PerPlayer, World, X, Y, Z, StructureID) VALUES (?,?,?,?,?,?,?,?)");
			statement.setInt(1, interval);
			statement.setString(2, lootTable);
			statement.setInt(3, perPlayer ? 1 : 0);
			statement.setString(4, chest.getWorld().getName());
			statement.setInt(5, chest.getX());
			statement.setInt(6, chest.getY());
			statement.setInt(7, chest.getZ());
			statement.setInt(8, structureID);

			statement.executeUpdate();
			IO.getConnection().commit();
			statement.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void delete()
	{
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("DELETE FROM chests WHERE ID = ?");
			statement.setInt(1, id);
			statement.executeUpdate();
			statement.close();

			statement = IO.getConnection().prepareStatement("DELETE FROM chestInventory WHERE ID = ?");
			statement.setInt(1, id);
			statement.executeUpdate();
			statement.close();

			statement = IO.getConnection().prepareStatement("DELETE FROM playerChests WHERE ID = ?");
			statement.setInt(1, id);
			statement.executeUpdate();
			statement.close();

			IO.getConnection().commit();

		}
		catch (SQLException e) {
		}
	}

	public boolean open(Player player)
	{
		RestockableChest chest = openedInventories.get(player);
		if (chest != null)
			return true;

		if (RChestsModule.instance.config.contains("LootTables." + lootTable + ".PlayerControl.TimeRestrict"))
		{			
			int minTime = RChestsModule.instance.config.getInt("LootTables." + lootTable + ".PlayerControl.TimeRestrict.MinTime");
			int maxTime = RChestsModule.instance.config.getInt("LootTables." + lootTable + ".PlayerControl.TimeRestrict.MaxTime");
			long curTime = chestBlock.getWorld().getTime();

			if (!((minTime < maxTime && curTime > minTime && curTime < maxTime) || (minTime > maxTime && (curTime > minTime || curTime < maxTime))))
			{
				String message = RChestsModule.instance.config.getString("LootTables." + lootTable + ".PlayerControl.TimeRestrict.Message");
				Util.Message(message, player);
				return true;
			}
		}
		
		int timerLeft = ChestTimeout.getRemainingTime(lootTable, player.getUniqueId(), chestBlock);
		if (timerLeft >= 0)
		{
			String message = RChestsModule.instance.config.getString("LootTables." + lootTable + ".PlayerControl.MultiChestTimeout.Message", "Admin of this server is too lazy to enter message!");
			message = message.replace("<TimeLeft>", Util.printTimeHours(timerLeft));
			Util.Message(message, player);
			
			return true;
		}
		
		if (player.hasPotionEffect(PotionEffectType.INVISIBILITY))
		{
			Util.Message(RChestSettings.MESSAGE_CHEST_INVISIBLE.string(), player);
			return true;
		}
		
		//Is player invincible (Core, respawn module)
		if (ProtectTimer.protectedPlayers.containsKey(player.getName()))
		{
			Util.Message(RChestSettings.MESSAGE_CHEST_INVINCIBLE.string(), player);
			return true;
		}

		applyPoison(player);


		if (!perPlayer) 
		{
			tryRestock(null);
			return false;
		}
		
		
		///	
		Inventory inventory = getInventory(player);
		InventoryView view = player.openInventory(inventory);
		inventoryCache.put(player, view.getTopInventory());
		openedInventories.put(player, this);
		storeInventoryToDB(player, null);
		///
		
		
		if (chestBlock.getType() == Material.TRAPPED_CHEST)
		{
			Block redstoneBlock = chestBlock.getRelative(0, -2, 0);
			if (redstoneBlock != null && redstoneBlock.getType() == Material.AIR)
				redstoneBlock.setType(Material.REDSTONE_BLOCK);
		}

		Integer currentAmountOfOpened = openedNumber.get(chestBlock);
		if (currentAmountOfOpened == null)
			currentAmountOfOpened = 0;
		currentAmountOfOpened++;
		openedNumber.put(chestBlock, currentAmountOfOpened);

		
		//Chest animation
		if (currentAmountOfOpened < 2 && (chestBlock.getType() == Material.CHEST || chestBlock.getType() == Material.TRAPPED_CHEST))
		{
			PacketPlayOutBlockAction chestOpenPacket = new PacketPlayOutBlockAction(new BlockPosition(chestBlock.getX(), chestBlock.getY(), chestBlock.getZ()), net.minecraft.server.v1_8_R1.Block.getById(chestBlock.getTypeId()), 1, 1);

			List<Entity> nearbyEntities = player.getNearbyEntities(20, 20, 20);
			nearbyEntities.add(player);
			for (Entity e : nearbyEntities)
			{
				if (e.getType() != EntityType.PLAYER)
					continue;

				((CraftPlayer) e).getHandle().playerConnection.sendPacket(chestOpenPacket);
			}

			chestBlock.getWorld().playSound(chestBlock.getLocation(), Sound.CHEST_OPEN, 1f, 1f);
		}

		return true;
	}

	public static void inventoryClosed(Player player)
	{
		RestockableChest chest = openedInventories.get(player);
		if (chest != null)
		{
			Inventory inventory = chest.inventoryCache.get(player);
			if (inventory != null)
			{
				chest.storeInventoryToDB(player, inventory);
				chest.inventoryCache.remove(player);

				if (chest.chestBlock.getType() == Material.TRAPPED_CHEST && chest.inventoryCache.size() == 0)
				{
					Block redstoneBlock = chest.chestBlock.getRelative(0, -2, 0);
					if (redstoneBlock != null && redstoneBlock.getType() == Material.REDSTONE_BLOCK)
						redstoneBlock.setType(Material.AIR);
				}

				Integer currentAmountOfOpened = openedNumber.get(chest.chestBlock);
				if (currentAmountOfOpened == null)
					currentAmountOfOpened = 1;
				currentAmountOfOpened--;
				openedNumber.put(chest.chestBlock, currentAmountOfOpened);

				//Chest animation
				if (currentAmountOfOpened < 1 && (chest.chestBlock.getType() == Material.CHEST  || chest.chestBlock.getType() == Material.TRAPPED_CHEST))
				{
					PacketPlayOutBlockAction chestClosePacket = new PacketPlayOutBlockAction(new BlockPosition(chest.chestBlock.getX(), chest.chestBlock.getY(), chest.chestBlock.getZ()), net.minecraft.server.v1_8_R1.Block.getById(chest.chestBlock.getTypeId()), 1, 0);
					
					List<Entity> nearbyEntities = player.getNearbyEntities(20, 20, 20);
					nearbyEntities.add(player);
					for (Entity e : nearbyEntities)
					{
						if (e.getType() != EntityType.PLAYER)
							continue;

						((CraftPlayer) e).getHandle().playerConnection.sendPacket(chestClosePacket);
					}

					
					
					chest.chestBlock.getWorld().playSound(chest.chestBlock.getLocation(), Sound.CHEST_CLOSE, 1f, 1f);
				}

			}

			openedInventories.remove(player);
		}
	}

	public static void inventoryClicked(InventoryClickEvent event)
	{
		if (!event.isShiftClick() || RChestSettings.USE_ONLY_CHEST_GUI.bool())
			return;

		RestockableChest chest = openedInventories.get(event.getWhoClicked());

		if (chest != null && chest.chestBlock.getType() != Material.CHEST)
		{
			event.setCancelled(true);
		}
	}

	public Inventory getInventory(Player player)
	{
		Inventory inv = tryRestock(player);
		if (inv != null)
			return inv;


		if (player == null)
			return inventoryHolder.getInventory();
		else
			return getInventoryFromDB(player);
	}

	public Inventory tryRestock(Player player)
	{				
		
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT * FROM playerChests WHERE ID = ? AND PlayerUUID = ? LIMIT 1");
			statement.setInt(1, id);
			statement.setString(2, player == null ? "[CHEST]" : player.getUniqueId().toString());
			ResultSet set = statement.executeQuery();

			int lastAccess;
			int restocks = 0;

			if (!set.next())
			{
				lastAccess = 0;
				restocks = 0;
			}
			else
			{
				lastAccess = set.getInt("LastAccess");
				restocks = set.getInt("Restocks");
			}


			statement.close();

			//Special case for overworld village chests;
			if(lootTable.equalsIgnoreCase(RChestSettings.DUMMY_LOOT_TABLE_OW.string()))
			{
				//Never accessed before, we can give loot;
				if(lastAccess >= 0)
				{
					return getOWChestLoot(player);
				}
			}
			else
			{
				if (lastAccess >= 0 && System.currentTimeMillis() / 1000 - lastAccess > interval * 3600)
					return restock(player, restocks, interval < 0);
			}
			
			

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	public Inventory restock(Player player, int restocks, boolean finiteChest)
	{		
		double addChance = 0;
		if (restocks > 0)
			addChance = -RChestsModule.instance.config.getDouble("LootTables." + lootTable + ".PlayerControl.ChanceDiminishing.SubtractChanceOnce", 0);
		if (restocks > 1)
			addChance += -RChestsModule.instance.config.getDouble("LootTables." + lootTable + ".PlayerControl.ChanceDiminishing.SubtractChances", 0) * (restocks - 1);

		restocks++;

		Integer timeoutMinutes = RChestsModule.instance.config.getInt("LootTables." + lootTable + ".PlayerControl.MultiChestTimeout.Timeout", 0);
		if (timeoutMinutes > 0)
			ChestTimeout.addTimer(lootTable, player.getUniqueId(), timeoutMinutes, chestBlock);
		
		String numberDisplay = "";
		int maxNumber = RChestsModule.instance.config.getInt("LootTables." + lootTable + ".PlayerControl.MaximumDisplayedAccessNumber", Integer.MAX_VALUE);
		if (restocks <= maxNumber)
			numberDisplay = Integer.toString(restocks);
		else
			numberDisplay = maxNumber + "+";

		LootTableNodeParser parser = new LootTableNodeParser(lootTable, 1, addChance, RChestsModule.instance.config);
		List<ItemStack> items = parser.parse();
		
		Inventory inventory = prepareInventory(player, parser.didAnyItemHadAnyChance(), numberDisplay);
		inventory = spreadItemsRandomly(items, inventory);

		storePlayerChestVisit(player, finiteChest, restocks);
		return inventory;
	}

	private Inventory getOWChestLoot(Player player)
	{
		List<ItemStack> items = new ArrayList<ItemStack>();
		List<Category> categories;
		Category basicCat = null;
		Category rareCat = null;
		Category compassCat = null;
		
		boolean isFromCompass = false;
		double diminishVillage = getDiminishVillage(player);
		
		PlayerTotal playerTotal = getDiminishTotal(player);
		
		double diminishTotal = playerTotal.diminishTotal;
		
		if(playerTotal.category != null)
		{
			if(playerTotal.chestID == id)
			{
				compassCat = RChestsModule.categories.get(playerTotal.category);
				if(compassCat == null)
				{
					MLog.warning("Compass category not found in all categories! This should not happen. Did something change?");
				}
				else
				{
					isFromCompass = true;
					if(compassCat.isRare())
					{
						rareCat = compassCat;
					}
					else
					{
						basicCat = compassCat;
					}
					CompassDestination.destinations.remove(player.getUniqueId());
					
					try
					{
						PreparedStatement statement = IO.getConnection().prepareStatement("UPDATE playerTotal SET CompassCategory = ?, CompassChestID = 0 WHERE PlayerUUID = ?");
						statement.setString(2, player.getUniqueId().toString());
						statement.setString(1, null);	
						statement.executeUpdate();
						statement.close();
						
						IO.getConnection().commit();
					}
					catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}

		//Only null if no compass category found
		if(basicCat == null)
		{
			categories = RChestsModule.basicCategories;
			basicCat = Category.pickOne(categories);
		}
		//Only null if no compass category found
		if(rareCat == null)
		{
			categories = RChestsModule.rareCategories;
			categories = Category.filterRareCategories(categories, player);
			rareCat = Category.pickOne(categories);
		}
		
		MLog.debug("-OwLoot---");
		MLog.debug("Player diminish Village : "+ diminishVillage);
		MLog.debug("Player diminish Total : "+ diminishTotal);
		MLog.debug("Is compass chest : " + isFromCompass + "  " + (isFromCompass == false ? "" : playerTotal.category));
		MLog.debug("Basic category   : " + basicCat.getLootTableName());
		MLog.debug("Rare  category   : " + rareCat.getLootTableName());
		
		items.addAll(getItemsFromCategory(basicCat, player, diminishVillage, diminishTotal, isFromCompass));
		items.addAll(getItemsFromCategory(rareCat, player, diminishVillage, diminishTotal, isFromCompass));
		
		
		updateDiminishVillage(player, diminishVillage);
		updateDiminishTotal(player, diminishTotal);
		
		
		String numberDisplay = "";
		Inventory inventory = prepareInventory(player, true, numberDisplay);
		inventory = spreadItemsRandomly(items, inventory);
		storePlayerChestVisit(player, true, 0);
		
		MLog.debug("----------");
		return inventory;
		
	}
	
	private double getDiminishVillage(Player player)
	{
		double dimValue = 1;
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT diminishVillage FROM playerVillage WHERE PlayerUUID = ? AND StructureID = ? LIMIT 1");
			
			statement.setString(1, player.getUniqueId().toString());
			statement.setInt(2, structureID);
			
			ResultSet set = statement.executeQuery();
			if(set.next())
			{
				dimValue = set.getDouble("diminishVillage");
			}
			else
			{
				PreparedStatement statement2 = IO.getConnection().prepareStatement("INSERT INTO playerVillage (PlayerUUID, StructureID, diminishVillage) VALUES (?,?,?)");
				statement2.setString(1, player.getUniqueId().toString());
				statement2.setInt(2, structureID);
				statement2.setDouble(3, dimValue);
				statement2.executeUpdate();
				statement2.close();
			}
			statement.close();
			IO.getConnection().commit();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		return dimValue;
	}
	
	private PlayerTotal getDiminishTotal(Player player)
	{
		double dimValue = 1;
		PlayerTotal playerTotal = null;
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT diminishTotal, CompassCategory, CompassChestID FROM playerTotal WHERE PlayerUUID = ? LIMIT 1");
			
			statement.setString(1, player.getUniqueId().toString());
			
			ResultSet set = statement.executeQuery();
			if(set.next())
			{
				dimValue = set.getDouble("diminishTotal");
				playerTotal = new PlayerTotal(dimValue, set.getString("CompassCategory"), set.getInt("CompassChestID"));
			}
			else
			{
				PreparedStatement statement2 = IO.getConnection().prepareStatement("INSERT INTO playerTotal (PlayerUUID, diminishTotal) VALUES (?,?)");
				statement2.setString(1, player.getUniqueId().toString());
				statement2.setDouble(2, dimValue);
				statement2.executeUpdate();
				statement2.close();
				playerTotal = new PlayerTotal(dimValue, null, -1);
			}
			statement.close();
			IO.getConnection().commit();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		return playerTotal;
	}
	
	private class PlayerTotal
	{
		public double diminishTotal;
		public String category;
		public int chestID;
		
		public PlayerTotal(double diminishTotal, String category, Integer chestID)
		{
			this.diminishTotal = diminishTotal;
			this.category = category;
			this.chestID = chestID;
		}
		
		
	}
	
	private void updateDiminishVillage(Player player, double value)
	{
		double newValue = value - RChestSettings.DIMINISH_VILLAGE.doubleNumber();
		if(newValue < 0)
			newValue = 0;
		
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("UPDATE playerVillage SET diminishVillage = ? WHERE PlayerUUID = ? AND StructureID = ?");
			statement.setString(2, player.getUniqueId().toString());
			statement.setInt(3, structureID);
			statement.setDouble(1, newValue);	
			statement.executeUpdate();
			statement.close();
			
			IO.getConnection().commit();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void updateDiminishTotal(Player player, double value)
	{
		double newValue = value - RChestSettings.DIMINISH_TOTAL.doubleNumber();
		if(newValue < 0)
			newValue = 0;
		
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("UPDATE playerTotal SET diminishTotal = ? WHERE PlayerUUID = ?");
			statement.setString(2, player.getUniqueId().toString());
			statement.setDouble(1, newValue);	
			statement.executeUpdate();
			statement.close();
			
			IO.getConnection().commit();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	private List<ItemStack> getItemsFromCategory(Category cat, Player player, double diminishVillage, double diminishTotal, boolean firstTimeAlways)
	{
		List<ItemStack> items = new ArrayList<ItemStack>();
		int timesPicked = cat.howManyTimes(player, diminishVillage, diminishTotal);		
		
		if(firstTimeAlways && timesPicked == 0)
		{
			timesPicked = 1;
		}
		
		if(timesPicked > 0)
		{
			updateCategoryCounterFor(cat, player, timesPicked);
		}
		
		MLog.debug("Category " + cat.getLootTableName() + " picked " + timesPicked + " times." );
		
		LootTableNodeParser parser = new LootTableNodeParser(cat.getLootTableName(), 1, 0, RChestsModule.instance.config);
		for(int i = 0;i< timesPicked;i++)
		{
			items.addAll(parser.parse());
		}
		return items;
	}
	
	private void updateCategoryCounterFor(Category cat, Player player, int timesPicked)
	{
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT TimesFound FROM playerCategory WHERE PlayerUUID = ? AND Category = ?");
			
			statement.setString(1, player.getUniqueId().toString());
			statement.setString(2, cat.getLootTableName());
			
			ResultSet set = statement.executeQuery();
			if(set.next())
			{
				timesPicked += set.getInt("TimesFound");
				PreparedStatement statement2 = IO.getConnection().prepareStatement("UPDATE playerCategory SET TimesFound = ? WHERE PlayerUUID = ? AND Category = ?");
				statement2.setString(2, player.getUniqueId().toString());
				statement2.setString(3, cat.getLootTableName());
				statement2.setInt(1, timesPicked);
				statement2.executeUpdate();
				statement2.close();
			}
			else
			{
				PreparedStatement statement2 = IO.getConnection().prepareStatement("INSERT INTO playerCategory (PlayerUUID, Category, TimesFound) VALUES (?,?,?)");
				statement2.setString(1, player.getUniqueId().toString());
				statement2.setString(2, cat.getLootTableName());
				statement2.setInt(3, timesPicked);
				statement2.executeUpdate();
				statement2.close();
				
			}
			statement.close();
			IO.getConnection().commit();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private Inventory prepareInventory(Player player, boolean anyItemHadAnyChance, String numberDisplay)
	{
		Inventory inventory;
		if (player == null)
			inventory = inventoryHolder.getInventory();
		else
		{
			inventory = createEmptyInventory(numberDisplay);
			
			if (anyItemHadAnyChance == false)
			{
				String message = RChestsModule.instance.config.getString("LootTables." + lootTable + ".PlayerControl.ChanceDiminishing.ZeroChanceMessage", "Admin of this server is too lazy to enter message!");
				Util.Message(message, player);
			}
		}
		inventory.clear();
		return inventory;
	}
	
	private Inventory spreadItemsRandomly(List<ItemStack> items, Inventory inventory)
	{
		for (ItemStack i : items)
		{
			int counter = 0;
			while (true)
			{
				int spot = MantlePlugin.random.nextInt(inventory.getSize());
				if (inventory.getItem(spot) == null)
				{
					inventory.setItem(spot, i);
					break;
				}

				counter++;
				if (counter > 100)
				{
					MLog.severe("[ " + lootTable + "] Failed to find empty spot in chest after 100 tries! Do you generate too many items?");
					break;
				}
			}
		}
		
		return inventory;
	}
	
	
	private void storePlayerChestVisit(Player player, boolean finiteChest, int restocks)
	{
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("DELETE FROM playerChests WHERE ID = ? AND PlayerUUID = ?");
			statement.setInt(1, id);
			statement.setString(2, player == null ? "[CHEST]" : player.getUniqueId().toString());
			statement.executeUpdate();
			statement.close();

			statement = IO.getConnection().prepareStatement("INSERT INTO playerChests (ID,PlayerUUID, LastAccess, Restocks) VALUES (?,?,?,?)");
			statement.setInt(1, id);
			statement.setString(2, player == null ? "[CHEST]" : player.getUniqueId().toString());
			statement.setInt(3, finiteChest ? -1 : (int) (System.currentTimeMillis() / 1000));
			statement.setInt(4, restocks);
			statement.executeUpdate();
			statement.close();

			IO.getConnection().commit();

		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private Inventory getInventoryFromDB(Player player)
	{


		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT Restocks FROM playerChests WHERE ID = ? AND PlayerUUID = ? LIMIT 1");
			statement.setInt(1, id);
			statement.setString(2, player == null ? "[CHEST]" : player.getUniqueId().toString());
			ResultSet set = statement.executeQuery();

			int restocks = 0;

			if (!set.next())
				restocks = 0;
			else
				restocks = set.getInt("Restocks");
			statement.close();

			String numberDisplay = "";
			int maxNumber = RChestsModule.instance.config.getInt("LootTables." + lootTable + ".PlayerControl.MaximumDisplayedAccessNumber", Integer.MAX_VALUE);
			if (restocks <= maxNumber)
				numberDisplay = Integer.toString(restocks);
			else
				numberDisplay = maxNumber + "+";

			Inventory inventory = createEmptyInventory(numberDisplay);

			statement = IO.getConnection().prepareStatement("SELECT * FROM chestInventory WHERE ID = ? AND PlayerUUID = ?");
			statement.setInt(1, id);
			statement.setString(2, player.getUniqueId().toString());
			set = statement.executeQuery();

			while (set.next())
			{
				int slot = set.getInt("Slot");
				int id = set.getInt("itemID");
				int damage = set.getInt("damage");
				int amount = set.getInt("amount");
				byte[] nbt = set.getBytes("NBT");

				ItemStack stack = new ItemStack(id, amount, (short) damage);
				NanobotUtil.loadNBT(nbt, NanobotUtil.getInternalNMSStack(stack));

				inventory.setItem(slot, stack);
			}

			statement.close();

			return inventory;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	private void storeInventoryToDB(Player player, Inventory inventory)
	{

		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("DELETE FROM chestInventory WHERE ID = ? AND PlayerUUID = ?");
			statement.setInt(1, id);
			statement.setString(2, player.getUniqueId().toString());
			statement.executeUpdate();
			statement.close();

			if (inventory == null)
			{
				IO.getConnection().commit();
				return;
			}

			statement = IO.getConnection().prepareStatement("INSERT INTO chestInventory (ID, PlayerUUID, Slot, ItemID, Damage, Amount, NBT) VALUES (?,?,?,?,?,?,?)");

			int size = inventory.getSize();
			for (int i = 0; i < size; i++)
			{
				ItemStack stack = inventory.getItem(i);
				if (stack == null || stack.getTypeId() == 0)
					continue;

				statement.setInt(1, id);
				statement.setString(2, player.getUniqueId().toString());
				statement.setInt(3, i);
				statement.setInt(4, stack.getTypeId());
				statement.setInt(5, stack.getDurability());
				statement.setInt(6, stack.getAmount());
				statement.setBytes(7, NanobotUtil.getNBT(NanobotUtil.getInternalNMSStack(stack)));
				statement.addBatch();
			}

			statement.executeBatch();
			IO.getConnection().commit();
			statement.close();
		}
		catch (SQLException e) {
		}
		
	}


	private void applyPoison(Player player)
	{
		MemorySection section = (MemorySection) RChestsModule.instance.config.get("LootTables." + lootTable + ".PlayerControl.Poison");
		if (section == null)
			return;

		Number chance = (Number) section.get("Chance");
		if (chance == null)
		{
			MLog.warning("Poison section in loot table " + lootTable + " is missing chance!");
			return;
		}

		if (chance.doubleValue() < MantlePlugin.random.nextDouble())
			return;

		String damageNode = (String) section.get("ApplyDamageNode");
		if (damageNode == null)
		{
			MLog.warning("Poison section in loot table " + lootTable + " is missing damage node!");
			return;
		}

		HardmodeModule.applyDamageNode(player, damageNode);

		Location location = player.getLocation();
		World world = location.getWorld();

		world.playSound(location, Sound.GLASS, 1f, 1f);
		ParticleLibrary.broadcastParticle(EnumParticle.SPELL, location, 0, 0, 0, 0, 3, null);
	}
	private String getCustomName()
	{
		if (!Util.isInventoryContainer(chestBlock.getTypeId()))
			return "";

		TileEntity tEntity = ((CraftWorld) chestBlock.getWorld()).getHandle().getTileEntity(new BlockPosition(chestBlock.getX(), chestBlock.getY(), chestBlock.getZ()));

		if (tEntity instanceof TileEntityChest)
		{
			TileEntityChest container = (TileEntityChest) tEntity;
			return container.hasCustomName() ? container.getName() : "Chest";
		}
		else if (tEntity instanceof TileEntityDispenser)
		{
			TileEntityDispenser container = (TileEntityDispenser) tEntity;
			return container.hasCustomName() ? container.getName() : "Dispenser";
		}
		else if (tEntity instanceof TileEntityFurnace)
		{
			TileEntityFurnace container = (TileEntityFurnace) tEntity;
			return container.hasCustomName() ? container.getName() : "Furnace";
		}
		else if (tEntity instanceof TileEntityHopper)
		{
			TileEntityHopper container = (TileEntityHopper) tEntity;
			return container.hasCustomName() ? container.getName() : "Hopper";
		}
		else if (tEntity instanceof TileEntityDropper)
		{
			TileEntityDropper container = (TileEntityDropper) tEntity;
			return container.hasCustomName() ? container.getName() : "Dropper";
		}
		else if (tEntity instanceof TileEntityBeacon)
		{
			TileEntityBeacon container = (TileEntityBeacon) tEntity;
			return container.hasCustomName() ? container.getName() : "Beacon";
		}
		else if (tEntity instanceof TileEntityBrewingStand)
		{
			TileEntityBrewingStand container = (TileEntityBrewingStand) tEntity;
			return container.hasCustomName() ? container.getName() : "Brewing Stand";
		}

		return Util.getMaterialName(chestBlock.getType());
	}

	@SuppressWarnings("incomplete-switch")
	private InventoryType getInventoryType()
	{

		switch (chestBlock.getType())
		{
		case BEACON:
			return InventoryType.BEACON;
		case BREWING_STAND:
			return InventoryType.BREWING;
		case DISPENSER:
			return InventoryType.DISPENSER;
		case DROPPER:
			return InventoryType.DROPPER;
		case FURNACE:
			return InventoryType.FURNACE;
		case HOPPER:
			return InventoryType.HOPPER;
		}

		return InventoryType.CHEST;
	}

	private Inventory createEmptyInventory(String numberDisplay)
	{
		InventoryType type = getInventoryType();

		int size = inventoryHolder.getInventory().getSize();
		boolean onlyChest = RChestSettings.USE_ONLY_CHEST_GUI.bool();
		if (onlyChest && type != InventoryType.CHEST)
		{
			size = (int) (Math.ceil(size / 9.0) * 9);
		}

		Inventory inventory = new CraftInventoryCustom(inventoryHolder, size, getCustomName() + " (" + numberDisplay + ")");

		if (!onlyChest && type != InventoryType.CHEST)
		{
			fixInventoryType(inventory, type);
		}

		return inventory;

	}

	private static void fixInventoryType(Inventory inventory, InventoryType type)
	{
		CraftInventory   customInv = (CraftInventory)  inventory;

		Field field;
		try {
			field = CraftInventory.class.getDeclaredField("inventory");
			field.setAccessible(true);

			Object internalInv = field.get(customInv);
			Class<?> internalInvClass = Class.forName("org.bukkit.craftbukkit.v1_8_R1.inventory.CraftInventoryCustom$MinecraftInventory");

			field = internalInvClass.getDeclaredField("type");
			field.setAccessible(true);

			field.set(internalInv, type);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}


	}

	public Block getBlock()
	{
		return chestBlock;
	}
}
