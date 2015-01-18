package us.corenetwork.mantle.holograms;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matej on 13.12.2014.
 */
public class HologramsChunk
{
    protected List<Hologram> holograms = new ArrayList<Hologram>();
    protected boolean needsUpdating = false;

    public void update()
    {
        if (!needsUpdating)
            return;

        needsUpdating = false;

        for (Hologram hologram : holograms)
            hologram.updateEntities();
    }
}
