package us.corenetwork.mantle.spellbooks.books;

import java.util.List;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.TreeSpecies;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Tree;
import us.corenetwork.core.claims.BlockWorker;
import us.corenetwork.core.claims.ClaimsModule;
import us.corenetwork.mantle.ParticleLibrary;
import us.corenetwork.mantle.spellbooks.EntityIterator;
import us.corenetwork.mantle.spellbooks.Spellbook;
import us.corenetwork.mantle.spellbooks.SpellbookItem;


public class GrowthBook extends Spellbook {

	private static final int EFFECT_AREA_HEIGHT_ABOVE_PLAYER = 31;
    private static final int EFFECT_AREA_HORIZONTAL_RADIUS = 32 / 2;

    public GrowthBook() {
		super("Growth");
		
		settings.setDefault(SETTING_TEMPLATE, "spell-growth");
		settings.setDefault(SETTING_NO_ITEMS, "No bonemeal");
	}

	@Override
	public BookFinishAction onActivate(SpellbookItem item, PlayerInteractEvent event) {

		Player player = event.getPlayer();
        
		Location effectLoc = player.getEyeLocation();
        ParticleLibrary.broadcastParticleRing(EnumParticle.VILLAGER_HAPPY, player.getEyeLocation(), 2, Math.PI / 12, 5);
		event.getPlayer().playSound(effectLoc, Sound.LEVEL_UP, 1.0f, 1.0f);
		
		Block baseBlock = event.getPlayer().getLocation().getBlock();
        GrowthBookWoker growthBookWoker = new GrowthBookWoker(baseBlock);
        ClaimsModule.instance.pool.addWorker(growthBookWoker);

        Location raisedLocation = event.getPlayer().getLocation();
        raisedLocation.setY(raisedLocation.getY() + EFFECT_AREA_HEIGHT_ABOVE_PLAYER / 2);

		List<Entity> nearbyEntities = EntityIterator.getEntitiesInCube(raisedLocation, EFFECT_AREA_HORIZONTAL_RADIUS);
		for (Entity entity : nearbyEntities)
        {
            if (entity instanceof Ageable)
            {
                Ageable ageable = (Ageable) entity;
                if (!ageable.isAdult())
                    ageable.setAdult();
            }
            else if (entity instanceof Zombie)
            {
                Zombie zombie = (Zombie) entity;
                if (zombie.isBaby())
                    zombie.setBaby(false);
            }
        }

		return BookFinishAction.BROADCAST_AND_CONSUME;
	}

	private static void processBlock(Block block)
	{
		if (block.getType() == Material.CROPS || block.getType() == Material.CARROT || block.getType() == Material.POTATO)
			block.setData((byte) 7);
		else if (block.getType() == Material.NETHER_WARTS)
			block.setData((byte) 3);
		else if (block.getType() == Material.MELON_STEM)
		{
            if (block.getData() < 7)
            {
                block.setData((byte) 7);
            }
            else
            {
                for (BlockFace face : new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH })
                {
                    final Block neighbour = block.getRelative(face);
                    if (neighbour != null && neighbour.isEmpty())
                    {
                        Block downBlock = neighbour.getRelative(BlockFace.DOWN);
                        if (downBlock != null && (downBlock.getType() == Material.GRASS || downBlock.getType() == Material.DIRT || downBlock.getType() == Material.SOIL))
                        {
                            neighbour.setType(Material.MELON_BLOCK);
                            break;
                        }
                    }
                }
            }
		}
        else if (block.getType() == Material.PUMPKIN_STEM)
        {
            if (block.getData() < 7)
            {
                block.setData((byte) 7);
            }
            else
            {
                for (BlockFace face : new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH })
                {
                    final Block neighbour = block.getRelative(face);
                    if (neighbour != null && neighbour.isEmpty())
                    {
                        Block downBlock = neighbour.getRelative(BlockFace.DOWN);
                        if (downBlock != null && (downBlock.getType() == Material.GRASS || downBlock.getType() == Material.DIRT || downBlock.getType() == Material.SOIL))
                        {
                            neighbour.setType(Material.PUMPKIN);
                            break;
                        }
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

    private static class GrowthBookWoker extends BlockWorker
    {
        private int x;
        private int y;
        private int z;

        private Block startBlock;

        public GrowthBookWoker(Block startBlock)
        {
            this.startBlock = startBlock;
        }

        @Override
        public void init()
        {
            x = -EFFECT_AREA_HORIZONTAL_RADIUS;
            y = -1;
            z = -EFFECT_AREA_HORIZONTAL_RADIUS;
        }

        @Override
        public void onDone()
        {

        }

        @Override
        public long getTaskSize()
        {
            //X and Z size = RADIUS * 2 + 1 block for player position
            // Y size = height above player + 1 block for player position + 1 block for one layer below player
            return (EFFECT_AREA_HORIZONTAL_RADIUS * 2 + 1) * (EFFECT_AREA_HORIZONTAL_RADIUS * 2 + 1) * (EFFECT_AREA_HEIGHT_ABOVE_PLAYER + 2);
        }

        @Override
        public long work(long amountToWork)
        {
            int worked = 0;

            outmost: for (; x <= EFFECT_AREA_HORIZONTAL_RADIUS; x++)
            {
                for (; y <= EFFECT_AREA_HEIGHT_ABOVE_PLAYER + 1; y++)
                {
                    if (y > EFFECT_AREA_HEIGHT_ABOVE_PLAYER)
                    {
                        y = -1;
                        break;
                    }

                    for (; z <= EFFECT_AREA_HORIZONTAL_RADIUS + 1; z++)
                    {
                        if (z > EFFECT_AREA_HORIZONTAL_RADIUS)
                        {
                            z = -EFFECT_AREA_HORIZONTAL_RADIUS;
                            break;
                        }

                        worked++;

                        Block block = startBlock.getRelative(x, y, z);

                        processBlock(block);

                        if (worked >= amountToWork)
                        {
                            break  outmost;
                        }
                    }
                }
            }

            return worked;
        }
    }


    @Override
	protected BookFinishAction onActivateEntity(SpellbookItem item, PlayerInteractEntityEvent event) {
		return BookFinishAction.NOTHING;
	}
}
