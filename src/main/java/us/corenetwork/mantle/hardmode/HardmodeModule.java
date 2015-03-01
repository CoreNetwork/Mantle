package us.corenetwork.mantle.hardmode;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import us.corenetwork.mantle.MantleModule;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.hardmode.animals.NearbyPlayerPathfinderGoalProxy;
import us.corenetwork.mantle.hardmode.wither.NMSWitherManager;


public class HardmodeModule extends MantleModule {
	public static HardmodeModule instance;

	public HardmodeModule() {
		super("Hard mode", null, "hardmode");
		
		instance = this;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		return false;
	}

	@Override
	protected boolean loadModule() {

		for (HardmodeSettings setting : HardmodeSettings.values())
		{
			if (config.get(setting.string) == null)
				config.set(setting.string, setting.def);
		}
		saveConfig();
				
		Bukkit.getPluginManager().registerEvents(new HardmodeListener(), MantlePlugin.instance);

		Bukkit.getScheduler().runTaskTimer(MantlePlugin.instance, new HardmodeTimer(), 20, 20);

        if (HardmodeSettings.BABY_ZOMBIE_BURN.bool()) {
            BabyZombieBurner burner = new BabyZombieBurner();

            Bukkit.getPluginManager().registerEvents(burner, MantlePlugin.instance);
            Integer interval = HardmodeSettings.BABY_ZOMBIE_CHECK_INTERVAL.integer();
            Bukkit.getScheduler().scheduleSyncRepeatingTask(MantlePlugin.instance, burner, interval, interval);
        }


		NMSWitherManager.register();
        
        return true;
	}
	
	public static void applyDamageNode(LivingEntity entity, String node)
	{
		if (instance == null || !instance.active)
			return;
		
		EntityDamageEvent customDamage = new EntityDamageEvent(entity, DamageCause.CUSTOM, 0d);
		DamageNodeParser.parseDamageEvent(customDamage, node, instance.config);
		
		double damage = customDamage.getDamage();
		if (damage > 0)
			entity.damage(damage);

	}

    @Override
    public void loadConfig()
    {
        super.loadConfig();
        NearbyPlayerPathfinderGoalProxy.maximumRangeToPlayer = HardmodeSettings.ANIMALS_AI_MAXIMUM_RANGE_TO_PLAYER.doubleNumber();
    }

    @Override
	protected void unloadModule() {
	}
}
