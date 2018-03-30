package tracking.Utils;

import com.ip2location.IP2Location;
import com.ip2location.IPResult;
import rfx.core.configs.RedisConfigs;
import rfx.core.configs.WorkerConfigs;
import rfx.core.nosql.jedis.RedisCommand;
import rfx.core.stream.util.HashUtil;
import rfx.core.util.CharPool;
import rfx.core.util.DateTimeUtil;
import rfx.core.util.StringUtil;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.exceptions.JedisException;

import java.util.Date;

public class IP2LocationUtil {
    public static final int AFTER_15_DAYS = 60 * 60 * 24 * 15;

    public static final int AFTER_7_DAYS = 60 * 60 * 24 * 7;

    public static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd";

    private static final String YYYY_MM_DD_HH = "yyyy-MM-dd-HH";

    public static final String DATE_HOUR_FORMAT_PATTERN = YYYY_MM_DD_HH;

    final public static ShardedJedisPool jedisLocationDataStats = RedisConfigs.load().get("locationDataStats")
            .getShardedJedisPool();
    private static final String OK = "OK";
    static final IP2Location loc = new IP2Location();

    static {
        loc.IPDatabasePath = WorkerConfigs.load().getCustomConfig("fullIP2LocationPath");
        System.out.println("loaded IPDatabasePath: " + loc.IPDatabasePath);
    }

    public static IPResult find(String ip) {
        try {
            IPResult rec = loc.IPQuery(ip);
            if (OK.equals(rec.getStatus())) {
                return rec;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static long updateLocationStats(int unixtime, final long locId, String metric) {
        Date date = new Date(unixtime * 1000L);
        final String dateStr = DateTimeUtil.formatDate(date, DATE_FORMAT_PATTERN);
        final String dateHourStr = DateTimeUtil.formatDate(date, DATE_HOUR_FORMAT_PATTERN);

        new RedisCommand<Boolean>(jedisLocationDataStats) {
            @Override
            protected Boolean build() throws JedisException {
                String keyD = "l:" + dateStr;
                String keyH = "l:" + dateHourStr;
                Pipeline p = jedis.pipelined();
                if ("plv".equals(metric)) {
                    String keyLocStats = "lcs:" + locId;
                    p.incr(keyLocStats);
                }
                p.hincrBy(keyD, locId + "-" + metric, 1L);
                p.expire(keyD, AFTER_15_DAYS);
                p.hincrBy(keyH, locId + "-" + metric, 1L);
                p.expire(keyH, AFTER_7_DAYS);
                p.sync();
                return true;
            }
        }.execute();

        return locId;
    }

    public static long updateAndGetLocationId(int unixtime, IPResult ipResult) {
        final String country = ipResult.getCountryLong();
        final String city = ipResult.getCity();
        final String loc = country + CharPool.POUND + city + CharPool.POUND + ipResult.getLatitude() + CharPool.POUND
                + ipResult.getLongitude();
        final long locId = HashUtil.hashUrl128Bit(loc);
        new RedisCommand<Boolean>(jedisLocationDataStats) {
            @Override
            protected Boolean build() throws JedisException {
                String keyLoc = "lc:" + locId;
                String rs = jedis.get(keyLoc);
                if (StringUtil.isEmpty(rs)) {
                    jedis.set(keyLoc, loc);
                }
                return true;
            }
        }.execute();

        return locId;
    }
}
