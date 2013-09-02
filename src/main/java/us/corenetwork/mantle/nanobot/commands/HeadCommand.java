package us.corenetwork.mantle.nanobot.commands;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class HeadCommand extends NanobotBaseCommand {

	public HeadCommand() {
		needPlayer = true;
		adminCommand = true;
		desc = "Load and apply tag to item in hand";
		permission = "head";
	}

	@SuppressWarnings("unchecked")
	public void run(CommandSender sender, String[] args) {
		if (args.length < 1) {
			sender.sendMessage("Syntax: /nbt head [Skeleton/Wither/Zombie/Creper/Player name]");
			return;
		}

		Player player = (Player) sender;
		ItemStack stack = new ItemStack(Material.SKULL_ITEM, 1);
		
		String param = args[0];
		if (param.equalsIgnoreCase("Skeleton"))
			stack.setDurability((short) 0);
		else if (param.equalsIgnoreCase("Wither"))
			stack.setDurability((short) 1);
		else if (param.equalsIgnoreCase("Zombie"))
			stack.setDurability((short) 2);
		else if (param.equalsIgnoreCase("Creeper"))
			stack.setDurability((short) 4);
		else
		{
			stack.setDurability((short) 3);
			SkullMeta meta = (SkullMeta) stack.getItemMeta();
			meta.setOwner(param);
			stack.setItemMeta(meta);		
		}
			

		player.getInventory().addItem(stack);
		
		if (args.length < 2 || !args[1].equals("silent"))
		sender.sendMessage("Tag was loaded sucessfully!");
	}
				

}
