package kep.main.KEP.kafka;

import kep.main.KEP.model.KafkaMessage;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutionException;

@Service
public class KafkaMessageSenderProcessor {
    private static final Logger logger = LogManager.getLogger(KafkaMessageSenderProcessor.class);

    private final KafkaUtils kafkaUtils;

    KafkaProducer<String, KafkaMessage> messageProducer;

    public KafkaMessageSenderProcessor(KafkaUtils kafkaUtils) {
        this.kafkaUtils = kafkaUtils;
    }

    @PreDestroy
    public void onDestroy() throws Exception {
        logger.debug("Destroying producer: {}", messageProducer);
        messageProducer.flush();
        messageProducer.close();
    }

    @PostConstruct
    private void createProducerOnStartUp() throws ExecutionException, InterruptedException {
        kafkaUtils.createTopicIfNotExist(kafkaUtils.messageTopicStorage,
                kafkaUtils.messageTopicStorageRetentionMS, kafkaUtils.defaultReplicaitonFactor);
        try {
            messageProducer = kafkaUtils.createKafkaProducer("all", StringSerializer.class, KafkaJsonSerializer.class);
            logger.debug("Successfully created kafka producer: {}", messageProducer);
        } catch (Exception e) {
            logger.error("Error while creating Kafka producer: {}", e.getMessage());
        }
    }

    public void startProducing(KafkaMessage kafkaMessage) {
        try {
            messageProducer.send(new ProducerRecord<>(kafkaUtils.messageTopicStorage, kafkaMessage.receiverUserId.toString(), kafkaMessage));
            logger.debug("Message successfully sent to topic: {} with receiver id: {}", kafkaUtils.messageTopicStorage, kafkaMessage.receiverUserId.toString());
        } catch (Exception e) {
            logger.error("Error sending message with receiver id: {} - to topic: {}, with error: {} ", kafkaMessage.receiverUserId.toString(), kafkaUtils.messageTopicStorage, e.getMessage());
        }
    }
}
