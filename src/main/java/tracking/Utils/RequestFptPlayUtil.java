package tracking.Utils;

import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.exceptions.JedisException;
import rfx.core.configs.RedisConfigs;
import rfx.core.nosql.jedis.RedisCommand;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class RequestFptPlayUtil {
    private static final String FPT_PLAY_API = "https://api.fptplay.net/api/v4.3_internal/vod/";

    private static final String INVALID_URL = "invalid_url";

    final public static ShardedJedisPool redisCacheContent =
            RedisConfigs.load().get("sponsoredContentStats").getShardedJedisPool();

    /**
     * Request to FPT Play API
     *
     * @param urlString make a request
     * @return Json String response
     * @throws Exception
     */
    public static String readURLFptPlayAPI(String videoID) throws Exception {

        // create request url to FPT Play API.
        String urlString = FPT_PLAY_API + videoID;
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            int read;
            char[] chars = new char[1024];
            StringBuffer buffer = new StringBuffer();
            while ((read = reader.read(chars)) != -1) {
                buffer.append(chars, 0, read);
            }
            return buffer.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     * Parsing Json object to VideoInfor object.
     *
     * @param json String
     * @return
     * @throws Exception
     */

    /**
     * Check input video ID is invalid or not.
     *
     * If video ID is invalid, return null and break.
     *
     * If video ID is valid, continue to request to server.
     *
     * @param videoId
     * @return
     */
    public static Boolean checkInvalidURL(String videoId) {

        // check invalid url from INVALID_URL redids
        Boolean invalidVideo = new RedisCommand<Boolean>(redisCacheContent) {
            @Override
            protected Boolean build() throws JedisException {
                // get json information from Redis.
                String jsonInfor = jedis.hget(INVALID_URL, videoId);
                if (jsonInfor != null) {
                    try {
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        }.execute();

        return invalidVideo;
    }

    /**
     * Get video meta-data using videoID.
     *
     * Save meta-data into Redis if infor does not exist.
     *
     * @param videoId video id.
     * @return VideoInfor after parsing JSON response.
     */

    public static String getContentIdnFromURL(String url) {
        String id = null;
        try {
            int idx1 = url.lastIndexOf("/") + 1;
            int idx2 = url.indexOf("-");
            if (idx1 > 1 && idx2 > idx1) {
                id = url.substring(idx1, idx2);
            } else if (idx1 > 1) {
                id = url.substring(idx1);
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
        return id;
    }


    public static String getContentIDIncludeSmartTv(String url) {

        String contentId = "";
        if (url.contains("smarttv") || url.contains("player")) {
            int startIndex = url.lastIndexOf("/");
            int endIndex = url.lastIndexOf("-");
            // get content ID from ULR.
            if (startIndex < endIndex) {
                contentId = url.substring(startIndex + 1, endIndex);
            } else {
                contentId = url.substring(startIndex + 1);
            }
        } else {

            int episodeIndex = url.lastIndexOf("#");
            if (episodeIndex > 0) {
                url = url.substring(0, episodeIndex);
            }
            int startIndex = url.lastIndexOf("-");
            int endIndex = url.lastIndexOf(".");
            if (startIndex < endIndex) {
                contentId = url.substring(startIndex + 1, endIndex);
            }
        }

        return contentId;
    }


    public static void main(String[] args) {
        String vi = getContentIDIncludeSmartTv("http:///vod/player/5914076b558320658eb88e44-1");
        System.out.println(vi);
    }
}
