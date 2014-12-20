package us.corenetwork.mantle.hardmode;

import net.minecraft.server.v1_8_R1.EntityLiving;
import net.minecraft.server.v1_8_R1.EnumParticle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import us.corenetwork.mantle.ParticleLibrary;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ginaf on 2014-12-18.
 */
public class MoveAcidCloud extends AbstractWitherMove {

    private float RANGE;
    private int DURATION;

    private int timeLeft;
    private List<Location> acidCloudLocations;
    public MoveAcidCloud(CustomWither wither)
    {
        super(wither, "Acid Cloud", "AC");
        this.a(2);
    }

    @Override
    protected void initializeMoveConfig()
    {
        RANGE = HardmodeSettings.WITHER_PH_AC_RANGE.floatNumber();
        DURATION = HardmodeSettings.WITHER_PH_AC_DURATION.integer();

        MANA_COST = HardmodeSettings.WITHER_PH_AC_MANACOST.integer();
        COOLDOWN = HardmodeSettings.WITHER_PH_AC_COOLDOWN.integer();
        NORMAL_ATTACK = HardmodeSettings.WITHER_PH_AC_NORMALATTACK.bool();
    }

    @Override
    public void c()
    {
        super.c();
        timeLeft = DURATION;

        spawnAcidClouds();

    }

    @Override
    public void e()
    {
        //tick damage/armour reduction

        timeLeft--;
        isActive = timeLeft == 0;
    }

    private void spawnAcidClouds()
    {
        acidCloudLocations = new ArrayList<Location>();
        for(Object o : wither.getTargetList())
        {
            EntityLiving entityLiving = (EntityLiving) o;
            Location loc = new Location(Bukkit.getWorld("world_nether"), entityLiving.locX, entityLiving.locY, entityLiving.locZ);
            ParticleLibrary.broadcastParticle(EnumParticle.SMOKE_LARGE, loc, 0.5f, 0.5f, 0.5f, 0, 10, null);
            acidCloudLocations.add(loc);
        }


        //add phhhaarticles


    }
}
