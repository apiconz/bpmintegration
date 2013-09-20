package bpm.rest.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Utils {

	public static Calendar parseTimestamp(String ts) {
		// 2012-04-12T23:09:32Z
		Calendar calendar = null;
		if (ts != null) {
			ts = ts.replace("Z", "");
			String tokens[] = ts.split("T");
			String dateTks[] = tokens[0].split("-");
			String timeTks[] = tokens[1].split(":");
			calendar = Calendar.getInstance();
			calendar.clear();
			calendar.set(Integer.parseInt(dateTks[0]),
					Integer.parseInt(dateTks[1]) - 1,
					Integer.parseInt(dateTks[2]), Integer.parseInt(timeTks[0]),
					Integer.parseInt(timeTks[1]), Integer.parseInt(timeTks[2]));
		}
		return calendar;
	}

	public static String convertToDateString(Calendar calendar, String pattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		String strDate = null;
		if (calendar != null) {
			strDate = sdf.format(calendar.getTime());
		}
		return strDate;
	}

	public static String convertToDateString(Calendar calendar) {
		// return convertToDateString(calendar, "MM/dd/yyyy '-' HH:mm:ss z");
		return convertToDateString(calendar, "MM/dd/yyyy '-' HH:mm:ss");
	}

}
