package us.corenetwork.mantle.hardmode;

import net.minecraft.server.v1_8_R1.PathfinderGoal;

public abstract class AbstractWitherMove extends PathfinderGoal {

    protected CustomWither wither;
    protected int MANA_COST;
    protected int COOLDOWN;

    protected int cooldownLeft;


    public AbstractWitherMove(CustomWither wither)
    {
        this.wither = wither;
        initializeMoveConfig();
    }

    /**
     * Initializez this Move, reading all required values from config.
     */
    protected void initializeMoveConfig() {}


    /**
     * @return true if wither has enough mana to use this move, false otherwise
     */
    public boolean hasEnoughMana()
    {
        return wither.getManaLeft() >= this.MANA_COST;
    }

    /**
     * @return true if Move on cooldown
     */
    public boolean isOnCooldown()
    {
        return cooldownLeft > 0;
    }

    /**
     * Decrease cooldownLeft by 1
     */
    public void tickCooldown()
    {
        tickCooldown(1);
    }

    private void tickCooldown(int i)
    {
        cooldownLeft -= i;
        cooldownLeft = cooldownLeft < 0 ? 0 : cooldownLeft;
    }

    /**
     * Returns whether the Move should begin execution.
     */
    public boolean a()
    {
        return !isOnCooldown() && hasEnoughMana();
    }


    /**
     * Returns whether an in-progress Move should continue executing
     */
    public boolean b()
    {
    	return super.b();
    }
	
    /**
     * Execute the Move, run one time on Move start
     */
    public void c()
    {
    	super.c();
    }
    
    /**
     * Resets the Move
     */
    public void d()
    {
    	super.d();
    }
    
    /**
     * Updates the Move
     */
    public void e()
    {
    	super.e();
    }
    
    /**
     * Determines if this Move is interruptible by a higher priority Move ( = lower value)
     */
    public boolean i()
    {
    	return super.i();
    }
    
    /**
     * Get concurrency mask of this Move. Bitwise AND. Zero == can run conc, anything other == nope
     */
    public int j()
    {
    	return super.j();
    }
    
    /**
     * Set concurrency mask of this Move.
     */
    public void a(int arg0)
    {
    	super.a(arg0);
    }
}
