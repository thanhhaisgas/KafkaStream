package tracking.Utils;

import rfx.core.util.LogUtil;

public class ParseUtil {
    public static String getContentIdFromURL(String url) {
        String id = null;

        if (url.contains("livetv")) {
            int beginIndex = url.lastIndexOf("/") + 1;
            try {
                id = url.substring(beginIndex);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                int idx1 = url.lastIndexOf("/") + 1;
                int idx2 = url.indexOf("-");
                if (idx1 > 1 && idx2 > idx1) {
                    id = url.substring(idx1, idx2);
                } else if (idx1 > 1) {
                    id = url.substring(idx1);
                }

                // If id still be the episode of film
                if (id != null && id.matches("\\d+")) {
                    id = url.substring(url.indexOf("vod/") + 4, url.lastIndexOf("/"));
                    LogUtil.i("Check id is episode of film", "Final contentID:" + id + ", from url:" + url, true);
                } else {
                    return "-";
                }

            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        return id;
    }
}
