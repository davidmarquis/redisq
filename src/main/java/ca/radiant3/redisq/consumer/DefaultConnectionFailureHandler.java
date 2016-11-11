package ca.radiant3.redisq.consumer;

import org.slf4j.Logger;
import org.springframework.data.redis.RedisConnectionFailureException;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * ConnectionFailureHandler implementation that will log an error only after a specified number of connection
 * errors (all other failures are logged as warnings) and will also wait a specified amount of time after each
 * connection failure.
 */
public class DefaultConnectionFailureHandler implements ConnectionFailureHandler {
    private static final long DEFAULT_MILLIS_TO_WAIT_AFTER_CONNECTION_FAILURE = 2000;
    private static final int DEFAULT_CONNECTION_FAILURES_BEFORE_ERROR_LOG = 10;

    private AtomicInteger connectionFailuresCounter = new AtomicInteger(0);
    /**
     * This is an intentional decision to borrow the 'logger' from the calling class.
     * Makes logging output as if it happened in the calling class and also easier for testing.
     */
    private Logger logger;

    /**
     * Number of milliseconds a processing thread will wait after a connection failure.
     */
    private long millisToWaitAfterConnectionFailure;
    /**
     * Number of connection failures to wait before logging an error. Below that threshold, only warns are logged.
     * The internal counter that is maintained to count connection failures is shared between all threads. This means
     * that if 10 processing threads are used, each error caused by a network failure will continuously generate 10 errors and
     * counted as 10 errors.
     * As a guideline, this value should be set to a multiple of the number of processing threads that are used.
     */
    private int connectionFailuresBeforeErrorLog;

    DefaultConnectionFailureHandler(Logger logger) {
        this.logger = logger;
        this.millisToWaitAfterConnectionFailure = DEFAULT_MILLIS_TO_WAIT_AFTER_CONNECTION_FAILURE;
        this.connectionFailuresBeforeErrorLog = DEFAULT_CONNECTION_FAILURES_BEFORE_ERROR_LOG;
    }

    public void serverConnectionFailed(RedisConnectionFailureException e) {
        int count = incrementFailureCounterAndGetCount();

        if (shouldLogError(count)) {
            logger.error("Could not connect to Redis after {} attempts, retrying in {}ms...", connectionFailuresCounter, millisToWaitAfterConnectionFailure,e);
            resetErrorCount();
        } else {
            logger.warn("Error connecting to Redis ({}), retrying in {}ms...", e.getMessage(), millisToWaitAfterConnectionFailure);
        }

        waitAfterConnectionFailure();
    }

    private void resetErrorCount() {
        connectionFailuresCounter.set(0);
    }

    private int incrementFailureCounterAndGetCount() {
        return connectionFailuresCounter.incrementAndGet();
    }

    private boolean shouldLogError(int count) {
        return (count - 1) == connectionFailuresBeforeErrorLog;
    }

    private void waitAfterConnectionFailure() {
        try {
            Thread.sleep(millisToWaitAfterConnectionFailure);
        } catch (InterruptedException e1) { /* no-op */ }
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    void setMillisToWaitAfterConnectionFailure(long millisToWaitAfterConnectionFailure) {
        this.millisToWaitAfterConnectionFailure = millisToWaitAfterConnectionFailure;
    }

    void setConnectionFailuresBeforeErrorLog(int connectionFailuresBeforeErrorLog) {
        this.connectionFailuresBeforeErrorLog = connectionFailuresBeforeErrorLog;
    }
}
