package us.corenetwork.mantle.treasurehunt;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Created by Ginaf on 2015-01-25.
 */
public class THuntPassManager {

    private final String AMOUNT = "amount.";

    public void addPass(OfflinePlayer offlinePlayer)
    {
        setAmount(offlinePlayer, getAmount(offlinePlayer) + 1);
    }

    public void addPass(OfflinePlayer offlinePlayer, int amount)
    {
        setAmount(offlinePlayer, getAmount(offlinePlayer) + amount);
    }

    public void removePass(OfflinePlayer offlinePlayer)
    {
        setAmount(offlinePlayer, getAmount(offlinePlayer) - 1);
    }

    private String getAmountPath(OfflinePlayer offlinePlayer)
    {
        return AMOUNT+offlinePlayer.getUniqueId().toString();
    }

    public int getAmount(OfflinePlayer offlinePlayer)
    {
        return THuntModule.instance.storageConfig.getInt(getAmountPath(offlinePlayer));
    }

    private void setAmount(OfflinePlayer offlinePlayer, int amount)
    {
        THuntModule.instance.storageConfig.set(getAmountPath(offlinePlayer), amount);
        THuntModule.instance.saveStorageYaml();
    }

    public void runPass(Player player)
    {
        THuntModule.manager.addToQueue(player.getName());
        THuntModule.manager.addPlayerToHunt(player);
    }
}
