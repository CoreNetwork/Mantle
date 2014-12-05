package us.corenetwork.mantle.animalspawning;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.MantlePlugin;


public class AnimalSpawningIO {
	private static Connection connection;

	public static synchronized Connection getConnection()
	{
		if (connection == null)
			connection = createConnection();
		return connection;
	}

	private static Connection createConnection()
	{
		try
		{
			Class.forName("org.sqlite.JDBC");
			Connection ret = DriverManager.getConnection("jdbc:sqlite:"
					+ new File(MantlePlugin.instance.getDataFolder().getPath(),
							"animals.sqlite").getPath());
			ret.setAutoCommit(false);
			return ret;
		} catch (ClassNotFoundException e)
		{
			e.printStackTrace();
			return null;
		} catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static synchronized void freeConnection()
	{
		Connection conn = getConnection();
		if (conn != null)
		{
			try
			{
				conn.close();
				conn = null;
			} catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
	}

	public static void PrepareDB()
	{
		Connection conn;
		Statement st = null;
		try
		{
			conn = AnimalSpawningIO.getConnection();// {
			st = conn.createStatement();
			st.executeUpdate("CREATE TABLE IF NOT EXISTS animals (UUID STRING, Type STRING)");
			st.executeUpdate("CREATE INDEX IF NOT EXISTS animals_uuid_idx ON animals (UUID)");
			conn.commit();
			st.close();
		} catch (SQLException e)
		{
			MLog.severe("[Mantle]: Error while creating animal tables! - " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void saveAnimals()
	{
		long startTime = System.currentTimeMillis();
		
		
		try
		{
			PreparedStatement statement = getConnection().prepareStatement("INSERT INTO animals VALUES (?, ?)");
			for(Entry<UUID, String> entry : AnimalSpawningModule.spawnedAnimals.entrySet())
			{
				statement.setString(1, entry.getKey().toString());
				statement.setString(2, entry.getValue());
				statement.addBatch();
			}
			statement.executeBatch();
            statement.close();
            getConnection().commit();
            
		} catch (SQLException e){
			MLog.severe("Error while saving new animals to the database !");
			e.printStackTrace();
		}
		
		try
		{
			PreparedStatement statement = getConnection().prepareStatement("DELETE FROM animals WHERE UUID = ?");
			for(UUID key : AnimalSpawningModule.killedAnimals)
			{
				statement.setString(1, key.toString());
				statement.addBatch();
			}
			statement.executeBatch();
            statement.close();
            getConnection().commit();
            
		} catch (SQLException e){
			MLog.severe("Error while removing dead animals from the database !");
			e.printStackTrace();
		}
		
		MLog.debug("[AnimalSpawning] Saved :   " + AnimalSpawningModule.spawnedAnimals.keySet().size());
		MLog.debug("[AnimalSpawning] Deleted : " + AnimalSpawningModule.killedAnimals.size());
		MLog.debug("[AnimalSpawning] Time :    " + (System.currentTimeMillis() - startTime));
		
		AnimalSpawningModule.spawnedAnimals.clear();
		AnimalSpawningModule.killedAnimals.clear();
		AnimalSpawningModule.animalCounts = getAnimalCounts();
		MLog.info("[AnimalSpawning] SavingTime :    " + (System.currentTimeMillis() - startTime));
		
	}

	public static Map<String, Integer> getAnimalCounts()
	{
		Map<String, Integer> counts = new HashMap<String, Integer>();
		try
		{
			PreparedStatement statement = getConnection().prepareStatement("SELECT COUNT(*), Type FROM animals GROUP BY Type");
			ResultSet set = statement.executeQuery();
			while(set.next())
			{
				counts.put(set.getString(2), set.getInt(1));
			}
			
		} catch (SQLException e)
		{
			MLog.severe("Error while retrieving animal counts from the database !");
			e.printStackTrace();
		}
		return counts;
	}
}