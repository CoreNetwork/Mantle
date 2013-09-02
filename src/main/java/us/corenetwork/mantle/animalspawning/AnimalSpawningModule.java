package us.corenetwork.mantle.animalspawning;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.MantleModule;
import us.corenetwork.mantle.IO;
import us.corenetwork.mantle.MantlePlugin;


public class AnimalSpawningModule extends MantleModule {
	public static AnimalSpawningModule instance;
	
	public AnimalSpawningModule() {
		super("Animal spawning", null, "animalspawn");
		
		instance = this;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		return false;
	}
	
	@Override
	protected boolean loadModule() {

		for (AnimalSpawningSettings setting : AnimalSpawningSettings.values())
		{
			if (config.get(setting.string) == null)
				config.set(setting.string, setting.def);
		}
				
		saveConfig();
				
		Bukkit.getServer().getPluginManager().registerEvents(new AnimalSpawningHelper(), MantlePlugin.instance);
		
		AnimalSpawningTimer.timerSingleton = new AnimalSpawningTimer();
		
		Bukkit.getScheduler().runTaskLaterAsynchronously(MantlePlugin.instance, AnimalSpawningTimer.timerSingleton, 20);
	    
		initSql();
		
		return true;
	}
	@Override
	protected void unloadModule() {
		saveConfig();
	}	
	
	private static void initSql()
	{
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT COUNT(*) FROM animal_chunks");
			ResultSet set = statement.executeQuery();
			
			int count = set.getInt(1);
			
			statement.close();
			
			if (count <= 0)
			{
				MLog.info("Initializing DB...");
				
				int minX = AnimalSpawningSettings.CHUNK_MIN_X.integer();
				int minZ = AnimalSpawningSettings.CHUNK_MIN_Z.integer();
				int maxX = AnimalSpawningSettings.CHUNK_MAX_X.integer();
				int maxZ = AnimalSpawningSettings.CHUNK_MAX_Z.integer();

				statement = IO.getConnection().prepareStatement("INSERT INTO animal_chunks (X, Z, Spawned) VALUES (?,?, random())");
				for (int x = minX; x <= maxX; x++)
				{
					for (int z = minZ; z <= maxZ; z++)
					{
						statement.setInt(1, x);
						statement.setInt(2, z);
						statement.addBatch();
					}
				}
				
				statement.executeBatch();
				statement.close();
				IO.getConnection().commit();
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
}
