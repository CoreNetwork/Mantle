package us.corenetwork.mantle.armorhologram;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.server.v1_7_R4.Entity;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Created by Matej on 5.9.2014.
 */
public class Hologram
{
    private int id;

    private World world;

    private double x;
    private double y;
    private double z;
    private String text;

    private int chunkX;
    private int chunkZ;

    public Hologram(Map<String, ?> map)
    {
        id = getNextEntityId();

        x = ((Number) map.get("x")).doubleValue();
        y = ((Number) map.get("y")).doubleValue();
        z = ((Number) map.get("z")).doubleValue();
        text = (String) map.get("text");
        world = Bukkit.getWorld((String) map.get("world"));

        chunkX = (int) x >> 4;
        chunkZ = (int) z >> 4;
    }

    public Hologram(World world, double x, double y, double z, String text)
    {
        id = getNextEntityId();

        Bukkit.broadcastMessage(Integer.toString(id));

        this.x = x;
        this.y = y;
        this.z = z;
        this.text = text;
        this.world = world;

        chunkX = (int) x >> 4;
        chunkZ = (int) z >> 4;
    }
    public Map<String, Object> serialize()
    {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put("x", (Double) x);
        map.put("y", (Double) y);
        map.put("z", (Double) z);
        map.put("text", text);
        map.put("world", world.getName());

        return map;
    }

    public int getId()
    {
        return id;
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

    public double getZ()
    {
        return z;
    }

    public String getText()
    {
        return text;
    }

    public int getChunkX()
    {
        return chunkX;
    }

    public int getChunkZ()
    {
        return chunkZ;
    }

    public World getWorld()
    {
        return world;
    }

    public boolean isInViewDistance(Player player)
    {
        World world = player.getWorld();
        Chunk chunk = player.getLocation().getChunk();
        return world.equals(this.world) && Math.min(Math.abs(chunk.getX() - this.chunkX), Math.abs(chunk.getZ() - this.chunkZ)) <= Bukkit.getServer().getViewDistance();
    }

    public boolean isLoaded()
    {
        return world.isChunkLoaded(chunkX, chunkZ);
    }

    public int display(Player player)
    {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING);

        int personalizedId = getNextEntityId();

        packet.getIntegers().write(0, personalizedId); //Entity ID, different every time - Client seems to have trouble respawning entities with same ID. My guess it is because they are not within chunk data?
        packet.getIntegers().write(1, 30); //Armor stand ID

        packet.getIntegers().write(2, (int) Math.floor(x * 32.0D)); //X
        packet.getIntegers().write(3, (int) Math.floor(y * 32.0D)); //Y
        packet.getIntegers().write(4, (int) Math.floor(z * 32.0D)); //Z

        packet.getBytes().write(0, (byte) (0 * 256.0F / 360.0F)); //Yaw - does not matter, can be 0
        packet.getBytes().write(1, (byte) (0 * 256.0F / 360.0F)); //Pitch - does not matter, can be 0

        WrappedDataWatcher watcher = new WrappedDataWatcher();
        watcher.setObject(2, text); //CustomName
        watcher.setObject(3, Byte.valueOf((byte) 1)); //Custom name always visible
        watcher.setObject(0, Byte.valueOf((byte) (1 << 5))); //Invisible (flag?)
        watcher.setObject(10, Byte.valueOf((byte) 0x2)); //No gravity (flag?)

        packet.getDataWatcherModifier().write(0, watcher);

        try
        {
            protocolManager.sendServerPacket(player, packet);
        } catch (InvocationTargetException e)
        {
            e.printStackTrace();
        }

        return personalizedId;
    }

    public void displayForAll()
    {
        for (Player player : Bukkit.getOnlinePlayers())
        {
            if (!HologramPlayerData.isPlayer18(player))
                continue;

            HologramPlayerData playerData = HologramPlayerData.get(player.getUniqueId());

            if (!playerData.isHologramDisplayed(id) && isInViewDistance(player))
            {
                playerData.addHologram(id, display(player));
            }
        }
    }

    public void remove(Player player, int id)
    {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);

        int[] entityArray = new int[1];
        entityArray[0] = id;

        packet.getIntegerArrays().write(0, entityArray); //Entity ID

        try
        {
            protocolManager.sendServerPacket(player, packet);
        } catch (InvocationTargetException e)
        {
            e.printStackTrace();
        }
    }

    public void removeForAll()
    {
        for (Player player : Bukkit.getOnlinePlayers())
        {
            HologramPlayerData playerData = HologramPlayerData.get(player.getUniqueId());

            if (playerData.isHologramDisplayed(id))
            {
                remove(player, playerData.getPersonalizedHologramId(id));
                playerData.setHologramAsNotDisplayed(id);
            }
        }
    }

    private static int getNextEntityId()
    {
        try
        {
            Class cl = Entity.class;
            Field field = cl.getDeclaredField("entityCount");
            field.setAccessible(true);

            int nextId = (int) field.get(null);
            field.set(null, nextId + 1 );

            return nextId;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return Integer.MAX_VALUE;
        }
    }
}
