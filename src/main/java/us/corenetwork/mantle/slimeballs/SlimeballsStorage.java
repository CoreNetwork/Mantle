package us.corenetwork.mantle.slimeballs;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import us.corenetwork.mantle.IO;

public class SlimeballsStorage {
	public static int getSlimeballs(UUID player)
	{
		int points = 1; //Default points = 1

		try {
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT Slimeballs FROM Slimeballs WHERE PlayerUUID = ? LIMIT 1");
			statement.setString(1, player.toString());
			ResultSet set = statement.executeQuery();
			if (set.next())
			{
				points = set.getInt("Slimeballs");
			}
			
			set.close();
			statement.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		return points;
	}
	
	public static void setSlimeballs(UUID player, Integer slimeballs)
	{
		try {
			PreparedStatement statement = IO.getConnection().prepareStatement("REPLACE INTO Slimeballs (PlayerUUID, Slimeballs) VALUES (?,?)");
			statement.setString(1, player.toString());
			statement.setInt(2, slimeballs);
			statement.executeUpdate();
			statement.close();
			
			IO.getConnection().commit();

		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
