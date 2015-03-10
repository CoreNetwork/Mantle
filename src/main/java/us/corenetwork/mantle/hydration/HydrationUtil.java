package us.corenetwork.mantle.hydration;

import java.util.Map.Entry;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import us.corenetwork.core.scoreboard.CoreScoreboardManager;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.hydration.CachedDrainConfig.WorldLayer;

public class HydrationUtil {
	public static void updateScoreboard(String player, PlayerData playerData)
	{
		if (playerData.hydrationLevel >= 100 && !playerData.recentlyDrained)
		{
			CoreScoreboardManager.setPlayerStat(player, HydrationSettings.SCOREBOARD_LINE.string(), null);
            CoreScoreboardManager.setPlayerStat(player, ChatColor.COLOR_CHAR + "7Sat.", null);
        }
		else
		{
			CoreScoreboardManager.setPlayerStat(player, HydrationSettings.SCOREBOARD_LINE.string(), (int) playerData.hydrationLevel);
            CoreScoreboardManager.setPlayerStat(player, ChatColor.COLOR_CHAR + "7Sat.", (int) playerData.saturationLevel);

        }

	}
	
	public static void updateNegativeEffects(Player player, PlayerData playerData, WorldLayer layer)
	{
		int neededHydrationToStop = (int) (playerData.fatigueLevel * 100 / 5);
		if (neededHydrationToStop == 0)
			neededHydrationToStop++;
		
		//Player is on low hydration
		if (playerData.hydrationLevel < neededHydrationToStop)
		{
			//1. Player's effect did not wore off yet
			long timePassed = System.currentTimeMillis() - playerData.fatigueEffectStart;
			if (timePassed < HydrationSettings.MINING_FATIGUE_DURATION_SECONDS.integer() * 1000)
			{
				if (player.hasPotionEffect(PotionEffectType.SLOW_DIGGING) && player.hasPotionEffect(PotionEffectType.SLOW) && player.hasPotionEffect(PotionEffectType.HUNGER))
					return;
				else
				{
					//Player lost negative effects somehow, lets reward him by giving it again
					int timeLeft = (int) (HydrationSettings.MINING_FATIGUE_DURATION_SECONDS.integer() - timePassed / 1000) + 1;
					
					PotionEffect effect = new PotionEffect(PotionEffectType.SLOW_DIGGING, timeLeft * 20, playerData.fatigueLevel - 1);
					player.addPotionEffect(effect, true);
					effect = new PotionEffect(PotionEffectType.SLOW, timeLeft * 20, playerData.fatigueLevel - 1);
					player.addPotionEffect(effect, true);
					effect = new PotionEffect(PotionEffectType.HUNGER, timeLeft * 20, 0);
					player.addPotionEffect(effect, true);
				}
			}
			// 2. Player's effect either wore off or he did not receive one yet, but he is at zero hydration
			// Let's give him some effects
			else if (playerData.hydrationLevel == 0)
			{
				if (layer == null)
					layer = CachedDrainConfig.getWoldLayer(player.getWorld().getName(), player.getLocation().getBlockY());
				
				playerData.fatigueLevel = Math.max(layer == null ? 0 : layer.startingMF, Math.min(playerData.fatigueLevel + 1, 5));
				
				int timeLeft = (int) (HydrationSettings.MINING_FATIGUE_DURATION_SECONDS.integer()) + 1;
				
				PotionEffect effect = new PotionEffect(PotionEffectType.SLOW_DIGGING, timeLeft * 20, playerData.fatigueLevel - 1);
	
				player.addPotionEffect(effect, true);
				effect = new PotionEffect(PotionEffectType.SLOW, timeLeft * 20, playerData.fatigueLevel - 1);
				player.addPotionEffect(effect, true);
				effect = new PotionEffect(PotionEffectType.HUNGER, timeLeft * 20, 0);
				player.addPotionEffect(effect, true);

				playerData.fatigueEffectStart = System.currentTimeMillis();
				playerData.save();

			}
			// 3. Player's effect has worn off, he does not have it anymore.
			else
			{
				playerData.fatigueEffectStart = 0;
				playerData.fatigueLevel = 0;
				playerData.save();

			}
		}
		//Player has OK hydration - remove it if needed
		else if (playerData.fatigueLevel > 0)
		{
			player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
			player.removePotionEffect(PotionEffectType.SLOW);
			player.removePotionEffect(PotionEffectType.HUNGER);
			playerData.fatigueEffectStart = 0;
			playerData.fatigueLevel = 0;
			playerData.save();
		}
	}
	
	public static void notify(PlayerData data, Player player)
	{
		for (Entry<Integer, String> e : CachedNotificationsConfig.layerMessages.entrySet())
		{
			if (data.hydrationLevel <= e.getKey() && !data.deliveredMessages.contains(e.getKey()))
			{
				data.deliveredMessages.add(e.getKey());
				Util.Message(e.getValue(), player);
			}
		}
	}
}
