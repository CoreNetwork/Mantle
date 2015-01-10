package us.corenetwork.mantle.slimespawning;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

import us.corenetwork.mantle.IO;

public class IgnoredSlimeChunks {
	private static HashMap<String, HashSet<Long>> ignoredChunkWorlds = new HashMap<String, HashSet<Long>>();
	
	public static void load()
	{
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT * FROM ignoredSlimeChunks");
			ResultSet set = statement.executeQuery();
			while (set.next())
			{
                String world = set.getString("world");
				getWorldIgnoredChunks(world).add(getCombinedLong(set.getInt("x"), set.getInt("y")));
			}
			
			statement.close();

			IO.getConnection().commit();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void addChunk(String world, int x, int y)
	{
		getWorldIgnoredChunks(world).add(getCombinedLong(x, y));
		
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("INSERT INTO ignoredSlimeChunks (World, X ,Y) VALUES (?,?,?)");
            statement.setString(1, world);
			statement.setInt(2, x);
			statement.setInt(3, y);
			statement.executeUpdate();

			IO.getConnection().commit();
			
			statement.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void removeChunk(String world, int x, int y)
	{
		getWorldIgnoredChunks(world).remove(getCombinedLong(x,y));
		
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("DELETE FROM ignoredSlimeChunks WHERE World = ? AND X=? AND Y=?");
            statement.setString(1, world);
            statement.setInt(2, x);
			statement.setInt(3, y);
			statement.executeUpdate();

			IO.getConnection().commit();
			
			statement.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public static boolean isIgnored(String world, int x, int y)
	{
		return getWorldIgnoredChunks(world).contains(getCombinedLong(x, y));
	}

    private static HashSet<Long> getWorldIgnoredChunks(String world)
    {
        HashSet<Long> chunks = ignoredChunkWorlds.get(world);
        if (chunks == null)
        {
            chunks = new HashSet<Long>();
            ignoredChunkWorlds.put(world, chunks);
        }

        return chunks;
    }

	private static long getCombinedLong(int x, int y)
	{
		long out = ((long) x & 0xFFFFFFFL) | ((long) y << 32); 
		return out;
	}	
}
