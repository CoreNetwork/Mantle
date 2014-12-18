package us.corenetwork.mantle.hardmode;

import net.minecraft.server.v1_8_R1.PathfinderGoal;
import us.corenetwork.mantle.MLog;

public abstract class AbstractWitherMove extends PathfinderGoal {

    //Move name, debug use only (probably)
    private String name;
    private String shortName;

    protected CustomWither wither;
    protected int MANA_COST;
    protected int COOLDOWN;
    protected boolean NORMAL_ATTACK;

    protected int cooldownLeft;
    protected boolean isActive;

    public AbstractWitherMove(CustomWither wither, String name, String shortName)
    {
        this.wither = wither;
        this.name = name;
        this.shortName = shortName;
        initializeMoveConfig();
        isActive = false;
        cooldownLeft = 0;
    }

    /**
     * Initializez this Move, reading all required values from config.
     */
    protected void initializeMoveConfig() {}


    public String getName()
    {
        return name;
    }

    public String getShortName()
    {
        return shortName;
    }


    /**
     * @return wheter the Move is currently in progress
     */
    public boolean isActive()
    {
        return isActive;
    }

    /**
     * @return true if Move uses normal black skull attack
     */
    public boolean usesNormalAttack()
    {
        return NORMAL_ATTACK;
    }


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

    public int getManaCost()
    {
        return MANA_COST;
    }

    public int getCooldown()
    {
        return COOLDOWN;
    }

    public int getCooldownLeft()
    {
        return cooldownLeft;
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
        return !isOnCooldown() && hasEnoughMana() && !wither.isInSpawningPhase();
    }

    /**
     * Returns whether an in-progress Move should continue executing
     */
    public boolean b()
    {
    	return isActive;
    }
	
    /**
     * Execute the Move, run one time on Move start
     */
    public void c()
    {
        MLog.debug("&f[&3Wither&f]&f Starting " + name + " move.");
    	super.c();
        isActive = true;
    }
    
    /**
     * Resets the Move
     */
    public void d()
    {
        MLog.debug("&f[&3Wither&f]&f Stopping " + name + " move.");
    	super.d();
        isActive = false;
        cooldownLeft = COOLDOWN;
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
    	return false;
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
