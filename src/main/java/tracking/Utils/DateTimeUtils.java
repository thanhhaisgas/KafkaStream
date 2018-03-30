package tracking.Utils;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateTimeUtils {
    public static Timestamp convertUnitTimeToDateTimeStamp(int unixTime, boolean isUnixTime) {
        Timestamp timestamp = new Timestamp(convertTimeToDate(unixTime, isUnixTime).getTime());
        return timestamp;
    }

    public static int convertUnitTimeToHour(int unixTime, boolean isUnixTime) {
        return convertTimeToHour(unixTime, true);
    }

    public static Date convertTimeToDate(int time, boolean isUnixTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());

        long longValue = Integer.valueOf(time).longValue();

        if (isUnixTime) {
            longValue = longValue * 1000L;
        }

        calendar.setTimeInMillis(longValue);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static int convertTimeToHour(int time, boolean isUnixTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());

        long longValue = Integer.valueOf(time).longValue();

        if (isUnixTime) {
            longValue = longValue * 1000L;
        }

        calendar.setTimeInMillis(longValue);
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public static void main(String[] args) {

        System.out.println(convertUnitTimeToDateTimeStamp(1506941495, true));

        System.out.println(convertUnitTimeToHour(1506941495, true));

    }
}
