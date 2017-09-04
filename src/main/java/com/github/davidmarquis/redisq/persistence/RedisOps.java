package com.github.davidmarquis.redisq.persistence;

import com.github.davidmarquis.redisq.Message;
import com.github.davidmarquis.redisq.MessageQueue;
import com.github.davidmarquis.redisq.serialization.DefaultMessageConverter;
import com.github.davidmarquis.redisq.serialization.JaxbPayloadSerializer;
import com.github.davidmarquis.redisq.serialization.MessageConverter;
import com.github.davidmarquis.redisq.serialization.PayloadSerializer;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.github.davidmarquis.redisq.utils.KeysFactory.*;

@SuppressWarnings("unchecked")
public class RedisOps {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired(required = false)
    private PayloadSerializer payloadSerializer = new JaxbPayloadSerializer();
    @Autowired(required = false)
    private MessageConverter messageConverter = new DefaultMessageConverter();

    public void ensureConsumerRegistered(String queueName, String consumerId) {
        BoundSetOperations<String, String> ops = redisTemplate.boundSetOps(keyForRegisteredConsumers(queueName));
        ops.add(consumerId);
    }

    public Collection<String> getRegisteredConsumers(String queueName) {
        BoundSetOperations<String, String> ops = redisTemplate.boundSetOps(keyForRegisteredConsumers(queueName));
        return ops.members();
    }

    public <T> void addMessage(String queueName, Message<T> message) {
        assert message != null;
        assert message.getTimeToLiveSeconds() != null;

        String msgId = generateNextMessageID(queueName);
        message.setId(msgId);
        if (message.getCreation() == null) {
            message.setCreation(Calendar.getInstance());
        }

        saveMessage(queueName, message);
    }

    public <T> void saveMessage(String queueName, Message<T> message) {
        assert message != null;
        assert message.getId() != null;
        assert message.getTimeToLiveSeconds() != null;

        Map<String, String> asMap = messageConverter.toMap(message, payloadSerializer);

        String messageKey = keyForMessage(queueName, message.getId());
        redisTemplate.opsForHash().putAll(messageKey, asMap);
        redisTemplate.expire(messageKey, message.getTimeToLiveSeconds(), TimeUnit.SECONDS);
    }

    public <T> Message<T> loadMessageById(String queueName, String id, Class<T> payloadType) {
        String messageKey = keyForMessage(queueName, id);
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(messageKey);
        Map<String, String> messageData = ops.entries();

        return messageConverter.toMessage(messageData, payloadType, payloadSerializer);
    }

    public String dequeueMessageFromHead(String queueName, String consumerId, long timeoutSeconds) {
        String queueKey = keyForConsumerSpecificQueue(queueName, consumerId);

        BoundListOperations<String, String> ops = redisTemplate.boundListOps(queueKey);
        return ops.leftPop(timeoutSeconds, TimeUnit.SECONDS);
    }

    public <T> Message<T> peekNextMessageInQueue(String queueName, String consumerId, Class<T> payloadType) {
        String queueKey = keyForConsumerSpecificQueue(queueName, consumerId);

        BoundListOperations<String, String> ops = redisTemplate.boundListOps(queueKey);

        String nextId = ops.index(0);
        if (nextId == null) {
            return null;
        }
        return loadMessageById(queueName, nextId, payloadType);
    }

    /**
     * @param rangeStart zero-based index of first item to retrieve
     * @param rangeEnd zero-based index of last item to retrieve
     */
    private <T> List<Message<T>> peekMessagesInQueue(String queueName, String consumerId, long rangeStart, long rangeEnd, Class<T> payloadType) {

        String queueKey = keyForConsumerSpecificQueue(queueName, consumerId);

        BoundListOperations<String, String> ops = redisTemplate.boundListOps(queueKey);

        List<String> messageIds = ops.range(rangeStart, rangeEnd);
        List<Message<T>> messages = new ArrayList<Message<T>>(messageIds.size());
        for (String id : messageIds) {
            messages.add(loadMessageById(queueName, id, payloadType));
        }
        return messages;
    }

    /**
     * Peeks messages in the specified queue (for the default consumer).
     * @param rangeStart zero-based index of first item to retrieve
     * @param rangeEnd zero-based index of last item to retrieve
     */
    public <T> List<Message<T>> peekMessagesInQueue(MessageQueue queue, long rangeStart, long rangeEnd, Class<T> payloadType) {
        return peekMessagesInQueue(queue.getQueueName(), queue.getDefaultConsumerId(), rangeStart, rangeEnd, payloadType);
    }

    public void emptyQueue(String queueName) {
        Collection<String> consumerIds = getRegisteredConsumers(queueName);

        for (String consumerId : consumerIds) {
            String queueKey = keyForConsumerSpecificQueue(queueName, consumerId);
            redisTemplate.delete(queueKey);
        }
    }

    private String generateNextMessageID(String queueName) {
        return Long.toString(
                redisTemplate.opsForValue().increment(keyForNextID(queueName), 1)
        );
    }

    public Long getQueueSizeForConsumer(String queueName, String consumerId) {
        return redisTemplate.opsForList().size(keyForConsumerSpecificQueue(queueName, consumerId));
    }

    public void enqueueMessageAtTail(String queueName, String consumerId, String messageId) {
        if (StringUtils.isEmpty(messageId)) {
            throw new IllegalArgumentException("Message must have been persisted before being enqueued.");
        }

        String key = keyForConsumerSpecificQueue(queueName, consumerId);

        redisTemplate.opsForList().rightPush(key, messageId);
    }

    public void enqueueMessageInSet(String queueName, String consumerId, String messageId) {
        if (StringUtils.isEmpty(messageId)) {
            throw new IllegalArgumentException("Message must have been persisted before being enqueued.");
        }

        String key = keyForConsumerSpecificQueue(queueName, consumerId);

        redisTemplate.opsForSet().add(key, messageId);
    }

    public void notifyWaitersOnSet(String queueName, String consumerId) {
        String key = keyForConsumerSpecificQueueNotificationList(queueName, consumerId);

        redisTemplate.opsForList().rightPush(key, "x");
    }

    public void waitOnSet(String queueName, String consumerId, long timeoutSeconds) {
        String key = keyForConsumerSpecificQueueNotificationList(queueName, consumerId);

        redisTemplate.opsForList().leftPop(key, timeoutSeconds, TimeUnit.SECONDS);
    }

    public String randomPopFromSet(String queueName, String consumerId) {
        String key = keyForConsumerSpecificQueue(queueName, consumerId);

        BoundSetOperations<String, String> ops = redisTemplate.boundSetOps(key);
        return ops.pop();
    }

    public boolean tryObtainLockForQueue(String queueName, String consumerId, long expirationTimeout, TimeUnit unit) {
        BoundValueOperations<String, Integer> ops = redisTemplate.boundValueOps(keyForConsumerSpecificQueueLock(queueName, consumerId));

        boolean lockAcquired = ops.setIfAbsent(1);
        if (lockAcquired) {
            ops.expire(expirationTimeout, unit);
            return true;
        }
        return false;
    }

    public void releaseLockForQueue(String queueName, String consumerId) {
        redisTemplate.delete(keyForConsumerSpecificQueueLock(queueName, consumerId));
    }

    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
}
