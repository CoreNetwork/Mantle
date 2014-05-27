package us.corenetwork.mantle.spellbooks.books;

import java.util.HashSet;

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

	private HashSet<String> sprinting = new HashSet<String>(); // List of players under sprinting effect

	public WindBook() {
		super("Wind");
		
		settings.setDefault(SETTING_TEMPLATE, "spell-wind");
	}
		
	@Override
	public BookFinishAction onActivate(SpellbookItem item, PlayerInteractEvent event) {
		Player player = event.getPlayer();
		String name = player.getName();
		
		sprinting.add(name);
		
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, EFFECT_DURATION, 49));

		Bukkit.getScheduler().runTaskLater(MantlePlugin.instance, new SprintingTimer(name), EFFECT_DURATION);
		
		FireworkEffect effect = FireworkEffect.builder().withColor(Color.WHITE).withFade(Color.WHITE).build();
		Location effectLoc = SpellbookUtil.getPointInFrontOfPlayer(player.getEyeLocation(), 2);
		Util.showFirework(effectLoc, effect);
//		
//		effectLoc.getWorld().playSound(effectLoc, Sound.ANVIL_LAND, 1f, 1f);
		return BookFinishAction.BROADCAST_AND_CONSUME;
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerItemConsumed(PlayerItemConsumeEvent event)
	{
		
		if (event.getItem().getType() == Material.MILK_BUCKET)
		{
			String name = event.getPlayer().getName();
			if (!sprinting.contains(name))
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
		String name = event.getEntity().getName();
		if (sprinting.contains(name))
		{
			event.setCancelled(true); //Don't drain hunger when sprinting
		}
	}
		
	
	private void finishSprint(Player player)
	{
		String name = player.getName();
		
		sprinting.remove(name);
		
		player.setFoodLevel(2);
		player.setSaturation(0);
	}
				
	private class SprintingTimer implements Runnable
	{
		private String name;
		
		public SprintingTimer(String name)
		{
			this.name = name;
		}

		@Override
		public void run() {
			if (sprinting.contains(name))
			{
				Player player = Bukkit.getPlayer(name);
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
