package us.corenetwork.mantle.slimespawning;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.World;
import org.bukkit.entity.Slime;

public class SlimeKillTimer implements Runnable {
    public static SlimeKillTimer timerSingleton;

    private static World overworld;
    private static boolean day = true;

    private static final FireworkEffect FIREWORK = FireworkEffect.builder().withColor(Color.GREEN).build();
    
    public SlimeKillTimer() {
        overworld = Bukkit.getWorld(SlimeSpawningSettings.OVERWORLD_NAME.string());
        
    }

    @Override
    public void run() {
        if (overworld.getTime() > SlimeSpawningSettings.SLIME_KILL_TIME.integer() && day)
        {
        	day = false;
        	
        	for (Slime slime : overworld.getEntitiesByClass(Slime.class))
        	{
//        		for (Entity e : slime.getNearbyEntities(10, 10, 10))
//        		{
//        			if (e instanceof Player)
//        			{
//                		ParticleLibrary.s.sendToPlayer((Player) e, slime.getLocation(), 0, 0, 0, 0,  10);
//        			}
//        		}
//        		
//        		slime.getWorld().playSound(slime.getLocation(), Sound.ENDERMAN_DEATH, 1f, 1f);
//        		
        		slime.getWorld().playEffect(slime.getLocation(), Effect.SMOKE, 0);
        		slime.remove();
        	}
        }
        else if (overworld.getTime() <= SlimeSpawningSettings.SLIME_KILL_TIME.integer())
        {
        	day = true;
        }
    }
}
