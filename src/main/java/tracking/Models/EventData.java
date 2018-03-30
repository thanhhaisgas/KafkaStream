package tracking.Models;

public abstract class EventData {
    protected long unixTime = System.currentTimeMillis() / 1000L;

    public EventData() {
    }

    public long getUnixTime() {
        return this.unixTime;
    }

    public void setUnixTime(long unixTime) {
        this.unixTime = unixTime;
    }

    public abstract String toStringMessage();
}
