package us.corenetwork.mantle.slimeballs.commands;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import us.core_network.cornel.common.Messages;
import us.core_network.cornel.strings.NumberParsing;
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


	public void run(CommandSender sender, String[] args)
    {
        int argumentCount = args.length;
        boolean silent = false;
        if (argumentCount >= 1 && args[argumentCount - 1].equalsIgnoreCase("silent"))
        {
            argumentCount--;

            silent = true;
        }


        if (argumentCount < 1)
        {
            sender.sendMessage("Usage: /slimeballs award <Player> [<Amount>]");
            return;
        }


        int amount = 1;
        int reasonPosition = 1;
        if (args.length > 1 && NumberParsing.isInteger(args[1]))
        {
            amount = Integer.parseInt(args[1]);
            reasonPosition++;
        }

        String reason = "";
        if (argumentCount > reasonPosition)
        {
            for (int i = reasonPosition; i < argumentCount; i++)
                reason = reason.concat(args[i]).concat(" ");

            if (!reason.isEmpty())
                reason = reason.substring(0, reason.length() - 1);
        }

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

        String awardedAnnouncement = reason.isEmpty() ? SlimeballsSettings.MESSAGE_SLIMEBALLS_AWARDED_OTHER.string() : SlimeballsSettings.MESSAGE_SLIMEBALLS_AWARDED_OTHER_REASON.string();
		awardedAnnouncement = awardedAnnouncement.replace("<Player>", offlinePlayer.getName());
		awardedAnnouncement = awardedAnnouncement.replace("<Amount>", Integer.toString(amount));
        awardedAnnouncement = awardedAnnouncement.replace("<Reason>", reason);

        if (amount == 1)
			awardedAnnouncement = awardedAnnouncement.replace("<PluralS>", "");
		else
			awardedAnnouncement = awardedAnnouncement.replace("<PluralS>", "s");

		if (offlinePlayer.isOnline())
		{
            String message = reason.isEmpty() ? SlimeballsSettings.MESSAGE_SLIMEBALLS_AWARDED_PLAYER.string() : SlimeballsSettings.MESSAGE_SLIMEBALLS_AWARDED_PLAYER_REASON.string();
			message = message.replace("<Amount>", Integer.toString(amount));
            message = message.replace("<Reason>", reason);
            if (amount == 1)
				message = message.replace("<PluralS>", "");
			else
				message = message.replace("<PluralS>", "s");

            Messages.send(message, offlinePlayer.getPlayer());

            if (!silent)
                Messages.broadcastWithExclusion(awardedAnnouncement, offlinePlayer.getName());
		}
        else if (!silent)
		{
			Messages.broadcast(awardedAnnouncement);
		}
	}
	

}
