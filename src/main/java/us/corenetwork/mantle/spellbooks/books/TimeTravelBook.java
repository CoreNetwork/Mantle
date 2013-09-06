package us.corenetwork.mantle.spellbooks.books;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.server.v1_6_R2.EntityVillager;
import net.minecraft.server.v1_6_R2.MerchantRecipe;
import net.minecraft.server.v1_6_R2.MerchantRecipeList;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftVillager;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
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
	}
	
	private Player curPlayer;
	
	@Override
	public void onActivate(SpellbookItem item, PlayerInteractEvent event) {
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
							
				curPlayer.giveExp(addedExp);
				
				curPlayer.updateInventory();
				
				if (enchantmentTypes.size() > 0)
					break;
			}
		}
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
    
    private static int getEnchantmentWorth(Enchantment enchantment, int level)
    {
    	return 25;
    }


}
