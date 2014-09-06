package us.corenetwork.mantle.armorhologram;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.server.v1_7_R4.Entity;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import us.corenetwork.mantle.nanobot.NanobotUtil;

/**
 * Created by Matej on 5.9.2014.
 */
public class Hologram
{
    private String name;
    private int id;

    private World world;

    private double x;
    private double y;
    private double z;
    private List<String> text = new ArrayList<>();

    private int chunkX;
    private int chunkZ;

    private boolean hidden;
    private boolean hiddenToSave;

    public Hologram(Map<String, ?> map)
    {
        id = getNextEntityId();

        x = ((Number) map.get("x")).doubleValue();
        y = ((Number) map.get("y")).doubleValue();
        z = ((Number) map.get("z")).doubleValue();

        Object text = map.get("text");
        if (text instanceof String)
        {
            parseSingleLine((String) text);
        } else
        {
            this.text.addAll((List) text);
        }

        Boolean hidden = (Boolean) map.get("hidden");
        this.hidden = hidden != null && hidden;
        this.hiddenToSave = this.hidden;

        world = Bukkit.getWorld((String) map.get("world"));
        name = (String) map.get("name");

        chunkX = (int) x >> 4;
        chunkZ = (int) z >> 4;
    }

    public Hologram(String name, World world, double x, double y, double z, String text)
    {
        id = getNextEntityId();

        Bukkit.broadcastMessage(Integer.toString(id));

        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        parseSingleLine(text);
        this.world = world;
        this.hidden = false;
        this.hiddenToSave = false;

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
        map.put("hidden", hiddenToSave);
        if (name != null)
            map.put("name", name);

        return map;
    }

    private void parseSingleLine(String text)
    {
        this.text.addAll(Arrays.asList(text.split("<N>")));
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

    public List<String> getText()
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

    public String getName()
    {
        return name;
    }

    public boolean isHidden()
    {
        return hidden;
    }

    public void setHidden(boolean hidden, boolean persistent)
    {
        this.hidden = hidden;
        if (persistent)
        {
            this.hiddenToSave = hidden;
            HologramStorage.save();
        }

        if (hidden)
        {
            removeForAll();
        }
        else
        {
            displayForAll();
        }
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

    public Integer[] display(Player player)
    {
        Integer[] personalizedIds = new Integer[text.size()];
        double y = this.y;

        for (int i = 0; i < text.size(); i++)
        {
            if (text.get(i).isEmpty())
                continue;
            
            ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
            PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING);

            personalizedIds[i] = getNextEntityId();

            packet.getIntegers().write(0, personalizedIds[i]); //Entity ID, different every time - Client seems to have trouble respawning entities with same ID. My guess it is because they are not within chunk data?
            packet.getIntegers().write(1, 30); //Armor stand ID

            packet.getIntegers().write(2, (int) Math.floor(x * 32.0D)); //X
            packet.getIntegers().write(3, (int) Math.floor(y * 32.0D)); //Y
            packet.getIntegers().write(4, (int) Math.floor(z * 32.0D)); //Z

            packet.getBytes().write(0, (byte) (0 * 256.0F / 360.0F)); //Yaw - does not matter, can be 0
            packet.getBytes().write(1, (byte) (0 * 256.0F / 360.0F)); //Pitch - does not matter, can be 0

            WrappedDataWatcher watcher = new WrappedDataWatcher();
            watcher.setObject(2, NanobotUtil.fixFormatting(text.get(i))); //CustomName
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

            y -= 0.25;
        }


        return personalizedIds;
    }

    public void displayForAll()
    {
        if (hidden)
            return;

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

    public void remove(Player player, Integer[] ids)
    {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);

        int[] idsUnboxed = new int[ids.length];
        for (int i = 0; i < ids.length; i++)
            idsUnboxed[i] = ids[i];

        packet.getIntegerArrays().write(0, idsUnboxed); //Entity ID

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

    public void update(String newText)
    {
        removeForAll();
        text.clear();
        parseSingleLine(newText);
        displayForAll();
    }

    public void updateLine(int line, String newText)
    {
        if (line >= text.size() || line < 0)
            return;

        removeForAll();
        text.set(line, newText);
        displayForAll();
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
