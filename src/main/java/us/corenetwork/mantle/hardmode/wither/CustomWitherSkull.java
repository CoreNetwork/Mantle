package us.corenetwork.mantle.hardmode.wither;

import net.minecraft.server.v1_8_R2.DamageSource;
import net.minecraft.server.v1_8_R2.EntityLiving;
import net.minecraft.server.v1_8_R2.EntityWitherSkull;
import net.minecraft.server.v1_8_R2.EnumDifficulty;
import net.minecraft.server.v1_8_R2.MobEffect;
import net.minecraft.server.v1_8_R2.MobEffectList;
import net.minecraft.server.v1_8_R2.MovingObjectPosition;
import net.minecraft.server.v1_8_R2.World;
import org.bukkit.event.entity.ExplosionPrimeEvent;

public class CustomWitherSkull extends EntityWitherSkull {

    public boolean shouldSpawnMinions = false;
    public float damage = 8F;
    public float explosionRadius = 1F;

    public CustomWitherSkull(World world)
    {
        super(world);
    }

    public CustomWitherSkull(World world, EntityLiving entityliving, double d0, double d1, double d2)
    {
        super(world, entityliving, d0, d1, d2);
    }


    protected void a(MovingObjectPosition movingobjectposition) {
            if (movingobjectposition.entity != null) {
                // Spigot start
                boolean didDamage = false;
                if (this.shooter != null) {
                    didDamage = movingobjectposition.entity.damageEntity(DamageSource.mobAttack(this.shooter), damage);
                    if (didDamage) {
                        if (!movingobjectposition.entity.isAlive()) {
                            this.shooter.heal(5.0F, org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason.WITHER); // CraftBukkit
                        } else {
                            this.a(this.shooter, movingobjectposition.entity);
                        }
                    }
                } else {
                    didDamage = movingobjectposition.entity.damageEntity(DamageSource.MAGIC, 5.0F);
                }

                if (didDamage && movingobjectposition.entity instanceof EntityLiving) {
                    // Spigot end
                    byte b0 = 0;

                    if (this.world.getDifficulty() == EnumDifficulty.NORMAL) {
                        b0 = 10;
                    } else if (this.world.getDifficulty() == EnumDifficulty.HARD) {
                        b0 = 40;
                    }

                    if (b0 > 0) {
                        ((EntityLiving) movingobjectposition.entity).addEffect(new MobEffect(MobEffectList.WITHER.id, 20 * b0, 1));
                    }
                }
            }

            // CraftBukkit start
            // this.world.createExplosion(this, this.locX, this.locY, this.locZ, 1.0F, false, this.world.getGameRules().getBoolean("mobGriefing"));
            ExplosionPrimeEvent event = new ExplosionPrimeEvent(this.getBukkitEntity(), 1.0F, false);
            this.world.getServer().getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                this.world.createExplosion(this, this.locX, this.locY, this.locZ, explosionRadius, event.getFire(), this.world.getGameRules().getBoolean("mobGriefing"));
            }
            // CraftBukkit end
            this.die();
    }

}
