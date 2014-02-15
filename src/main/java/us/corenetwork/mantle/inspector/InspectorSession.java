package us.corenetwork.mantle.inspector;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import us.corenetwork.mantle.IO;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.regeneration.RegenerationUtil;


public class InspectorSession {
	public static HashMap<String, InspectorSession> sessions = new HashMap<String, InspectorSession>();
	
	public int current = -1;
	public HashSet<Integer> skipped = new HashSet<Integer>();
	
	public void teleportToNext(Player player)
	{
		StructureData nextStructure = getNext();
		if (nextStructure == null)
		{
			current = -1;
			Util.Message(InspectorSettings.MESSAGE_NO_STRUCTURE_FOUND.string(), player);
			return;
		}
		
		current = nextStructure.id;
		Util.safeTeleport(player, nextStructure.teleport);
		String message = InspectorSettings.MESSAGE_TELEPORTED.string();
		message = message.replace("<ID>", Integer.toString(current));
		Util.Message(message, player);
	}
	
	/**
	 * @param state 0 = uninspected, 1 = approve, 2 = postpone
	 */
	public void setState(int state)
	{
		if (current < 0)
			return;
		
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("UPDATE regeneration_structures SET InspectionStatus = ? WHERE ID = ?");
			if (state == 2)
			{
				int time = (int) (System.currentTimeMillis() / 1000);
				time += InspectorSettings.POSTPONE_TIME.integer();
				statement.setInt(1, -time);
			}
			else
				statement.setInt(1, state);
			
			statement.setInt(2, current);
			
			statement.executeUpdate();
			statement.close();
			IO.getConnection().commit();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public void rejectStructure()
	{
		RegenerationUtil.regenerateStructure(current, (int) (System.currentTimeMillis() / 1000), true);
		setState(0); //Just in case
	}
	
	public void skip()
	{
		skipped.add(current);
	}
	
	private StructureData getNext()
	{
		StringBuilder excludedBuilder = new StringBuilder();
		
		for (InspectorSession session : sessions.values())
		{
			if (session.current >= 0)
			{
				excludedBuilder.append(Integer.toString(session.current));
				excludedBuilder.append(',');
			}
		}
		
		
		for (Integer id : skipped)
		{
			excludedBuilder.append(Integer.toString(id));
			excludedBuilder.append(',');
		}
		
		if (excludedBuilder.length() > 1)
			excludedBuilder.deleteCharAt(excludedBuilder.length() - 1);
		
		StructureData structure = null;
		int curTime = (int) (System.currentTimeMillis() / 1000);
		
		
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT * FROM regeneration_structures WHERE (InspectionStatus = 0 OR (InspectionStatus < 0 AND InspectionStatus > ?)) AND lastCheck <> lastRestore AND id NOT IN (" + excludedBuilder.toString() + ") LIMIT 1");
			statement.setInt(1, -curTime);
			
			ResultSet set = statement.executeQuery();
			
			if (!set.next())
			{
				statement.close();
				return null;
			}
			
			int x = set.getInt("CornerX") + set.getInt("SizeX") / 2;
			int z = set.getInt("CornerZ") + set.getInt("SizeZ") / 2;
			World world = Bukkit.getWorld(set.getString("World"));
			
			int y = Math.max(InspectorSettings.MINIMUM_TELEPORT_Y.integer(), world.getHighestBlockYAt(x, z) + 1);
			
			structure = new StructureData();
			structure.id = set.getInt("ID");
			structure.teleport = new Location(world, x + 0.5, y, z + 0.5);
			
			statement.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		return structure;
	}
	
	private static class StructureData
	{
		public Location teleport;
		public int id;
		
	}
}
