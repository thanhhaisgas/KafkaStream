package tracking;

import rfx.core.util.StringPool;
import rfx.core.util.StringUtil;

import java.util.HashMap;

public class Tuple {
    private HashMap<String, Object> data;

    public static final String PARTITION_ID = "partitionId";
    public static final String OFFSET_ID = "offsetId";
    public static final String TOPIC = "topic";
    public static final String USERAGENT = "useragent";
    public static final String IP = "ip";
    public static final String LOGGEDTIME = "loggedtime";
    public static final String COOKIE = "cookie";
    public static final String QUERY = "query";

    public Tuple(String ip, int loggedtime, String useragent, String query, String cookie, String topic, long offsetId, int parttionId) {
        data = new HashMap<>();
        this.addField(Tuple.IP, ip);
        this.addField(Tuple.LOGGEDTIME, loggedtime);
        this.addField(Tuple.USERAGENT, useragent);
        this.addField(Tuple.QUERY, query);
        this.addField(Tuple.COOKIE, cookie);
        this.addField(Tuple.TOPIC, topic);
        this.addField(Tuple.OFFSET_ID, offsetId);
        this.addField(Tuple.PARTITION_ID, parttionId);
    }

    public void addField(String key, Object value){
        data.put(key, value);
    }

    public HashMap<String, Object> getData() {
        return data;
    }
}
