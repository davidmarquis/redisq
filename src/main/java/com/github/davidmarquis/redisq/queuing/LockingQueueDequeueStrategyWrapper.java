package com.github.davidmarquis.redisq.queuing;

import com.github.davidmarquis.redisq.consumer.MessageCallback;
import com.github.davidmarquis.redisq.persistence.RedisOps;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * Strategy for dequeuing messages based on a lock stored in Redis. The lock is stored as a Redis value in a
 * specific key. Using this strategy results in a single message being processed from the queue at any given time,
 * even if consumers are deployed as a cluster. Use this strategy when you need to make sure messages from
 * the queue are processed sequentially and in order.
 *
 * IMPORTANT:
 * The use of this strategy *must* be in place for all consumers of the queue for it to have an effect.
 */
public class LockingQueueDequeueStrategyWrapper implements QueueDequeueStrategy {

    @Autowired
    private RedisOps redisOps;

    private long lockExpirationTimeout = 5;
    private TimeUnit lockExpirationTimeoutUnit = TimeUnit.SECONDS;
    private long lockWaitTimeout = 1;
    private TimeUnit lockWaitTimeoutUnit = TimeUnit.SECONDS;

    private QueueDequeueStrategy wrapped;

    public LockingQueueDequeueStrategyWrapper(QueueDequeueStrategy wrapped) {
        this.wrapped = wrapped;

        if (this.wrapped == null) {
            throw new IllegalStateException("LockingQueueDequeueStrategyWrapper needs an instance to wrap.");
        }
    }

    public void dequeueNextMessage(String queueName, String consumerId, MessageCallback callback) {

        boolean lockAcquired = redisOps.tryObtainLockForQueue(queueName, consumerId, lockExpirationTimeout, lockExpirationTimeoutUnit);
        if (lockAcquired) {
            try {
                wrapped.dequeueNextMessage(queueName, consumerId, callback);
            } finally {
                redisOps.releaseLockForQueue(queueName, consumerId);
            }
        } else {
            try {
                lockWaitTimeoutUnit.sleep(lockWaitTimeout);
            } catch (InterruptedException e) {
                /* no-op */
            }
        }
    }

    public void enqueueMessage(String queueName, String consumerId, String messageId) {
        wrapped.enqueueMessage(queueName, consumerId, messageId);
    }

    public void setLockExpirationTimeout(long lockExpirationTimeout) {
        this.lockExpirationTimeout = lockExpirationTimeout;
    }

    public void setLockExpirationTimeoutUnit(TimeUnit lockExpirationTimeoutUnit) {
        this.lockExpirationTimeoutUnit = lockExpirationTimeoutUnit;
    }

    public void setLockWaitTimeout(long lockWaitTimeout) {
        this.lockWaitTimeout = lockWaitTimeout;
    }

    public void setLockWaitTimeoutUnit(TimeUnit lockWaitTimeoutUnit) {
        this.lockWaitTimeoutUnit = lockWaitTimeoutUnit;
    }
}
