package kep.main.KEP.kafka;

import kep.main.KEP.elasticsearch.service.KafkaElasticsearchBlockUserManager;
import kep.main.KEP.model.KafkaBlockUser;
import kep.main.KEP.model.KafkaMessage;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class KafkaBlockUserElasticsearchProcessor {

    private static final Logger logger = LogManager.getLogger(KafkaMessageElasticsearchProcessor.class);


    public static final String GROUP_ID = "2";
    private final KafkaUtils kafkaUtils;

    private final KafkaElasticsearchBlockUserManager kafkaElasticsearchBlockUserManager;


    private Consumer<String, KafkaBlockUser> consumer;

    public KafkaBlockUserElasticsearchProcessor(KafkaUtils kafkaUtils, KafkaElasticsearchBlockUserManager kafkaElasticsearchBlockUserManager) {
        this.kafkaUtils = kafkaUtils;
        this.kafkaElasticsearchBlockUserManager = kafkaElasticsearchBlockUserManager;
    }


    @PreDestroy
    public void onDestroy() throws Exception {
        logger.debug("Destroying consumer: {}", consumer);
        consumer.close();
    }

    @PostConstruct
    private void createKafkaConsumerOnStartup() throws ExecutionException, InterruptedException {
        kafkaUtils.createTopicIfNotExist(kafkaUtils.messageTopicStorage,
                kafkaUtils.messageTopicStorageRetentionMS, kafkaUtils.defaultReplicaitonFactor);

        consumer = kafkaUtils.createKafkaConsumer(GROUP_ID, new StringDeserializer(), new JsonDeserializer<>(KafkaMessage.class));

        List<String> topics = new ArrayList<>();
        topics.add(kafkaUtils.messageTopicStorage);

        logger.debug("Consumer {} successfully created!", consumer);

        consumer.subscribe(topics);
        logger.debug("Consumer {} successfully subscribed to topics: {}!", consumer, topics);
    }

    public void saveBlockedUserToElasticsearch() {
        synchronized (consumer) {
            ConsumerRecords<String, KafkaBlockUser> consumerRecords = consumer.poll(Duration.ofMillis(5));

            if (consumerRecords.count() > 0) {
                consumerRecords.forEach(crv -> {
                    try {
                        kafkaElasticsearchBlockUserManager.saveBlockedUserToElastic(crv.value());
                    } catch (Exception e) {
                        logger.error("Error while saving blocked user to Elasticsearch: {}", e.getMessage());
                    }
                });
            }
        }
    }


    public List<KafkaBlockUser> loadBlockedUserFromElastic(Long senderId) {
        List<KafkaBlockUser> blockedUsers = new ArrayList<>();

        try {
           blockedUsers = kafkaElasticsearchBlockUserManager.findAllBySenderUserId(senderId);
        } catch (Exception e) {
            logger.error("Error while loading blocked users for senderId {} out of ES: {}", senderId, e.getMessage());
        }

        if (blockedUsers.size() > 0) {
            logger.debug(" Number of records: {} - pulled out of ES index: {}! For sender user with id: {}",
                    blockedUsers.size(), kafkaUtils.elasticIndex, senderId);

            blockedUsers.sort(Comparator.comparing(kafkaBlockUser -> kafkaBlockUser.id, Comparator.reverseOrder()));
        }

        return blockedUsers;
    }
}
