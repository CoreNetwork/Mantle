package us.corenetwork.mantle;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_6_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftFirework;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import us.corenetwork.core.respawn.ProtectTimer;

public class Util {

	public static void placeSign(Block block, String message)
	{
		Block belowBlock = block.getRelative(BlockFace.DOWN);
		if (belowBlock != null && belowBlock.getType().isSolid())
		{
			block.setType(Material.SIGN_POST);
		}
		else
		{
			block.setType(Material.WALL_SIGN);

		}

		Sign sign = (Sign) block.getState();
		if (block.getType() == Material.WALL_SIGN)
		{
			//Rotate sign so it will be on wall
			org.bukkit.material.Sign data = (org.bukkit.material.Sign) sign.getData();
			for (BlockFace face : new BlockFace[] {BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH})
			{
				Block nextBlock = block.getRelative(face);
				if (nextBlock != null && nextBlock.getType().isSolid())
				{
					data.setFacingDirection(face.getOppositeFace());
					sign.setData(data);

					break;
				}
			}
		}

		populateSign(message, sign);


		sign.update();
	}

	public static void populateSign(String message, Sign sign)
	{
		message = message.replaceAll("\\&([0-9abcdef])", ChatColor.COLOR_CHAR + "$1");
		String[] lines = message.split("\\[NEWLINE\\]");

		int max = Math.min(4, lines.length);
		for (int i = 0; i < max; i++)
		{
			sign.setLine(i, lines[i]);
		}
	}

	public static Block findBestSignLocation(ArrayList<Block> blocks)
	{
		for (Block b : blocks)
		{
			if (!b.isEmpty())
				continue;

			Block upperBlock = b.getRelative(BlockFace.UP);

			if (upperBlock != null && upperBlock.isEmpty())
				return b;
		}

		for (Block b : blocks)
		{
			if (!b.isEmpty())
				continue;

			Block upperBlock = b.getRelative(BlockFace.UP);

			if (upperBlock != null && upperBlock.isEmpty())
				return b;
		}

		for (Block b : blocks)
		{
			if (!b.isEmpty())
				continue;

			Block lowerBlock = b.getRelative(BlockFace.DOWN);

			if (lowerBlock != null && lowerBlock.getType().isSolid())
				return b;
		}

		for (Block b : blocks)
		{
			if (b.isEmpty())
				return b;			
		}

		return blocks.get(0);
	}

	public static void Message(String message, CommandSender sender)
	{
		message = message.replaceAll("\\&([0-9abcdefklmnor])", ChatColor.COLOR_CHAR + "$1");

		final String newLine = "\\[NEWLINE\\]";
		String[] lines = message.split(newLine);

		for (int i = 0; i < lines.length; i++) {
			lines[i] = lines[i].trim();

			if (i == 0)
				continue;

			int lastColorChar = lines[i - 1].lastIndexOf(ChatColor.COLOR_CHAR);
			if (lastColorChar == -1 || lastColorChar >= lines[i - 1].length() - 1)
				continue;

			char lastColor = lines[i - 1].charAt(lastColorChar + 1);
			lines[i] = Character.toString(ChatColor.COLOR_CHAR).concat(Character.toString(lastColor)).concat(lines[i]);	
		}		

		for (int i = 0; i < lines.length; i++)
			sender.sendMessage(lines[i]);


	}
	
	public static void Broadcast(String message)
	{
		for (Player p : Bukkit.getOnlinePlayers())
		{
				Util.Message(message, p);
		}

	}

	public static void Broadcast(String message, String exclusion)
	{
		for (Player p : Bukkit.getOnlinePlayers())
		{
			if (!p.getName().equals(exclusion))
				Util.Message(message, p);
		}

	}

	public static void MessagePermissions(String message, String permission)
	{
		for (Player p : Bukkit.getOnlinePlayers())
		{
			if (Util.hasPermission(p,permission))
				Util.Message(message, p);
		}
	}

	public static String printTimeHours(int minutes)
	{
		if (minutes > 60)
		{
			return Math.round(minutes / 60.0) + " hours";
		}
		else if (minutes > 1)
		{
			return minutes + " minutes";
		}
		else
		{
			return "several seconds";
		}
	}
	
	public static void showFirework(Location location, FireworkEffect effect)
	{
		Firework firework = location.getWorld().spawn(location, Firework.class);

		FireworkMeta meta = firework.getFireworkMeta();
		meta.clearEffects();
		meta.addEffect(effect);
		meta.setPower(0);
		firework.setFireworkMeta(meta);

		net.minecraft.server.v1_6_R3.EntityFireworks nmsFirework = ((CraftFirework) firework).getHandle();
		net.minecraft.server.v1_6_R3.World world = ((CraftWorld) location.getWorld()).getHandle();

		world.broadcastEntityEffect(nmsFirework, (byte) 17);

		firework.remove();
	}


	public static Boolean isInteger(String text) {
		try {
			Integer.parseInt(text);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static Boolean isDouble(String text) {
		try {
			Double.parseDouble(text);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static void safeTeleport(final Entity entity, final Location location)
	{
		Chunk c = location.getChunk();
		if (!c.isLoaded())
			location.getChunk().load();

		MantleListener.godEntities.add(entity.getEntityId());
		entity.teleport(location);
			
		Bukkit.getServer().getScheduler().runTask(MantlePlugin.instance, new Runnable() {
			@Override
			public void run() {
				entity.teleport(location);
				MantleListener.godEntities.remove(entity.getEntityId());

			}
		});
	}

	public static int flatDistanceSquared(Location a, Location b)
	{
		return ((a.getBlockX() - b.getBlockX()) * (a.getBlockX() - b.getBlockX())) + ((a.getBlockZ() - b.getBlockZ()) * (a.getBlockZ() - b.getBlockZ()));
	}

	public static Location unserializeLocation(String text)
	{
		String[] split = text.split(";");

		World world = Bukkit.getWorld(split[0]);
		double x = Double.parseDouble(split[1]);
		double y = Double.parseDouble(split[2]);
		double z = Double.parseDouble(split[3]);
		float pitch = Float.parseFloat(split[4]);
		float yaw = Float.parseFloat(split[5]);

		return new Location(world, x, y, z, yaw, pitch);
	}

	public static String serializeLocation(Location location)
	{
		String locString = location.getWorld().getName().concat(";");
		locString = locString.concat(Double.toString(location.getX())).concat(";").concat(Double.toString(location.getY())).concat(";").concat(Double.toString(location.getZ())).concat(";");
		locString = locString.concat(Float.toString(location.getPitch())).concat(";").concat(Float.toString(location.getYaw()));

		return locString;
	}

	public static boolean hasPermission(CommandSender player, String permission)
	{
		while (true)
		{
			if (player.hasPermission(permission))
				return true;

			if (permission.length() < 2)
				return false;

			if (permission.endsWith("*"))
				permission = permission.substring(0, permission.length() - 2);

			int lastIndex = permission.lastIndexOf(".");
			if (lastIndex < 0)
				return false;

			permission = permission.substring(0, lastIndex).concat(".*");  
		}
	}

	public static boolean isInventoryContainer(int id)
	{
		return id == Material.CHEST.getId() || id == Material.TRAPPED_CHEST.getId() || id == Material.DISPENSER.getId() || id == Material.FURNACE.getId() || id == Material.DROPPER.getId() || id == Material.BREWING_STAND.getId() || id == Material.HOPPER.getId();
	}

	// Material name snippet by TechGuard
	public static String getMaterialName(Material material) {
		String name = material.toString();
		name = name.replaceAll("_", " ");
		if (name.contains(" ")) {
			String[] split = name.split(" ");
			for (int i = 0; i < split.length; i++) {
				split[i] = split[i].substring(0, 1).toUpperCase() + split[i].substring(1).toLowerCase();
			}
			name = "";
			for (String s : split) {
				name += " " + s;
			}
			name = name.substring(1);
		} else {
			name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
		}
		return name;
	}	
}
