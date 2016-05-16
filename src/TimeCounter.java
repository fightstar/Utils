

import java.util.Date;

public class TimeCounter {

/*
 	long startTime = SystemClock.elapsedRealtime();
	long endTime = SystemClock.elapsedRealtime();
	long elapsedMilliSeconds = endTime - startTime;
	double elapsedSeconds = elapsedMilliSeconds / 1000.0;
*/
	
	public static long getCurrentTime ()
	{
	return new Date().getTime(); // start time
	}
	
	public static String getDifference (long difference)
	{
//		long difference = lEndTime - lStartTime; // check different
		Common.print("Elapsed seconds: " + difference/1000);
		Common.print("Elapsed minutes: " + (difference/1000)/60);
		return "" + (difference/1000)/60;
	}

}
