package us.corenetwork.mantle.treasurehunt;

import us.corenetwork.mantle.MantlePlugin;

public class THuntTimer implements Runnable {

	@Override
	public void run()
	{
		if (THuntModule.manager.canStart())
			THuntModule.manager.startHunt();
		THuntModule.manager.checkSkipDay();
	}

}
