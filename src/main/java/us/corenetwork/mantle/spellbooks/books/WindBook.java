package us.corenetwork.mantle.spellbooks.books;

import java.util.HashSet;
import java.util.UUID;
import net.minecraft.server.v1_8_R2.EnumParticle;
import net.minecraft.server.v1_8_R2.GenericAttributes;
import net.minecraft.server.v1_8_R2.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R2.CraftServer;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
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
import us.corenetwork.mantle.ParticleLibrary;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.spellbooks.Spellbook;
import us.corenetwork.mantle.spellbooks.SpellbookItem;
import us.corenetwork.mantle.spellbooks.SpellbookUtil;


public class WindBook extends Spellbook implements Listener {
	private static final int EFFECT_DURATION = 20 * 20;
    private static final int SLOWNESS_DURATION = 10 * 20;

    private static final double TARGET_SPEED = 1.404; //Magic number calcualated from (base player speed * (1 + 0.2 * 49))

	private HashSet<UUID> sprinting = new HashSet<UUID>(); // List of entities under sprinting effect

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

        LivingEntity mount = (LivingEntity) player.getVehicle();
        if (mount != null && mount instanceof Horse)
        {
            double baseMountSpeed = ((CraftLivingEntity) mount).getHandle().getAttributeInstance(GenericAttributes.d).getValue();
            int targetPotionLevel = (int) (((TARGET_SPEED / baseMountSpeed) - 1) / 0.2);

            sprinting.add(mount.getUniqueId());
            Bukkit.getScheduler().runTaskLater(MantlePlugin.instance, new SprintingTimer(mount.getUniqueId()), EFFECT_DURATION);

            mount.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, EFFECT_DURATION, targetPotionLevel));
        }

		Bukkit.getScheduler().runTaskLater(MantlePlugin.instance, new SprintingTimer(uuid), EFFECT_DURATION);

        ParticleLibrary.broadcastParticleRing(EnumParticle.EXPLOSION_NORMAL, player.getEyeLocation(), 2, Math.PI / 12, 5);

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

	private void finishSprint(LivingEntity entity)
	{		
		if(sprinting.contains(entity.getUniqueId()))
		{
            if (entity instanceof Player)
            {
                Player player = (Player) entity;
                player.setFoodLevel(2);
                player.setSaturation(0);
            }
            else
            {
                entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, SLOWNESS_DURATION, 4));
            }

			sprinting.remove(entity.getUniqueId());
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
				LivingEntity entity = (LivingEntity) ((CraftServer) Bukkit.getServer()).getServer().a(uuid).getBukkitEntity();
				if (entity != null)
				{
					finishSprint(entity);
				}

			}
		}
	}

	@Override
	protected BookFinishAction onActivateEntity(SpellbookItem item, PlayerInteractEntityEvent event) {
		return BookFinishAction.NOTHING;
	}

}
