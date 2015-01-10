package us.corenetwork.mantle.hydration;

import com.sk89q.worldedit.entity.Player;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import us.corenetwork.mantle.IO;

public class PlayerData {	
	private static HashMap<UUID, PlayerData> playerCache = new HashMap<UUID, PlayerData>();

	private UUID playerUUID;
	
	public int fatigueLevel;
	public long fatigueEffectStart;
	public double hydrationLevel;
	public double saturationLevel;
	public List<Integer> deliveredMessages;
	public boolean waitingToSave;
	
	public static PlayerData getPlayer(UUID uuid)
	{		
		PlayerData data = playerCache.get(uuid);
		if (data != null)
			return data;
		
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT * FROM hydration WHERE PlayerUUID = ? LIMIT 1");
			statement.setString(1, uuid.toString());
			ResultSet set = statement.executeQuery();
			if (set.next())
			{
				data = new PlayerData(uuid, set);
			}
			else
			{
				data = new PlayerData(uuid);
			}
			
			statement.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		playerCache.put(uuid, data);

		return data;
	}
			
	public void save()
	{
		waitingToSave = true;
	}

	private PlayerData(UUID uuid)
	{
		playerUUID = uuid;
		fatigueLevel = 0;
		fatigueEffectStart = 0;
		hydrationLevel = 100;
		saturationLevel = 50;
		deliveredMessages = new ArrayList<Integer>();
	}
	
	private PlayerData(UUID uuid, ResultSet set) throws SQLException
	{
		playerUUID = uuid;
		hydrationLevel = set.getDouble("Hydration");
		saturationLevel = set.getDouble("Saturation");
		fatigueLevel = set.getInt("FatigueLevel");
		fatigueEffectStart = set.getLong("FatigueLevelStart");
		deliveredMessages = unserializeIntegerList(set.getString("DeliveredMessages"));
	}
	
	private static List<Integer> unserializeIntegerList(String listString)
	{
		if (listString.trim().length() == 0)
			return new ArrayList<Integer>();
		
		String[] splittedString = listString.split(",");
		List<Integer> list = new ArrayList<Integer>(splittedString.length);
		
		for (String s : splittedString)
			list.add(Integer.parseInt(s));
		
		return list;
	}
	
	private static String serializeIntegerList(List<Integer> list)
	{
		StringBuilder builder = new StringBuilder();
		for (int number : list)
		{
			builder.append(number);
			builder.append(',');
		}
		
		return builder.toString();
	}

	public static void saveAll()
	{
		try
		{
			PreparedStatement insertStatement = IO.getConnection().prepareStatement("REPLACE INTO hydration (PlayerUUID, Hydration, Saturation, FatigueLevel, FatigueLevelStart, DeliveredMessages) VALUES (?,?,?,?,?,?)");

			for (PlayerData playerData : playerCache.values())
			{
				if (playerData.waitingToSave)
				{
					insertStatement.setString(1, playerData.playerUUID.toString());
					insertStatement.setDouble(2, playerData.hydrationLevel);
					insertStatement.setDouble(3, playerData.saturationLevel);
					insertStatement.setInt(4, playerData.fatigueLevel);
					insertStatement.setLong(5, playerData.fatigueEffectStart);
					insertStatement.setString(6, serializeIntegerList(playerData.deliveredMessages));
					insertStatement.addBatch();

					playerData.waitingToSave = false;
				}
			}

			insertStatement.executeBatch();
			insertStatement.close();
			IO.getConnection().commit();

		} catch (SQLException e)
		{
			e.printStackTrace();
		}

	}
	
}
