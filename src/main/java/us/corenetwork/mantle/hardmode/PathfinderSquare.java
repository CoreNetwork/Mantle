package us.corenetwork.mantle.hardmode;

import net.minecraft.server.v1_8_R1.EntityCreature;

public class PathfinderSquare extends AbstractWitherMove {

    private int phase;

    public PathfinderSquare(CustomWither wither)
    {
        super(wither);
        this.a(127);
    }

    @Override
    public boolean a()
    {
        return true;
    }


    @Override
    public boolean b()
    {
        return !(wither.getNavigation().m() && phase == 4);
    }

    @Override
    public void c()
    {
        phase = 0;
    }

    @Override
    public void e()
    {
/*
        if(!entity.getControllerMove().a())
		{
			double x = entity.locX;
			double y = entity.locY;
			double z = entity.locZ;


			switch (phase)
			{
			case 0:
				entity.getControllerMove().a(x+10,y,z,1);
				break;
			case 1:
				entity.getControllerMove().a(x,y,z+10,1);
				break;
			case 2:
				entity.getControllerMove().a(x-10,y,z,1);
				break;
			case 3:
				entity.getControllerMove().a(x,y,z-10,1);
				break;
			default:
				break;
			}
			phase++;
		}
*/
        if (wither.getNavigation().m())
        {
            double x = wither.locX;
            double y = wither.locY;
            double z = wither.locZ;


            switch (phase)
            {
                case 0:
                    wither.getNavigation().a(x + 10, y, z, 1);
                    break;
                case 1:
                    wither.getNavigation().a(x, y, z + 10, 1);
                    break;
                case 2:
                    wither.getNavigation().a(x - 10, y, z, 1);
                    break;
                case 3:
                    wither.getNavigation().a(x, y, z - 10, 1);
                    break;
                default:
                    break;
            }
            phase++;
        }
    }


    @Override
    public void d()
    {
        phase = 0;
    }


}
