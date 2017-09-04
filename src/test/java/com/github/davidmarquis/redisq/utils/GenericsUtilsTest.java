package com.github.davidmarquis.redisq.utils;

import com.github.davidmarquis.redisq.Message;
import com.github.davidmarquis.redisq.consumer.MessageListener;
import org.junit.Test;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.concurrent.Callable;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class GenericsUtilsTest {

    @Test
    public void test_getGenericTypeOf_specific_interface() {
        Class<?> result = GenericsUtils.getGenericTypeOfInterface(MessageListenerMultiple.class, MessageListener.class);

        assertTrue(result.equals(String.class));
    }

    @Test
    public void test_getGenericTypeOf_specific_interface_multiple_typed_interfaces() {
        Class<?> result = GenericsUtils.getGenericTypeOfInterface(MessageListenerMultipleTyped.class, MessageListener.class);

        assertTrue(result.equals(String.class));
    }

    @Test
    public void test_getGenericTypeOf_interface_not_implemented() {
        Class<?> result = GenericsUtils.getGenericTypeOfInterface(MessageListenerSingle.class, Runnable.class);

        assertThat(result, is(nullValue()));
    }


    private class MessageListenerSingle implements MessageListener<String> {
        public void onMessage(Message<String> message) {
        }
    }

    private class MessageListenerMultiple implements PropertyChangeListener, MessageListener<String> {
        public void onMessage(Message<String> message) {
        }

        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        }
    }

    private class MessageListenerMultipleTyped implements Callable<Map>, MessageListener<String> {
        public void onMessage(Message<String> message) {
        }

        public Map call() throws Exception {
            return null;
        }
    }
}
