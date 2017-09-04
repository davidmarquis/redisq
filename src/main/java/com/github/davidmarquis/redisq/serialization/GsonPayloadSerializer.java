package com.github.davidmarquis.redisq.serialization;

import com.google.gson.Gson;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * Serializes message payloads as JSON using Google's Gson library.
 *
 * To use this serializer, you must include the Gson library in your project. It is
 * not automatically provided as a runtime dependency of RedisQ.
 */
public class GsonPayloadSerializer implements PayloadSerializer {

    private Gson serializer = new Gson();

    public String serialize(Object payload) throws SerializationException {
        return serializer.toJson(payload);
    }

    public <T> T deserialize(String payload, Class<T> type) throws SerializationException {
        return serializer.fromJson(payload, type);
    }
}
