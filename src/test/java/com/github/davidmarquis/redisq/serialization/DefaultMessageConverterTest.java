package com.github.davidmarquis.redisq.serialization;

import com.github.davidmarquis.redisq.Message;
import org.junit.Test;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class DefaultMessageConverterTest {

    private DefaultMessageConverter converter = new DefaultMessageConverter();

    @Test
    public void can_convert_to_map() {

        // given
        Message msg = Message.create("123", "some payload");
        msg.setRetryCount(2);
        msg.setTimeToLiveSeconds(1000L);

        // execute
        Map<String, String> map = converter.toMap(msg, new StringPayloadSerializer());

        // assert
        assertThat(map.get(DefaultMessageConverter.PAYLOAD_HASH_KEY), is("some payload"));
        assertThat(map.get(DefaultMessageConverter.ID_HASH_KEY), is("123"));
        assertThat(map.get(DefaultMessageConverter.CREATION_HASH_KEY), is(Long.toString(msg.getCreation().getTimeInMillis())));
        assertThat(map.get(DefaultMessageConverter.TTL_HASH_KEY), is("1000"));
        assertThat(map.get(DefaultMessageConverter.RETRY_COUNT), is("2"));
    }

    @Test
    public void can_convert_from_map() {

        // given
        Map<String, String> map = new HashMap<String, String>();
        map.put(DefaultMessageConverter.PAYLOAD_HASH_KEY, "titi toto");
        map.put(DefaultMessageConverter.ID_HASH_KEY, "987");
        map.put(DefaultMessageConverter.CREATION_HASH_KEY, Long.toString(Calendar.getInstance().getTimeInMillis()));
        map.put(DefaultMessageConverter.TTL_HASH_KEY, "2500");
        map.put(DefaultMessageConverter.RETRY_COUNT, "9");

        // execute
        Message<String> result = converter.toMessage(map, String.class, new StringPayloadSerializer());

        // assert
        assertThat(result.getId(), is("987"));
        assertThat(result.getPayload(), is("titi toto"));
        assertThat(result.getCreation(), is(notNullValue()));
        assertThat(result.getTimeToLiveSeconds(), is(2500L));
        assertThat(result.getRetryCount(), is(9));
    }

    @Test
    public void can_convert_from_map_null_payload() {

        // given
        Map<String, String> map = new HashMap<String, String>();
        map.put(DefaultMessageConverter.PAYLOAD_HASH_KEY, null);
        map.put(DefaultMessageConverter.ID_HASH_KEY, "987");
        map.put(DefaultMessageConverter.CREATION_HASH_KEY, Long.toString(Calendar.getInstance().getTimeInMillis()));
        map.put(DefaultMessageConverter.TTL_HASH_KEY, "2500");
        map.put(DefaultMessageConverter.RETRY_COUNT, "9");

        // execute
        Message<String> result = converter.toMessage(map, String.class, new StringPayloadSerializer());

        // assert
        assertThat(result.getId(), is("987"));
        assertThat(result.getPayload(), is(nullValue()));
        assertThat(result.getCreation(), is(notNullValue()));
        assertThat(result.getTimeToLiveSeconds(), is(2500L));
        assertThat(result.getRetryCount(), is(9));
    }

    @Test
    public void cannot_convert_from_null_value() {

        // execute
        Message<String> result = converter.toMessage(null, String.class, new StringPayloadSerializer());

        // assert
        assertThat(result, is(nullValue()));
    }

    @Test
    public void cannot_convert_from_empty_map() {

        // execute
        Map<String, String> emptyMap = new HashMap<String, String>();
        Message<String> result = converter.toMessage(emptyMap, String.class, new StringPayloadSerializer());

        // assert
        assertThat(result, is(nullValue()));
    }

    @Test
    public void can_convert_from_map_no_retry_count() {

        // given
        Map<String, String> map = new HashMap<String, String>();
        map.put(DefaultMessageConverter.PAYLOAD_HASH_KEY, "titi toto");
        map.put(DefaultMessageConverter.ID_HASH_KEY, "987");
        map.put(DefaultMessageConverter.CREATION_HASH_KEY, Long.toString(Calendar.getInstance().getTimeInMillis()));
        map.put(DefaultMessageConverter.TTL_HASH_KEY, "2500");

        // execute
        Message<String> result = converter.toMessage(map, String.class, new StringPayloadSerializer());

        // assert
        assertThat(result.getRetryCount(), is(0));
    }
}
