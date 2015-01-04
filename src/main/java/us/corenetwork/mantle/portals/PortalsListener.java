package us.corenetwork.mantle.portals;

import java.util.HashSet;
import java.util.LinkedList;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.minecraft.server.v1_8_R1.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftVillager;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.PortalCreateEvent.CreateReason;
import org.bukkit.inventory.ItemStack;
import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.Util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class PortalsListener implements Listener {
	private HashMap<Block, FlintSteelData> flintSteelUsage = new HashMap<Block, FlintSteelData>();
    private HashMap<UUID, Block> lastPortalBlocks = new HashMap<UUID, Block>(); //In which portal block is entity currently located

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		Block clicked = event.getClickedBlock();
		Player player = event.getPlayer();
		ItemStack hand = event.getPlayer().getItemInHand();

		if (event.getAction() == Action.LEFT_CLICK_BLOCK)
		{
			return;
		}
		else if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;


		//Investigation tool for portals
		if (hand != null && hand.getTypeId() == PortalsSettings.INVESTIGATION_TOOL.integer())
		{
			Claim sourceClaim = GriefPrevention.instance.dataStore.getClaimAt(clicked.getLocation(), true, null);
			if (sourceClaim == null || sourceClaim.allowBuild(player, Material.STONE) == null)
			{
				Location destination = PortalUtil.getOtherSide(clicked).getLocation();
				Claim claim = GriefPrevention.instance.dataStore.getClaimAt(destination, true, null);

				if (claim != null && claim.allowBuild(player, Material.STONE) != null)
				{
					String owner = claim.getOwnerName();
					if (owner == null)
						owner = "ADMIN";
					
					String message = PortalsSettings.MESSAGE_CANT_MAKE_PORTAL.string();
					message = message.replace("<OtherDimension>", clicked.getWorld().getEnvironment() == Environment.NORMAL ? "Nether" : "Overworld");
					message = message.replace("<Owner>", owner);

					Util.Message(message, player);
				}
				else
				{
					Util.Message(PortalsSettings.MESSAGE_CAN_MAKE_PORTAL.string(), player);
				}
			}
		}

		if (event.isCancelled())
			return;

		//Flint and steel detection for portals
		if (hand != null && hand.getType() == Material.FLINT_AND_STEEL)
		{
			Block target = getLastBlock(player);
			if (target != null)
			{
				FlintSteelData data = new FlintSteelData();
				data.timestamp = System.currentTimeMillis();
				data.player = player.getUniqueId();
				
				
				flintSteelUsage.put(target, data);
			}
			
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPortalCreate(PortalCreateEvent event)
	{
		if (event.getReason() == CreateReason.FIRE)
		{
			//Prevent creating portal out of boundaries
			int minX;
			int maxX;
			int minZ;
			int maxZ;
			int minY;
			int maxY;
			if (event.getBlocks().get(0).getWorld().getEnvironment() == Environment.NETHER)
			{
				minX = PortalsSettings.NETHER_MIN_X.integer();
				maxX = PortalsSettings.NETHER_MAX_X.integer();
				minZ = PortalsSettings.NETHER_MIN_Z.integer();
				maxZ = PortalsSettings.NETHER_MAX_Z.integer();
				minY = PortalsSettings.NETHER_MIN_Y.integer();
				maxY = PortalsSettings.NETHER_MAX_Y.integer();

			}
			else
			{
				minX = PortalsSettings.OVERWORLD_MIN_X.integer();
				maxX = PortalsSettings.OVERWORLD_MAX_X.integer();
				minZ = PortalsSettings.OVERWORLD_MIN_Z.integer();
				maxZ = PortalsSettings.OVERWORLD_MAX_Z.integer();
				minY = PortalsSettings.OVERWORLD_MIN_Y.integer();
				maxY = PortalsSettings.OVERWORLD_MAX_Y.integer();
			}


			for (Block b : event.getBlocks())
			{
				if (b.getX() < minX || b.getX() > maxX || b.getZ() < minZ || b.getZ() > maxZ)
				{
					Util.placeSign(PortalUtil.findBestSignLocation(event.getBlocks()), PortalsSettings.SIGN_PORTAL_OUT_OF_BOUNDARIES_TOO_FAR.string());

					event.setCancelled(true);
					return;
				}
				if (b.getY() < minY)
				{
					Util.placeSign(PortalUtil.findBestSignLocation(event.getBlocks()), PortalsSettings.SIGN_PORTAL_OUT_OF_BOUNDARIES_TOO_LOW.string());

					event.setCancelled(true);
					return;
				}
				if  (b.getY() > maxY)
				{
					Util.placeSign(PortalUtil.findBestSignLocation(event.getBlocks()), PortalsSettings.SIGN_PORTAL_OUT_OF_BOUNDARIES_TOO_HIGH.string());

					event.setCancelled(true);
					return;
				}

			}

			int minYBlock = 30000;
			int minXBlock = 30000;
			int maxXBlock = -30000;
			int minZBlock = 30000;
			int maxZBlock = -30000;

			for(Block b : event.getBlocks())
			{
				if (b.getY() < minYBlock)
					minYBlock = b.getY();
				if (b.getZ() < minZBlock)
					minZBlock = b.getZ();
				if (b.getX() < minXBlock)
					minXBlock = b.getX();
				if (b.getZ() > maxZBlock)
					maxZBlock = b.getZ();
				if (b.getX() > maxXBlock)
					maxXBlock = b.getX();
			}

			int rotation = minXBlock == maxXBlock ? 1 : 0;

			List<Block> listOfBlocksToCheck = new LinkedList<>();
			List<Block> listOfOnTheOtherSide = new LinkedList<>();

			//move one up to represent purplish portal block
			minYBlock++;
			if(rotation == 0)
			{
				for(int x = minXBlock + 1; x < maxXBlock;x++)
				{
					listOfBlocksToCheck.add(event.getWorld().getBlockAt(x, minYBlock, maxZBlock));
				}
			}
			else
			{
				for(int z = minZBlock + 1; z < maxZBlock; z++)
				{
					listOfBlocksToCheck.add(event.getWorld().getBlockAt(maxXBlock, minYBlock, z));
				}
			}


			Set<Claim> claims = new HashSet<>();
			//Check first travel, if any block and area around it that will be created contains any foreign claims
			for(Block block : listOfBlocksToCheck)
			{
				Block otherSide = PortalUtil.getOtherSideExact(block);
				listOfOnTheOtherSide.add(otherSide);
				claims.addAll(getClaimsAround(otherSide));
			}

			//Here are the blocks that might host the portal on the other side. Now for each we should check other-other side, to see if
			//portal created on other side could possibly lead to other-other side in someones claim
			for(Block otherSideBlock : listOfOnTheOtherSide)
			{
				Block otherOtherSide = PortalUtil.getOtherSideExact(otherSideBlock);
				claims.addAll(getClaimsAround(otherOtherSide));
			}

			if (claims.size() > 0)
			{
				FlintSteelData creator = null;

				for (Block b : event.getBlocks())
				{					
					creator = flintSteelUsage.get(b);
					if (creator != null)
						break;
				}
								
				if (creator == null)
				{
					Util.placeSign(PortalUtil.findBestSignLocation(event.getBlocks()), PortalsSettings.SIGN_OVERLAP_CLAIM.string());

					event.setCancelled(true);
					return;
				}

				Player player = Bukkit.getServer().getPlayer(creator.player);

				for (Claim claim : claims)
				{
					if (player == null || claim.allowBuild(player, Material.STONE) != null)
					{
						Util.placeSign(PortalUtil.findBestSignLocation(event.getBlocks()), PortalsSettings.SIGN_OVERLAP_CLAIM.string());

						event.setCancelled(true);
						return;
					}
				}
			}
		}
	}	


	private List<Claim> getClaimsAround(Block block)
	{
		List<Claim> claims = new LinkedList<Claim>();

		//Search for 5x3x5 area around block
		for (int x = -2; x <= 2; x++)
		{
			for (int y = -1; y <= 2; y++)
			{
				for (int z = -2; z <= 2; z++)
				{
					Claim claim = GriefPrevention.instance.dataStore.getClaimAt(block.getRelative(x,y,z).getLocation(), true, null);
					if (claim != null)
						claims.add(claim);
				}
			}
		}
		return claims;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onPlayerPortal(final PlayerPortalEvent event)
	{
		//Controlled nether portals
		if (event.getCause() == TeleportCause.NETHER_PORTAL)
		{
            Block portalBlock = lastPortalBlocks.get(event.getPlayer().getUniqueId());
            lastPortalBlocks.remove(event.getPlayer().getUniqueId());

			Location destination = PortalUtil.processTeleport(event.getPlayer(), portalBlock);
			event.setTo(destination);
			event.useTravelAgent(false);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onEntityPortal(EntityPortalEvent event)
	{
		if (event.getEntityType() == EntityType.VILLAGER)
		{
			int profession = ((CraftVillager) event.getEntity()).getHandle().getProfession();
			if (profession == 5)
			{
				event.setCancelled(true);
				return;
			}
		}

        Block portalBlock = lastPortalBlocks.get(event.getEntity().getUniqueId());
        lastPortalBlocks.remove(event.getEntity().getUniqueId());


        Location destination = PortalUtil.processTeleport(event.getEntity(), portalBlock);
		event.setTo(destination);
		event.useTravelAgent(false);
		
	}
		
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onPlayerPortalFinal(final PlayerPortalEvent event)
	{
		final Location to = event.getTo().clone();
		
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(MantlePlugin.instance, new Runnable() {
			@Override
			public void run() {
				Location loc = event.getPlayer().getLocation();
				loc.setYaw(to.getYaw());
				
				event.getPlayer().teleport(to);
			}
		}, 1);

		
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(MantlePlugin.instance, new Runnable() {
			@Override
			public void run() {
				event.getPlayer().teleport(to);
			}
		}, 10);
		
		//Enable going back instantly
		if (event.getPlayer().getGameMode() != GameMode.CREATIVE)
		{
			Bukkit.getScheduler().runTask(MantlePlugin.instance, new Runnable() {
				@Override
				public void run() {
					EntityPlayer nmsPlayer = ((CraftPlayer) event.getPlayer()).getHandle();
					
					try
					{
						Field portalCounterField = net.minecraft.server.v1_8_R1.Entity.class.getDeclaredField("al");
						portalCounterField.setAccessible(true);

						portalCounterField.set(nmsPlayer, 0);
					}
					catch (Exception e)
					{
						MLog.severe("Error while applying portal fixes! Go bug matejdro!");
						e.printStackTrace();
					}
					
					nmsPlayer.portalCooldown = 0;
				}
			});
		}
	}

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityEnterPortal(EntityPortalEnterEvent event)
    {
        lastPortalBlocks.put(event.getEntity().getUniqueId(), event.getLocation().getBlock());
    }
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(final BlockBreakEvent event)
	{
		final Block block = event.getBlock();

		//Do not drop colored sign
		if (block.getState() instanceof Sign)
		{
			Sign sign = (Sign) block.getState();

			String colorSymbol = "\u00A7";
			for (String line : sign.getLines())
			{
				if (line.contains(colorSymbol))
				{
					block.setType(Material.AIR);
					event.setCancelled(true);
					return;
				}
			}
		} 
	}

	private static Block getLastBlock(Player player)
	{
		List<Block> blocks = player.getLastTwoTargetBlocks(null, 10);
		if (blocks.size() == 0)
			return null;
		return blocks.get(0);
	}
}
