package ca.radiant3.redisq.serialization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.serializer.SerializationException;

import javax.xml.bind.*;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

/**
 * Serializes and deserializes payload object instances as XML through JAXB. Payload classes must
 * be annotated using JAXB annotations.
 */
public class JaxbPayloadSerializer implements PayloadSerializer {

    private static final Logger log = LoggerFactory.getLogger(JaxbPayloadSerializer.class);

    private static final int INITIAL_BUFFER_SIZE = 1024;

    public String serialize(Object payload) {
        Class<?> payloadType = payload.getClass();
        try {
            JAXBContext context = JAXBContext.newInstance(payloadType);
            Marshaller marshaller = context.createMarshaller();

            ByteArrayOutputStream payloadAsXml = new ByteArrayOutputStream(INITIAL_BUFFER_SIZE);

            marshaller.marshal(payload, payloadAsXml);

            return new String(payloadAsXml.toByteArray());

        } catch (JAXBException e) {
            throw new SerializationException("JAXB error while serializing object.", e);
        }
    }

    public <T> T deserialize(String payload, Class<T> payloadType) {
        try {
            JAXBContext context = JAXBContext.newInstance(payloadType);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            if (log.isDebugEnabled()) {
                // input stream can only be read once.
                log.debug("Message payload content as JAXB XML:");
                log.debug("++++++++++++++++++++++++++++++++++");
                log.debug(payload);
                log.debug("----------------------------------");
            }

            ByteArrayInputStream payloadStream = new ByteArrayInputStream(payload.getBytes());
            JAXBElement<T> element = unmarshaller.unmarshal(new StreamSource(payloadStream), payloadType);

            return (element == null) ? null : element.getValue();

        } catch (JAXBException e) {
            throw new SerializationException("Could not deserialize payload.", e);
        }
    }
}
