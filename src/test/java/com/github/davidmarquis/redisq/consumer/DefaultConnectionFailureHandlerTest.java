package com.github.davidmarquis.redisq.consumer;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.data.redis.RedisConnectionFailureException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class DefaultConnectionFailureHandlerTest {

    private DefaultConnectionFailureHandler defaultConnectionFailureHandler;

    @Mock
    private Logger mockLogger;

    @Before
    public void setUp() throws Exception {
        defaultConnectionFailureHandler = new DefaultConnectionFailureHandler(mockLogger);
        defaultConnectionFailureHandler.setMillisToWaitAfterConnectionFailure(0);
    }

    @Test
    public void test_logs_warnings_then_error() throws Exception {

        // given
        defaultConnectionFailureHandler.setConnectionFailuresBeforeErrorLog(3);

        // execute
        whenConnectionFailureTimes(4);

        // verify
        verifyLogLevelsInOrder("3w,1e");
    }

    @Test
    public void test_logs_warnings_then_error_then_warnings() throws Exception {

        // given
        defaultConnectionFailureHandler.setConnectionFailuresBeforeErrorLog(3);

        // execute
        whenConnectionFailureTimes(6);

        // verify
        verifyLogLevelsInOrder("3w,1e,2w");
    }

    @Test
    public void test_logs_all_errors() throws Exception {

        // given
        defaultConnectionFailureHandler.setConnectionFailuresBeforeErrorLog(0);

        // execute
        whenConnectionFailureTimes(3);

        // verify
        verifyLogLevelsInOrder("3e");
    }

    @Test
    public void test_waits_after_failure() throws Exception {

        // given
        final int waitMillisAfterFailure = 20;
        final int numberOfFailures = 5;
        defaultConnectionFailureHandler.setMillisToWaitAfterConnectionFailure(waitMillisAfterFailure);

        // execute
        int duration = meterDuration(new Runnable() {
            public void run() {
                whenConnectionFailureTimes(numberOfFailures);
            }
        });

        // verify
        assertThat(duration, is(greaterThan(numberOfFailures * waitMillisAfterFailure)));
    }

    private void whenConnectionFailureTimes(int count) {
        for (int i = 0; i < count; i++) {
            defaultConnectionFailureHandler.serverConnectionFailed(new RedisConnectionFailureException("error!"));
        }
    }

    /**
     * Verifies logging level in the order of invocation according to provided pattern.
     * @param pattern expected to be in format: [numInvocations][w|e],{...}
     */
    private void verifyLogLevelsInOrder(String pattern) {
        InOrder order = inOrder(mockLogger);
        String[] checks = StringUtils.split(pattern, ",");

        for (String check : checks) {
            int lastIdx = check.length() - 1;
            char last = check.charAt(lastIdx);
            int numInvokes = Integer.parseInt(check.substring(0, lastIdx));

            switch (last) {
                case 'w':
                    order.verify(mockLogger, times(numInvokes)).warn(anyString(), any(), any());
                    break;
                case 'e':
                    order.verify(mockLogger, times(numInvokes)).error(anyString(), any(), any(), any());
                    break;
            }
        }
    }

    private int meterDuration(Runnable runnable) {
        long start = System.currentTimeMillis();
        runnable.run();
        return (int) (System.currentTimeMillis() - start);
    }
}