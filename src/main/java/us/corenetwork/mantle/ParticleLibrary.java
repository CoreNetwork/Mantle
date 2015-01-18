package us.corenetwork.mantle;

import net.minecraft.server.v1_8_R1.EnumParticle;
import net.minecraft.server.v1_8_R1.PacketPlayOutWorldParticles;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import us.corenetwork.mantle.util.ReflectionUtils;

public class ParticleLibrary {
    public static void broadcastParticle(EnumParticle particle, Location location, float offsetX, float offsetY, float offsetZ, float data, int count, int[] dataArray)
    {
        for (Player player : Bukkit.getOnlinePlayers())
        {
            if (player.getWorld().equals(location.getWorld()) && Util.flatDistanceSquared(player.getLocation(), location) < 100)
            {
                sendToPlayer(particle, player, location, offsetX, offsetY, offsetZ, data, count, dataArray);
            }
        }
    }

    public static void sendToPlayer(EnumParticle particle, Player player, Location location, float offsetX, float offsetY, float offsetZ, float data, int count, int[] dataArray) {
        try
        {
            PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles();
            ReflectionUtils.set(packet, "a", particle);
            ReflectionUtils.set(packet, "b", (float) location.getX());
            ReflectionUtils.set(packet, "c", (float) location.getY());
            ReflectionUtils.set(packet, "d", (float) location.getZ());
            ReflectionUtils.set(packet, "e", offsetX);
            ReflectionUtils.set(packet, "f", offsetY);
            ReflectionUtils.set(packet, "g", offsetZ);
            ReflectionUtils.set(packet, "h", data);
            ReflectionUtils.set(packet, "i", count);

            if (dataArray != null)
            {
                ReflectionUtils.set(packet, "k", dataArray);
            }

            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
