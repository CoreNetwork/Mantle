package us.corenetwork.mantle.hardmode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import us.corenetwork.mantle.MantlePlugin;
import net.minecraft.server.v1_8_R1.EntityWither;
import net.minecraft.server.v1_8_R1.EntityWitherSkull;
import net.minecraft.server.v1_8_R1.PathfinderGoal;

public class PathfinderGoalGoUpAndShoot extends AbstractPathfinderGoal {

	private CustomWither wither;
	static boolean done = false;
	public PathfinderGoalGoUpAndShoot(CustomWither wither)
	{
		this.wither = wither;
	}
	
	@Override
	public boolean a()
	{
		if(PathfinderGoalGoUpAndShoot.done == false)
		{
			PathfinderGoalGoUpAndShoot.done = true;
			return true;
		}
		return false;
	}

	
	@Override
	public boolean b()
	{
		return true;
	}
	
	@Override
	public void c()
	{
		double x = wither.locX;
		double y = wither.locY;
		double z = wither.locZ;

		wither.getNavigation().a(x, y + 10,z,1);
			
		
	}
	
	@Override
	public void d()
	{
		PathfinderGoalGoUpAndShoot.done = false;
	}
	
	@Override
	public void e()
	{
		if(wither.world.getTime() % 100 != 0)
			return;
		
		for(int i = -5; i<=5;++i)
		{
			for(int j = -5;j<=5;++j)
			{
					EntityWitherSkull entitywitherskull = new EntityWitherSkull(wither.world, wither,i,0,j);
					entitywitherskull.locX = wither.locX;
					entitywitherskull.locY = wither.locY + 3;
					entitywitherskull.locZ = wither.locZ;
					entitywitherskull.setCharged(true);
					wither.world.addEntity(entitywitherskull);
			}
		}
		
		
		
		
	}
	
	@Override
	public boolean i()
	{
		return super.i();
	}
}
