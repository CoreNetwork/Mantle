package us.corenetwork.mantle.slimeballs.commands;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.slimeballs.SlimeballItem;
import us.corenetwork.mantle.slimeballs.SlimeballsSettings;
import us.corenetwork.mantle.slimeballs.SlimeballsStorage;


public class SlimeballsGiveCommand extends BaseSlimeballsCommand
{

	public SlimeballsGiveCommand()
	{
		permission = "give";
		desc = "Give slimeballs to player (when spawning in limbo)";
		needPlayer = false;
	}


	public void run(CommandSender sender, String[] args) {
		if (args.length < 1)
		{
			sender.sendMessage("Usage: /slimeballs give <Player>");
			return;
		}

		Player player = Bukkit.getPlayerExact(args[0]);
		if (player == null)
			return;

		int slimeballs = SlimeballsStorage.getSlimeballs(player.getUniqueId());

		player.getInventory().addItem(SlimeballItem.create(Math.min(slimeballs, 64)));
	}
	

}
