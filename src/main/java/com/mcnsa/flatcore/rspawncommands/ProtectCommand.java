package com.mcnsa.flatcore.rspawncommands;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import com.mcnsa.flatcore.GriefPreventionHandler;
import com.mcnsa.flatcore.Setting;
import com.mcnsa.flatcore.Settings;
import com.mcnsa.flatcore.Util;

public class ProtectCommand extends BaseRSpawnCommand {	
	public static Map<String, Integer> protectedPlayers = new HashMap<String, Integer>();

	public ProtectCommand()
	{
		needPlayer = false;
		permission = "protect";
	}

	public Boolean run(final CommandSender sender, String[] args) {
		if (args.length < 1)
			return true;
		
		int startTime = Settings.getInt(Setting.SPAWN_PROTECTION_LENGTH);
		String playerName = args[0];
		protectedPlayers.put(playerName, startTime);
		
		Player player = Bukkit.getServer().getPlayerExact(playerName);
		if (player != null)
		{
			//Resetting player
			player.setHealth(20);
			player.setFoodLevel(20);
			player.setSaturation(20);
			player.setExp(0);
			player.setLevel(0);
			for (int i =0; i < player.getInventory().getSize() + 4; i++)
				player.getInventory().setItem(i, null);
			
			//Mob removal
			int removalRadiusSquared = Settings.getInt(Setting.MOB_REMOVAL_RADIUS_SQUARED);
			
			Collection<Monster> monsters = player.getWorld().getEntitiesByClass(Monster.class);
			Iterator<Monster> iterator = monsters.iterator();
			while (iterator.hasNext())
			{
				Monster monster = iterator.next();
				int distance = Util.flatDistance(player.getLocation(), monster.getLocation());
				if (distance < removalRadiusSquared)
				{
					monster.remove();
				}
			}
			
			//Announcing to player
			String message = Settings.getString(Setting.MESSAGE_SPAWN_PROTECTION_START);
			message = message.replace("<Time>", Integer.toString(startTime));
			Util.Message(message, player);
		}
		
		return true;
	}	
	
	public static void endProtection(Player player)
	{
		String message;
		if (GriefPreventionHandler.playerHasClaim(player.getName()))
			message = Settings.getString(Setting.MESSAGE_SPAWN_PROTECTION_END_CLAIMS);
		else
			message = Settings.getString(Setting.MESSAGE_SPAWN_PROTECTION_END_NO_CLAIMS);
	
		Util.Message(message, player);
	}
	
	public static class ProtectTimer implements Runnable
	{

		@Override
		public void run() {
			Iterator<Entry<String, Integer>> i = protectedPlayers.entrySet().iterator();
			while (i.hasNext())
			{
				Entry<String, Integer> e = i.next();
				
				Player player = Bukkit.getServer().getPlayerExact(e.getKey());
				if (player == null)
					continue;
				
				int timeLeft = e.getValue();
				timeLeft--;
				if (timeLeft <= 0)
				{
					i.remove();
					endProtection(player);
				}
				else
				{
					e.setValue(timeLeft);
					
					List<Integer> notifications = (List<Integer>) Settings.getList(Setting.SPAWN_PROTECTION_NOTIFICATIONS);
					for (Integer nTime : notifications)
					{
						if (nTime == timeLeft)
						{
							String message = Settings.getString(Setting.MESSAGE_SPAWN_PROTECTION_NOTIFICATION);
							message = message.replace("<Time>", Integer.toString(timeLeft));
							Util.Message(message, player);

							break;
						}
					}
				}
			}
			
		}
		
	}
}
