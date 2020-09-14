package kep.main.KEP.kafka;

import kep.main.KEP.elasticsearch.KafkaElasticsearchManager;
import kep.main.KEP.model.KafkaMessage;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class KafkaMessageElasticsearchProcessor {
    private static final Logger logger = LogManager.getLogger(KafkaMessageElasticsearchProcessor.class);


    public static final String GROUP_ID = "1";
    private final KafkaElasticUtils kafkaElasticUtils;

    private final KafkaElasticsearchManager kafkaElasticsearchManager;

    private Consumer<String, KafkaMessage> consumer;

    public KafkaMessageElasticsearchProcessor(KafkaElasticUtils kafkaElasticUtils, KafkaElasticsearchManager kafkaElasticsearchManager) {
        this.kafkaElasticUtils = kafkaElasticUtils;
        this.kafkaElasticsearchManager = kafkaElasticsearchManager;
    }

    @PostConstruct
    private void createKafkaConsumerOnStartup() throws ExecutionException, InterruptedException {
        kafkaElasticUtils.createTopicIfNotExist(kafkaElasticUtils.messageTopicStorage,
                kafkaElasticUtils.messageTopicStorageRetentionMS, kafkaElasticUtils.defaultReplicaitonFactor);

        consumer = kafkaElasticUtils.createKafkaConsumer(GROUP_ID, new StringDeserializer(), new JsonDeserializer<>(KafkaMessage.class));

        List<String> topics = new ArrayList<>();
        topics.add(kafkaElasticUtils.messageTopicStorage);

        logger.debug("Consumer {} successfully created!", consumer);

        consumer.subscribe(topics);
        logger.debug("Consumer {} successfully subscribed to topics: {}!", consumer, topics);
    }

    public void kafkaElasticsearchReceiver() {
        synchronized (consumer) {
            ConsumerRecords<String, KafkaMessage> consumerRecords = consumer.poll(Duration.ofMillis(300));

            if (consumerRecords.count() > 0) {
                consumerRecords.forEach(crv -> {
                    long topicLag = getTopicLag(crv);
                    logger.info("Topic {} lag: {} ", kafkaElasticUtils.messageTopicStorage, topicLag);
                    try {
                        kafkaElasticsearchManager.saveToElastic(crv.value());
                    } catch (Exception e) {
                        logger.error("Error while saving to Elasticsearch: {}", e.getMessage());
                    }
                });
            }
        }
    }

    public List<KafkaMessage> loadFromElasticsearch(Long senderId, Long receiverId) {
        List<KafkaMessage> conversationMessageList = new ArrayList<>();

        List<KafkaMessage> receiverMessageList;
        List<KafkaMessage> senderMessageList;

        try {
            receiverMessageList = kafkaElasticsearchManager.loadAllMessagesForUser(receiverId, senderId);
            senderMessageList = kafkaElasticsearchManager.loadAllMessagesForUser(senderId, receiverId);

            conversationMessageList.addAll(senderMessageList);
            conversationMessageList.addAll(receiverMessageList);
        } catch (Exception e) {
            logger.error("Error while loading messages out of ES: {}", e.getMessage());
        }

        if (conversationMessageList.size() > 0) {
            logger.debug(" Number of records: {} - pulled out of ES index: {}! For sender user with id: {} and receiver user with id: {}",
                    conversationMessageList.size(), kafkaElasticUtils.elasticIndex, senderId, receiverId);

            conversationMessageList.sort(Comparator.comparing(kafkaMessage -> kafkaMessage.id, Comparator.reverseOrder()));
        }

        return conversationMessageList;
    }

    public long getTopicLag(org.apache.kafka.clients.consumer.ConsumerRecord<String, KafkaMessage> crv) {
        List<TopicPartition> partitions = consumer.partitionsFor(kafkaElasticUtils.messageTopicStorage)
                .stream().map(p -> new TopicPartition(p.topic(), p.partition()))
                .collect(Collectors.toList());

        Map<TopicPartition, Long> endOffsets = consumer.endOffsets(partitions);

        long topicLag = 0;
        for (Map.Entry<TopicPartition, Long> endOffset : endOffsets.entrySet()) {
            Long currentOffset = crv.offset();
            long partitionLag = endOffset.getValue() - currentOffset;
            topicLag += partitionLag;
        }
        return topicLag;
    }


}
