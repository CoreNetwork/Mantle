package com.mcnsa.flatcore.rspawncommands;

import java.util.Deque;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.flatcore.FCLog;
import com.mcnsa.flatcore.GriefPreventionHandler;
import com.mcnsa.flatcore.MCNSAFlatcore;
import com.mcnsa.flatcore.Setting;
import com.mcnsa.flatcore.Settings;
import com.mcnsa.flatcore.Util;
import com.mcnsa.flatcore.flatcorecommands.GenerateMoreVillagesCommand;

public class RSpawnCommand extends BaseRSpawnCommand {	
	public RSpawnCommand()
	{
		needPlayer = true;
		permission = "rspawn";
	}

	public Boolean run(final CommandSender sender, String[] args) {
		
		Player player = (Player) sender;
		
		int minX = Settings.getInt(Setting.GENERATION_MIN_X);
		int minZ = Settings.getInt(Setting.GENERATION_MIN_Z);
		int maxX = Settings.getInt(Setting.GENERATION_MAX_X);
		int maxZ = Settings.getInt(Setting.GENERATION_MAX_Z);

		Location biggestClaim = null;
		if (!ToggleCommand.ignoredPlayers.contains(player.getName()) && throwDice(player))
			biggestClaim = GriefPreventionHandler.findBiggestClaim(player.getName());
				
		ToggleCommand.ignoredPlayers.remove(player.getName());
		
		if (biggestClaim != null)
		{
			minX = Math.max(minX, biggestClaim.getBlockX() - 2500);
			maxX = Math.min(maxX, biggestClaim.getBlockX() + 2500);
			minZ = Math.max(minZ, biggestClaim.getBlockZ() - 2500);
			maxZ = Math.min(maxZ, biggestClaim.getBlockZ() + 2500);
		}
		
		teleport((Player) sender, minX, maxX, minZ, maxZ);		
		return true;
	}	
	
	public boolean throwDice(Player player)
	{
		if (Util.hasPermission(player,"mcnsaflatcore.donorsky"))
			return true;
		else if (Util.hasPermission(player,"mcnsaflatcore.donorgrass"))
			return MCNSAFlatcore.random.nextInt(100) < 66;
		else
			return MCNSAFlatcore.random.nextInt(100) < 33;
	}
	
	public void teleport(Player player, int minX, int maxX, int minZ, int maxZ)
	{
		World overworld = Bukkit.getWorlds().get(0);
		
		Deque<Location> claims = GriefPreventionHandler.getAllClaims();	
		GenerateMoreVillagesCommand.getAllVillages(claims);
		
		int counter = 0;
		int range = 40000;
		
		int xDiff = maxX - minX;
		int zDiff = maxZ - minZ;
		
		int y = Settings.getInt(Setting.TELEPORT_Y);
		
		while (true)
		{
			counter++;
			
			if (counter > 100)
			{
				counter = 0;
				range = Math.max(0, range - 3000);
				
				FCLog.warning("Could not found teleport location after 100 tries. Is map overcrowded?s Current range:" + range);

			}
						
			int x = MCNSAFlatcore.random.nextInt(xDiff) + minX;
			int z = MCNSAFlatcore.random.nextInt(zDiff) + minZ;
			
			Location location = new Location(overworld, x, y, z);
			Block block = location.getBlock();
			
			Block belowBlock = block.getRelative(BlockFace.DOWN);
			if (belowBlock == null || belowBlock.getType() != Material.GRASS)
				continue;
			
			if (!isEmpty(block))
				continue;
			
			int smallestDist = GenerateMoreVillagesCommand.getSmallestDistance(claims, location);			
			if (smallestDist < range)
				continue;
			
			Util.safeTeleport(player, location);
			break;
		}
	}
	
	public boolean isEmpty(Block block)
	{
		for (int i = 1; i < 13; i++)
		{
			Block aboveBlock = block.getRelative(BlockFace.UP, i);
			if (aboveBlock != null && !aboveBlock.isEmpty())
				return false;
		}
		return true;
	}
	
	public boolean isDangerNearby(Block block)
	{
		for (int x = -3; x < 3; x++)
		{
			for (int y = -3; y < 3; y++)
			{
				for (int z = -3; z < 3; z++)
				{
					Block check = block.getRelative(x, y, z);
					if (check != null && (check.getType() == Material.LAVA || check.getType() == Material.STATIONARY_LAVA || check.getType() == Material.FIRE))
						return true;
				}
			}
		}
		
		return false;
	}
	
	
	
	
}
