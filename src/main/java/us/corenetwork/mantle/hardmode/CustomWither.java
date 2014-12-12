package us.corenetwork.mantle.hardmode;

import com.google.common.base.Predicate;
import net.minecraft.server.v1_8_R1.*;
import org.bukkit.craftbukkit.v1_8_R1.util.UnsafeList;

import java.lang.reflect.Field;

public class CustomWither extends EntityWither {

	private static final Predicate bp = new EntitySelectorHuman();


	private int[] bm = new int[2];
	public CustomWither(World world)
	{
		super(world);

		try
		{

			Field gsa = PathfinderGoalSelector.class.getDeclaredField("b");

			gsa.setAccessible(true);
			gsa.set(this.goalSelector, new UnsafeList());
			gsa.set(this.targetSelector, new UnsafeList());


			
		} catch (SecurityException e)
		{
			e.printStackTrace();
		} catch (NoSuchFieldException e)
		{
			e.printStackTrace();
		} catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		} catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
        //this.goalSelector.a(0, new PathfinderGoalGoUpAndShoot2(this, 1.0D));

		this.goalSelector.a(0, new PathfinderStomp(this));
		this.goalSelector.a(3, new PathfinderFloat(this));
		//this.goalSelector.a(4, new PathfinderSquare(this));
		//this.goalSelector.a(5, new PathfinderGoalGoUpAndShoot(this));
		//this.goalSelector.a(3, new PathfinderPentagram(this));

        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, false, new Class[0]));
	}

	@Override
	public void m()
	{
		super.m();



	}


	@Override
	protected void doTick()
	{
		//age up
		//++this.aO;

		//check despawn
		//this.D();

		this.getEntitySenses().a();

		//Albo byc moze tu, przez to ze ustawiamy ten target? I to w tym juz sie robi follow, a w dalszym sie tak na prawde nic nie robi?
		//W tym sie moze ustawiac jakis dziwny Path, ktory pozniej bedzie totalnie odkrywany przez navigation -> move contorller?
		this.targetSelector.a();


		this.goalSelector.a();

		//Tu sie dzieje cala magia~
		this.navigation.k();
		//^ Right here

		//Entity tick
		this.E();

		//Byc moze troche tez tu
		this.moveController.c();

		this.getControllerLook().a();
		this.getControllerJump().b();


	}

	@Override
	protected float bD() {
		return 0.8F;
	}

	protected void E() {
		if (this.getGoalTarget() != null) {
			this.b(0, this.getGoalTarget().getId());
		} else {
			this.b(0, 0);
		}
	}

}

final class EntitySelectorHuman implements Predicate {

	EntitySelectorHuman()
	{
	}

    public boolean a(Entity entity) {
        return entity instanceof EntityLiving && ((EntityLiving) entity).getMonsterType() != EnumMonsterType.UNDEAD;
    }

    public boolean apply(Object object) {
        return this.a((Entity) object);
    }
}