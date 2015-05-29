package us.corenetwork.mantle.spellbooks.books;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import us.corenetwork.mantle.ParticleLibrary;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.nanobot.NanobotUtil;
import us.corenetwork.mantle.spellbooks.Spellbook;
import us.corenetwork.mantle.spellbooks.SpellbookItem;
import us.corenetwork.mantle.spellbooks.SpellbookUtil;


public class TimeBook extends Spellbook {	
	public TimeBook() {
		super("Time");
		
		initEnchants();		
		
		settings.setDefault(SETTING_TEMPLATE, "spell-time");
	}
	
	private Player curPlayer;
	
	@Override
	public BookFinishAction onActivate(SpellbookItem item, PlayerInteractEvent event) {
		curPlayer = event.getPlayer();

        ParticleLibrary.broadcastParticle(EnumParticle.ENCHANTMENT_TABLE, SpellbookUtil.getPointInFrontOfPlayer(curPlayer.getEyeLocation(), 0.3), 0.3f, 0.3f, 0.3f, 0, 30, null);
				
		for (int i = 0; i < curPlayer.getInventory().getSize() + 4; i++)
		{
			ItemStack stack = curPlayer.getInventory().getItem(i);
			if (stack != null)
			{
				if (NanobotUtil.hasTag(stack, "HideFlags")) //Do not process items with hidden flags
					continue;

				int addedLevels = 0;
				List<Enchantment> enchantmentTypes = new ArrayList<Enchantment>(stack.getEnchantments().size());
				
				for (Entry<Enchantment, Integer> e : stack.getEnchantments().entrySet())
				{
					addedLevels += getEnchantmentValue(e.getKey(), e.getValue());
					enchantmentTypes.add(e.getKey());
				}
				
				for (Enchantment e : enchantmentTypes)
					stack.removeEnchantment(e);
							
				if (addedLevels > 25)
					addedLevels = 25;
				
				for (int level = 0; level < addedLevels; level++)
				{
					curPlayer.giveExp(expCost(level + 1));
				}
				
				if (enchantmentTypes.size() > 0)
					break;
			}
		}
		
		curPlayer.updateInventory();

		
		return BookFinishAction.BROADCAST_AND_CONSUME;
	}
	
    private static Map<Enchantment, Integer[]> levelTable;
    
    private static int getEnchantmentValue(Enchantment enchantment, int level)
    {
    	Integer[] enchantmentLevels = levelTable.get(enchantment);
    	if (enchantmentLevels == null)
    		return 8;
    	
    	return enchantmentLevels[0] * --level + enchantmentLevels[1];
    }

    int expCost(int currentLevel) {
        if (currentLevel >= 30) {
            return 62 + (currentLevel - 30) * 7;
        } else if (currentLevel >= 15) {
            return 17 + (currentLevel - 15) * 3;
        } else {
            return 17;
        }
    }
    
    private static void initEnchants()
    {
    	levelTable = new HashMap<Enchantment, Integer[]>();
    	
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
    	
    	levelTable.put(enchantment, parameters);
    }

	@Override
	protected BookFinishAction onActivateEntity(SpellbookItem item, PlayerInteractEntityEvent event) {
		return BookFinishAction.NOTHING;
	}
    

}
