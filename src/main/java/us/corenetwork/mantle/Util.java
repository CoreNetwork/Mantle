package us.corenetwork.mantle;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftFirework;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

public class Util
{
    public static String printTimeHours(int minutes)
    {
        if (minutes > 60)
        {
            return Math.round(minutes / 60.0) + " hours";
        } else if (minutes > 1)
        {
            return minutes + " minutes";
        } else
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

        net.minecraft.server.v1_8_R1.EntityFireworks nmsFirework = ((CraftFirework) firework).getHandle();
        net.minecraft.server.v1_8_R1.World world = ((CraftWorld) location.getWorld()).getHandle();

        world.broadcastEntityEffect(nmsFirework, (byte) 17);

        firework.remove();
    }

    public static void safeTeleport(final Entity entity, final Location location)
    {
        Chunk c = location.getChunk();
        if (!c.isLoaded())
            location.getChunk().load();
        entity.teleport(location);

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(MantlePlugin.instance, new Runnable()
        {
            @Override
            public void run()
            {
                entity.teleport(location);

            }
        }, 10);
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

    public static Object findEnum(Object[] enumList, String name)
    {
        for (Object enumEntry : enumList)
        {
            String entryName = ((Enum) enumEntry).name();
            entryName = entryName.replace("_", " ");
            if (entryName.equalsIgnoreCase(name))
                return enumEntry;
        }

        return null;
    }

    private static long debugTimer = System.currentTimeMillis();
    public static void debugTime(String message)
    {
        MLog.debug(message + " " + (System.currentTimeMillis() - debugTimer));
        debugTimer = System.currentTimeMillis();
    }
}
