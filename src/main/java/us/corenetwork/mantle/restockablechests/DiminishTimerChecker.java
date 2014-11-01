package us.corenetwork.mantle.restockablechests;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import us.corenetwork.mantle.IO;
import us.corenetwork.mantle.MLog;

public class DiminishTimerChecker implements Runnable{

	@Override
	public void run()
	{
		MLog.debug("Running DiminishTimerChecker!");
		int diminishVillage = RChestsModule.instance.storageConfig.getInt("DiminishVillage", 0) + RChestSettings.DIMINISH_CHECKER_INTERNVAL.integer();
		int diminishTotal = RChestsModule.instance.storageConfig.getInt("DiminishTotal", 0) + RChestSettings.DIMINISH_CHECKER_INTERNVAL.integer();
		
		
		if(diminishVillage >= RChestSettings.DIMINISH_RESTORE_INTERVAL_VILLAGE.integer())
		{
			diminishVillage = 0;
			updateDiminishVillageForEveryone();
		}
		
		if(diminishTotal >= RChestSettings.DIMINISH_RESTORE_INTERVAL_TOTAL.integer())
		{
			diminishTotal = 0;
			updateDiminishTotalForEveryone();
		}
		
		RChestsModule.instance.storageConfig.set("DiminishVillage", diminishVillage);
		RChestsModule.instance.storageConfig.set("DiminishTotal", diminishTotal);
		RChestsModule.instance.saveStorageYaml();
		MLog.debug("Finished DiminishTimerChecker!");
	}
	
	private void updateDiminishVillageForEveryone()
	{
		double toRestore = RChestSettings.DIMINISH_RESTORE_VILLAGE.doubleNumber();
		
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("UPDATE playerVillage SET diminishVillage = diminishVillage + ? WHERE diminishVillage < 1");
			statement.setDouble(1, toRestore);	
			statement.executeUpdate();
			statement.close();
			
			IO.getConnection().commit();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void updateDiminishTotalForEveryone()
	{
		double toRestore = RChestSettings.DIMINISH_RESTORE_TOTAL.doubleNumber();
		
		try
		{
			PreparedStatement statement = IO.getConnection().prepareStatement("UPDATE playerTotal SET diminishTotal = diminishTotal + ? WHERE diminishTotal < 1");
			statement.setDouble(1, toRestore);	
			statement.executeUpdate();
			statement.close();
			
			IO.getConnection().commit();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
