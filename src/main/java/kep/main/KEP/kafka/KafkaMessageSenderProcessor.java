package kep.main.KEP.kafka;

import kep.main.KEP.model.KafkaMessage;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class KafkaMessageSenderProcessor {

    private final KafkaUtils kafkaUtils;

    public KafkaMessageSenderProcessor(KafkaUtils kafkaUtils) {
        this.kafkaUtils = kafkaUtils;
    }

    public void startProducing(KafkaMessage kafkaMessage) throws ExecutionException, InterruptedException {

        String conversationTopicName = kafkaUtils.messageTopicStorage + "-" + kafkaMessage.senderUserId + kafkaMessage.receiverUserId;
        KafkaProducer<String, KafkaMessage> messageProducer = kafkaUtils.createKafkaProducer("all", StringSerializer.class, KafkaJsonSerializer.class);
        kafkaUtils.createTopicIfNotExist(conversationTopicName,
                kafkaUtils.messageTopicStorageRetentionMS, kafkaUtils.defaultReplicaitonFactor);

        messageProducer.send(new ProducerRecord<>(conversationTopicName, kafkaMessage.receiverUserId.toString(), kafkaMessage));
        messageProducer.flush();
        messageProducer.close();
    }
}
