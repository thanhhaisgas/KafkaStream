package tracking.Models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import rfx.core.util.StringUtil;

import java.sql.Timestamp;

public class RawlogData extends EventData {
    protected int tracking_id;
    // Time
    protected int loggedTime;
    protected int loggedHour;
    protected Timestamp loggedDate;

    protected int placement;
    protected String uuid = "0";
    protected int deviceType = DeviceParserUtil.PC;
    protected String ip = "0";
    protected long locationId = 0;
    protected String country = "0";
    protected String city = "0";
    protected String url = "0";
    protected String refererUrl = "0";
    protected String deviceName = "0";
    protected String deviceOs = "0";
    protected int partitionId;
    protected String userAgent;
    protected String contextKeyword;
    // ContentId
    protected String contentId;

    public RawlogData(int loggedTime, int placement, String uuid, String ip, int partitionId) {
        super();
        this.loggedTime = loggedTime;
        this.placement = placement;
        this.uuid = uuid;
        this.ip = ip;
        this.partitionId = partitionId;
    }

    public RawlogData(int loggedTime, int placement, String uuid, String ip, int partitionId, String contentId) {
        super();
        this.loggedTime = loggedTime;
        this.placement = placement;
        this.uuid = uuid;
        this.ip = ip;
        this.partitionId = partitionId;
        this.contentId = contentId;
    }

    public RawlogData() {
        super();
    }

    public void setPlacement(int placement) {
        if (this.placement == 0) {
            this.placement = placement;
        }
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public void setCountry(String country) {
        if (country != null)
            this.country = country;
    }

    public void setCity(String city) {
        if (city != null) {
            this.city = city;
        }
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        if (deviceName != null) {
            this.deviceName = deviceName;
        }
    }

    public int getPartitionId() {
        return partitionId;
    }

    public void setPartitionId(int partitionId) {
        this.partitionId = partitionId;
    }

    public String getDeviceOs() {
        return deviceOs;
    }

    public void setDeviceOs(String deviceOs) {
        if (deviceOs != null) {
            this.deviceOs = deviceOs;
        }
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setUrl(String url) {
        if (StringUtil.isNotEmpty(url)) {
            this.url = url;
        }
    }

    public void setRefererUrl(String refererUrl) {
        if (StringUtil.isNotEmpty(refererUrl)) {
            this.refererUrl = refererUrl;
        }
    }

    public int getDeviceType() {
        return deviceType;
    }

    public long getLocationId() {
        return locationId;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public void setLocationId(long locationId) {
        this.locationId = locationId;
    }

    public String getUrl() {
        if (StringUtil.isEmpty(url)) {
            url = "0";
        }
        return url;
    }

    public String getRefererUrl() {
        if (StringUtil.isEmpty(refererUrl)) {
            refererUrl = "0";
        }
        return refererUrl;
    }

    public String getUuid() {
        return uuid;
    }

    public int getPlacement() {
        return placement;
    }

    public int getLoggedTime() {
        return loggedTime;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getContextKeyword() {
        return contextKeyword;
    }

    public void setContextKeyword(String contextKeyword) {
        this.contextKeyword = contextKeyword;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public String toRawLogRecord() {
        return toLogRecordSplitByTab();
    }

    public int getTracking_id() {
        return tracking_id;
    }

    public void setTracking_id(int tracking_id) {
        this.tracking_id = tracking_id;
    }

    public int getLoggedHour() {
        return loggedHour;
    }

    public void setLoggedHour(int loggedHour) {
        this.loggedHour = loggedHour;
    }

    public Timestamp getLoggedDate() {
        return loggedDate;
    }

    public void setLoggedDate(Timestamp loggedDate) {
        this.loggedDate = loggedDate;
    }

    public void setLoggedTime(int loggedTime) {
        this.loggedTime = loggedTime;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    // Need to modify
    String toLogRecordSplitByTab() {
        return convertObjectToJSONString();
    }

    private String convertObjectToJSONString() {
        ObjectMapper mapper = new ObjectMapper();
        RawlogData obj = this;

        // Object to JSON in String
        String jsonInString = "";
        try {
            jsonInString = mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return jsonInString;
    }

    @Override
    public String toStringMessage() {
        return toRawLogRecord() ;
    }
}
