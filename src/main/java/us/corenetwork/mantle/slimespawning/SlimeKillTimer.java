package us.corenetwork.mantle.slimespawning;

import net.minecraft.server.v1_8_R1.EnumParticle;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;

import us.corenetwork.mantle.ParticleLibrary;

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
        		for (Entity e : slime.getNearbyEntities(10, 10, 10))
        		{
        			if (e instanceof Player)
        			{
                		ParticleLibrary.sendToPlayer(EnumParticle.EXPLOSION_NORMAL, (Player)e, slime.getLocation(), 0, 0, 0, 0, 10, null);
        			}
        		}
        		
        		slime.getWorld().playSound(slime.getLocation(), Sound.SLIME_WALK, 1f, 1f);
        		
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
