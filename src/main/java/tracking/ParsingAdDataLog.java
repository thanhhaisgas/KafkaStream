package tracking;

import com.ip2location.IPResult;
import rfx.core.util.StringUtil;
import tracking.Models.AdData;
import tracking.Utils.BeaconUtil;
import tracking.Utils.IP2LocationUtil;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class ParsingAdDataLog {
    private HashMap<String, Object> data;
    private AdData adData;
    private HashMap<String, Object> mapTokens;
    public static final String METRIC = "metric";
    public static final String ADID = "adid";
    public static final String PLACEMENT_ID = "placementId";
    public static final String CAMPAIGN_ID = "campaignId";
    public static final String FLIGHT_ID = "flightId";
    public static final String CONTENT_ID = "contentId";
    public static final String BUY_TYPE = "buyType";
    public static final String TIME = "t";
    public static final String IP_CITY = "ipCity";
    public static final String IP_COUNTRY = "ipCountry";
    public static final String DEVICE_NAME = "deviceName";
    public static final String DEVICE_OS = "deviceOs";
    public static final String DEVICE_TYPE = "deviceType";

    public ParsingAdDataLog(HashMap<String, Object> data) {
        this.data = data;
        adData = new AdData();
        mapTokens = new HashMap<>();
        parsingIp((String) this.data.get(Tuple.IP));
        parsingQuery((String) this.data.get(Tuple.QUERY));
        parsingDevice((String) this.data.get(Tuple.USERAGENT), StringUtil.safeParseInt(this.mapTokens.get(ParsingAdDataLog.PLACEMENT_ID)));
        printParam();
    }

    private void parsingQuery(String logTracking){
        String[] tokens = logTracking.split("[\\=\\&]");
        for (int i = 0; i < tokens.length; i+=2) {
            switch (i){
                case 0:
                    addField(ParsingAdDataLog.METRIC, StringUtil.safeString(tokens[i+1]));
                    break;
                case 2:
                    addField(ParsingAdDataLog.ADID, StringUtil.safeParseInt(tokens[i+1]));
                    break;
                case 4:
                    addField(ParsingAdDataLog.PLACEMENT_ID, StringUtil.safeParseInt(tokens[i+1]));
                    break;
                case 6:
                    addField(ParsingAdDataLog.CAMPAIGN_ID, StringUtil.safeParseInt(tokens[i+1]));
                    break;
                case 8:
                    addField(ParsingAdDataLog.FLIGHT_ID, StringUtil.safeParseInt(tokens[i+1]));
                    break;
                case 10:
                    addField(ParsingAdDataLog.CONTENT_ID, StringUtil.safeString(tokens[i+1]));
                    break;
                case 12:
                    addField(ParsingAdDataLog.BUY_TYPE, StringUtil.safeString(tokens[i+1]));
                    break;
                case 14:
                    addField(ParsingAdDataLog.TIME, StringUtil.safeParseInt(tokens[i+1]));
                    break;
            }
        }
    }

    private void parsingIp(String ip){
        IPResult ipResult = IP2LocationUtil.find(ip);
        if (ipResult!=null){
            addField(ParsingAdDataLog.IP_CITY, ipResult.getCity());
            addField(ParsingAdDataLog.IP_COUNTRY, ipResult.getCountryLong());
        }
    }

    private void parsingDevice(String userAgent, Integer placementId){
        try {
            DeviceParserUtil.DeviceInfo deviceInfo = DeviceParserUtil.parseWithCache(userAgent, placementId);
            addField(ParsingAdDataLog.DEVICE_NAME, StringUtil.safeString(deviceInfo.deviceName));
            addField(ParsingAdDataLog.DEVICE_OS, StringUtil.safeString(deviceInfo.deviceOs));
            addField(ParsingAdDataLog.DEVICE_TYPE, StringUtil.safeParseInt(deviceInfo.deviceType));
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void parsingCookie(String cookie){
//        String url = BeaconUtil.extractRefererURL(cookie);
//        String contentID = "-";
//        if (url.contains("/xem-video/") || url.contains("/player/")) {
//            contentID = RequestFptPlayUtil.getContentIDIncludeSmartTv(url);
//        }
//
//        if ("-".equals(contentID) || contentID.isEmpty()){
//            // get contentId from mobile device
//            System.out.println("---------query " + query);
//            contentID = BeaconUtil.getParam(params, "ctid", "-");
//        }
//
//        if ("-".equals(contentID)){
//            contentID = BeaconUtil.getParam(params, "cid", "-");
//        }
//
//        if ("-".equals(contentID) && placementId == 202) {
//            contentID = ParseUtil.getContentIdFromURL(url);
//        }
    }

    private void addField(String key, Object value){
        mapTokens.put(key,value);
    }

    private void printParam(){
        for (HashMap.Entry<String, Object> entry : mapTokens.entrySet()) {
            String key = entry.getKey().toString();
            Object value = entry.getValue();
            System.out.println(key +"  -  "+ value);
        }
    }
}
