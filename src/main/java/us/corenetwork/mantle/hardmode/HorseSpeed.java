package us.corenetwork.mantle.hardmode;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import us.corenetwork.mantle.IO;

public class HorseSpeed {
	public static double getOriginalHorseSpeed(String id)
	{
		double speed = -1;
		
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT Speed FROM horseSpeeds WHERE ID = ?");
			statement.setString(1, id);
			
			ResultSet set = statement.executeQuery();
			if (set.next())
				speed = set.getDouble(1);
			
			statement.close();
			
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		return speed;
	}
	
	public static void setOriginalHorseSpeed(String id, double speed)
	{
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("DELETE FROM horseSpeeds WHERE ID = ?");
			statement.setString(1, id);
			statement.executeUpdate();
			statement.close();
			
			statement = IO.getConnection().prepareStatement("INSERT INTO horseSpeeds (ID, Speed) VALUES (?,?)");
			statement.setString(1, id);
			statement.setDouble(2, speed);
			statement.executeUpdate();
			statement.close();
			
			IO.getConnection().commit();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
}
