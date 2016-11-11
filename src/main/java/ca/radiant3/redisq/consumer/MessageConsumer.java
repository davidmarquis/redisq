package ca.radiant3.redisq.consumer;

import ca.radiant3.redisq.Message;
import ca.radiant3.redisq.MessageQueue;
import ca.radiant3.redisq.consumer.retry.MessageRetryStrategy;
import ca.radiant3.redisq.consumer.retry.NoRetryStrategy;
import ca.radiant3.redisq.consumer.retry.RetryableMessageException;
import ca.radiant3.redisq.persistence.RedisOps;
import ca.radiant3.redisq.producer.MessageProducer;
import ca.radiant3.redisq.queuing.FIFOQueueDequeueStrategy;
import ca.radiant3.redisq.queuing.LockingQueueDequeueStrategyWrapper;
import ca.radiant3.redisq.utils.GenericsUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisConnectionFailureException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * A message consumer monitors messages from a MessageQueue continuously, and when a message
 * is found, delegates the actual processing of the message to a configured MessageListener.
 * <p/>
 * Each message consumer requires a consumer ID to be set. A consumer ID represents a single
 * logical entity that consumes messages (usually an application). Multiple processes (or threads)
 * can be watching a queue with the same consumer ID, in which case the messages will effectively
 * be processed in a distributed fashion.
 * <p/>
 * In practice, each consumer registers a consumer ID on the queue upon startup, and it is then
 * the producer's responsibility to enqueue messages in per-consumer queues.
 * <p/>
 * A QueueDequeueStrategy must also be set. That strategy determines the way messages will be dequeued.
 * The default strategy (FIFOQueueDequeueStrategy) will simply dequeue as soon as a message is made
 * available in the queue. A special strategy allows a single message to be processed at any given
 * time (LockingQueueDequeueStrategyWrapper).
 *
 * @param <T>
 * @see MessageProducer
 * @see FIFOQueueDequeueStrategy
 * @see LockingQueueDequeueStrategyWrapper
 */
public class MessageConsumer<T> {

    private static final Logger log = LoggerFactory.getLogger(MessageConsumer.class);

    private MessageQueue queue;
    private String consumerId;
    private MessageListener<T> messageListener;

    private ThreadingStrategy threadingStrategy = new SingleThreadingStrategy();
    private MessageRetryStrategy<T> retryStrategy = new NoRetryStrategy<T>();
    private ConnectionFailureHandler connectionFailureHandler = new DefaultConnectionFailureHandler(log);

    @Autowired
    private RedisOps redisOps;

    private Class<T> payloadType;

    private boolean autoStartConsumers = true;

    @PostConstruct
    public void initialize() {
        consumerId = (consumerId == null) ? queue.getDefaultConsumerId() : consumerId;

        if (StringUtils.isEmpty(consumerId)) {
            throw new IllegalStateException("Consumer ID is not set but is mandatory.");
        }

        String queueName = queue.getQueueName();

        redisOps.ensureConsumerRegistered(queueName, consumerId);

        log.debug(String.format("Registered as consumer ID [%s] on queue [%s]", consumerId, queueName));

        payloadType = extractMessagePayloadTypeFromListener();

        log.debug(String.format("Handling payloads from messages in queue [%s] as objects of class [%s]", queueName, payloadType));

        if (autoStartConsumers) {
            startConsumer();
        }
    }

    public void startConsumer() {
        startDequeue();
    }

    protected void startDequeue() {
        String queueName = queue.getQueueName();

        threadingStrategy.start(queueName, new Runnable() {
            public void run() {
                try {
                    processNextMessage();
                } catch (RedisConnectionFailureException e) {
                    connectionFailureHandler.serverConnectionFailed(e);
                }
            }
        });
    }

    @PreDestroy
    public void stopConsumer() {
        threadingStrategy.stop();
    }

    protected void processNextMessage() {

        queue.dequeue(consumerId, new MessageCallback() {
            public void handle(String messageId) {
                Message<T> message = redisOps.loadMessageById(queue.getQueueName(), messageId, payloadType);
                try {
                    handleMessage(message);
                } catch (RetryableMessageException e) {
                    retryStrategy.retry(message, queue, consumerId);
                } catch (Throwable t) {
                    handleExceptionWhileProcessingMessage(message, t);
                }
            }
        });
    }

    protected void handleMessage(Message<T> message) throws RetryableMessageException {
        messageListener.onMessage(message);
    }

    protected void handleExceptionWhileProcessingMessage(Message<T> message, Throwable exception) {
        log.error(String.format("Exception while handling message with ID [%s]", message.getId()), exception);
    }

    @SuppressWarnings("unchecked")
    private Class<T> extractMessagePayloadTypeFromListener() {
        return (Class<T>) GenericsUtils.getGenericTypeOfInterface(messageListener.getClass(), MessageListener.class);
    }

    public void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }

    public void setRedisOps(RedisOps redisOps) {
        this.redisOps = redisOps;
    }

    public void setMessageListener(MessageListener<T> messageListener) {
        this.messageListener = messageListener;
    }

    public void setQueue(MessageQueue queue) {
        this.queue = queue;
    }

    public MessageListener<T> getMessageListener() {
        return messageListener;
    }

    public void setThreadingStrategy(ThreadingStrategy threadingStrategy) {
        this.threadingStrategy = threadingStrategy;
    }

    public void setAutoStartConsumers(boolean autoStartConsumers) {
        this.autoStartConsumers = autoStartConsumers;
    }

    public void setRetryStrategy(MessageRetryStrategy retryStrategy) {
        this.retryStrategy = retryStrategy;
    }
}
