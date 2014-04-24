package us.corenetwork.mantle.spellbooks.books;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.server.v1_7_R3.EntityVillager;
import net.minecraft.server.v1_7_R3.MerchantRecipe;
import net.minecraft.server.v1_7_R3.MerchantRecipeList;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftVillager;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.spellbooks.CircleIterator;
import us.corenetwork.mantle.spellbooks.Spellbook;
import us.corenetwork.mantle.spellbooks.SpellbookItem;
import us.corenetwork.mantle.spellbooks.SpellbookUtil;


public class TimeTravelBook extends Spellbook implements CircleIterator.EntityReceiver {

	public TimeTravelBook() {
		super("Spellbook of Time Travel");
		
		initEnchants();
	}
	
	private Player curPlayer;
	
	@Override
	public boolean onActivate(SpellbookItem item, PlayerInteractEvent event) {
		curPlayer = event.getPlayer();

		Location effectLoc = SpellbookUtil.getPointInFrontOfPlayer(curPlayer.getEyeLocation(), 2);

		FireworkEffect effect = FireworkEffect.builder().withColor(Color.BLUE).withFade(Color.BLUE).build();
		Util.showFirework(effectLoc, effect);
		
		CircleIterator.iterateCircleEntities(this, event.getPlayer().getLocation(), 8 / 2);
		
		for (int i = 0; i < curPlayer.getInventory().getSize() + 4; i++)
		{
			ItemStack stack = curPlayer.getInventory().getItem(i);
			if (stack != null)
			{
				int addedExp = 0;
				List<Enchantment> enchantmentTypes = new ArrayList<Enchantment>(stack.getEnchantments().size());
				
				for (Entry<Enchantment, Integer> e : stack.getEnchantments().entrySet())
				{
					addedExp += getEnchantmentWorth(e.getKey(), e.getValue());
					enchantmentTypes.add(e.getKey());
				}
				
				for (Enchantment e : enchantmentTypes)
					stack.removeEnchantment(e);
							
				if (addedExp > 25)
					addedExp = 25;
				curPlayer.giveExpLevels(addedExp);
				
				curPlayer.updateInventory();
				
				if (enchantmentTypes.size() > 0)
					break;
			}
		}
		
		return true;
	}

	@Override
	public void onCircleEntity(Entity entity) {
		if (entity.getType() == EntityType.VILLAGER)
		{
			EntityVillager nbtVillager = ((CraftVillager) entity).getHandle();
			MerchantRecipeList offers = nbtVillager.getOffers(((CraftPlayer) curPlayer).getHandle());
			for (Object recipeObj : offers)
			{
				MerchantRecipe recipe = (MerchantRecipe) recipeObj;
				recipe.a(maxUses());
			}
		}
	}
	
    private static int removalMinimum = 2;
    private static int removalMaximum = 13;
    
    private static int maxUses() {
        int firstDice = (removalMaximum - removalMinimum)/2 + 1;
        int secondDice = removalMaximum - removalMinimum - firstDice + 2;
        
        //Insurance if either value came out invalid.
        firstDice = (firstDice < 1) ? 1 : firstDice;
        secondDice = (secondDice < 1) ? 1 : secondDice;

        return MantlePlugin.random.nextInt(firstDice) + MantlePlugin.random.nextInt(secondDice) + removalMinimum;
    }
    
    private static Map<Enchantment, Integer[]> xpTable;
    
    private static int getEnchantmentWorth(Enchantment enchantment, int level)
    {
    	Integer[] enchantmentLevels = xpTable.get(enchantment);
    	if (enchantmentLevels == null)
    		return 8;
    	
    	return enchantmentLevels[0] * --level + enchantmentLevels[1];
    }

    private static void initEnchants()
    {
    	xpTable = new HashMap<Enchantment, Integer[]>();
    	
    	//Armor
    	addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 11, 1);
    	addEnchant(Enchantment.PROTECTION_FIRE, 8, 10);
    	addEnchant(Enchantment.PROTECTION_FALL, 6, 5);
    	addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 8, 5);
    	addEnchant(Enchantment.PROTECTION_PROJECTILE, 6, 3);
    	addEnchant(Enchantment.OXYGEN, 10, 10);
    	addEnchant(Enchantment.WATER_WORKER, 0, 10);
    	addEnchant(Enchantment.THORNS, 20, 10);

    	//Sword
    	addEnchant(Enchantment.DAMAGE_ALL, 11, 1);
    	addEnchant(Enchantment.DAMAGE_UNDEAD, 8, 10);
    	addEnchant(Enchantment.DAMAGE_ARTHROPODS, 8, 10);
    	addEnchant(Enchantment.KNOCKBACK, 15, 5);
    	addEnchant(Enchantment.FIRE_ASPECT, 20, 10);
    	addEnchant(Enchantment.LOOT_BONUS_MOBS, 9, 15);

    	//Bow
    	addEnchant(Enchantment.ARROW_DAMAGE, 10, 1);
    	addEnchant(Enchantment.ARROW_KNOCKBACK, 20, 12);
    	addEnchant(Enchantment.ARROW_FIRE, 0, 20);
    	addEnchant(Enchantment.ARROW_INFINITE, 0, 20);
    	
    	//Tool
    	addEnchant(Enchantment.DIG_SPEED, 10, 1);
    	addEnchant(Enchantment.SILK_TOUCH, 0, 15);
    	addEnchant(Enchantment.DURABILITY, 8, 5);
    	addEnchant(Enchantment.LOOT_BONUS_BLOCKS, 9, 15);

    }
    
    private static void addEnchant(Enchantment enchantment, int k, int m)
    {
    	Integer[] parameters = new Integer[2];
    	parameters[0] = k;
    	parameters[1] = m;
    	
    	xpTable.put(enchantment, parameters);
    }

	@Override
	protected boolean onActivateEntity(SpellbookItem item, PlayerInteractEntityEvent event) {
		return false;
	}
    

}
