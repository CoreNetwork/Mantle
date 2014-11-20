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
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.corenetwork.mantle.IO;
import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.slimeballs.SlimeballsSettings;
import us.corenetwork.mantle.slimeballs.SlimeballsStorage;


public class SlimeballsCheckCommand extends BaseSlimeballsCommand
{

	public SlimeballsCheckCommand()
	{
		permission = "check";
		desc = "Check your current slimy balance";
		needPlayer = false;
	}


	public void run(CommandSender sender, String[] args) {
		UUID uuid;
		boolean you = true;

		if (args.length > 0 && Util.hasPermission(sender, "slimeballs.commands.check.others"))
		{
			you = false;

			uuid = Bukkit.getOfflinePlayer(args[0]).getUniqueId();
			if (uuid == null)
			{
				String message = SlimeballsSettings.MESSAGE_SLIMEBALLS_PLAYER_NOT_EXISTS.string();
				message = message.replace("<Player>", args[0]);
				Util.Message(message, sender);

				return;
			}
		}
		else if (!(sender instanceof  Player))
		{
			//Just display dummy message in case of console trying to request player and not supplying arguments
			Util.Message(SlimeballsSettings.MESSAGE_SLIMEBALLS_PLAYER_NOT_EXISTS.string(), sender);
			return;
		}
		else
		{
			uuid = ((Player) sender).getUniqueId();
		}

		int slimeballs = SlimeballsStorage.getSlimeballs(uuid);

		String message;
		if (you)
		{
			if (slimeballs == 0)
				message = SlimeballsSettings.MESSAGE_SLIMEBALLS_ACCOUNT_HEADER_EMPTY.string();
			else
				message = SlimeballsSettings.MESSAGE_SLIMEBALLS_ACCOUNT_HEADER_NOT_EMPTY.string();

			message = message.concat("[NEWLINE]").concat(SlimeballsSettings.MESSAGE_SLIMEBALLS_ACCOUNT_FOOTER.string());

			message = message.replace("<Amount>", Integer.toString(slimeballs));

			if (slimeballs == 1)
				message = message.replace("<PluralS>", "");
			else
				message = message.replace("<PluralS>", "s");
		}
		else
		{
			message = SlimeballsSettings.MESSAGE_SLIMEBALLS_ACCOUNT_MOD.string();
			message = message.replace("<Player>", args[0]);
			message = message.replace("<Amount>", Integer.toString(slimeballs));
		}

		Util.Message(message, sender);
	}
	

}
