package us.corenetwork.mantle.slimeballs.commands;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import us.core_network.cornel.common.Messages;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.slimeballs.SlimeballsSettings;
import us.corenetwork.mantle.slimeballs.SlimeballsStorage;


public class SlimeballsAwardCommand extends BaseSlimeballsCommand
{

	public SlimeballsAwardCommand()
	{
		permission = "award";
		desc = "Award slimeballs to someone";
		needPlayer = false;
	}


	public void run(CommandSender sender, String[] args) {
		if (args.length < 1)
		{
			sender.sendMessage("Usage: /slimeballs award <Player> [<Amount>]");
			return;
		}

		int amount = 1;
		if (args.length > 1 && Util.isInteger(args[1]))
			amount = Integer.parseInt(args[1]);

		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
		UUID uuid = offlinePlayer.getUniqueId();
		if (!offlinePlayer.hasPlayedBefore() || uuid == null)
		{
			String message = SlimeballsSettings.MESSAGE_SLIMEBALLS_PLAYER_NOT_EXISTS.string();
			message = message.replace("<Player>", args[0]);
            Messages.send(message, sender);

			return;
		}

		int slimeballs = SlimeballsStorage.getSlimeballs(uuid);
		slimeballs += amount;
		SlimeballsStorage.setSlimeballs(uuid, slimeballs);

		if (amount <= 0)
			return; //Do not display any messages for negative

		String awardedAnnouncement = SlimeballsSettings.MESSAGE_SLIMEBALLS_AWARDED_OTHER.string();
		awardedAnnouncement = awardedAnnouncement.replace("<Player>", offlinePlayer.getName());
		awardedAnnouncement = awardedAnnouncement.replace("<Amount>", Integer.toString(amount));
		if (slimeballs == 1)
			awardedAnnouncement = awardedAnnouncement.replace("<PluralS>", "");
		else
			awardedAnnouncement = awardedAnnouncement.replace("<PluralS>", "s");

		if (offlinePlayer.isOnline())
		{
			String message = SlimeballsSettings.MESSAGE_SLIMEBALLS_AWARDED_PLAYER.string();
			message = message.replace("<Amount>", Integer.toString(amount));
			if (slimeballs == 1)
				message = message.replace("<PluralS>", "");
			else
				message = message.replace("<PluralS>", "s");

            Messages.send(message, offlinePlayer.getPlayer());
            Messages.broadcastWithExclusion(awardedAnnouncement, offlinePlayer.getName());
		}
		else
		{
			Messages.broadcast(awardedAnnouncement);
		}
	}
	

}
