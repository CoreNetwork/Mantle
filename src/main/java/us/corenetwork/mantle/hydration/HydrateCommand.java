package us.corenetwork.mantle.hydration;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.corenetwork.mantle.mantlecommands.BaseMantleCommand;


public class HydrateCommand extends BaseMantleCommand {	
	public HydrateCommand()
	{
		permission = "hydrate";
		desc = "Reset hydration for specified player";
		needPlayer = true;
	}


	public void run(final CommandSender sender, String[] args) {
		
		Player player = (Player) sender;
		
		PlayerData playerData = PlayerData.getPlayer(player.getName());
		playerData.hydrationLevel = 100;
		playerData.saturationLevel = 100;
		playerData.fatigueEffectStart = 0;
		playerData.fatigueLevel = 0;
		playerData.save();
				
		HydrationUtil.upateMineFatigue(player, playerData, null);
		HydrationUtil.updateScoreboard(player.getName(), 100);
	}	
}
