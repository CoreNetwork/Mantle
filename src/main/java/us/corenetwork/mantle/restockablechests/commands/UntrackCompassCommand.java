package us.corenetwork.mantle.restockablechests.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.corenetwork.mantle.IO;
import us.corenetwork.mantle.mantlecommands.BaseMantleCommand;
import us.corenetwork.mantle.restockablechests.CompassDestination;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by Ginaf on 2015-02-22.
 */
public class UntrackCompassCommand extends BaseMantleCommand {
    public UntrackCompassCommand()
    {
        permission = "untrack";
        desc = "Untrack the compass category";
        needPlayer = true;
    }

    public void run(CommandSender sender, String[] args)
    {
        Player player = (Player) sender;

        CompassDestination.destinations.remove(player.getUniqueId());

        try
        {
            PreparedStatement statement = IO.getConnection().prepareStatement("UPDATE playerTotal SET CompassCategory = ?, CompassChestID = 0 WHERE PlayerUUID = ?");
            statement.setString(2, player.getUniqueId().toString());
            statement.setString(1, null);
            statement.executeUpdate();
            statement.close();

            IO.getConnection().commit();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        CompassDestination.resetCompassTarget(player);
    }
}
