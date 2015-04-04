package us.corenetwork.mantle.hardmode;

import java.lang.reflect.Field;
import net.minecraft.server.v1_8_R2.IRangedEntity;
import net.minecraft.server.v1_8_R2.PathfinderGoalArrowAttack;

/**
 * Custom CustomPathfinderGoalArrowAttack that divides initial delay between targeting and shooting first projectile by specific amount
 */
public class CustomPathfinderGoalArrowAttack extends PathfinderGoalArrowAttack
{
    private static Field countdownField;

    /**
     * Whether skeleton was targeting previous tick or not
     */
    private boolean wasTargetingBefore;

    private HardmodeSettings multiplierSetting;

    static
    {
        try
        {
            countdownField = PathfinderGoalArrowAttack.class.getDeclaredField("d");
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        countdownField.setAccessible(true);
    }

    public CustomPathfinderGoalArrowAttack(IRangedEntity iRangedEntity, double v, int i, int i1, float v1, HardmodeSettings multiplierSetting)
    {
        super(iRangedEntity, v, i, i1, v1);
        this.multiplierSetting = multiplierSetting;
    }

    @Override
    public void e()
    {

        try
        {

            if (((int) countdownField.get(this)) < 0) //Countdown field is -1 if skeleton did not fire single arrow yet (just targeted)
            {
                wasTargetingBefore = true;
            }
            else if (wasTargetingBefore) //Previous tick set initial delay for arrow, we will reduce it now
            {
                int newNumber = (int) countdownField.get(this);
                newNumber *= multiplierSetting.doubleNumber();
                newNumber = Math.max(1, newNumber); //newNumber must stay above 0 as minecraft first decrements the number which would turn it into -1 if left at 0.
                countdownField.set(this, newNumber);

                wasTargetingBefore = false;
            }
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        super.e();
    }
}
