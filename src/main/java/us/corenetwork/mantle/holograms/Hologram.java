package us.corenetwork.mantle.holograms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import us.core_network.cornel.common.Messages;

/**
 * Created by Matej on 5.9.2014.
 */
public class Hologram
{
    private String name;

    private World world;

    private double x;
    private double y;
    private double z;
    private List<String> text = new ArrayList<String>();

    private List<UUID> linkedEntities = new ArrayList<UUID>();

    private int chunkX;
    private int chunkZ;

    private boolean hidden;

    private boolean needsUpdating;
    private boolean deleteOnNextUpdate;
    private boolean doNotSave;

    private HologramsChunk parentChunk;

    public Hologram(Map<String, ?> map)
    {
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

        if (map.containsKey("linkedEntities"))
            loadLinkedEntitiesStringList((List) map.get("linkedEntities"));

        Boolean hidden = (Boolean) map.get("hidden");
        this.hidden = hidden != null && hidden;

        world = Bukkit.getWorld((String) map.get("world"));
        name = (String) map.get("id");

        needsUpdating = true;
        if (map.containsKey("needsUpdating"))
            needsUpdating = (Boolean) map.get("needsUpdating");
        deleteOnNextUpdate = false;
        if (map.containsKey("deleteOnNextUpdate"))
            deleteOnNextUpdate = (Boolean) map.get("deleteOnNextUpdate");

        chunkX = (int) x >> 4;
        chunkZ = (int) z >> 4;
    }

    public Hologram(String name, World world, double x, double y, double z, String text)
    {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        parseSingleLine(text);
        this.world = world;
        this.hidden = false;

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
        map.put("hidden", hidden);
        map.put("needsUpdating", needsUpdating);
        map.put("deleteOnNextUpdate", deleteOnNextUpdate);
        map.put("linkedEntities", getLinkedEntitiesStringList());

        if (name != null)
            map.put("id", name);

        return map;
    }

    private List<String> getLinkedEntitiesStringList()
    {
        List<String> entityIDs = new ArrayList<String>(linkedEntities.size());
        for (UUID uuid : linkedEntities)
            entityIDs.add(uuid.toString());

        return entityIDs;
    }

    private void loadLinkedEntitiesStringList(List<String> list)
    {
        for (String entry : list)
            linkedEntities.add(UUID.fromString(entry));
    }

    private void parseSingleLine(String text)
    {
        this.text.addAll(Arrays.asList(text.split("<N>")));
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

    public boolean getShouldNotSave()
    {
        return doNotSave;
    }

    public boolean getNeedsUpdating()
    {
        return needsUpdating;
    }

    public void setParentChunk(HologramsChunk parentChunk)
    {
        this.parentChunk = parentChunk;

        if (needsUpdating)
            parentChunk.needsUpdating = true;
    }

    public void updateEntityText()
    {
        if (!isLoaded())
        {
            needsUpdating = true;
            parentChunk.needsUpdating = true;
            return;
        }

        Entity[] entitiesInChunk = getChunk().getEntities();
        for (Entity entity : entitiesInChunk)
        {
            if (entity.getType() != EntityType.ARMOR_STAND)
                continue;

            for (int i = 0; i < linkedEntities.size(); i++)
            {
                if (entity.getUniqueId().equals(linkedEntities.get(i)))
                    ((ArmorStand) entity).setCustomName(Messages.applyFormattingCodes(text.get(i)));
            }
        }

    }

    public void updateEntities()
    {
        if (!isLoaded())
        {
            needsUpdating = true;
            parentChunk.needsUpdating = true;
            return;
        }

        needsUpdating = false;

        //Remove existing holograms
        Entity[] entitiesInChunk = getChunk().getEntities();
        for (Entity entity : entitiesInChunk)
        {
            if (entity.getType() == EntityType.ARMOR_STAND && linkedEntities.contains(entity.getUniqueId()))
                entity.remove();
        }

        linkedEntities.clear();

        if (deleteOnNextUpdate)
            doNotSave = true;

        if (deleteOnNextUpdate || hidden)
            return;

        double y = this.y;

        for (String line : text)
        {
            net.minecraft.server.v1_8_R3.World nmsWorld = ((CraftWorld) world).getHandle();

            EntityArmorStand armorStand = new EntityArmorStand(nmsWorld);
            armorStand.setPosition(x, y, z);
            armorStand.setCustomNameVisible(true);
            armorStand.setCustomName(Messages.applyFormattingCodes(line));
            armorStand.setGravity(true); //This actually means setNoGravity... SPIGOT!
            armorStand.setInvisible(true);

            nmsWorld.addEntity(armorStand);

            linkedEntities.add(armorStand.getUniqueID());

            y -= 0.25;
        }

        HologramStorage.save();
    }

    public void setHidden(boolean hidden)
    {
        this.hidden = hidden;

        updateEntities();
    }

    public boolean isLoaded()
    {
        return world.isChunkLoaded(chunkX, chunkZ);
    }

    public Chunk getChunk()
    {
        return world.getChunkAt(chunkX, chunkZ);
    }

    public void update(String newText)
    {
        text.clear();
        parseSingleLine(newText);
    }

    public void updateLine(int line, String newText)
    {
        if (line >= text.size() || line < 0)
            return;

        text.set(line, newText);
    }

    public void delete()
    {
        deleteOnNextUpdate = true;
        updateEntities();
    }
}
