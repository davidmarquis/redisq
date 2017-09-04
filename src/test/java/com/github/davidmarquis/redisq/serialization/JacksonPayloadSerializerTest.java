package com.github.davidmarquis.redisq.serialization;

import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;


public class JacksonPayloadSerializerTest {

    private JacksonPayloadSerializer jacksonPayloadSerializer = new JacksonPayloadSerializer();

    @Test
    public void test_serialize_null_value() throws Exception {
        // execute
        String serialized = jacksonPayloadSerializer.serialize(null);

        // assert
        assertThat(serialized, is("null"));
    }

    @Test
    public void test_deserialize_null_value_in_string() throws Exception {
        // execute
        Object deserialized = jacksonPayloadSerializer.deserialize("null", Map.class);

        // assert
        assertThat(deserialized, is(nullValue()));
    }

    @Test
    public void test_deserialize_null_value() throws Exception {
        // execute
        Object deserialized = jacksonPayloadSerializer.deserialize(null, Map.class);

        // assert
        assertThat(deserialized, is(nullValue()));
    }
}
