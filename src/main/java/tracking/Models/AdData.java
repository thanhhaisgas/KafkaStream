package tracking.Models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AdData extends RawlogData {
    private String metric;
    private int creativeId;
    private int campaignId;
    private int flightId;
    private int timeEngaged = 1;

    public AdData(){

    }

    public AdData(String metric, int loggedTime, String uuid, int placement, int creativeId, int campaignId,
                  int flightId) {
        super();
        this.metric = metric;
        this.loggedTime = loggedTime;
        this.uuid = uuid;
        this.placement = placement;
        this.creativeId = creativeId;
        this.campaignId = campaignId;
        this.flightId = flightId;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public int getCreativeId() {
        return creativeId;
    }

    public void setCreativeId(int creativeId) {
        // TODO set and check creativeId
        if (this.creativeId == 0) {
            this.creativeId = creativeId;
        }
    }

    public int getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(int campaignId) {
        this.campaignId = campaignId;
    }

    public int getFlightId() {
        return flightId;
    }

    public void setFlightId(int flightId) {
        this.flightId = flightId;
    }

    public int getTimeEngaged() {
        return timeEngaged;
    }

    public void setTimeEngaged(int timeEngaged) {
        this.timeEngaged = timeEngaged;
    }

    @Override
    public String toLogRecordSplitByTab() {
        return convertObjectToJSONString();
    }

    private String convertObjectToJSONString() {
        ObjectMapper mapper = new ObjectMapper();
        AdData obj = this;

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
        return toLogRecordSplitByTab();
    }
}
