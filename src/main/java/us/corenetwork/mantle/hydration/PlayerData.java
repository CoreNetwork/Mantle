package us.corenetwork.mantle.hydration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import us.corenetwork.mantle.IO;

public class PlayerData {	
	private static HashMap<String, PlayerData> pendingSaves = new HashMap<String, PlayerData>();
	
	private String playerName;
	
	public int fatigueLevel;
	public long fatigueEffectStart;
	public double hydrationLevel;
	public double saturationLevel;
	public List<Integer> deliveredMessages;
	public boolean waitingToSave;
	
	public static PlayerData getPlayer(String name)
	{
		name = name.toLowerCase();
		
		PlayerData data = pendingSaves.get(name);
		if (data != null)
			return data;
		
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT * FROM hydration WHERE Player = ? LIMIT 1");
			statement.setString(1, name);
			ResultSet set = statement.executeQuery();
			if (set.next())
			{
				data = new PlayerData(name, set);
			}
			else
			{
				data = new PlayerData(name);
			}
			
			statement.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
						
		return data;
	}
			
	public void save()
	{
		waitingToSave = true;
		pendingSaves.put(playerName, this);
//		try
//		{
//			PreparedStatement delStatement = IO.getConnection().prepareStatement("DELETE FROM hydration WHERE Player = ?");
//			PreparedStatement insertStatement = IO.getConnection().prepareStatement("INSERT INTO hydration (Player, Hydration, Saturation, FatigueLevel, FatigueLevelStart, DeliveredMessages) (?,?,?,?,?,?)");
//		
//			save(delStatement, insertStatement);
//			
//			delStatement.executeUpdate();
//			delStatement.close();
//			
//			insertStatement.executeUpdate();
//			insertStatement.close();
//			
//			IO.getConnection().commit();
//		}
//		catch (SQLException e)
//		{
//			e.printStackTrace();
//		}	
	}
	
	public void save(PreparedStatement delStatement, PreparedStatement insertStatement) throws SQLException
	{
		delStatement.setString(1, playerName);
		
		insertStatement.setString(1, playerName);
		insertStatement.setDouble(2, hydrationLevel);
		insertStatement.setDouble(3, saturationLevel);
		insertStatement.setInt(4, fatigueLevel);
		insertStatement.setLong(5, fatigueEffectStart);
		insertStatement.setString(6, serializeIntegerList(deliveredMessages));
		
		waitingToSave = false;
		pendingSaves.remove(playerName);
	}
	
	private PlayerData(String name)
	{
		playerName = name;
		fatigueLevel = 0;
		fatigueEffectStart = 0;
		hydrationLevel = 100;
		saturationLevel = 50;
		deliveredMessages = new ArrayList<Integer>();
	}
	
	private PlayerData(String name, ResultSet set) throws SQLException
	{
		playerName = name;
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
	
}
