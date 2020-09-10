package kep.main.KEP.kafka;

import kep.main.KEP.elasticsearch.KafkaElasticsearchManager;
import kep.main.KEP.model.KafkaMessage;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class KafkaMessageElasticsearchProcessor {

    public static final String GROUP_ID = "1";
    private final KafkaUtils kafkaUtils;

    private final KafkaElasticsearchManager kafkaElasticsearchManager;

    private Consumer<String, KafkaMessage> consumer;

    public KafkaMessageElasticsearchProcessor(KafkaUtils kafkaUtils, KafkaElasticsearchManager kafkaElasticsearchManager) {
        this.kafkaUtils = kafkaUtils;
        this.kafkaElasticsearchManager = kafkaElasticsearchManager;
    }

    @PostConstruct
    private void createKafkaConsumerOnStartup() throws ExecutionException, InterruptedException {
        kafkaUtils.createTopicIfNotExist(kafkaUtils.messageTopicStorage,
                kafkaUtils.messageTopicStorageRetentionMS, kafkaUtils.defaultReplicaitonFactor);

        consumer = kafkaUtils.createKafkaConsumer(GROUP_ID, new StringDeserializer(), new JsonDeserializer<>(KafkaMessage.class));

        List<String> topics = new ArrayList<>();
        topics.add(kafkaUtils.messageTopicStorage);

        consumer.subscribe(topics);
    }

    public void kafkaElasticsearchReceiver() {
        synchronized (consumer) {
            ConsumerRecords<String, KafkaMessage> consumerRecords = consumer.poll(Duration.ofMillis(300));

            if (consumerRecords.count() > 0) {
                consumerRecords.forEach(crv -> {
                    kafkaElasticsearchManager.saveToElastic(crv.value());
                });
            }
        }
    }

    public List<KafkaMessage> loadFromElasticsearch(Long senderId, Long receiverId) {
        List<KafkaMessage> conversationMessageList = new ArrayList<>();

        List<KafkaMessage> receiverMessageList = kafkaElasticsearchManager.loadAllMessagesForUser(receiverId, senderId);
        List<KafkaMessage> senderMessageList = kafkaElasticsearchManager.loadAllMessagesForUser(senderId, receiverId);

        conversationMessageList.addAll(senderMessageList);
        conversationMessageList.addAll(receiverMessageList);

        if (conversationMessageList.size() > 0) {
            conversationMessageList.sort(Comparator.comparing(kafkaMessage -> kafkaMessage.id));
        }

        return conversationMessageList;
    }

}
