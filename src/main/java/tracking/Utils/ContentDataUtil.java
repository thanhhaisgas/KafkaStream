package tracking.Utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import redis.clients.jedis.Pipeline;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.exceptions.JedisException;
import rfx.core.configs.RedisConfigs;
import rfx.core.nosql.jedis.RedisCommand;
import rfx.core.util.LogUtil;
import rfx.core.util.StringUtil;
import rfx.data.util.json.JSONArray;
import tracking.Models.ContentMetaData;

public class ContentDataUtil {
    private static final String EMPTY_STRING = "";

    private static final String LIST_STRUCTURE = "list_structure_name";

    private static final String PREMIER_LEAGUE = "premier-league";
    private static final String XEM_EPL = "/xem-epl/";

    private static final String LIVE_EVENT_API =
            "https://api.fptplay.net/api/v4.3_internal/highlight/";

    private static final String FPT_PLAY_API = "https://api.fptplay.net/api/v4.3_internal/vod/";

    private static final int AFTER_3_DAYS = 60 * 60 * 24 * 3;

    final public static ShardedJedisPool redisDeliveryCache =
            RedisConfigs.load().get("deliveryCache").getShardedJedisPool();

    static final LoadingCache<String, ContentMetaData> contentMetaDataCache =
            CacheBuilder.newBuilder().maximumSize(100000).expireAfterWrite(10, TimeUnit.SECONDS)
                    .build(new CacheLoader<String, ContentMetaData>() {
                        public ContentMetaData load(String contentId) {
                            // query from Redis
                            ContentMetaData result = new RedisCommand<ContentMetaData>(redisDeliveryCache) {
                                @Override
                                protected ContentMetaData build() throws JedisException {
                                    String key = "vod:" + contentId;
                                    Map<String, String> map = this.jedis.hgetAll(key);
                                    System.out.println("content metadata " + map);
                                    if (map != null && map.size() > 0) {
                                        String source = map.getOrDefault("source", "nguoi-dung");
                                        String type = map.getOrDefault("type", "vod");

                                        String structure = map.getOrDefault("structure", EMPTY_STRING);
                                        List<String> categories = new ArrayList<>();
                                        if (structure.equals(EMPTY_STRING)) {
                                            categories = new ArrayList<>();
                                        } else {

                                            if (structure.startsWith("[")) {
                                                JSONArray jsonArray = new JSONArray(structure);
                                                int jsonArrayLenght = jsonArray.length();

                                                for (int i = 0; i < jsonArrayLenght; i++) {
                                                    categories.add(jsonArray.getString(i));
                                                }
                                            } else {
                                                categories = new ArrayList<>();
                                            }
                                        }

                                        // get tag_plots
                                        String tags = map.getOrDefault("tags_plot", EMPTY_STRING);
                                        List<String> tagsPlot = new ArrayList<>();
                                        if (tags.equals(EMPTY_STRING)) {
                                            tagsPlot = new ArrayList<>();
                                        } else {
                                            JSONArray jsonArray = new JSONArray(tags);
                                            int jsonArrayLenght = jsonArray.length();

                                            for (int i = 0; i < jsonArrayLenght; i++) {
                                                tagsPlot.add(jsonArray.getString(i));
                                            }
                                        }

                                        // get list_structure_id
                                        String structureIds = map.getOrDefault("list_structure_id", EMPTY_STRING);
                                        List<String> listStructureId = new ArrayList<>();
                                        if (structureIds.equals(EMPTY_STRING)) {
                                            listStructureId = new ArrayList<>();
                                        } else {
                                            JSONArray jsonArray = new JSONArray(structureIds);
                                            int jsonArrayLenght = jsonArray.length();

                                            for (int i = 0; i < jsonArrayLenght; i++) {
                                                listStructureId.add(jsonArray.getString(i));
                                            }
                                        }

                                        return new ContentMetaData(categories, source, type, tagsPlot, listStructureId);
                                    }
                                    return null;
                                }
                            }.execute();

                            // if video information does not exist in Redis, make request
                            // to FPT Play API.
                            if (result == null) {
                                String json = EMPTY_STRING;
                                // make request to FPT Play.
                                try {
                                    json = readURLFptPlayAPI(contentId);
                                    if (!json.isEmpty()) {
                                        result = parsingObjectFromJson(json);
                                        List<String> structure = result.getCategories();
                                        String source = result.getSource();
                                        String type = result.getType();
                                        List<String> tags = result.getTagsPlot();
                                        List<String> listStructureId = result.getListStructureId();

                                        // save to redis
                                        // save valid vod into redis - this content ID will use for delivery ads
                                        new RedisCommand<Void>(redisDeliveryCache) {
                                            @Override
                                            protected Void build() throws JedisException {
                                                // get json information from Redis.
                                                try {
                                                    String key = "vod:" + contentId;

                                                    Pipeline p = jedis.pipelined();

                                                    p.hset(key, "structure", structure.toString());
                                                    p.hset(key, "source", source);
                                                    p.hset(key, "type", type);
                                                    p.hset(key, "tags_plot", tags.toString());
                                                    p.hset(key, "list_structure_id", listStructureId.toString());

                                                    p.sync();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                return null;
                                            }
                                        }.execute();
                                    }

                                } catch (Exception e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }

                            // check finally
                            if (result == null) {
                                return new ContentMetaData(new ArrayList<String>(), "nguoi-dung", "vod",
                                        new ArrayList<String>(), new ArrayList<String>());
                            }

                            return result;
                        }
                    });

    /**
     * Request to FPT Play API
     *
     * @param urlString make a request
     * @return Json String response
     * @throws Exception
     */
    public static String readURLFptPlayAPI(String videoID) throws Exception {

        // create request url to FPT Play API.
        if ("-".equals(videoID)){
            LogUtil.i("readURLFptPlayAPI", "ContentId is invalid", true);
            return EMPTY_STRING;
        }

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
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return EMPTY_STRING;
    }

    /**
     * Parsing Json object to VideoInfor object.
     *
     * @param json String
     * @return
     * @throws Exception
     */
    public static ContentMetaData parsingObjectFromJson(String json) throws Exception {
        // Convert to a JSON object to print data
        JsonParser jp = new JsonParser(); // from gson
        JsonElement root = jp.parse(json);
        JsonObject rootobj = root.getAsJsonObject();
        JsonObject result = rootobj.getAsJsonObject("result");

        // get video category
        JsonArray listStructure = result.getAsJsonArray(LIST_STRUCTURE);
        List<String> categories = new ArrayList<>();
        for (int i = 0; i < listStructure.size(); i++) {
            categories.add(listStructure.get(i).getAsString());
        }

        String source = result.get("source_provider").getAsString();
        if (source.isEmpty()) {
            source = "nguoi-dung";
        }

        String type = result.get("type").getAsString();
        if (type.isEmpty()) {
            type = "vod";
        }

        JsonArray jsonTagsPlot = result.getAsJsonArray("tags_plot");
        List<String> tagsPlot = new ArrayList<>();
        for (int i = 0; i < jsonTagsPlot.size(); i++) {
            tagsPlot.add(jsonTagsPlot.get(i).getAsString());
        }

        JsonArray jsonListStructureId = result.getAsJsonArray("list_structure_id");
        List<String> listStructureId = new ArrayList<>();
        for (int i = 0; i < jsonListStructureId.size(); i++) {
            listStructureId.add(jsonListStructureId.get(i).getAsString());
        }


        // create video information using data.
        ContentMetaData contentMetadata =
                new ContentMetaData(categories, source, type, tagsPlot, listStructureId);
        return contentMetadata;
    }

    public static ContentMetaData getContentMetaData(String contentId) {
        if (StringUtil.isNotEmpty(contentId)) {
            try {
                ContentMetaData r1 = contentMetaDataCache.get(contentId);
                return r1;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new ContentMetaData(new ArrayList<String>(), "nguoi-dung", "vod",
                new ArrayList<String>(), new ArrayList<String>());
    }

    static final LoadingCache<String, Boolean> liveEventMetaDataCache =
            CacheBuilder.newBuilder().maximumSize(100000).expireAfterWrite(10, TimeUnit.SECONDS)
                    .build(new CacheLoader<String, Boolean>() {

                        public Boolean load(String eventId) {
                            boolean isEplLiveEvent = false;

                            // query from Redis
                            Map<String, String> eventMetaData =
                                    new RedisCommand<Map<String, String>>(redisDeliveryCache) {
                                        @Override
                                        protected Map<String, String> build() throws JedisException {

                                            String key = "live:" + eventId;
                                            Map<String, String> map = this.jedis.hgetAll(key);
                                            System.out.println("live event metadata " + map);
                                            return map;
                                        }
                                    }.execute();

                            if (eventMetaData != null && !eventMetaData.isEmpty()) {
                                String channelName = eventMetaData.getOrDefault("channelName", "livetv");
                                if (channelName.contains(XEM_EPL) || channelName.contains(PREMIER_LEAGUE)) {
                                    return true;
                                } else {
                                    return false;
                                }
                            } else {
                                isEplLiveEvent = new RedisCommand<Boolean>(redisDeliveryCache) {
                                    @Override
                                    protected Boolean build() throws JedisException {

                                        boolean isEplChannel = false;

                                        String keyEvent = "live:" + eventId;

                                        try {
                                            String jsonApiResult = readFptPlayLiveEventAPI(eventId);

                                            String channelName = getLiveEventChannelName(jsonApiResult);
                                            if (!channelName.equals(EMPTY_STRING)) {
                                                if (channelName.contains(XEM_EPL) || channelName.contains(PREMIER_LEAGUE)) {
                                                    isEplChannel = true;
                                                }
                                                jedis.hset(keyEvent, "channelName", channelName);
                                                jedis.expire(keyEvent, AFTER_3_DAYS);
                                            }

                                        } catch (Exception e) {
                                            // TODO Auto-generated catch block
                                            jedis.hset(keyEvent, "channelName", "livetv");
                                            jedis.expire(keyEvent, AFTER_3_DAYS);
                                            e.printStackTrace();
                                        }

                                        return isEplChannel;
                                    }
                                }.execute();
                            }

                            return isEplLiveEvent;
                        }
                    });

    /**
     * Request to FPT Play API
     *
     * @param urlString make a request
     * @return Json String response
     * @throws Exception
     */
    public static String readFptPlayLiveEventAPI(String eventId) throws Exception {

        // create request url to FPT Play API.
        String urlString = LIVE_EVENT_API + eventId;
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

    public static String getLiveEventChannelName(String json) {

        String channelName = EMPTY_STRING;

        try {
            JsonParser jp = new JsonParser(); // from gson
            JsonElement root = jp.parse(json);
            JsonObject rootobj = root.getAsJsonObject();
            JsonObject data = rootobj.getAsJsonObject("data");

            if (data.isJsonObject()) {
                JsonElement tvchannel_id = data.get("tvchannel_id");
                if (tvchannel_id.isJsonArray()) {
                    channelName = tvchannel_id.getAsJsonArray().toString();
                }
            }
        } catch (Exception e) {
            channelName = EMPTY_STRING;
        }


        return channelName;
    }

    /**
     * Check is EPL Live Event.
     *
     * @param eventId
     * @return true if event
     */
    public static boolean checkEplLiveEvent(String eventId) {
        if (StringUtil.isNotEmpty(eventId)) {

            if (eventId.contains(XEM_EPL) || eventId.contains(PREMIER_LEAGUE)) {
                return true;
            } else {
                try {
                    boolean isEplLiveEvent = liveEventMetaDataCache.get(eventId);
                    return isEplLiveEvent;
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
        return false;
    }

    // public static void main(String[] args) throws ExecutionException {
    // boolean r1 = checkEplLiveEvent("547c268117dc1367bd9acdea.json");
    //
    // System.out.println(r1);
    // }

    public static void main(String[] args) throws ExecutionException {
//    ContentMetaData r1 = getContentMetaData("5996db965583202189bcd0d5");
//
//    System.out.println(r1.getTagsPlot().contains("glee-viet-nam"));
//
//    List<String> categories = r1.getCategories();
//    List<String> tgkws = new ArrayList<>(Arrays.asList("Hoa ngữ", "and"));
//
//    Set<String> intersection =
//        Sets.intersection(Sets.newHashSet(categories), Sets.newHashSet(tgkws));
//    System.out.println(intersection.size());
//    System.out.println(r1.getCategories());
//    System.out.println(new Gson().toJson(r1));

        try {
            readURLFptPlayAPI("59a7eb7a5583206944bf61f7");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
