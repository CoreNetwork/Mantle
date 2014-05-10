package us.corenetwork.mantle.slimespawning;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

import us.corenetwork.mantle.IO;

public class IgnoredSlimeChunks {
	private static HashSet<Long> chunks = new HashSet<Long>(); 
	
	public static void load()
	{
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT * FROM ignoredSlimeChunks");
			ResultSet set = statement.executeQuery();
			while (set.next())
			{
				chunks.add(getCombinedLong(set.getInt("x"), set.getInt("y")));
			}
			
			statement.close();

			IO.getConnection().commit();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void addChunk(int x, int y)
	{
		chunks.add(getCombinedLong(x, y));
		
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("INSERT INTO ignoredSlimeChunks (X,Y) VALUES (?,?)");
			statement.setInt(1, x);
			statement.setInt(2, y);
			statement.executeUpdate();

			IO.getConnection().commit();
			
			statement.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void removeChunk(int x, int y)
	{
		chunks.remove(getCombinedLong(x,y));
		
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("DELETE FROM ignoredSlimeChunks WHERE X=? AND Y=?");
			statement.setInt(1, x);
			statement.setInt(2, y);
			statement.executeUpdate();

			IO.getConnection().commit();
			
			statement.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public static boolean isIgnored(int x, int y)
	{
		return chunks.contains(getCombinedLong(x, y));
	}
	
	private static long getCombinedLong(int x, int y)
	{
		long out = ((long) x & 0xFFFFFFFL) | ((long) y << 32); 
		return out;
	}	
}
