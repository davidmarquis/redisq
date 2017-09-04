package com.github.davidmarquis.redisq.serialization;

import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JaxbPayloadSerializerTest {

    private JaxbPayloadSerializer jaxbPayloadSerializer;

    @Before
    public void setup() {
        jaxbPayloadSerializer = new JaxbPayloadSerializer();
    }

    @Test
    public void test_serialize_deserialize() {
        String serializedForm = jaxbPayloadSerializer.serialize(new TestPayload("1", "2"));
        TestPayload result = jaxbPayloadSerializer.deserialize(serializedForm, TestPayload.class);

        assertThat(result.getAttr1(), is("1"));
        assertThat(result.getAttr2(), is("2"));
    }


    @XmlRootElement(name = "someObject")
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class TestPayload {
        @XmlAttribute
        private String attr1;
        @XmlAttribute
        private String attr2;

        private TestPayload(String attr1, String attr2) {
            this.attr1 = attr1;
            this.attr2 = attr2;
        }

        private TestPayload() {
            /* required for JAXB deserialization */
        }

        String getAttr1() {
            return attr1;
        }

        String getAttr2() {
            return attr2;
        }
    }
}
