package com.mcnsa.flatcore.restockablechests.commands;

import java.util.HashMap;

import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.flatcore.IO;
import com.mcnsa.flatcore.Setting;
import com.mcnsa.flatcore.Settings;
import com.mcnsa.flatcore.Util;
import com.mcnsa.flatcore.flatcorecommands.BaseAdminCommand;
import com.mcnsa.flatcore.restockablechests.RestockableChest;

public class CreateChestCommand extends BaseAdminCommand {
	private static HashMap<Player, PlayerData> selectionPlayers = new HashMap<Player, PlayerData>();
	
	public CreateChestCommand()
	{
		desc = "Create restockable chest";
		needPlayer = true;
	}


	public Boolean run(CommandSender sender, String[] args) {
		
		if (args.length < 3 || !Util.isInteger(args[1]) || !Util.isInteger(args[2]))
		{
			Util.Message("/flatcore createchest [Looting Table] [Restock interval in hours] [Per-player chests - 1/0]", sender);
			return true;
		}
		
		if (IO.config.get("LootTables." + args[0]) == null)
		{
			Util.Message(Settings.getString(Setting.MESSAGE_LOOTING_TABLE_DOES_NOT_EXIST), sender);
			return true;
		}
		
		PlayerData playerData = new PlayerData();
		playerData.lootingTable = args[0];
		playerData.interval = Integer.parseInt(args[1]);
		playerData.perPlayer = Integer.parseInt(args[2]) != 0;
		
		selectionPlayers.put((Player) sender, playerData);
		
		Util.Message(Settings.getString(Setting.MESSAGE_RIGHT_CLICK_CHEST_WITH_ARM), sender);
		return true;

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
			Util.Message(Settings.getString(Setting.MESSAGE_CHEST_EXISTS), player);
			return true;
		}
		
		RestockableChest.createChest(chest, data.lootingTable,  data.interval, data.perPlayer);
		
		
		Util.Message(Settings.getString(Setting.MESSAGE_CHEST_CREATED), player);
		return true;
	}

	private static class PlayerData
	{
		public int interval;
		public String lootingTable;
		public boolean perPlayer;
	}
}
