package us.corenetwork.mantle.spellbooks.books;

import java.util.HashSet;

import net.minecraft.server.v1_7_R3.AttributeInstance;
import net.minecraft.server.v1_7_R3.EntityPlayer;
import net.minecraft.server.v1_7_R3.GenericAttributes;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftLivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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


public class DeadweightBook extends Spellbook implements Listener {
	private static int EFFECT_DURATION = 20 * 45;
	
	private HashSet<String> hardenedPlayers = new HashSet<String>();
	
	public DeadweightBook() {
		super("Deadweight");
		
		settings.setDefault(SETTING_TEMPLATE, "spell-deadweight");
	}
		
	@Override
	public BookFinishAction onActivate(SpellbookItem item, PlayerInteractEvent event) {
		Player player = event.getPlayer();
		String name = player.getName();
		
		hardenedPlayers.add(name);
		setKnockbackResistance(player, 1);
		
		player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, EFFECT_DURATION, 0));
		player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, EFFECT_DURATION, 0));
		player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, EFFECT_DURATION, 1));

		Bukkit.getScheduler().runTaskLater(MantlePlugin.instance, new DeadweightTimer(name), EFFECT_DURATION);
		
		FireworkEffect effect = FireworkEffect.builder().withColor(Color.PURPLE).withFade(Color.PURPLE).build();
		Location effectLoc = SpellbookUtil.getPointInFrontOfPlayer(player.getEyeLocation(), 2);
		Util.showFirework(effectLoc, effect);
		
		effectLoc.getWorld().playSound(effectLoc, Sound.ANVIL_LAND, 1f, 1f);
		
		return BookFinishAction.BROADCAST_AND_CONSUME;
	}
	
	@EventHandler
	public void onPlayerItemConsumed(PlayerItemConsumeEvent event)
	{
		if (event.getItem().getType() == Material.MILK_BUCKET)
		{
			unhardenPlayer(event.getPlayer());
		}
	}

	@EventHandler
	public void onPlayerJoined(PlayerJoinEvent event)
	{
		unhardenPlayer(event.getPlayer());
	}
		
	private void unhardenPlayer(Player player)
	{
		String name = player.getName();
		
		if (!hardenedPlayers.contains(name))
			return;
		
		hardenedPlayers.remove(name);
		setKnockbackResistance(player, 0);

	}
	
	private void unhardenPlayer(String name)
	{
		if (!hardenedPlayers.contains(name))
			return;
		
		hardenedPlayers.remove(name);
		Player player = Bukkit.getPlayer(name);
		
		if (player != null)
			setKnockbackResistance(player, 0);

	}
	
	private static void setKnockbackResistance(Player player, double resistance)
	{
		AttributeInstance attributes = ((EntityPlayer)((CraftLivingEntity) player).getHandle()).getAttributeInstance(GenericAttributes.c);
		attributes.setValue(resistance);
	}
	
	private class DeadweightTimer implements Runnable
	{
		private String name;
		
		public DeadweightTimer(String name)
		{
			this.name = name;
		}

		@Override
		public void run() {
			unhardenPlayer(name);
		}
	}

	@Override
	protected BookFinishAction onActivateEntity(SpellbookItem item, PlayerInteractEntityEvent event) {
		return BookFinishAction.NOTHING;
	}

}
