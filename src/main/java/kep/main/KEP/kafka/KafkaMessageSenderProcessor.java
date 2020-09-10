package kep.main.KEP.kafka;

import kep.main.KEP.model.KafkaMessage;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutionException;

@Service
public class KafkaMessageSenderProcessor {

    private final KafkaUtils kafkaUtils;

    KafkaProducer<String, KafkaMessage> messageProducer;

    public KafkaMessageSenderProcessor(KafkaUtils kafkaUtils) {
        this.kafkaUtils = kafkaUtils;
    }

    @PostConstruct
    private void createProducerOnStartUp() throws ExecutionException, InterruptedException {
        kafkaUtils.createTopicIfNotExist(kafkaUtils.messageTopicStorage,
                kafkaUtils.messageTopicStorageRetentionMS, kafkaUtils.defaultReplicaitonFactor);

        messageProducer = kafkaUtils.createKafkaProducer("all", StringSerializer.class, KafkaJsonSerializer.class);
    }

    public void startProducing(KafkaMessage kafkaMessage) {
        messageProducer.send(new ProducerRecord<>(kafkaUtils.messageTopicStorage, kafkaMessage.receiverUserId.toString(), kafkaMessage));
    }
}
