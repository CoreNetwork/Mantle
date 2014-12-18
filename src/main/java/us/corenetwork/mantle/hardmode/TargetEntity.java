package us.corenetwork.mantle.hardmode;

import net.minecraft.server.v1_8_R1.DamageSource;
import net.minecraft.server.v1_8_R1.EntityArmorStand;
import net.minecraft.server.v1_8_R1.World;
import us.corenetwork.mantle.MLog;

/**
 * Created by Ginaf on 2014-12-14.
 */
public class TargetEntity extends EntityArmorStand {


    public TargetEntity(World world) {
        super(world);
    }

    public TargetEntity(World world, double locX, double locY, double locZ)
    {
        super(world);

        this.setPosition(locX, locY, locZ);
        //TODO dodaj listener do cancelowania eventu zderzenia z tym entity
        setGravity(true);
        setInvisible(true);
        setSmall(true);
        setBasePlate(false);

        if(HardmodeSettings.WITHER_DEBUG.bool())
        {
            setCustomName("TARGET");
            setCustomNameVisible(true);
            setInvisible(false);
        }
        this.a(0F, 0F);
    }

    public TargetEntity(World world, boolean debug)
    {
        this(world, 0,0,0);
    }


    @Override
    public void setPosition(double d0, double d1, double d2) {
        super.setPosition(d0, d1, d2);
        MLog.debug("&f[&3Wither&f]&f TARGET at " + d0 + " " + d1 + " " + d2);
    }

    @Override
    public boolean isInvulnerable(DamageSource damagesource)
    {
        return true;
    }
}
