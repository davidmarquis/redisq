package com.github.davidmarquis.redisq.serialization;

import com.github.davidmarquis.redisq.Message;
import org.apache.commons.lang.StringUtils;

import java.util.*;

public class DefaultMessageConverter implements MessageConverter {

    static final String PAYLOAD_HASH_KEY = "payload";
    static final String ID_HASH_KEY = "id";
    static final String CREATION_HASH_KEY = "creation";
    static final String TTL_HASH_KEY = "ttl";
    static final String RETRY_COUNT = "retries";

    public Map<String, String> toMap(Message message, PayloadSerializer payloadSerializer) {
        String payloadAsText = payloadSerializer.serialize(message.getPayload());

        Map<String, String> data = new HashMap<String, String>(4);
        data.put(ID_HASH_KEY, message.getId());
        data.put(CREATION_HASH_KEY, Long.toString(message.getCreation().getTime().getTime()));
        data.put(PAYLOAD_HASH_KEY, payloadAsText);
        data.put(RETRY_COUNT, Integer.toString(message.getRetryCount()));
        if (message.getTimeToLiveSeconds() != null) {
            data.put(TTL_HASH_KEY, Long.toString(message.getTimeToLiveSeconds()));
        }

        return data;
    }

    public <T> Message<T> toMessage(Map<String, String> data, Class<T> payloadType, PayloadSerializer payloadSerializer) {
        if (data == null || data.isEmpty()) {
            return null;
        }

        T payload = payloadSerializer.deserialize(data.get(PAYLOAD_HASH_KEY), payloadType);

        Message<T> result = new Message<T>();
        result.setId(data.get(ID_HASH_KEY));
        result.setCreation(calendarFromEpochString(data.get(CREATION_HASH_KEY)));
        result.setTimeToLiveSeconds(Long.parseLong(data.get(TTL_HASH_KEY)));
        result.setPayload(payload);

        String retryCount = data.get(RETRY_COUNT);
        if (retryCount != null) {
            result.setRetryCount(Integer.parseInt(retryCount));
        }

        return result;
    }

    private Calendar calendarFromEpochString(String epoch) {
        if (StringUtils.isEmpty(epoch)) {
            return null;
        }

        long epochValue = Long.valueOf(epoch);

        Calendar result = new GregorianCalendar();
        result.setTime(new Date(epochValue));
        return result;
    }
}
