package com.github.davidmarquis.redisq.serialization;

import com.github.davidmarquis.redisq.Message;

import java.util.Map;

public interface MessageConverter {

    <T> Map<String, String> toMap(Message<T> message, PayloadSerializer payloadSerializer);

    <T> Message<T> toMessage(Map<String, String> data, Class<T> payloadType, PayloadSerializer payloadSerializer);
}
