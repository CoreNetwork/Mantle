package us.corenetwork.mantle.hydration.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.hydration.HydrationUtil;
import us.corenetwork.mantle.hydration.PlayerData;


public class SetCommand extends BaseHydrationCommand {	
	public SetCommand()
	{
		permission = "set";
		desc = "Set hydration to specified levels";
		needPlayer = true;
	}


	public void run(final CommandSender sender, String[] args) {
		
		if (args.length < 1 || !Util.isDouble(args[0]))
			return;
		
		Player player = (Player) sender;
		
		double hydration = Double.parseDouble(args[0]);
		
		double saturation = 50;
		if (args.length > 1 && Util.isDouble(args[1]))
			saturation = Double.parseDouble(args[1]);
		
		
		PlayerData playerData = PlayerData.getPlayer(player.getName());
		playerData.hydrationLevel = hydration;
		playerData.saturationLevel = saturation;
		playerData.save();
				
		HydrationUtil.upateMineFatigue(player, playerData, null);
		HydrationUtil.updateScoreboard(player.getName(), (int) Math.round(hydration));
	}	
}
