package us.corenetwork.mantle.hydration.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.corenetwork.mantle.hydration.HydrationUtil;
import us.corenetwork.mantle.hydration.PlayerData;


public class RestoreCommand extends BaseHydrationCommand {	
	public RestoreCommand()
	{
		permission = "restore";
		desc = "Reset hydration for specified player";
		needPlayer = true;
	}


	public void run(final CommandSender sender, String[] args) {
		
		Player player = (Player) sender;
		
		PlayerData playerData = PlayerData.getPlayer(player.getUniqueId());
		playerData.hydrationLevel = 100;
		playerData.saturationLevel = 50;
		playerData.fatigueEffectStart = 0;
		playerData.fatigueLevel = 0;
		playerData.recentlyDrained = false;
		playerData.save();
				
		HydrationUtil.updateNegativeEffects(player, playerData, null);
		HydrationUtil.updateScoreboard(player.getName(), playerData);
	}	
}
