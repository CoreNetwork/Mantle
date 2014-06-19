package us.corenetwork.mantle.spellbooks.books;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.spellbooks.Spellbook;
import us.corenetwork.mantle.spellbooks.SpellbookItem;
import us.corenetwork.mantle.spellbooks.SpellbookUtil;


public class WindBook extends Spellbook implements Listener {
	private static int EFFECT_DURATION = 20 * 20;
	//private static int HUNGER_DURATION = 20 * 5;

	private HashSet<UUID> sprinting = new HashSet<UUID>(); // List of players under sprinting effect

	public WindBook() {
		super("Wind");
		
		settings.setDefault(SETTING_TEMPLATE, "spell-wind");
	}
		
	@Override
	public BookFinishAction onActivate(SpellbookItem item, PlayerInteractEvent event) {
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		
		sprinting.add(uuid);
		
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, EFFECT_DURATION, 49));

		Bukkit.getScheduler().runTaskLater(MantlePlugin.instance, new SprintingTimer(uuid), EFFECT_DURATION);
		
		FireworkEffect effect = FireworkEffect.builder().withColor(Color.WHITE).withFade(Color.WHITE).build();
		Location effectLoc = SpellbookUtil.getPointInFrontOfPlayer(player.getEyeLocation(), 2);
		Util.showFirework(effectLoc, effect);

		return BookFinishAction.BROADCAST_AND_CONSUME;
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerItemConsumed(PlayerItemConsumeEvent event)
	{
		
		if (event.getItem().getType() == Material.MILK_BUCKET)
		{
			if (!sprinting.contains(event.getPlayer().getUniqueId()))
			{
				return;
			}

			finishSprint(event.getPlayer());
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerJoined(PlayerJoinEvent event)
	{
		finishSprint(event.getPlayer());
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onHungerChange(FoodLevelChangeEvent event)
	{
		if (sprinting.contains(event.getEntity().getUniqueId()))
		{
			event.setCancelled(true); //Don't drain hunger when sprinting
		}
	}
		
	
	private void finishSprint(Player player)
	{		
		if(sprinting.contains(player.getUniqueId()))
		{
			player.setFoodLevel(2);
			player.setSaturation(0);
			sprinting.remove(player.getUniqueId());
		}
	}
				
	private class SprintingTimer implements Runnable
	{
		private UUID uuid;
		
		public SprintingTimer(UUID uuid)
		{
			this.uuid = uuid;
		}

		@Override
		public void run() {
			if (sprinting.contains(uuid))
			{
				Player player = Bukkit.getPlayer(uuid);
				if (player != null)
				{
					finishSprint(player);
				}

			}
		}
	}

	@Override
	protected BookFinishAction onActivateEntity(SpellbookItem item, PlayerInteractEntityEvent event) {
		return BookFinishAction.NOTHING;
	}

}
