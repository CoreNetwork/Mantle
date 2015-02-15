package us.corenetwork.mantle.portals;

import org.bukkit.Material;
import us.core_network.cornel.blocks.SchematicBlock;

public class PortalSchematic
{
    public static final SchematicBlock[] portal = new SchematicBlock[] {
            //Front view:
            // OOOO
            // OPPO
            // OPPO
            // OXPO
            // OOOO
            // O = Obsidian
            // P = Portal
            // X = Origin portal block

            //Side view:
            // AOA
            // AOA
            // AOA
            // AOA
            // OOO
            // A = Air

            //Top frame
            new SchematicBlock(-1, 3, 0, Material.OBSIDIAN),
            new SchematicBlock(0, 3, 0, Material.OBSIDIAN),
            new SchematicBlock(1, 3, 0, Material.OBSIDIAN),
            new SchematicBlock(2, 3, 0, Material.OBSIDIAN),

            //Bottom frame
            new SchematicBlock(-1, -1, 0, Material.OBSIDIAN),
            new SchematicBlock(0, -1, 0, Material.OBSIDIAN),
            new SchematicBlock(1, -1, 0, Material.OBSIDIAN),
            new SchematicBlock(2, -1, 0, Material.OBSIDIAN),

            //Left side
            new SchematicBlock(-1, -1, 0, Material.OBSIDIAN),
            new SchematicBlock(-1, 0, 0, Material.OBSIDIAN),
            new SchematicBlock(-1, 1, 0, Material.OBSIDIAN),
            new SchematicBlock(-1, 2, 0, Material.OBSIDIAN),

            //Right side
            new SchematicBlock(2, -1, 0, Material.OBSIDIAN),
            new SchematicBlock(2, 0, 0, Material.OBSIDIAN),
            new SchematicBlock(2, 1, 0, Material.OBSIDIAN),
            new SchematicBlock(2, 2, 0, Material.OBSIDIAN),

            //Obsidian ledge
            new SchematicBlock(0, -1, 1, Material.OBSIDIAN, true),
            new SchematicBlock(1, -1, 1, Material.OBSIDIAN, true),
            new SchematicBlock(0, -1, -1, Material.OBSIDIAN, true),
            new SchematicBlock(1, -1, -1, Material.OBSIDIAN, true),

            //Portal blocks
            new SchematicBlock(0, 0, 0, Material.PORTAL),
            new SchematicBlock(0, 1, 0, Material.PORTAL),
            new SchematicBlock(0, 2, 0, Material.PORTAL),
            new SchematicBlock(1, 0, 0, Material.PORTAL),
            new SchematicBlock(1, 1, 0, Material.PORTAL),
            new SchematicBlock(1, 2, 0, Material.PORTAL),

            //Air pockets around portal
            new SchematicBlock(0, 0, 1, Material.AIR),
            new SchematicBlock(0, 1, 1, Material.AIR),
            new SchematicBlock(0, 2, 1, Material.AIR),
            new SchematicBlock(1, 0, 1, Material.AIR),
            new SchematicBlock(1, 1, 1, Material.AIR),
            new SchematicBlock(1, 2, 1, Material.AIR),
            new SchematicBlock(0, 0, -1, Material.AIR),
            new SchematicBlock(0, 1, -1, Material.AIR),
            new SchematicBlock(0, 2, -1, Material.AIR),
            new SchematicBlock(1, 0, -1, Material.AIR),
            new SchematicBlock(1, 1, -1, Material.AIR),
            new SchematicBlock(1, 2, -1, Material.AIR)
    };
}
