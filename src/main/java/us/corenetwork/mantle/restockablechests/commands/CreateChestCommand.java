package us.corenetwork.mantle.restockablechests.commands;

import java.util.HashMap;

import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.corenetwork.mantle.IO;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.mantlecommands.BaseMantleCommand;
import us.corenetwork.mantle.restockablechests.RChestSettings;
import us.corenetwork.mantle.restockablechests.RChestsModule;
import us.corenetwork.mantle.restockablechests.RestockableChest;


public class CreateChestCommand extends BaseMantleCommand {
	private static HashMap<Player, PlayerData> selectionPlayers = new HashMap<Player, PlayerData>();
	
	public CreateChestCommand()
	{
		permission = "createchest";
		desc = "Create restockable chest";
		needPlayer = true;
	}


	public void run(CommandSender sender, String[] args) {
		
		if (args.length < 3 || !Util.isInteger(args[1]) || !Util.isInteger(args[2]))
		{
			Util.Message("/flatcore createchest [Looting Table] [Restock interval in hours] [Per-player chests - 1/0]", sender);
			return;
		}
		
		if (RChestsModule.instance.config.get("LootTables." + args[0]) == null)
		{
			Util.Message(RChestSettings.MESSAGE_LOOTING_TABLE_DOES_NOT_EXIST.string(), sender);
			return;
		}
		
		PlayerData playerData = new PlayerData();
		playerData.lootingTable = args[0];
		playerData.interval = Integer.parseInt(args[1]);
		playerData.perPlayer = Integer.parseInt(args[2]) != 0;
		
		selectionPlayers.put((Player) sender, playerData);
		
		Util.Message(RChestSettings.MESSAGE_RIGHT_CLICK_CHEST_WITH_ARM.string(), sender);

	}
	
	public static boolean playerHitChestArm(Player player, Block chest)
	{
		PlayerData data = selectionPlayers.get(player);
		if (data == null)
			return false;
		
		selectionPlayers.remove(player);

		RestockableChest rChest = RestockableChest.getChest(chest);
		if (rChest != null)
		{
			Util.Message(RChestSettings.MESSAGE_CHEST_EXISTS.string(), player);
			return true;
		}
		
		RestockableChest.createChest(chest, data.lootingTable,  data.interval, data.perPlayer);
		
		
		Util.Message(RChestSettings.MESSAGE_CHEST_CREATED.string(), player);
		return true;
	}

	private static class PlayerData
	{
		public int interval;
		public String lootingTable;
		public boolean perPlayer;
	}
}
