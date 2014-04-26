package us.corenetwork.mantle.spellbooks.books;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.TreeSpecies;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Tree;
import org.bukkit.util.Vector;

import us.corenetwork.mantle.ParticleLibrary;
import us.corenetwork.mantle.spellbooks.EntityIterator;
import us.corenetwork.mantle.spellbooks.Spellbook;
import us.corenetwork.mantle.spellbooks.SpellbookItem;
import us.corenetwork.mantle.spellbooks.SpellbookUtil;


public class GrowthBook extends Spellbook implements EntityIterator.EntityReceiver {

	private static final int EFFECT_RADIUS = 32 / 2;
	
	public GrowthBook() {
		super("Growth");
		
		settings.setDefault(SETTING_TEMPLATE, "spell-growth");
	}

	@Override
	public boolean onActivate(SpellbookItem item, PlayerInteractEvent event) {
		
		Location effectLoc = SpellbookUtil.getPointInFrontOfPlayer(event.getPlayer().getEyeLocation(), 2);
		Vector direction = event.getPlayer().getLocation().getDirection();

		ParticleLibrary.HAPPY_VILLAGER.sendToPlayer(event.getPlayer(), effectLoc, (float) (1.0 - direction.getX()), 0.5f, (float) (1.0 - direction.getZ()), 0, 10);
		event.getPlayer().playSound(effectLoc, Sound.LEVEL_UP, 1.0f, 1.0f);
		
		Block baseBlock = event.getPlayer().getLocation().getBlock();
		for (int x = -EFFECT_RADIUS; x <= EFFECT_RADIUS; x++)
		{
			for (int y = -EFFECT_RADIUS; y <= EFFECT_RADIUS; y++)
			{
				for (int z = -EFFECT_RADIUS; z <= EFFECT_RADIUS; z++)
				{
					Block block = baseBlock.getRelative(x, y, z);
					processBlock(block);
				}
			}
		}
		
		EntityIterator.iterateEntitiesInCube(this, event.getPlayer().getLocation(), EFFECT_RADIUS);
		
		return true;
	}

	private void processBlock(Block block)
	{
		if (block.getType() == Material.CROPS || block.getType() == Material.CARROT || block.getType() == Material.POTATO)
			block.setData((byte) 7);
		else if (block.getType() == Material.NETHER_WARTS)
			block.setData((byte) 3);
		else if (block.getType() == Material.MELON_STEM)
		{
			block.setData((byte) 7);
			for (BlockFace face : new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH })
			{
				final Block neighbour = block.getRelative(face);
				if (neighbour != null && neighbour.isEmpty())
				{
					Block downBlock = block.getRelative(BlockFace.DOWN);
					if (downBlock != null && (downBlock.getType() == Material.GRASS || downBlock.getType() == Material.DIRT || downBlock.getType() == Material.SOIL))
					{
						neighbour.setType(Material.MELON_BLOCK);
						break;
					}
				}
			}
		}
		else if (block.getType() == Material.SAPLING)
		{
			byte data = block.getData();
			TreeSpecies species = new Tree(block.getTypeId(), data).getSpecies();
			
			TreeType tree;
			
			switch (species)
			{
			case BIRCH:
				tree = TreeType.BIRCH;
				break;
			case JUNGLE:
				tree = TreeType.JUNGLE;
				break;
			case REDWOOD:
				tree = TreeType.REDWOOD;
				break;
			case ACACIA:
				tree = TreeType.ACACIA;
				break;
			case DARK_OAK:
				tree = TreeType.DARK_OAK;
				break;
			case GENERIC:
				tree = TreeType.TREE;
				break;

			default:
				tree = TreeType.BIG_TREE;
			}
			
			block.setType(Material.AIR);
			
			boolean generated = block.getWorld().generateTree(block.getLocation(), tree);
			if (!generated)
				block.setTypeIdAndData(Material.SAPLING.getId(), data, false);
		}
		
	}

	@Override
	public void onEntityFound(Entity entity) {
		if (entity instanceof Ageable)
		{
			Ageable ageable = (Ageable) entity;
			if (!ageable.isAdult())
			{
				ageable.setAdult();
			}
		}
	}

	@Override
	protected boolean onActivateEntity(SpellbookItem item, PlayerInteractEntityEvent event) {
		return false;
	}
}
