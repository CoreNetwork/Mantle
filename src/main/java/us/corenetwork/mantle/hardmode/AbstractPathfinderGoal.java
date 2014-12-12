package us.corenetwork.mantle.hardmode;

import net.minecraft.server.v1_8_R1.PathfinderGoal;

public abstract class AbstractPathfinderGoal extends PathfinderGoal {

    /**
     * Returns whether the PathfinderGoal should begin execution.
     */
    public abstract boolean a();
    

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean b()
    {
    	return super.b();
    }
	
    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void c()
    {
    	super.c();
    }
    
    /**
     * Resets the task
     */
    public void d()
    {
    	super.d();
    }
    
    /**
     * Updates the task
     */
    public void e()
    {
    	super.e();
    }
    
    /**
     * Determine if this AI Task is interruptible by a higher (= lower value) priority task. All vanilla AITask have
     * this value set to true.
     */
    public boolean i()
    {
    	return super.i();
    }
    
    /**
     * Get a bitmask telling which other tasks may not run concurrently. The test is a simple bitwise AND - if it yields
     * zero, the two tasks may run concurrently, if not - they must run exclusively from each other.
     */
    public int j()
    {
    	// TODO Auto-generated method stub
    	return super.j();
    }
    
    /**
     * Sets a bitmask telling which other tasks may not run concurrently. The test is a simple bitwise AND - if it
     * yields zero, the two tasks may run concurrently, if not - they must run exclusively from each other.
     */
    public void a(int arg0)
    {
    	super.a(arg0);
    }
}
