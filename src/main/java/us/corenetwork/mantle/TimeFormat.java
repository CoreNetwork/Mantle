package us.corenetwork.mantle;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeFormat
{
    public static String formatTimeSeconds(int seconds)
    {
        if (seconds > 60)
            return formatTimeMinutes((int) Math.round(seconds / 60.0));

        String out = Integer.toString(seconds).concat(" second");
        if (seconds != 1)
            out = out.concat("s");
        return out;
    }

    public static String formatTimeMinutes(int minutes)
    {
        if (minutes > 90)
            return formatTimeHours((int) Math.round(minutes / 60.0));

        String out = Integer.toString(minutes).concat(" minute");
        if (minutes != 1)
            out = out.concat("s");
        return out;

    }


    private static String formatTimeHours(int hours)
    {
        if (hours > 36)
            return formatTimeDays((int) Math.round(hours / 24));

        String out = Integer.toString(hours).concat(" hour");
        if (hours != 1)
            out = out.concat("s");
        return out;
    }

    private static String formatTimeDays(int days)
    {
        String out = Integer.toString(days).concat(" day");
        if (days != 1)
            out = out.concat("s");
        return out;

    }


    public static String formatDate(long cas)
	{
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(cas * 1000);
		int dan = c.get(Calendar.DAY_OF_MONTH);
		
		SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
		
		return dan + getDayOfMonthSuffix(dan) + " " + monthFormat.format(new Date(cas * 1000));
		
	}
	
	private static String getDayOfMonthSuffix(final int n) {
	    if (n >= 11 && n <= 13) {
	        return "th";
	    }
	    switch (n % 10) {
	        case 1:  return "st";
	        case 2:  return "nd";
	        case 3:  return "rd";
	        default: return "th";
	    }
	}
}
