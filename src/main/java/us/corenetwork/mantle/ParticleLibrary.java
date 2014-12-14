package us.corenetwork.mantle;

import java.lang.reflect.Field;

import net.minecraft.server.v1_8_R1.EnumParticle;
import net.minecraft.server.v1_8_R1.PacketPlayOutWorldParticles;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

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
            ReflectionUtilities.setValue(packet, "a", particle);
            ReflectionUtilities.setValue(packet, "b", (float) location.getX());
            ReflectionUtilities.setValue(packet, "c", (float) location.getY());
            ReflectionUtilities.setValue(packet, "d", (float) location.getZ());
            ReflectionUtilities.setValue(packet, "e", offsetX);
            ReflectionUtilities.setValue(packet, "f", offsetY);
            ReflectionUtilities.setValue(packet, "g", offsetZ);
            ReflectionUtilities.setValue(packet, "h", data);
            ReflectionUtilities.setValue(packet, "i", count);

            if (dataArray != null)
            {
                ReflectionUtilities.setValue(packet, "k", dataArray);
            }

            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
        catch (Exception e)
        {
        	e.printStackTrace();
        }
    }
   
    private static class ReflectionUtilities {
    	 
        /**
        * sets a value of an {@link Object} via reflection
        *
        * @param instance instance the class to use
        * @param fieldName the name of the {@link Field} to modify
        * @param value the value to set
        * @throws Exception
        */
        public static void setValue(Object instance, String fieldName, Object value) throws Exception {
            Field field = instance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, value);
        }
     
        /**
        * get a value of an {@link Object}'s {@link Field}
        *
        * @param instance the target {@link Object}
        * @param fieldName name of the {@link Field}
        * @return the value of {@link Object} instance's {@link Field} with the
        *        name of fieldName
        * @throws Exception
        */
        public static Object getValue(Object instance, String fieldName) throws Exception {
            Field field = instance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(instance);
        }
     
    }
}
