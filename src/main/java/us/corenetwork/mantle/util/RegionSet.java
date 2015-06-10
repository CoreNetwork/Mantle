package us.corenetwork.mantle.util;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.awt.*;
import java.util.ArrayList;

public class RegionSet extends ArrayList<ProtectedRegion>
{
    public boolean isInsideAnyRegion(int x, int y, int z)
    {
        for (ProtectedRegion rectangle : this)
        {
            if (rectangle.contains(x, y, z))
                return true;
        }

        return false;
    }
}
