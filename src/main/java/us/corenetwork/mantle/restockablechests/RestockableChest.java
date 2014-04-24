package us.corenetwork.mantle.restockablechests;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import net.minecraft.server.v1_7_R3.PacketPlayOutBlockAction;
import net.minecraft.server.v1_7_R3.TileEntity;
import net.minecraft.server.v1_7_R3.TileEntityBeacon;
import net.minecraft.server.v1_7_R3.TileEntityBrewingStand;
import net.minecraft.server.v1_7_R3.TileEntityChest;
import net.minecraft.server.v1_7_R3.TileEntityDispenser;
import net.minecraft.server.v1_7_R3.TileEntityDropper;
import net.minecraft.server.v1_7_R3.TileEntityFurnace;
import net.minecraft.server.v1_7_R3.TileEntityHopper;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.MemorySection;
import org.bukkit.craftbukkit.v1_7_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R3.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_7_R3.inventory.CraftInventoryCustom;
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

		Inventory inventory = ((InventoryHolder) chest.getState()).getInventory();
		if (inventory instanceof DoubleChestInventory)
		{
			DoubleChestInventory doubleChestInventory = (DoubleChestInventory) inventory;
			Chest leftChest = (Chest) doubleChestInventory.getLeftSide().getHolder();
			chest = leftChest.getBlock();
		}

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
			e.printStackTrace();
		}

		return null;
	}

	public static void createChest(Block chest, String lootTable, int interval, boolean perPlayer)
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
		
		int timerLeft = ChestTimeout.getRemainingTime(lootTable, player.getName(), chestBlock);
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
			PacketPlayOutBlockAction chestOpenPacket = new PacketPlayOutBlockAction(chestBlock.getX(), chestBlock.getY(), chestBlock.getZ(), net.minecraft.server.v1_7_R3.Block.e(chestBlock.getTypeId()), 1, 1);

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
					PacketPlayOutBlockAction chestClosePacket = new PacketPlayOutBlockAction(chest.chestBlock.getX(), chest.chestBlock.getY(), chest.chestBlock.getZ(), net.minecraft.server.v1_7_R3.Block.e(chest.chestBlock.getTypeId()), 1, 0);
					
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
			ChestTimeout.addTimer(lootTable, player.getName(), timeoutMinutes, chestBlock);
		
		String numberDisplay = "";
		int maxNumber = RChestsModule.instance.config.getInt("LootTables." + lootTable + ".PlayerControl.MaximumDisplayedAccessNumber", Integer.MAX_VALUE);
		if (restocks <= maxNumber)
			numberDisplay = Integer.toString(restocks);
		else
			numberDisplay = maxNumber + "+";

		LootTableNodeParser parser = new LootTableNodeParser(lootTable, 1, addChance, RChestsModule.instance.config);
		List<ItemStack> items = parser.parse();
		
		Inventory inventory;
		if (player == null)
			inventory = inventoryHolder.getInventory();
		else
		{
			inventory = createEmptyInventory(numberDisplay);
			
			if (!parser.didAnyItemHadAnyChance())
			{
				String message = RChestsModule.instance.config.getString("LootTables." + lootTable + ".PlayerControl.ChanceDiminishing.ZeroChanceMessage", "Admin of this server is too lazy to enter message!");
				Util.Message(message, player);

			}
		}

		inventory.clear();

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
			int maxNumber = RChestsModule.instance.config.getInt("LootTables." + lootTable + ".PlayerControl.MaximumDisplayedAccessNumber", Integer.MAX_VALUE);
			if (restocks <= maxNumber)
				numberDisplay = Integer.toString(restocks);
			else
				numberDisplay = maxNumber + "+";

			Inventory inventory = createEmptyInventory(numberDisplay);

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

				ItemStack stack = new ItemStack(id, amount, (short) damage);

				inventory.setItem(slot, stack);
			}

			statement.close();

			NBTStorage.loadNbtTags(id, player.getName(), inventory);
			
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

			statement = IO.getConnection().prepareStatement("INSERT INTO chestInventory (ID, Player, Slot, ItemID, Damage, Amount) VALUES (?,?,?,?,?,?)");

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

				statement.addBatch();
			}

			statement.executeBatch();
			IO.getConnection().commit();
			statement.close();
		}
		catch (SQLException e) {
		}
		
		NBTStorage.saveNbtTags(id, player.getName(), inventory);
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
		ParticleLibrary.SPELL.sendToPlayer(player, location, 0, 0, 0, 0, 3);
	}
	private String getCustomName()
	{
		if (!Util.isInventoryContainer(chestBlock.getTypeId()))
			return "";

		TileEntity tEntity = ((CraftWorld) chestBlock.getWorld()).getHandle().getTileEntity(chestBlock.getX(), chestBlock.getY(), chestBlock.getZ());

		if (tEntity instanceof TileEntityChest)
		{
			TileEntityChest container = (TileEntityChest) tEntity;
			return container.k_() ? container.getInventoryName() : "Chest";
		}
		else if (tEntity instanceof TileEntityDispenser)
		{
			TileEntityDispenser container = (TileEntityDispenser) tEntity;
			return container.k_() ? container.getInventoryName() : "Dispenser";
		}
		else if (tEntity instanceof TileEntityFurnace)
		{
			TileEntityFurnace container = (TileEntityFurnace) tEntity;
			return container.k_() ? container.getInventoryName() : "Furnace";
		}
		else if (tEntity instanceof TileEntityHopper)
		{
			TileEntityHopper container = (TileEntityHopper) tEntity;
			return container.k_() ? container.getInventoryName() : "Hopper";
		}
		else if (tEntity instanceof TileEntityDropper)
		{
			TileEntityDropper container = (TileEntityDropper) tEntity;
			return container.k_() ? container.getInventoryName() : "Dropper";
		}
		else if (tEntity instanceof TileEntityBeacon)
		{
			TileEntityBeacon container = (TileEntityBeacon) tEntity;
			return container.k_() ? container.getInventoryName() : "Beacon";
		}
		else if (tEntity instanceof TileEntityBrewingStand)
		{
			TileEntityBrewingStand container = (TileEntityBrewingStand) tEntity;
			return container.k_() ? container.getInventoryName() : "Brewing Stand";
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
			Class<?> internalInvClass = Class.forName("org.bukkit.craftbukkit.v1_7_R3.inventory.CraftInventoryCustom$MinecraftInventory");

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
}
