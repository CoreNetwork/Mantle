package us.corenetwork.mantle.restockablechests.commands;

import java.util.HashMap;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.core_network.cornel.common.Messages;
import us.core_network.cornel.java.NumberUtil;
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
		
		if (args.length < 3 || !NumberUtil.isInteger(args[1]) || !NumberUtil.isInteger(args[2]))
		{
            Messages.send("/flatcore createchest [Looting Table] [Restock interval in hours] [Per-player chests - 1/0]", sender);
			return;
		}
		
		if (RChestsModule.instance.config.get("LootTables." + args[0]) == null)
		{
            Messages.send(RChestSettings.MESSAGE_LOOTING_TABLE_DOES_NOT_EXIST.string(), sender);
			return;
		}
		
		PlayerData playerData = new PlayerData();
		playerData.lootingTable = args[0];
		playerData.interval = Integer.parseInt(args[1]);
		playerData.perPlayer = Integer.parseInt(args[2]) != 0;
		
		selectionPlayers.put((Player) sender, playerData);

        Messages.send(RChestSettings.MESSAGE_RIGHT_CLICK_CHEST_WITH_ARM.string(), sender);
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
            Messages.send(RChestSettings.MESSAGE_CHEST_EXISTS.string(), player);
			return true;
		}
		
		RestockableChest.createChest(chest, data.lootingTable,  data.interval, data.perPlayer, null);


        Messages.send(RChestSettings.MESSAGE_CHEST_CREATED.string(), player);
		return true;
	}

	private static class PlayerData
	{
		public int interval;
		public String lootingTable;
		public boolean perPlayer;
	}
}
