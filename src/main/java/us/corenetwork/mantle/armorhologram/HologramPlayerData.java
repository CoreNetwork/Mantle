package us.corenetwork.mantle.armorhologram;

import java.util.HashMap;
import java.util.UUID;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * Created by Matej on 5.9.2014.
 */
public class HologramPlayerData
{
    public static HashMap<UUID, HologramPlayerData> playerMap = new HashMap<UUID, HologramPlayerData>();
    public static HologramPlayerData get(UUID uuid)
    {
        HologramPlayerData player = playerMap.get(uuid);
        if (player == null)
        {
            player = new HologramPlayerData();
            playerMap.put(uuid, player);
        }

        return player;
    }

    private HashMap<Integer, Integer[]> hologramIds = new HashMap<Integer, Integer[]>();

    public boolean isHologramDisplayed(int id)
    {
        return hologramIds.containsKey(id);
    }

    public void addHologram(int globalId, Integer[] perPlayerId)
    {
        hologramIds.put(globalId, perPlayerId);
    }

    public void setHologramAsNotDisplayed(int id)
    {
        hologramIds.remove(id);
    }

    public void clearDisplayedHolograms()
    {
        hologramIds.clear();
    }

    public Integer[] getPersonalizedHologramId(int globalId)
    {
        return hologramIds.get(globalId);
    }

    public HologramPlayerData()
    {

    }


    public static boolean isPlayer18(Player player)
    {
        EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        return nmsPlayer.playerConnection.networkManager.getVersion() >= 36;
    }
}
