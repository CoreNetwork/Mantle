package us.corenetwork.mantle.slimeballs.commands;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.slimeballs.SlimeballsSettings;
import us.corenetwork.mantle.slimeballs.SlimeballsStorage;


public class SlimeballsPayCommand extends BaseSlimeballsCommand
{

	public SlimeballsPayCommand()
	{
		permission = "pay";
		desc = "Pay slimeball to get out of limbo";
		needPlayer = true;
	}


	public void run(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		int slimeballs = SlimeballsStorage.getSlimeballs(player.getUniqueId());

		if (slimeballs < 1)
		{
			Util.Message(SlimeballsSettings.MESSAGE_SLIMEBALLS_RELEASE_EMPTY_ACCOUNT.string(), player);
			return;
		}

		slimeballs--;

		String modMessage = SlimeballsSettings.MESSAGE_SLIMEBALLS_RELEASE_NOTIFICATION.string();
		modMessage = modMessage.replace("<Player>", player.getName());
		modMessage = modMessage.replace("<Amount>", Integer.toString(slimeballs));
		for (Player mod : Bukkit.getOnlinePlayers())
		{
			if (Util.hasPermission(mod, "mantle.slimeballs.command.pay.notification"))
				Util.Message(modMessage, mod);
		}
		System.out.println(modMessage);

		SlimeballsStorage.setSlimeballs(player.getUniqueId(), slimeballs);

		String releaseCommand = SlimeballsSettings.RELEASE_COMMAND.string();
		releaseCommand = releaseCommand.replace("<Player>", player.getName());
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), releaseCommand);

	}
	

}