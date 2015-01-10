package us.corenetwork.mantle.restockablechests;

/**
 * Created by Ginaf on 2015-01-10.
 */
public class CompassDestinationHelper {
    public CompassDestination destination;
    public long timestamp;
    public CompassDestinationHelper(CompassDestination destination)
    {
        this.destination = destination;
        this.timestamp = System.currentTimeMillis();
    }
}
