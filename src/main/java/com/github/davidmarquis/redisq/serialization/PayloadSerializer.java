package com.github.davidmarquis.redisq.serialization;

import org.springframework.data.redis.serializer.SerializationException;

/**
 * Responsible for the serialization and deserialization of RedisQ message bodies.
 * The same PayloadSerializer implementation must be used both on producer and consumer sides.
 */
public interface PayloadSerializer {

    /**
     * Encodes the payload object into a String for storing in a message body. Used on the producer
     * side of the queue.
     * @param payload source object to serialize.
     * @return a String version of the payload object.
     * @throws SerializationException if there is any problem serializing the object.
     */
    String serialize(Object payload) throws SerializationException;

    /**
     * Decodes the payload from its String representation. Used on the consumer side of the queue.
     * @param payload the payload read from the RedisQ message that is to be consumed.
     * @param type type of object expected to be deserialized to.
     * @param <T> type of object expected to be deserialized to.
     * @return the fully reconstructed object.
     * @throws SerializationException if there is any problem deserializing the object.
     */
    <T> T deserialize(String payload, Class<T> type) throws SerializationException;
}
