package us.corenetwork.mantle.spellbooks.books;

import java.util.HashMap;
import java.util.List;
import net.minecraft.server.v1_8_R2.EntityAgeable;
import net.minecraft.server.v1_8_R2.EntityAnimal;
import net.minecraft.server.v1_8_R2.EntityHorse;
import net.minecraft.server.v1_8_R2.EntityHuman;
import net.minecraft.server.v1_8_R2.EntityLiving;
import net.minecraft.server.v1_8_R2.EntityPlayer;
import net.minecraft.server.v1_8_R2.EntityVillager;
import net.minecraft.server.v1_8_R2.EnumParticle;
import net.minecraft.server.v1_8_R2.NBTTagCompound;
import net.minecraft.server.v1_8_R2.PacketPlayOutUpdateEntityNBT;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.TreeSpecies;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftAgeable;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftAnimals;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Zombie;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Tree;
import org.bukkit.util.Vector;
import us.corenetwork.core.claims.BlockWorker;
import us.corenetwork.core.claims.ClaimsModule;
import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.ParticleLibrary;
import us.corenetwork.mantle.spellbooks.EntityIterator;
import us.corenetwork.mantle.spellbooks.Spellbook;
import us.corenetwork.mantle.spellbooks.SpellbookItem;
import us.corenetwork.mantle.spellbooks.SpellbookUtil;
import us.corenetwork.mantle.util.InventoryUtil;


public class GrazingBook extends Spellbook
{

    private static final int EFFECT_AREA_HEIGHT_ABOVE_PLAYER = 31;
    private static final int EFFECT_AREA_HORIZONTAL_RADIUS = 32 / 2;

    private static final HashMap<EntityType, ItemStack[]> FOOD = new HashMap<>();

    static
    {
        FOOD.put(EntityType.PIG, new ItemStack[]{new ItemStack(Material.CARROT_ITEM, 1)});
        FOOD.put(EntityType.SHEEP, new ItemStack[]{new ItemStack(Material.WHEAT, 1)});
        FOOD.put(EntityType.COW, new ItemStack[]{new ItemStack(Material.WHEAT, 1)});
        FOOD.put(EntityType.CHICKEN, new ItemStack[]{new ItemStack(Material.SEEDS, 1)});
        FOOD.put(EntityType.HORSE, new ItemStack[]{new ItemStack(Material.GOLDEN_CARROT, 1), new ItemStack(Material.GOLDEN_APPLE)});
    }

    public GrazingBook()
    {
        super("Grazing");

        settings.setDefault(SETTING_TEMPLATE, "spell-grazing");
    }

    @Override
    public BookFinishAction onActivate(SpellbookItem item, PlayerInteractEvent event)
    {
        Player player = event.getPlayer();

        Location effectLoc = SpellbookUtil.getPointInFrontOfPlayer(event.getPlayer().getEyeLocation(), 2);
        event.getPlayer().playSound(effectLoc, Sound.PIG_IDLE, 1.0f, 1.0f);

        Location raisedLocation = event.getPlayer().getLocation();
        raisedLocation.setY(raisedLocation.getY() + EFFECT_AREA_HEIGHT_ABOVE_PLAYER / 2);

        Inventory playerInventory = player.getInventory();

        List<Entity> nearbyEntities = EntityIterator.getEntitiesInCube(raisedLocation, EFFECT_AREA_HORIZONTAL_RADIUS);
        for (Entity entity : nearbyEntities)
        {
            if (!(entity instanceof Animals))
                continue;


            ItemStack[] acceptedFood = FOOD.get(entity.getType());
            if (acceptedFood == null)
                continue;


            for (ItemStack foodItem : acceptedFood)
            {
                if (InventoryUtil.getAmountOfItems(playerInventory, foodItem.getType(), foodItem.getDurability()) < foodItem.getAmount())
                    continue;

                if (putInLoveMode(((CraftAnimals) entity).getHandle(), ((CraftPlayer) player).getHandle()))
                {
                    InventoryUtil.removeItems(playerInventory, foodItem.getType(), foodItem.getDurability(), foodItem.getAmount());

                }
            }
        }

        player.updateInventory();

        return BookFinishAction.BROADCAST_AND_CONSUME;
    }


    private static boolean putInLoveMode(EntityAnimal animal, EntityPlayer breeder)
    {
        if (animal.isBaby())
            return false;

        if (animal.isInLove())
            return false;

        if (animal instanceof EntityHorse && !((EntityHorse) animal).isTame())
            return false;

        //Only animals with Age = 0 can breed (breeding timeout)
        if (animal.getAge() != 0)
            return false;


        animal.c(breeder); //Enable breeding mode
        return true;
    }

    @Override
	protected BookFinishAction onActivateEntity(SpellbookItem item, PlayerInteractEntityEvent event) {
		return BookFinishAction.NOTHING;
	}
}
