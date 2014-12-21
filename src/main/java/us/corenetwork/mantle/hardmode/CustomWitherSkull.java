package us.corenetwork.mantle.hardmode;

import net.minecraft.server.v1_8_R1.EntityLiving;
import net.minecraft.server.v1_8_R1.EntityWitherSkull;
import net.minecraft.server.v1_8_R1.World;

/**
 * Created by Ginaf on 2014-12-15.
 */
public class CustomWitherSkull extends EntityWitherSkull {

    private float customNormalSpeed;
    private float customChargedSpeed;
    public boolean shouldSpawnMinions = false;

    public CustomWitherSkull(World world)
    {
        super(world);
        initSpeeds();
    }

    public CustomWitherSkull(World world, EntityLiving entityliving, double d0, double d1, double d2)
    {
        super(world, entityliving, d0, d1, d2);
        initSpeeds();
    }


    private void initSpeeds()
    {
        customNormalSpeed = super.j();
        customChargedSpeed = 0.73F;
    }

    public void setCustomNormalSpeed(float value)
    {
        customNormalSpeed = value;
    }

    public void setCustomChargedSpeed(float value)
    {
        customChargedSpeed = value;
    }

    //Return speed of the skull
    @Override
    protected float j()
    {
        //return 0.5F;
        //TODO Fixit fixit fixit
        return super.j();
        //return isCharged() ? customChargedSpeed : customNormalSpeed;
    }

    @Override
    public void s_() {
        super.s_();

    }

}
