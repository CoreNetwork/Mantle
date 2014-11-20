package us.corenetwork.mantle.slimeballs.commands;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import us.corenetwork.mantle.IO;
import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.Util;


public class SlimeballsImportCommand extends BaseSlimeballsCommand
{

	public SlimeballsImportCommand()
	{
		permission = "import";
		desc = "Import from CH";
		needPlayer = false;
	}


	public void run(CommandSender sender, String[] args) {
		File db = new File(MantlePlugin.instance.getDataFolder(), "persistance.db");
		if (!db.exists())
		{
			Util.Message("&cpersistance.db does not exists!", sender);
			return;
		}

		Util.Message("&6Importing... Please wait! (Server might freeze for several seconds)", sender);

		HashMap<UUID, Integer> allPlayers = new HashMap<UUID, Integer>();
		try {
			Connection conn = DriverManager.getConnection("jdbc:sqlite:" + db.getPath());
			conn.setAutoCommit(false);

			PreparedStatement statement = conn.prepareStatement("SELECT * FROM persistance WHERE key LIKE 'storage.slimeballStorage.%'");
			ResultSet set = statement.executeQuery();

			while (set.next())
			{
				String name = set.getString("key").split(Pattern.quote("."))[2];
				Integer slimeballs = Integer.parseInt(set.getString("value").replace("\"", ""));

				UUID uuid = Bukkit.getOfflinePlayer(name).getUniqueId();

				if (uuid == null)
				{
					MLog.warning("Failed to import Player " + name + " - cannot find his UUID! Slimeballs: " + slimeballs);
					continue;
				}

				allPlayers.put(uuid, slimeballs);
			}

			statement.close();
			conn.close();

			PreparedStatement delStatement = IO.getConnection().prepareStatement("DELETE FROM slimeballs WHERE PlayerUUID = ?");
			statement = IO.getConnection().prepareStatement("INSERT INTO slimeballs (PlayerUUID, Slimeballs) VALUES (?,?)");

			for (Entry<UUID, Integer> e : allPlayers.entrySet())
			{
				String uuid = e.getKey().toString();
				Integer points = e.getValue();

				delStatement.setString(1, uuid);

				statement.setString(1, uuid);
				statement.setInt(2, points);

				statement.addBatch();
				delStatement.addBatch();

			}

			delStatement.executeBatch();
			delStatement.close();
			statement.executeBatch();
			statement.close();

			IO.getConnection().commit();

		} catch (SQLException e) {
			e.printStackTrace();
		}



		Util.Message("&aData imported successfully! Check console for any failures.", sender);
	}
	

}
