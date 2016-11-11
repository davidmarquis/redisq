package ca.radiant3.redisq.serialization;

import ca.radiant3.redisq.Message;

import java.util.Map;

public interface MessageConverter {

    <T> Map<String, String> toMap(Message<T> message, PayloadSerializer payloadSerializer);

    <T> Message<T> toMessage(Map<String, String> data, Class<T> payloadType, PayloadSerializer payloadSerializer);
}
