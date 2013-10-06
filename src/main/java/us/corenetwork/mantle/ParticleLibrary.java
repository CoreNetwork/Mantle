package us.corenetwork.mantle;

import java.lang.reflect.Field;

import net.minecraft.server.v1_6_R4.Packet63WorldParticles;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_6_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

public enum ParticleLibrary {
    HUGE_EXPLOSION("hugeexplosion"),
    LARGE_EXPLODE("largeexplode"),
    FIREWORKS_SPARK("fireworksSpark"),
    BUBBLE("bubble"),
    SUSPEND("suspend"),
    DEPTH_SUSPEND("depthSuspend"),
    TOWN_AURA("townaura"),
    CRIT("crit"),
    MAGIC_CRIT("magicCrit"),
    MOB_SPELL("mobSpell"),
    MOB_SPELL_AMBIENT("mobSpellAmbient"),
    SPELL("spell"),
    INSTANT_SPELL("instantSpell"),
    WITCH_MAGIC("witchMagic"),
    NOTE("note"),
    PORTAL("portal"),
    ENCHANTMENT_TABLE("enchantmenttable"),
    EXPLODE("explode"),
    FLAME("flame"),
    LAVA("lava"),
    FOOTSTEP("footstep"),
    SPLASH("splash"),
    LARGE_SMOKE("largesmoke"),
    CLOUD("cloud"),
    RED_DUST("reddust"),
    SNOWBALL_POOF("snowballpoof"),
    DRIP_WATER("dripWater"),
    DRIP_LAVA("dripLava"),
    SNOW_SHOVEL("snowshovel"),
    SLIME("slime"),
    HEART("heart"),
    ANGRY_VILLAGER("angryVillager"),
    HAPPY_VILLAGER("happyVillager"),
    ICONCRACK("iconcrack_"),
    TILECRACK("tilecrack_");
   
    private String particleName;
   
    ParticleLibrary(String particleName) {
        this.particleName = particleName;
    }
 
    public void sendToPlayer(Player player, Location location, float offsetX, float offsetY, float offsetZ, float speed, int count) {
        try
        {
        	Packet63WorldParticles packet = new Packet63WorldParticles();
            ReflectionUtilities.setValue(packet, "a", particleName);
            ReflectionUtilities.setValue(packet, "b", (float) location.getX());
            ReflectionUtilities.setValue(packet, "c", (float) location.getY());
            ReflectionUtilities.setValue(packet, "d", (float) location.getZ());
            ReflectionUtilities.setValue(packet, "e", offsetX);
            ReflectionUtilities.setValue(packet, "f", offsetY);
            ReflectionUtilities.setValue(packet, "g", offsetZ);
            ReflectionUtilities.setValue(packet, "h", speed);
            ReflectionUtilities.setValue(packet, "i", count);
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
