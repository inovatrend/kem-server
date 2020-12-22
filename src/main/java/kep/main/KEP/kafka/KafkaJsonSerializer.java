package kep.main.KEP.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class KafkaJsonSerializer implements Serializer {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Override
    public void configure(Map configs, boolean isKey) {

    }

    @Override
    public byte[] serialize(String topic, Object data) {
        return new byte[0];
    }

    @Override
    public byte[] serialize(String topic, Headers headers, Object data) {
        byte[] retVal = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            retVal = objectMapper.writeValueAsBytes(data);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return retVal;
    }

    @Override
    public void close() {

    }
}
