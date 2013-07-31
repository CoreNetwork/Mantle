package com.mcnsa.flatcore.restockablechests;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.server.v1_6_R2.Packet54PlayNoteBlock;
import net.minecraft.server.v1_6_R2.TileEntityChest;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_6_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_6_R2.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_6_R2.inventory.CraftInventoryCustom;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import com.mcnsa.flatcore.FCLog;
import com.mcnsa.flatcore.IO;
import com.mcnsa.flatcore.MCNSAFlatcore;
import com.mcnsa.flatcore.Util;

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

	private RestockableChest()
	{

	}

	public static RestockableChest getChest(Block chest)
	{
		if (!Util.isInventoryContainer(chest.getTypeId()))
			return null;

		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT ID,Interval,PerPlayer,LootTable FROM chests WHERE World = ? AND X = ? AND Y = ? AND Z = ? LIMIT 1");
			statement.setString(1, chest.getWorld().getName());
			statement.setInt(2, chest.getX());
			statement.setInt(3, chest.getY());
			statement.setInt(4, chest.getZ());
			ResultSet set = statement.executeQuery();

			if (!set.next())
			{
				return null;
			}

			RestockableChest rChest = new RestockableChest();

			rChest.id = set.getInt("ID");
			rChest.interval = set.getInt("Interval");
			rChest.perPlayer = set.getInt("perPlayer") == 1;
			rChest.lootTable = set.getString("LootTable");
			rChest.chestBlock = chest;
			rChest.inventoryHolder = (InventoryHolder) chest.getState();
			statement.close();
			return rChest;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public static void createChest(Block chest, String lootTable, int interval, boolean perPlayer)
	{
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("INSERT INTO chests (Interval, LootTable, PerPlayer, World, X, Y, Z) VALUES (?,?,?,?,?,?,?)");
			statement.setInt(1, interval);
			statement.setString(2, lootTable);
			statement.setInt(3, perPlayer ? 1 : 0);
			statement.setString(4, chest.getWorld().getName());
			statement.setInt(5, chest.getX());
			statement.setInt(6, chest.getY());
			statement.setInt(7, chest.getZ());

			statement.executeUpdate();
			IO.getConnection().commit();
			statement.close();
		}
		catch (SQLException e) {
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
		if (!perPlayer) player = null;

		RestockableChest chest = openedInventories.get(player);
		if (chest != null)
			return true;

		if (player == null) 
		{
			tryRestock(player);
			return false;
		}
		else
		{
			Inventory inventory = getInventory(player);
			InventoryView view = player.openInventory(inventory);
			inventoryCache.put(player, view.getTopInventory());
			openedInventories.put(player, this);
			storeInventoryToDB(player, null);

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
				FCLog.info("Creating packet");
				Packet54PlayNoteBlock chestOpenPacket = new Packet54PlayNoteBlock(chestBlock.getX(), chestBlock.getY(), chestBlock.getZ(), chestBlock.getTypeId() , 1, 1);

				List<Entity> nearbyEntities = player.getNearbyEntities(20, 20, 20);
				nearbyEntities.add(player);
				for (Entity e : nearbyEntities)
				{
					if (e.getType() != EntityType.PLAYER)
						continue;

					FCLog.info("Sending packet");
					
					((CraftPlayer) e).getHandle().playerConnection.sendPacket(chestOpenPacket);
				}
				
				chestBlock.getWorld().playSound(chestBlock.getLocation(), Sound.CHEST_OPEN, 1f, 1f);
			}

			return true;
		}
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
					Packet54PlayNoteBlock chestOpenPacket = new Packet54PlayNoteBlock(chest.chestBlock.getX(), chest.chestBlock.getY(), chest.chestBlock.getZ(), chest.chestBlock.getTypeId(), 1, 0);

					List<Entity> nearbyEntities = player.getNearbyEntities(20, 20, 20);
					nearbyEntities.add(player);
					for (Entity e : nearbyEntities)
					{
						if (e.getType() != EntityType.PLAYER)
							continue;

						((CraftPlayer) e).getHandle().playerConnection.sendPacket(chestOpenPacket);
					}
					
					chest.chestBlock.getWorld().playSound(chest.chestBlock.getLocation(), Sound.CHEST_CLOSE, 1f, 1f);
				}

			}

			openedInventories.remove(player);
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
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT * FROM playerChests WHERE ID = ? AND Player = ? LIMIT 1");
			statement.setInt(1, id);
			statement.setString(2, player == null ? "[CHEST]" : player.getName());
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

			if (lastAccess >= 0 && System.currentTimeMillis() / 1000 - lastAccess > interval * 3600)
				return restock(player, restocks, interval < 0);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public Inventory restock(Player player, int restocks, boolean finiteChest)
	{		
		double multiplyChance = Math.pow(IO.config.getDouble("LootTables." + lootTable + ".MultiplyChances", 1), restocks);
		double addChance = IO.config.getDouble("LootTables." + lootTable + ".AddChances", 0) * restocks;
		restocks++;

		String numberDisplay = "";
		int maxNumber = IO.config.getInt("LootTables." + lootTable + ".MaximumDisplayedAccessNumber", Integer.MAX_VALUE);
		if (restocks <= maxNumber)
			numberDisplay = Integer.toString(restocks);
		else
			numberDisplay = maxNumber + "+";

		List<ItemStack> items = LootTableNodeParser.parseTable(lootTable, multiplyChance, addChance, RChestsModule.instance.config);
		Inventory inventory;
		if (player == null)
			inventory = inventoryHolder.getInventory();
		else
		{
			inventory = new CraftInventoryCustom(inventoryHolder, inventoryHolder.getInventory().getSize(), getCustomName() + " (" + numberDisplay + ")");
		
			InventoryType type = getInventoryType();
			if (type != InventoryType.CHEST)
				fixInventoryType(inventory, type);
		}
		
		inventory.clear();

		for (ItemStack i : items)
		{
			int counter = 0;
			while (true)
			{
				int spot = MCNSAFlatcore.random.nextInt(inventory.getSize());
				if (inventory.getItem(spot) == null)
				{
					inventory.setItem(spot, i);
					break;
				}

				counter++;
				if (counter > 100)
				{
					FCLog.severe("[ " + lootTable + "] Failed to find empty spot in chest after 100 tries! Do you generate too many items?");
					break;
				}
			}
		}


		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("DELETE FROM playerChests WHERE ID = ? AND Player = ?");
			statement.setInt(1, id);
			statement.setString(2, player == null ? "[CHEST]" : player.getName());
			statement.executeUpdate();
			statement.close();

			statement = IO.getConnection().prepareStatement("INSERT INTO playerChests (ID,Player, LastAccess, Restocks) VALUES (?,?,?,?)");
			statement.setInt(1, id);
			statement.setString(2, player == null ? "[CHEST]" : player.getName());
			statement.setInt(3, finiteChest ? -1 : (int) (System.currentTimeMillis() / 1000));
			statement.setInt(4, restocks);
			statement.executeUpdate();
			statement.close();

			IO.getConnection().commit();

		}
		catch (SQLException e) {
			e.printStackTrace();
		}

		return inventory;
	}

	private Inventory getInventoryFromDB(Player player)
	{


		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT Restocks FROM playerChests WHERE ID = ? AND Player = ? LIMIT 1");
			statement.setInt(1, id);
			statement.setString(2, player == null ? "[CHEST]" : player.getName());
			ResultSet set = statement.executeQuery();

			int restocks = 0;

			if (!set.next())
				restocks = 0;
			else
				restocks = set.getInt("Restocks");
			statement.close();

			String numberDisplay = "";
			int maxNumber = IO.config.getInt("LootTable." + lootTable + ".MaximumDisplayedAccessNumber", Integer.MAX_VALUE);
			if (restocks <= maxNumber)
				numberDisplay = Integer.toString(restocks);
			else
				numberDisplay = maxNumber + "+";

			Inventory inventory = new CraftInventoryCustom(inventoryHolder, inventoryHolder.getInventory().getSize(), getCustomName() + " (" + numberDisplay + ")");

			InventoryType type = getInventoryType();
			if (type != InventoryType.CHEST)
				fixInventoryType(inventory, type);

			statement = IO.getConnection().prepareStatement("SELECT * FROM chestInventory WHERE ID = ? AND Player = ?");
			statement.setInt(1, id);
			statement.setString(2, player.getName());
			set = statement.executeQuery();

			while (set.next())
			{
				int slot = set.getInt("Slot");
				int id = set.getInt("itemID");
				int damage = set.getInt("damage");
				int amount = set.getInt("amount");
				String enchants[] = set.getString("enchants").split(",");

				ItemStack stack = new ItemStack(id, amount, (short) damage);

				for (String enchant : enchants)
				{
					if (!enchant.contains(":"))
						continue;

					String[] enchantS = enchant.split(":");
					int eID = Integer.parseInt(enchantS[0]);
					int eLevel = Integer.parseInt(enchantS[1]);

					stack.addUnsafeEnchantment(Enchantment.getById(eID), eLevel);
				}

				inventory.setItem(slot, stack);
			}

			statement.close();

			return inventory;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	private void storeInventoryToDB(Player player, Inventory inventory)
	{

		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("DELETE FROM chestInventory WHERE ID = ? AND Player = ?");
			statement.setInt(1, id);
			statement.setString(2, player.getName());
			statement.executeUpdate();
			statement.close();

			if (inventory == null)
			{
				IO.getConnection().commit();
				return;
			}

			statement = IO.getConnection().prepareStatement("INSERT INTO chestInventory (ID, Player, Slot, ItemID, Damage, Amount, Enchants) VALUES (?,?,?,?,?,?,?)");

			int size = inventory.getSize();
			for (int i = 0; i < size; i++)
			{
				ItemStack stack = inventory.getItem(i);
				if (stack == null || stack.getTypeId() == 0)
					continue;

				statement.setInt(1, id);
				statement.setString(2, player.getName());
				statement.setInt(3, i);
				statement.setInt(4, stack.getTypeId());
				statement.setInt(5, stack.getDurability());
				statement.setInt(6, stack.getAmount());

				String enchants = "";
				for (Entry<Enchantment, Integer> e : stack.getEnchantments().entrySet())
				{
					enchants += e.getKey().getId() + ":" + e.getValue() + ",";
				}

				statement.setString(7, enchants);
				statement.addBatch();
			}

			statement.executeBatch();
			IO.getConnection().commit();
			statement.close();
		}
		catch (SQLException e) {
		}
	}

	private String getCustomName()
	{
		if (!Util.isInventoryContainer(chestBlock.getTypeId()))
			return "";

		if (chestBlock.getType() == Material.CHEST || chestBlock.getType() == Material.TRAPPED_CHEST)
		{
			TileEntityChest tileEntity = (TileEntityChest) ((CraftWorld) chestBlock.getWorld()).getHandle().getTileEntity(chestBlock.getX(), chestBlock.getY(), chestBlock.getZ());
			return tileEntity.c() ? tileEntity.getName() : "Chest";

		}
		else
			return Util.getMaterialName(chestBlock.getType());
	}
	
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
	
	private static void fixInventoryType(Inventory inventory, InventoryType type)
	{
		CraftInventory   customInv = (CraftInventory)  inventory;

		Field field;
		try {
			field = CraftInventory.class.getDeclaredField("inventory");
			field.setAccessible(true);
			
			Object internalInv = field.get(customInv);
			Class<?> internalInvClass = Class.forName("org.bukkit.craftbukkit.v1_6_R2.inventory.CraftInventoryCustom$MinecraftInventory");
			
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
