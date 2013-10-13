package us.corenetwork.mantle.restockablechests;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.server.v1_6_R3.NBTBase;
import net.minecraft.server.v1_6_R3.NBTTagCompound;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_6_R3.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import us.corenetwork.mantle.IO;
import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.MantlePlugin;

public class NBTStorage {
	public static boolean hasNbt(ItemStack stack)
	{
		net.minecraft.server.v1_6_R3.ItemStack nmsStack =  CraftItemStack.asNMSCopy(stack);
		return nmsStack.tag != null;
	}
	
	public static void saveNbtTags(Integer chestId, String player, Inventory inventory)
	{
		YamlConfiguration configuration = new YamlConfiguration();
		
		int nbtItems = 0;
		for (int i = 0; i < inventory.getSize(); i++)
		{
			ItemStack item = inventory.getItem(i);
			if (item == null || item.getType() == Material.AIR)
				continue;
			
			NBTTagCompound tag = CraftItemStack.asNMSCopy(item).tag;
			if (tag == null)
				continue;

			ConfigurationSection section = configuration.createSection(Integer.toString(i));
			
			Collection<NBTBase> c = tag.c();
			for (NBTBase base : c) {
				us.corenetwork.mantle.nanobot.commands.SaveCommand.addTag(section, base);
			}
			
			nbtItems++;
		}
		
		File dataFolder = new File(MantlePlugin.instance.getDataFolder(), "chestNbtData");
		if (!dataFolder.exists())
			dataFolder.mkdir();
		
		File file = new File(dataFolder, chestId + "_" + player + ".yml");
		
		if (nbtItems > 0)
		{
			try
			{
				configuration.save(file);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		else if (file.exists())
			file.delete();
	}
	
	public static void loadNbtTags(Integer chestId, String player, Inventory inventory)
	{
		File dataFolder = new File(MantlePlugin.instance.getDataFolder(), "chestNbtData");
		if (!dataFolder.exists())
			return;
		
		File file = new File(dataFolder, chestId + "_" + player + ".yml");
		if (!file.exists())
			return;
		
		YamlConfiguration configuration = new YamlConfiguration();
		try
		{
			configuration.load(file);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
		
		for (Entry<String, Object> e : configuration.getValues(false).entrySet())
		{
			Integer slot = Integer.parseInt(e.getKey());
						
			NBTTagCompound tag;
			if (e.getValue() instanceof Map)
				tag = us.corenetwork.mantle.nanobot.commands.LoadCommand.load((Map<?,?>) e.getValue());
			else if (e.getValue() instanceof MemorySection)
				tag = us.corenetwork.mantle.nanobot.commands.LoadCommand.load(((MemorySection) e.getValue()).getValues(false));
			else
			{
				MLog.warning("Unknown NBT yaml type: " + e.getValue().getClass().getName());
				continue;
			}
			
			net.minecraft.server.v1_6_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(inventory.getItem(slot));
			nmsStack.tag = tag;
			inventory.setItem(slot, CraftItemStack.asBukkitCopy(nmsStack));

		}
	}

	public static void cleanStorage()
	{
		MLog.info("Cleaning NBT storage...");
		
		File dataFolder = new File(MantlePlugin.instance.getDataFolder(), "chestNbtData");
		if (!dataFolder.exists())
			return;
		
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT playerChests.ID,Player FROM playerChests LEFT JOIN chests ON playerChests.ID = chests.ID WHERE (strftime('%s', 'now') - lastAccess > interval * 3600)");
			ResultSet set = statement.executeQuery();
			
			while (set.next())
			{
				int id = set.getInt("ID");
				String player = set.getString("Player");
				
				File file = new File(dataFolder, id + "_" + player + ".yml");
				if (file.exists())
					file.delete();
			}
			
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
	}
}
