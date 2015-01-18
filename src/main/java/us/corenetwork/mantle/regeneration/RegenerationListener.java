package us.corenetwork.mantle.regeneration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import us.corenetwork.mantle.IO;
import us.corenetwork.mantle.Util;


public class RegenerationListener implements Listener {
	@EventHandler(ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event)
	{				
		if (Util.hasPermission(event.getPlayer(), "mantle.mod"))
		{
			for (RegStructure structure : RegenerationModule.instance.structures.values())
			{
				if (!structure.shouldWarnAtHighClaimChance())
					continue;
				
				try
				{
					PreparedStatement statement = IO.getConnection().prepareStatement("SELECT (SELECT COUNT(*) FROM regeneration_structures WHERE InspectionStatus <= 0 AND lastCheck <> lastRestore AND StructureName = ?) AS claimed, (SELECT COUNT(*) FROM regeneration_structures  WHERE InspectionStatus <= 0 AND StructureName = ?) AS every");
					statement.setString(1, structure.getName());
					statement.setString(2, structure.getName());

					ResultSet set = statement.executeQuery();

					set.next();
					int total = set.getInt("every");
					int claimed = set.getInt("claimed");
					set.close();

					int percentage = total == 0 ? 0 : (claimed * 100 / total);

					if (percentage >= RegenerationSettings.RESTORATION_WARN_PERCENTAGE.integer())
					{
						String message = RegenerationSettings.MESSAGE_LOGIN_WARN.string();
						message = message.replace("<Structure>", structure.getName().replace('_', ' '));
						message = message.replace("<Claimed>", Integer.toString(claimed));
						message = message.replace("<Total>", Integer.toString(total));
						message = message.replace("<Percentage>", Integer.toString(percentage));

						Util.Message(message, event.getPlayer());
					}
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
