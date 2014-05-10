package us.corenetwork.mantle.hydration.commands;

import java.util.HashMap;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.hydration.HydrationUtil;
import us.corenetwork.mantle.hydration.PlayerData;


public class SaveCommand extends BaseHydrationCommand {	
	public static HashMap<String, HydrationState> savedHydration = new HashMap<String, HydrationState>();
	
	public SaveCommand()
	{
		permission = "save";
		desc = "Save current hydration state";
		needPlayer = true;
	}


	public void run(final CommandSender sender, String[] args) {
		
		
		Player player = (Player) sender;
		PlayerData playerData = PlayerData.getPlayer(player.getName());

		HydrationState state = new HydrationState();
		state.hydration = playerData.hydrationLevel;
		state.saturation = playerData.saturationLevel;
				
		savedHydration.put(player.getName(), state);
	}	
	
	public static class HydrationState
	{
		double hydration;
		double saturation;
	}
}
