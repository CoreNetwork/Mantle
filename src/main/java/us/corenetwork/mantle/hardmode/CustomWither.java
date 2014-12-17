package us.corenetwork.mantle.hardmode;

import com.google.common.base.Predicate;
import net.minecraft.server.v1_8_R1.*;
import org.bukkit.craftbukkit.v1_8_R1.util.UnsafeList;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomWither extends EntityWither {


    private boolean BS_ENABLED;
    private float BS_SPEED;
    private double BS_SEARCH_HORIZ;
    private double BS_SEARCH_VERT;
    private double BS_SHOOT_MAX_DISTANCE;
    private int BS_SHOOT_BASIC_TIME;
    private int BS_SHOOT_TIME_VARIANCE;
    private int BS_RE_SEARCH_TIME;

    private boolean inSpawningPhase;


    private int nextTargetSearchTime;
    private int nextShootTime;


    private int manaMax;
    private int manaLeft;
    private int MANA_REGEN;

    private int shieldMax;
    private int shieldLeft;
    private int SHIELD_REGEN;

    private double baseDamage;
    private double BASE_DMG;

    //Used by E, here to initialize it before E
    private List targetList = new ArrayList();

    //Collection for all the moves, to iterate over & lower cooldowns
    //Only modify it in constructor
    private List<AbstractWitherMove> moves = new ArrayList<AbstractWitherMove>();

    public CustomWither(World world)
    {
        super(world);
        initializeFromConfig();



        try
        {

            Field gsa = PathfinderGoalSelector.class.getDeclaredField("b");

            gsa.setAccessible(true);
            gsa.set(this.goalSelector, new UnsafeList());
            gsa.set(this.targetSelector, new UnsafeList());

            Field suffCounter = EntityWither.class.getDeclaredField("bo");
            suffCounter.setAccessible(true);
            suffCounter.getInt(this);

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


        MoveStationaryArtillery moveStationaryArtillery = new MoveStationaryArtillery(this);
        MoveStomp moveStomp = new MoveStomp(this);
        MoveWitherAura pathfinderPentagram = new MoveWitherAura(this);
        moves.add(moveStationaryArtillery);
        //moves.add(moveStomp);
        moves.add(pathfinderPentagram);

        this.goalSelector.a(2, pathfinderPentagram);
        //this.goalSelector.a(2, moveStomp);
        this.goalSelector.a(3, moveStationaryArtillery);

        //this.goalSelector.a(4, new PathfinderSquare(this));
        //this.goalSelector.a(5, new PathfinderGoalGoUpAndShoot(this));
        //this.goalSelector.a(3, new PathfinderPentagram(this));
    }

    protected void initializeFromConfig()
    {
        BS_ENABLED = HardmodeSettings.WITHER_BS_ENABLED.bool();
        BS_SPEED = HardmodeSettings.WITHER_BS_SPEED.floatNumber();
        BS_SEARCH_HORIZ = HardmodeSettings.WITHER_BS_SEARCH_HORIZ.doubleNumber();
        BS_SEARCH_VERT = HardmodeSettings.WITHER_BS_SEARCH_VERT.doubleNumber();
        BS_SHOOT_MAX_DISTANCE = HardmodeSettings.WITHER_BS_SHOOT_MAX_DISTANCE.doubleNumber() * HardmodeSettings.WITHER_BS_SHOOT_MAX_DISTANCE.doubleNumber();
        BS_SHOOT_BASIC_TIME = HardmodeSettings.WITHER_BS_SHOOT_BASIC_TIME.integer();
        BS_SHOOT_TIME_VARIANCE = HardmodeSettings.WITHER_BS_SHOOT_TIME_VARIANCE.integer();
        BS_RE_SEARCH_TIME = HardmodeSettings.WITHER_BS_RE_SEARCH_TIME.integer();
        MANA_REGEN = HardmodeSettings.WITHER_MANA_REGEN.integer();
        SHIELD_REGEN = HardmodeSettings.WITHER_SHIELD_REGEN.integer();
        BASE_DMG = HardmodeSettings.WITHER_BASE_DMG.doubleNumber();
    }

    public boolean isInSpawningPhase()
    {
        return inSpawningPhase;
    }

    /**
     * ========================
     * Mana & Armor methods
     * ========================
     */

    //--MANA
    public int getManaLeft()
    {
        return manaLeft;
    }

    public void setManaLeft(int value)
    {
        manaLeft = value;
    }

    public void tickMana()
    {
        manaLeft += MANA_REGEN;
        manaLeft = manaLeft > manaMax ? manaMax : manaLeft;
    }

    public void setManaMax(int value)
    {
        manaMax = value;
    }


    //--SHIELD
    public int getShieldLeft()
    {
        return shieldLeft;
    }

    public void setShieldLeft(int value)
    {
        shieldLeft = value;
    }

    public void tickShield()
    {
        shieldLeft += SHIELD_REGEN;
        shieldLeft = shieldLeft > shieldMax ? shieldMax : shieldLeft;
    }

    public void setShieldMax(int value)
    {
        shieldMax = value;
    }

    //--Damage
    public double getBaseDamage()
    {
        return baseDamage;
    }

    public void setBaseDamage(double value)
    {
        baseDamage = value;
    }

    public void resetBaseDamage()
    {
        baseDamage = BASE_DMG;
    }

    //--Health

    public void changeMaxAmountsOnNumOfPlayers(int numOfPlayer)
    {

    }


    //Move tick
    @Override
    public void m()
    {
        super.m();
    }

    //Entity tick - removed some stuff from original
    protected void E()
    {
        int i;

        //If on flashing-blue phase during spawning.
        if (inSpawningPhase)
        {
            i = this.cj() - 1;
            if (i <= 0)
            {
                this.world.createExplosion(this, this.locX, this.locY + (double) this.getHeadHeight(), this.locZ, 7.0F, false, this.world.getGameRules().getBoolean("mobGriefing"));
                this.world.a(1013, new BlockPosition(this), 0);
                inSpawningPhase = false;
            }

            this.r(i);
            if (this.ticksLived % 10 == 0)
            {
                this.heal(10.0F);
            }

        }
        else
        {
            int j;


            //Look for targets around
            if (this.ticksLived >= nextTargetSearchTime)
            {
                nextTargetSearchTime = ticksLived + BS_RE_SEARCH_TIME;
                targetList = this.world.a(EntityLiving.class, this.getBoundingBox().grow(BS_SEARCH_HORIZ, BS_SEARCH_VERT, BS_SEARCH_HORIZ), new EntitySelectorHuman());
            }

            //Shoot them!
            if (ticksLived > nextShootTime && BS_ENABLED)
            {
                nextShootTime = ticksLived + BS_SHOOT_BASIC_TIME + this.bb().nextInt(2 * BS_SHOOT_TIME_VARIANCE) - BS_SHOOT_TIME_VARIANCE;

                Collections.shuffle(targetList);
                i = 1;
                for (Object o : targetList)
                {

                    EntityLiving entityliving = (EntityLiving) o;
                    if (entityliving != null && entityliving.isAlive() && this.h(entityliving) <= BS_SHOOT_MAX_DISTANCE && this.hasLineOfSight(entityliving))
                    {
                        if (entityliving instanceof EntityHuman && ((EntityHuman) entityliving).abilities.isInvulnerable)
                        {
                            continue;
                        }
                        this.b(i, entityliving.getId());
                        this.a(i + 1, entityliving, false);
                        i = i == 1 ? 2 : 1;
                    }
                }
            }

            if (this.getGoalTarget() != null)
            {
                this.b(0, this.getGoalTarget().getId());
            } else
            {
                this.b(0, 0);
            }

            try
            {

                //reflection to get/set value of suffocation counter
                Field suffCounter = EntityWither.class.getDeclaredField("bo");
                suffCounter.setAccessible(true);
                int suffCount = suffCounter.getInt(this);

                //remove blocks from around if 	suffered from not-human-sourced damage
                if (suffCount > 0)
                {

                    --suffCount;
                    suffCounter.setInt(this, suffCount);

                    if (suffCount == 0 && this.world.getGameRules().getBoolean("mobGriefing"))
                    {
                        i = MathHelper.floor(this.locY);
                        j = MathHelper.floor(this.locX);
                        int j1 = MathHelper.floor(this.locZ);
                        boolean flag = false;

                        for (int k1 = -1; k1 <= 1; ++k1)
                        {
                            for (int l1 = -1; l1 <= 1; ++l1)
                            {
                                for (int i2 = 0; i2 <= 3; ++i2)
                                {
                                    int j2 = j + k1;
                                    int k2 = i + i2;
                                    int l2 = j1 + l1;
                                    Block block = this.world.getType(new BlockPosition(j2, k2, l2)).getBlock();

                                    if (block.getMaterial() != Material.AIR && block != Blocks.BEDROCK && block != Blocks.END_PORTAL && block != Blocks.END_PORTAL_FRAME && block != Blocks.COMMAND_BLOCK && block != Blocks.BARRIER)
                                    {
                                        flag = this.world.setAir(new BlockPosition(j2, k2, l2), true) || flag;
                                    }
                                }
                            }
                        }

                        if (flag)
                        {
                            this.world.a((EntityHuman) null, 1012, new BlockPosition(this), 0);
                        }
                    }
                }


            } catch (IllegalAccessException e1)
            {
                e1.printStackTrace();
            } catch (NoSuchFieldException e1)
            {
                e1.printStackTrace();
            }
            if (this.ticksLived % 20 == 0)
            {
                this.heal(1.0F);
            }

            tickCooldowns();
        }
    }

    private void tickCooldowns()
    {
        for(AbstractWitherMove move : moves)
        {
            move.tickCooldown();
        }
    }

    //With new armour idea, no armor for arrows
    @Override
    public boolean ck()
    {
        return false;
    }

    //Higher jump - if we ever need to make him actually jump on one block
    @Override
    protected float bD()
    {
        return 0.8F;
    }


    //Setting up spawning phase
    @Override
    public void n()
    {
        inSpawningPhase = true;
        super.n();
    }

	/*
     * =========================================
	 * COPY-PASTA from EntityWither, without changes - coz its easier than reflection
	 * =========================================
	 */

    private void a(int i, EntityLiving entityliving, boolean flag)
    {
        this.a(i, entityliving.locX, entityliving.locY + (double) entityliving.getHeadHeight() * 0.5D, entityliving.locZ, flag);
    }

    private void a(int i, EntityLiving entityliving)
    {
        this.a(i, entityliving.locX, entityliving.locY + (double) entityliving.getHeadHeight() * 0.5D, entityliving.locZ, i == 0 && this.random.nextFloat() < 0.001F);
    }

    private void a(int i, double d0, double d1, double d2, boolean flag)
    {
        this.world.a((EntityHuman) null, 1014, new BlockPosition(this), 0);
        double d3 = this.t(i);
        double d4 = this.u(i);
        double d5 = this.v(i);
        double d6 = d0 - d3;
        double d7 = d1 - d4;
        double d8 = d2 - d5;
        CustomWitherSkull entitywitherskull = new CustomWitherSkull(this.world, this, d6, d7, d8);
        entitywitherskull.setCustomNormalSpeed(BS_SPEED);
        if (flag)
        {
            entitywitherskull.setCharged(true);
        }

        entitywitherskull.locY = d4;
        entitywitherskull.locX = d3;
        entitywitherskull.locZ = d5;
        this.world.addEntity(entitywitherskull);
    }

    public void a(EntityLiving entityliving, float f)
    {
        this.a(0, entityliving);
    }


    private double t(int i)
    {
        if (i <= 0)
        {
            return this.locX;
        } else
        {
            float f = (this.aG + (float) (180 * (i - 1))) / 180.0F * 3.1415927F;
            float f1 = MathHelper.cos(f);

            return this.locX + (double) f1 * 1.3D;
        }
    }

    private double u(int i)
    {
        return i <= 0 ? this.locY + 3.0D : this.locY + 2.2D;
    }

    private double v(int i)
    {
        if (i <= 0)
        {
            return this.locZ;
        } else
        {
            float f = (this.aG + (float) (180 * (i - 1))) / 180.0F * 3.1415927F;
            float f1 = MathHelper.sin(f);

            return this.locZ + (double) f1 * 1.3D;
        }
    }

    private float b(float f, float f1, float f2)
    {
        float f3 = MathHelper.g(f1 - f);

        if (f3 > f2)
        {
            f3 = f2;
        }

        if (f3 < -f2)
        {
            f3 = -f2;
        }

        return f + f3;
    }
}

final class EntitySelectorHuman implements Predicate {

    public boolean a(Entity entity)
    {
        return entity instanceof EntityHuman;
    }

    public boolean apply(Object object)
    {
        return this.a((Entity) object);
    }
}