package us.corenetwork.mantle.hardmode.wither;

import com.google.common.base.Predicate;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.minecraft.server.v1_8_R2.Block;
import net.minecraft.server.v1_8_R2.BlockPosition;
import net.minecraft.server.v1_8_R2.Blocks;
import net.minecraft.server.v1_8_R2.DamageSource;
import net.minecraft.server.v1_8_R2.Entity;
import net.minecraft.server.v1_8_R2.EntityHuman;
import net.minecraft.server.v1_8_R2.EntityLiving;
import net.minecraft.server.v1_8_R2.EntityWither;
import net.minecraft.server.v1_8_R2.EnumParticle;
import net.minecraft.server.v1_8_R2.GenericAttributes;
import net.minecraft.server.v1_8_R2.Material;
import net.minecraft.server.v1_8_R2.MathHelper;
import net.minecraft.server.v1_8_R2.PathfinderGoalSelector;
import net.minecraft.server.v1_8_R2.World;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R2.util.UnsafeList;
import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.hardmode.HardmodeSettings;

public class CustomWither extends EntityWither {

    private boolean WITHER_DEBUG;
    private String WITHER_SHIELD_COLOR;
    private List<String> WITHER_NAMES;

    private int SPAWNING_PHASE_DURATION;
    //Not in settings for now, not really needed there
    private float SPAWNING_HP_FRACTION = 0.4F;
    private float SPAWNING_PHASE_REGEN;


    private double BS_SEARCH_HORIZ;
    private double BS_SEARCH_VERT;
    private double BS_SHOOT_MAX_DISTANCE;
    private int BS_SHOOT_BASIC_TIME;
    private int BS_SHOOT_TIME_VARIANCE;
    private int BS_RE_SEARCH_TIME;
    public float BS_RADIUS;

    public float KNOCKBACK_POWER;

    private boolean inSpawningPhase;


    private int nextTargetSearchTime;
    private int nextShootTime;


    private float manaMax = 1;
    private float manaLeft = 1;
    private float MANA_REGEN;
    private List<Integer> MANA_MAX_AMOUNTS;

    private boolean shieldActive;
    private float shieldMax = 1;
    private float shieldLeft = 1;
    private float SHIELD_REGEN;
    private List<Integer> SHIELD_MAX_AMOUNTS;

    private float healthMax = 1;
    private float HEALTH_REGEN;
    private List<Integer> HEALTH_MAX_AMOUNTS;

    private int delayBetweenMovesLeft = 1;
    private int delayBetweenMovesMax;
    private List<Integer> DELAY_MAX_AMOUNTS;

    public float BASE_DMG;

    public org.bukkit.World bukkitWorld;

    //Used by E, here to initialize it before E
    private List targetList = new ArrayList();

    //Collection for all the moves, to iterate over & lower cooldowns
    //Only modify it in constructor
    private List<AbstractWitherMove> moves = new ArrayList<AbstractWitherMove>();

    //to cast all spells before they are cast again?
    private List<AbstractWitherMove> movesOrdering = new ArrayList<AbstractWitherMove>();
    private List<AbstractWitherMove> allButOne = new ArrayList<AbstractWitherMove>();



    public CustomWither(World world)
    {
        super(world);


        //Fuckit, hacky way to get bukkit world.
        if(((CraftWorld)Bukkit.getWorld("world")).getHandle().equals(world))
        {
            bukkitWorld = Bukkit.getWorld("world");
        }
        else if(((CraftWorld)Bukkit.getWorld("world_nether")).getHandle().equals(world))
        {
            bukkitWorld = Bukkit.getWorld("world_nether");
        }

        initializeFromConfig();
        searchForTargets();
        changeAmountsOnNumOfPlayers(targetList.size());
        updateName();
        //Set delay left to current max, to not start the skill right away
        startDelayAfterMove();

        try
        {

            Field gsa = PathfinderGoalSelector.class.getDeclaredField("b");

            gsa.setAccessible(true);
            gsa.set(this.goalSelector, new UnsafeList());
            gsa.set(this.targetSelector, new UnsafeList());

            Field suffCounter = EntityWither.class.getDeclaredField("bp");
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


        MoveArtillery moveArtillery = new MoveArtillery(this);
        MoveStomp moveStomp = new MoveStomp(this);
        MoveWitherAura moveWitherAura = new MoveWitherAura(this);
        MoveWitherAuraTestGround moveWitherAuraTestGround = new MoveWitherAuraTestGround(this);
        MoveAcidCloud moveAcidCloud = new MoveAcidCloud(this);
        MoveMinions moveMinions = new MoveMinions(this);


        moves.add(moveArtillery);
        moves.add(moveStomp);
        moves.add(moveWitherAura);
        moves.add(moveWitherAuraTestGround);
        moves.add(moveAcidCloud);
        moves.add(moveMinions);


        //Move order stuff
        allButOne.addAll(moves);
        allButOne.remove(moveArtillery);
        movesOrdering.addAll(allButOne);

        //To start with random order
        Collections.shuffle(allButOne);
        for(int i = 0; i < allButOne.size();i++)
        {
            this.goalSelector.a(i, allButOne.get(i));
        }
        //This has to be last, coz its like a basic move.
        this.goalSelector.a(10, moveArtillery);


        //this.goalSelector.a(4, new PathfinderSquare(this));
        //this.goalSelector.a(5, new PathfinderGoalGoUpAndShoot(this));
        //this.goalSelector.a(3, new PathfinderPentagram(this));
    }

    protected void initializeFromConfig()
    {

        WITHER_DEBUG = HardmodeSettings.WITHER_DEBUG.bool();
        WITHER_SHIELD_COLOR = HardmodeSettings.WITHER_SHIELD_COLOR.string();

        WITHER_NAMES = HardmodeSettings.WITHER_NAMES.stringList();

        SPAWNING_PHASE_DURATION = HardmodeSettings.WITHER_SPAWNING_PHASE_DURATION.integer();

        BS_SEARCH_HORIZ = HardmodeSettings.WITHER_BS_SEARCH_HORIZ.doubleNumber();
        BS_SEARCH_VERT = HardmodeSettings.WITHER_BS_SEARCH_VERT.doubleNumber();
        BS_SHOOT_MAX_DISTANCE = HardmodeSettings.WITHER_BS_SHOOT_MAX_DISTANCE.doubleNumber() * HardmodeSettings.WITHER_BS_SHOOT_MAX_DISTANCE.doubleNumber();
        BS_SHOOT_BASIC_TIME = HardmodeSettings.WITHER_BS_SHOOT_BASIC_TIME.integer();
        BS_SHOOT_TIME_VARIANCE = HardmodeSettings.WITHER_BS_SHOOT_TIME_VARIANCE.integer();
        BS_RE_SEARCH_TIME = HardmodeSettings.WITHER_BS_RE_SEARCH_TIME.integer();
        BS_RADIUS = HardmodeSettings.WITHER_BS_RADIUS.floatNumber();

        KNOCKBACK_POWER = HardmodeSettings.WITHER_KNOCKBACK_POWER.floatNumber();

        MANA_REGEN = HardmodeSettings.WITHER_MANA_REGEN.floatNumber();
        MANA_MAX_AMOUNTS = HardmodeSettings.WITHER_MANA_MAX_AMOUNTS.intList();
        SHIELD_REGEN = HardmodeSettings.WITHER_SHIELD_REGEN.floatNumber();
        SHIELD_MAX_AMOUNTS = HardmodeSettings.WITHER_SHIELD_MAX_AMOUNTS.intList();
        HEALTH_REGEN = HardmodeSettings.WITHER_HEALTH_REGEN.floatNumber();
        HEALTH_MAX_AMOUNTS = HardmodeSettings.WITHER_HEALTH_MAX_AMOUNTS.intList();

        DELAY_MAX_AMOUNTS = HardmodeSettings.WITHER_DELAY_BETWEEN_SKILLS_AMOUNTS.intList();

        BASE_DMG = HardmodeSettings.WITHER_BASE_DMG.floatNumber();
    }

    public void useMove(AbstractWitherMove move)
    {
        movesOrdering.remove(move);
        if(movesOrdering.size() == 0)
        {
            movesOrdering.addAll(allButOne);
        }
    }

    public boolean canUseMove(AbstractWitherMove move)
    {
        return movesOrdering.contains(move);
    }


    public boolean isInSpawningPhase()
    {
        return inSpawningPhase;
    }

    public boolean isOnDelayBetweenMoves()
    {
        return delayBetweenMovesLeft > 0;
    }

    public void startDelayAfterMove()
    {
        delayBetweenMovesLeft = delayBetweenMovesMax;
    }

    private void setDelayBetweenMovesMax(int value)
    {
         delayBetweenMovesMax = value;
    }

    public void tickDelay()
    {
        delayBetweenMovesLeft--;
        if(delayBetweenMovesLeft < 0)
            delayBetweenMovesLeft = 0;
    }


    public List getTargetList()
    {
        return targetList;
    }


    /**
     * ========================
     * Mana & Armor methods
     * ========================
     */

    //--MANA
    public float getManaLeft()
    {
        return manaLeft;
    }

    public void setManaLeft(float value)
    {
        manaLeft = value;
    }

    public void tickMana()
    {
        manaLeft += MANA_REGEN * manaMax;
        manaLeft = manaLeft > manaMax ? manaMax : manaLeft;
    }

    public void setManaMax(float value)
    {
        manaMax = value;
    }

    public float getManaMax()
    {
        return manaMax;
    }

    //--SHIELD
    public float getShieldLeft()
    {
        return shieldLeft;
    }

    public void setShieldLeft(float value)
    {
        shieldLeft = value;
    }

    public void tickShield()
    {
        shieldLeft += SHIELD_REGEN * shieldMax;
        shieldLeft = shieldLeft > shieldMax ? shieldMax : shieldLeft;
    }

    public void setShieldMax(float value)
    {
        shieldMax = value;
    }

    public float getShieldMax()
    {
        return shieldMax;
    }

    //--HEALTH

    public void tickHealth()
    {
        regenHealth(HEALTH_REGEN);
    }

    public void regenHealth(float regen)
    {
        float health = this.getHealth() + regen * healthMax;
        health =  health > healthMax ? healthMax : health;
        this.setHealth(health);
    }


    public void setHealthMax(float value)
    {
        this.getAttributeInstance(GenericAttributes.maxHealth).setValue(value);
        healthMax = value;
    }

    public float getHealthMax()
    {
        return healthMax;
    }



    public void changeAmountsOnNumOfPlayers(int numOfPlayer)
    {
        float newMaxValue = 100;
        float percent;
        int i;

        percent = getManaLeft() / getManaMax();
        for(i = 0; i <= numOfPlayer && i < MANA_MAX_AMOUNTS.size(); ++i)
        {
            newMaxValue = MANA_MAX_AMOUNTS.get(i);
        }
        setManaMax(newMaxValue);
        setManaLeft(percent * newMaxValue);

        percent = getShieldLeft() / getShieldMax();
        for(i = 0; i <= numOfPlayer && i < SHIELD_MAX_AMOUNTS.size(); ++i)
        {
            newMaxValue = SHIELD_MAX_AMOUNTS.get(i);
        }
        setShieldMax(newMaxValue);
        setShieldLeft(percent * newMaxValue);

        percent = getHealth() / getHealthMax();
        for(i = 0; i <= numOfPlayer && i < HEALTH_MAX_AMOUNTS.size(); ++i)
        {
            newMaxValue = HEALTH_MAX_AMOUNTS.get(i);
        }
        setHealthMax(newMaxValue);
        setHealth(percent * newMaxValue);

        int newDelayValue = 0;
        for(i = 0; i <= numOfPlayer && i < DELAY_MAX_AMOUNTS.size(); ++i)
        {
            newDelayValue = DELAY_MAX_AMOUNTS.get(i);
        }
        setDelayBetweenMovesMax(newDelayValue);

    }


    //Move tick
    @Override
    public void m()
    {
        super.m();
        if(shieldActive)
        {
            for (j = 0; j < 3; ++j) {
                this.world.addParticle(EnumParticle.SPELL_MOB, this.locX + this.random.nextGaussian() * 1.0D, this.locY + (double) (this.random.nextFloat() * 3.3F), this.locZ + this.random.nextGaussian() * 1.0D, 0.699999988079071D, 0.699999988079071D, 0.8999999761581421D, new int[0]);
            }
        }
    }


    private static String regionId;
    //Entity tick - removed some stuff from original
    protected void E()
    {
        int i;

        //If on flashing-blue phase during spawning.
        if (inSpawningPhase)
        {
            i = this.cl() - 1;

            if(i == 1)
            {
                regionId = createWorldGuardRegionUnderWither();
            }
            if (i <= 0)
            {
                this.world.createExplosion(this, this.locX, this.locY + (double) this.getHeadHeight(), this.locZ, 7.0F, false, this.world.getGameRules().getBoolean("mobGriefing"));
                this.world.a(1013, new BlockPosition(this), 0);
                removeRegionUnderWither(regionId);

                inSpawningPhase = false;
            }

            this.r(i);
            if (this.ticksLived % 20 == 0)
            {
                regenHealth(SPAWNING_PHASE_REGEN);
            }

        }
        else
        {
            int j;


            //Look for targets around
            if (this.ticksLived >= nextTargetSearchTime)
            {
                nextTargetSearchTime = ticksLived + BS_RE_SEARCH_TIME;
                searchForTargets();
            }

            //Shoot them!
            if (ticksLived > nextShootTime && isNormalAttackEnabled())
            {
                nextShootTime = ticksLived + BS_SHOOT_BASIC_TIME + MantlePlugin.random.nextInt(2 * BS_SHOOT_TIME_VARIANCE) - BS_SHOOT_TIME_VARIANCE;

                Collections.shuffle(targetList);
                i = 1;
                for (Object o : targetList)
                {

                    EntityLiving entityliving = (EntityLiving) o;
                    if (entityliving != null && entityliving.isAlive() && this.h(entityliving) <= BS_SHOOT_MAX_DISTANCE && this.hasLineOfSight(entityliving))
                    {
                        //lets target gm1 too for now
                        //if (entityliving instanceof EntityHuman && ((EntityHuman) entityliving).abilities.isInvulnerable) continue;

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
                Field suffCounter = EntityWither.class.getDeclaredField("bp");
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
                tickHealth();
                tickMana();
                tickShield();
            }

            tickCooldowns();
            tickDelay();
            setInvulnerable(targetList.size() == 0);
        }

        //Here alone to update during spawning phase
        if (this.ticksLived % 20 == 0)
        {
            updateName();
            changeAmountsOnNumOfPlayers(targetList.size());
        }
        updateShield();
    }

    private String createWorldGuardRegionUnderWither()
    {
        WorldGuardPlugin wg = (WorldGuardPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
        RegionManager manager = wg.getRegionManager(bukkitWorld);

        int halfSize = HardmodeSettings.WITHER_BOX_UNDER_SIZE.integer() / 2;

        Location firstPoint = new Location(bukkitWorld, this.locX - halfSize, this.locY - halfSize, this.locZ - halfSize);
        Location secondPoint = new Location(bukkitWorld, this.locX + halfSize, this.locY - 2, this.locZ + halfSize);
        BlockVector firstVector = new BlockVector(firstPoint.getX(), firstPoint.getY(), firstPoint.getZ());
        BlockVector secondVector = new BlockVector(secondPoint.getX(), secondPoint.getY(), secondPoint.getZ());

        ProtectedRegion region = new ProtectedCuboidRegion("UnderWither"+getUniqueID().toString(), firstVector, secondVector);
        region.setFlag(DefaultFlag.OTHER_EXPLOSION, StateFlag.State.DENY);
        region.setFlag(DefaultFlag.CREEPER_EXPLOSION, StateFlag.State.DENY);
        region.setFlag(DefaultFlag.GHAST_FIREBALL, StateFlag.State.DENY);
        region.setFlag(DefaultFlag.ENDERDRAGON_BLOCK_DAMAGE, StateFlag.State.DENY);

        manager.addRegion(region);
        try
        {
            manager.save();
        } catch (StorageException e1)
        {
            e1.printStackTrace();
        }

        return region.getId();
    }

    private void removeRegionUnderWither(String regionId)
    {
        WorldGuardPlugin wg = (WorldGuardPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
        RegionManager manager = wg.getRegionManager(bukkitWorld);

        manager.removeRegion(regionId);
        try
        {
            manager.save();
        } catch (StorageException e1)
        {
            e1.printStackTrace();
        }
    }

    public void setSuffCounter(int value)
    {
        try
        {

            //reflection to get/set value of suffocation counter
            Field suffCounter = EntityWither.class.getDeclaredField("bp");
            suffCounter.setAccessible(true);
            suffCounter.setInt(this, value);
        } catch (Exception e1)
        {
            e1.printStackTrace();
        }
    }

    private void searchForTargets()
    {
        targetList = this.world.a(EntityLiving.class, this.getBoundingBox().grow(BS_SEARCH_HORIZ, BS_SEARCH_VERT, BS_SEARCH_HORIZ), new EntitySelectorHuman());
    }

    private boolean isNormalAttackEnabled()
    {
        if(moves.size() == 0)
            return false;
        boolean enabled = true;
        for(AbstractWitherMove move : moves)
        {
            enabled = move.isActive() ? enabled && move.usesNormalAttack() : enabled;
        }
        return enabled;
    }

    public boolean shouldKnockback()
    {
        return isNormalAttackEnabled();
    }


    private void tickCooldowns()
    {
        for(AbstractWitherMove move : moves)
        {
            move.tickCooldown();
        }
    }

    private void updateName()
    {
        if(WITHER_DEBUG)
        {
            String customName =  "&D"+"D:"+delayBetweenMovesLeft+"/"+delayBetweenMovesMax+" &4"+"H:"+getHealth()+"/"+getHealthMax()+" &B"+"M:"+getManaLeft()+"/"+getManaMax()
                    + " &6"+"S:"+getShieldLeft()+"/"+getShieldMax();
            for(AbstractWitherMove move : moves)
            {
                if(move.isActive())
                {
                    customName += " &D"+ move.getShortName()+"&F:"+move.getCooldownLeft()+"/"+move.getCooldown();
                }
                else
                {
                    customName += " &F"+ move.getShortName()+":"+move.getCooldownLeft()+"/"+move.getCooldown();
                }

            }
            setCustomName(ChatColor.translateAlternateColorCodes('&', customName));
        }
        else
        {
            if(!shieldActive || (shieldActive && shieldLeft == 0))
            {
                setCustomName(WITHER_NAMES.get(0));
            }
            else
            {
                int per = 100 / (WITHER_NAMES.size() - 1);
                int whichOne = ((int)(100* shieldLeft / shieldMax)) / per ;
                //MLog.debug(per + "  " + whichOne);
                setCustomName(ChatColor.translateAlternateColorCodes('&', WITHER_SHIELD_COLOR + WITHER_NAMES.get(whichOne)));
            }

        }
    }

    //Turn shield on or off
    private void updateShield()
    {
        shieldActive = getHealth()*2 < getHealthMax();
    }

    public void setInvulnerable(boolean val)
    {
        this.r(val ? 2 : 0);
    }

    //With new armour idea, no armor for arrows
    @Override
    public boolean ck()
    {
        return false;
    }

    public boolean damageEntity(DamageSource damagesource, float damage)
    {
        if(shieldActive && shieldLeft > 0)
        {
            float toHP = 0F;
            if(damage >= shieldLeft)
            {
                toHP = damage - shieldLeft;
                setShieldLeft(0);

            }
            else
            {
                setShieldLeft(shieldLeft - damage);
            }
            return super.damageEntity(damagesource, toHP);
        }
        else
            return super.damageEntity(damagesource, damage);
    }



    //Setting up spawning phase
    @Override
    public void n()
    {
        inSpawningPhase = true;
        this.r(SPAWNING_PHASE_DURATION);
        this.setHealth(this.getMaxHealth() * SPAWNING_HP_FRACTION);
        SPAWNING_PHASE_REGEN = (1 - SPAWNING_HP_FRACTION) / (SPAWNING_PHASE_DURATION / 20.0F);
    }

    @Override
    public void die(DamageSource damagesource)
    {
        super.die(damagesource);
        for(AbstractWitherMove move : moves)
        {
            move.cleanUp();
        }
    }

    @Override
    public void die()
    {
        super.die();
        for(AbstractWitherMove move : moves)
        {
            move.cleanUp();
        }
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
        entitywitherskull.damage = BASE_DMG;
        entitywitherskull.explosionRadius = BS_RADIUS;
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