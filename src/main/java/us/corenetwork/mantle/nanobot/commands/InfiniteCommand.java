package us.corenetwork.mantle.nanobot.commands;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class InfiniteCommand extends NanobotBaseCommand {

	public InfiniteCommand() {
		needPlayer = true;
		adminCommand = true;
		desc = "Make stack in hand infinite";
		permission = "infinite";
	}

	@SuppressWarnings("unchecked")
	public void run(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		ItemStack stack = player.getItemInHand();
		if (stack != null && stack.getType() != Material.AIR)
		{
			stack.setAmount(-1);
			player.setItemInHand(stack);
			
			if (args.length < 1 || !args[0].equals("silent"))
				sender.sendMessage("Stack is now infinite!");

		}
		else
		{
			if (args.length < 1 || !args[0].equals("silent"))
				sender.sendMessage("Your hands are empty!");
		}		
	}
				

}
