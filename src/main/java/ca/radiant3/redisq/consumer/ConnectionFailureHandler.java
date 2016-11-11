package ca.radiant3.redisq.consumer;

import org.springframework.data.redis.RedisConnectionFailureException;

public interface ConnectionFailureHandler {

    /**
     * Called when a connection failure happens. This method is called in the context of each message processing thread.
     * @param e the details on the connection failure
     */
    void serverConnectionFailed(RedisConnectionFailureException e);
}
