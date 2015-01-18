package us.corenetwork.mantle.portals;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

/**
 * Created by Matej on 1.1.2015.
 */
public class PortalInfo
{
    public Block entryBlock;

    //Total size
    public int sizeX;
    public int sizeZ;

    //Portal blocks left in direction from entry block
    public int portalBlocksLeftSouth;
    public int portalBlocksLeftNorth;
    public int portalBlocksLeftWest;
    public int portalBlocksLeftEast;

    public int orientation;


    public static PortalInfo getPortalInfo(Block block)
    {
        block = PortalUtil.getFarthestPortalBlock(block, BlockFace.DOWN);

        PortalInfo info = new PortalInfo();
        info.sizeX = 1;
        info.sizeZ = 1;
        info.orientation = PortalUtil.getPortalBlockOrientation(block);

        Block secondBlock = block;
        if (info.orientation == 1)
        {
            //Find size in Z-
            while (true)
            {
                Block neighbour = secondBlock.getRelative(BlockFace.SOUTH);
                if (neighbour.getType() != Material.PORTAL)
                    break;

                info.portalBlocksLeftSouth++;
                info.sizeZ++;

                secondBlock = neighbour;
            }

            //Find size in Z+
            secondBlock = block;
            while (true)
            {
                Block neighbour = secondBlock.getRelative(BlockFace.NORTH);
                if (neighbour.getType() != Material.PORTAL)
                    break;

                info.portalBlocksLeftNorth++;
                info.sizeZ++;

                secondBlock = neighbour;
            }
        }
        else
        {
            //Find size in X+
            secondBlock = block;
            while (true)
            {
                Block neighbour = secondBlock.getRelative(BlockFace.EAST);
                if (neighbour.getType() != Material.PORTAL)
                    break;

                info.portalBlocksLeftEast++;
                info.sizeX++;

                secondBlock = neighbour;
            }

            secondBlock = block;
            //Find size in X-
            while (true)
            {
                Block neighbour = secondBlock.getRelative(BlockFace.WEST);
                if (neighbour.getType() != Material.PORTAL)
                    break;

                info.portalBlocksLeftWest++;
                info.sizeX++;

                secondBlock = neighbour;
            }
        }

        info.entryBlock = block;

        return info;
    }
}
