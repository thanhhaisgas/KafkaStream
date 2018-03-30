package tracking;

import com.ip2location.IPResult;
import rfx.core.util.DateTimeUtil;
import rfx.core.util.LogUtil;
import rfx.core.util.StringUtil;
import tracking.Models.AdData;
import tracking.Models.ContentMetaData;
import tracking.Utils.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParsingAdDataLogOrigin {
    private HashMap<String, Object> data;

    public ParsingAdDataLogOrigin(HashMap<String, Object> data) {
        this.data = data;
    }

    public void Parsing(){
        try {
            // Contain tracking params
            // Example: metric=view100&adid=2044&beacon=2pzgzhzj2pzhzjznzn2pzj2pzj&t=1522120927
            String query = (String) this.data.get(tracking.Tuple.QUERY);

            // Time on server. It was calculated on the first time server receive the event
            int loggedTime = (int) this.data.get(tracking.Tuple.LOGGEDTIME);

            // Useragent
            String userAgent = (String) this.data.get(tracking.Tuple.USERAGENT);

            // remote ip
            String ip = (String) this.data.get(tracking.Tuple.IP);

            // Cookie
            String cookie = (String) this.data.get(tracking.Tuple.COOKIE);

            // Kafka partition from producer
            int partitionId = (int) this.data.get(tracking.Tuple.PARTITION_ID);
            if (StringUtil.isEmpty(query)) {
                return;
            }

            Map<String, List<String>> params = BeaconUtil.getQueryMap(query);
            int creativeId = StringUtil.safeParseInt(BeaconUtil.getParam(params, "adid", ""));
            String metric = BeaconUtil.getParam(params, "metric");
            String beacon = BeaconUtil.getParam(params, "beacon");
            String cacheBuster = BeaconUtil.getParam(params, "cb");

            if (StringUtil.isEmpty(beacon)) {
                System.out.println("Beacon is Empty!");
                return;
            }

            // Time in tracking/delivery (not time on server) api
            int clientTime = StringUtil.safeParseInt(BeaconUtil.getParam(params, "t"));

            // parsing beacon ad data to object
            AdData adData = AdsPlayBeaconUtil.parseBeaconData(metric, loggedTime, beacon, cacheBuster);

            // Set loggedHour, loggedDate
            adData.setLoggedHour(DateTimeUtils.convertUnitTimeToHour(loggedTime, true));
            adData.setLoggedDate(DateTimeUtils.convertUnitTimeToDateTimeStamp(loggedTime, true));

            // time from get Ad Info to event
            int timeEngaged = loggedTime - clientTime;
            // System.out.println("timeEngaged = " + timeEngaged);

            if (timeEngaged >= 0 && creativeId > 0 && adData != null) {

                // verified OK
                String url = BeaconUtil.extractRefererURL(cookie);
                long locationId = 0;

                // [API]: get location from remote ip
                IPResult ipResult = IP2LocationUtil.find(ip);
                if (ipResult != null) {
                    // System.out.println(ip + " #### location
                    // "+ipResult.getCity());
                    // locationId =
                    // IP2LocationUtil.updateAndGetLocationId(loggedTime,
                    // ipResult);
                    adData.setCity(ipResult.getCity());
                    adData.setCountry(ipResult.getCountryLong());
                }
                // else {
                // System.out.println(ip + " #### location NOT FOUND");
                // }
                adData.setPartitionId(partitionId);
                adData.setCreativeId(creativeId);
                adData.setLocationId(locationId);
                adData.setIp(ip);
                adData.setUrl(url);
                adData.setTimeEngaged(timeEngaged);
                adData.setUserAgent(userAgent);

                // processing Device Info
                int placementId = adData.getPlacement();

                // [API] - Get device info from useragent and placement ID
                DeviceParserUtil.DeviceInfo deviceInfo = DeviceParserUtil.parseWithCache(userAgent, placementId);
                adData.setDeviceName(deviceInfo.deviceName);
                adData.setDeviceOs(deviceInfo.deviceOs);
                adData.setDeviceType(deviceInfo.deviceType);

                // here to get contentID from url
                String contentID = "-";
                if (url.contains("/xem-video/") || url.contains("/player/")) {
                    contentID = RequestFptPlayUtil.getContentIDIncludeSmartTv(url);
                }

                if ("-".equals(contentID) || contentID.isEmpty()){
                    // get contentId from mobile device
                    System.out.println("---------query " + query);
                    contentID = BeaconUtil.getParam(params, "ctid", "-");
                }

                if ("-".equals(contentID)){
                    contentID = BeaconUtil.getParam(params, "cid", "-");
                }

                if ("-".equals(contentID) && placementId == 202) {
                    contentID = ParseUtil.getContentIdFromURL(url);
                }

                LogUtil.i(this.getClass().getName(), "Found contentID:" + contentID + ", from AdID:" + creativeId, true);

                // add content id data
                adData.setContentId(contentID);

                String catID = BeaconUtil.getParam(params, "catid", "-");;


                try {
                    if ("-".equals(catID)){
                        // [API] get list structure/categories from contentId vod
                        ContentMetaData contentMetaData = ContentDataUtil.getContentMetaData(contentID);
                        List<String> listCategories = contentMetaData.getListStructureId();
                        String contextKeywords = String.join(", ", listCategories);
                        adData.setContextKeyword(contextKeywords);
                    } else {
                        adData.setContextKeyword(catID);
                    }
                } catch (Exception e){
                    LogUtil.error(e);
                }

                System.out.println("----------------------------------------------");
                // System.out.println("url "+ url);
                System.out.println("loggedTime " + DateTimeUtil.formatDateHourMinute(new Date(loggedTime * 1000l)));
                // System.out.println("clientTime "+
                // DateTimeUtil.formatDateHourMinute(new Date(clientTime *
                // 1000l)));
                System.out.println("----------------------------------------------");
            }

            // System.out.println(query);

        } catch (IllegalArgumentException e) {
            LogUtil.e("ParsingPageviewImpressionLog occurs", e.getMessage());
        } catch (Exception e) {
            LogUtil.e("ParsingPageviewImpressionLog occurs", e.getClass().getName() + " - " + e.getMessage());
        }
    }
}
