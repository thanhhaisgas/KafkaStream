package tracking.Utils;

import rfx.core.util.SecurityUtil;
import rfx.core.util.StringUtil;
import tracking.Models.AdData;

public class AdsPlayBeaconUtil {
    private static final String BEACON_DELIMITER = "\\|";

    public static String decryptBeaconValue(String beacon) {
        return SecurityUtil.decryptBeaconValue(beacon);
    }

    public static AdData parseBeaconData(String metric, int loggedTime, String encodedStr) {
        return parseBeaconData(metric, loggedTime, encodedStr, "");
    }

    public static AdData parseBeaconData(String metric, int loggedTime, String encodedStr, String cacheBuster) {
        String raw = decryptBeaconValue(encodedStr);
        // System.out.println(raw);
        String[] toks = raw.split(BEACON_DELIMITER);
        // System.out.println(new ArrayList<String>(Arrays.asList(toks)));

        if (toks.length == 5) {
            String uuid = toks[0];
            if (StringUtil.isEmpty(uuid)) {
                uuid = "0";
            }
            int placement = StringUtil.safeParseInt(toks[1]);
            int adId = StringUtil.safeParseInt(toks[2]);
            int campaignId = StringUtil.safeParseInt(toks[3]);
            int flightId = StringUtil.safeParseInt(toks[4]);
            // Separate tvc with banner
            String finalMetricName = (cacheBuster != null && !cacheBuster.isEmpty()) ? metric + "-" + "banner" : metric;
            return new AdData(finalMetricName, loggedTime, uuid, placement, adId, campaignId, flightId);
        }
        return null;
    }
}
