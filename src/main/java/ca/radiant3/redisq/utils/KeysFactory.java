package ca.radiant3.redisq.utils;

public class KeysFactory {

    private static final String REGISTERED_CONSUMERS_KEY_PATTERN = "redisq.%s.consumers";
    private static final String MESSAGE_KEY_PATTERN = "redisq.%s.messages.%s";
    private static final String QUEUE_PATTERN = "redisq.%s.queues.%s";
    private static final String NEXT_MESSAGE_ID_PATTERN = "redisq.%s.nextID";
    private static final String LOCK_SUFFIX = ".lock";
    private static final String NOTIF_LIST_SUFFIX = ".notifs";

    public static String keyForConsumerSpecificQueue(String queue, String consumerId) {
        return String.format(QUEUE_PATTERN, queue, consumerId);
    }

    public static String keyForConsumerSpecificQueueNotificationList(String queue, String consumerId) {
        return keyForConsumerSpecificQueue(queue, consumerId) + NOTIF_LIST_SUFFIX;
    }

    public static String keyForConsumerSpecificQueueLock(String queue, String consumerId) {
        return keyForConsumerSpecificQueue(queue, consumerId) + LOCK_SUFFIX;
    }

    public static String keyForMessage(String queueName, String messageId) {
        return String.format(MESSAGE_KEY_PATTERN, queueName, messageId);
    }

    public static String keyForRegisteredConsumers(String queue) {
        return String.format(REGISTERED_CONSUMERS_KEY_PATTERN, queue);
    }

    public static String keyForNextID(String queue) {
        return String.format(NEXT_MESSAGE_ID_PATTERN, queue);
    }
}
