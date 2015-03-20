package us.corenetwork.mantle.slimeballs.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.slimeballs.SlimeballsSettings;
import us.corenetwork.mantle.slimeballs.SlimeballsStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class SlimeballsPayCommand extends BaseSlimeballsCommand
{

	private Map<UUID, Long> lastExec = new HashMap<UUID, Long>();

	public SlimeballsPayCommand()
	{
		permission = "pay";
		desc = "Pay slimeball to get out of limbo";
		needPlayer = true;
	}


	public void run(CommandSender sender, String[] args) {
		Player player = (Player) sender;

		UUID uuid = player.getUniqueId();

		if(lastExec.containsKey(uuid))
		{
			if(System.currentTimeMillis() - lastExec.get(uuid) < SlimeballsSettings.MINIMUM_TIME_BETWEEN_PAY_SECONDS.integer()*1000)
			{
				return;
			}
		}

		lastExec.put(uuid, System.currentTimeMillis());

		int slimeballs = SlimeballsStorage.getSlimeballs(uuid);

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

		SlimeballsStorage.setSlimeballs(player.getUniqueId(), slimeballs);

		String releaseCommand = SlimeballsSettings.RELEASE_COMMAND.string();
		releaseCommand = releaseCommand.replace("<Player>", player.getName());
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), releaseCommand);

	}
	

}
