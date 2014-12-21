package us.corenetwork.mantle.hardmode;

import net.minecraft.server.v1_8_R1.EntityLiving;
import net.minecraft.server.v1_8_R1.EnumParticle;
import net.minecraft.server.v1_8_R1.MathHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import us.corenetwork.mantle.ParticleLibrary;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Ginaf on 2014-12-18.
 */
public class MoveAcidCloud extends AbstractWitherMove {

    private float RANGE;
    private int DURATION;
    private int PARTICLE;
    private int PARTICLE_REFRESH_RATE;
    private int PARTICLE_AMOUNT;
    private int DEBUFF_REFRESH_RATE;
    private List<Integer> DEBUFF_DURA_REMOVED;
    private boolean DEBUFF_HUNGER;

    private int timeLeft;
    private List<Location> acidCloudLocations;
    private Set<Player> playersInClouds = new HashSet<Player>();

    public MoveAcidCloud(CustomWither wither)
    {
        super(wither, "Acid Cloud", "AC");
        this.a(2);
    }

    @Override
    protected void initializeMoveConfig()
    {
        RANGE = HardmodeSettings.WITHER_PH_AC_RANGE.floatNumber();
        DURATION = HardmodeSettings.WITHER_PH_AC_DURATION.integer();
        PARTICLE = HardmodeSettings.WITHER_PH_AC_PARTICLE.integer();
        PARTICLE_REFRESH_RATE = HardmodeSettings.WITHER_PH_AC_PARTICLE_REFRESH_RATE.integer();
        PARTICLE_AMOUNT = HardmodeSettings.WITHER_PH_AC_PARTICLE_AMOUT.integer();

        DEBUFF_REFRESH_RATE = HardmodeSettings.WITHER_PH_AC_DEBUFF_REFRESH_RATE.integer();
        DEBUFF_DURA_REMOVED = HardmodeSettings.WITHER_PH_AC_DEBUFF_DURA_REMOVED.intList();
        DEBUFF_HUNGER = HardmodeSettings.WITHER_PH_AC_DEBUFF_HUNGER.bool();

        MANA_COST = HardmodeSettings.WITHER_PH_AC_MANACOST.integer();
        COOLDOWN = HardmodeSettings.WITHER_PH_AC_COOLDOWN.integer();
        NORMAL_ATTACK = HardmodeSettings.WITHER_PH_AC_NORMALATTACK.bool();
    }

    @Override
    public void c()
    {
        super.c();
        timeLeft = DURATION;

        getLocations();

    }

    @Override
    public void e()
    {
        float offR, angleLong, angleLat;
        float offX, offY, offZ;

        //Damage/debuff stuff

        if(timeLeft % DEBUFF_REFRESH_RATE == 0)
        {
            playersInClouds.clear();

            for(Player playerInNether : Bukkit.getWorld("world_nether").getEntitiesByClass(Player.class))
            {
                for(Location loc : acidCloudLocations)
                {
                    if(playerInNether.getLocation().distanceSquared(loc) <= RANGE*RANGE)
                    {
                        playersInClouds.add(playerInNether);
                        break;
                    }
                }
            }

            for(Player playerInCloud : playersInClouds)
            {
                if(DEBUFF_HUNGER)
                    playerInCloud.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, DEBUFF_REFRESH_RATE, 1, false));

                for(ItemStack armorItem : playerInCloud.getEquipment().getArmorContents())
                {
                    int protLevel = armorItem.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
                    int duraToRemove = DEBUFF_DURA_REMOVED.get(protLevel);

                    armorItem.setDurability((short) (armorItem.getDurability() + duraToRemove));
                }
            }
        }


        //Draw particles~!
        if(timeLeft % PARTICLE_REFRESH_RATE == 0)
        {
            for(int i = 0; i < PARTICLE_AMOUNT; i++)
            {
                angleLong = wither.bb().nextFloat()*6.28318530718F;
                angleLat = wither.bb().nextFloat()*3.14159265359F - 1.5707963268F;
                offR = wither.bb().nextFloat()*RANGE;
                offX = offR * MathHelper.cos(angleLat) * MathHelper.cos(angleLong);
                offY = offR * MathHelper.sin(angleLat);
                offZ = offR * MathHelper.sin(angleLong) * MathHelper.cos(angleLat);

                for(Location loc : acidCloudLocations)
                {
                    ParticleLibrary.broadcastParticle(EnumParticle.a(PARTICLE), loc, 0.5f+ offX, 1f + offY, 0.5f + offZ, 0, 10, null);
                }
            }

        }


        timeLeft--;
        isActive = timeLeft != 0;
    }

    private void getLocations()
    {
        acidCloudLocations = new ArrayList<Location>();
        for(Object o : wither.getTargetList())
        {
            EntityLiving entityLiving = (EntityLiving) o;
            Location loc = new Location(Bukkit.getWorld("world_nether"), entityLiving.locX, entityLiving.locY, entityLiving.locZ);
            acidCloudLocations.add(loc);
        }
    }
}
