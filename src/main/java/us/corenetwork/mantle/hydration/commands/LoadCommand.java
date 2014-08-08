package us.corenetwork.mantle.hydration.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.corenetwork.mantle.hydration.HydrationUtil;
import us.corenetwork.mantle.hydration.PlayerData;
import us.corenetwork.mantle.hydration.commands.SaveCommand.HydrationState;


public class LoadCommand extends BaseHydrationCommand {	
	public LoadCommand()
	{
		permission = "load";
		desc = "Load previously saved hydration data";
		needPlayer = true;
	}


	public void run(final CommandSender sender, String[] args) {
				
		Player player = (Player) sender;
		HydrationState state = SaveCommand.savedHydration.get(player.getUniqueId());
		if (state == null)
			return;		
		
		PlayerData playerData = PlayerData.getPlayer(player.getUniqueId());
		playerData.hydrationLevel = state.hydration;
		playerData.saturationLevel = state.saturation;
		playerData.save();
				
		HydrationUtil.updateNegativeEffects(player, playerData, null);
		HydrationUtil.updateScoreboard(player.getName(), (int) Math.round(state.hydration));
	}	
}
