package ca.radiant3.redisq;

import java.util.Calendar;

public class Message<T> {
    private String id;
    private Calendar creation;
    private Long timeToLiveSeconds;
    private int retryCount = 0;
    private T payload;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Calendar getCreation() {
        return creation;
    }

    public void setCreation(Calendar creation) {
        this.creation = creation;
    }

    public Long getTimeToLiveSeconds() {
        return timeToLiveSeconds;
    }

    public void setTimeToLiveSeconds(Long timeToLiveSeconds) {
        this.timeToLiveSeconds = timeToLiveSeconds;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    public static <T> Message<T> create(String id, T payload) {
        Message<T> created = new Message<T>();
        created.setId(id);
        created.setCreation(Calendar.getInstance());
        created.setPayload(payload);
        return created;
    }
}
