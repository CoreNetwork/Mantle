package us.corenetwork.mantle.gametweaks;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;

public class GameTweaksListener implements Listener {
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onItemCrafted(CraftItemEvent event)
	{
		Recipe recipe = event.getRecipe();
		
		//Make sound on item repair
		if (recipe instanceof ShapelessRecipe)
		{
			Material resultType = recipe.getResult().getType();
			if (!resultType.isBlock())
			{
				boolean isRepairRecipe = true;
				
				ShapelessRecipe shapeless = (ShapelessRecipe) recipe;
				for (ItemStack i :shapeless.getIngredientList())
				{
					if (!i.getType().equals(resultType))
					{
						isRepairRecipe = false;
						break;
					}
				}
				
				if (isRepairRecipe)
				{
					event.getWhoClicked().getWorld().playSound(event.getWhoClicked().getLocation(), Sound.ANVIL_USE, 0.5f, 2f);
				}
			}
		}		
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event)
	{
		//Huge mushrooms should only drop sponge part.
		if (event.getBlock().getType() == Material.HUGE_MUSHROOM_1 || event.getBlock().getType() == Material.HUGE_MUSHROOM_2)
			event.getBlock().setData((byte) 0);
	}

}
