package kep.main.KEP.kafka;

import kep.main.KEP.model.KafkaMessage;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.springframework.stereotype.Service;

@Service
public class KafkaMessageSenderProcessor {

    private final KafkaUtils kafkaUtils;

    public KafkaMessageSenderProcessor(KafkaUtils kafkaUtils) {
        this.kafkaUtils = kafkaUtils;
    }

    public void startProducing(KafkaMessage kafkaMessage) {
        KafkaProducer messageProducer = kafkaUtils.createKafkaProducer("all", StringSerializer.class, JsonSerializer.class);

        messageProducer.send(new ProducerRecord(kafkaUtils.messageTopicStorage, 3, kafkaMessage.receiverUserId, kafkaMessage));
    }
}
