package kep.main.KEP.kafka;

import kep.main.KEP.model.KafkaMessage;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class KafkaMessageReceiverProcessor {

    private final KafkaUtils kafkaUtils;

    public KafkaMessageReceiverProcessor(KafkaUtils kafkaUtils) {
        this.kafkaUtils = kafkaUtils;
    }


    public List<KafkaMessage> receive(Long groupId) {
        Consumer<String, KafkaMessage> consumer = kafkaUtils.createKafkaConsumer(groupId.toString(), new StringDeserializer(),  new JsonDeserializer<>(KafkaMessage.class));

        List<String> topics = new ArrayList<>();
        topics.add(kafkaUtils.messageTopicStorage);

        consumer.subscribe(topics);
        List<KafkaMessage> consumerRecordValues = new ArrayList<>();

        ConsumerRecords<String, KafkaMessage> consumerRecords = consumer.poll(Duration.ofMillis(100));
        consumerRecords.forEach(crv -> {
            if (crv.key().equals(groupId.toString())) {
                consumerRecordValues.add(crv.value());
            }
        });

        consumer.close();
        return consumerRecordValues;
    }
}
