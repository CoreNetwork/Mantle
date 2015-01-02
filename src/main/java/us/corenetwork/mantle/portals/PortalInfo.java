package us.corenetwork.mantle.portals;

import org.bukkit.block.Block;

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
}
