package kep.main.KEP.kafka;

import kep.main.KEP.elasticsearch.KafkaElasticsearchManager;
import kep.main.KEP.model.KafkaMessage;
import kep.main.KEP.model.KafkaMonitorMetrics;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
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

    private final KafkaLagProcessor kafkaLagProcessor;

    private Consumer<String, KafkaMessage> consumer;

    public KafkaMessageElasticsearchProcessor(KafkaElasticUtils kafkaElasticUtils, KafkaElasticsearchManager kafkaElasticsearchManager, KafkaLagProcessor kafkaLagProcessor) {
        this.kafkaElasticUtils = kafkaElasticUtils;
        this.kafkaElasticsearchManager = kafkaElasticsearchManager;
        this.kafkaLagProcessor = kafkaLagProcessor;
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



    public List<KafkaMessage> loadFromElasticsearch(Long senderId, Long receiverId) {
        List<KafkaMessage> conversationMessageList = new ArrayList<>();

        List<KafkaMessage> receiverMessageList;
        List<KafkaMessage> senderMessageList;

        saveMessageToElasticAndProcessTopicLag();

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

    private void saveMessageToElasticAndProcessTopicLag() {
        synchronized (consumer) {
            ConsumerRecords<String, KafkaMessage> consumerRecords = consumer.poll(Duration.ofMillis(5));

            if (consumerRecords.count() > 0) {
                consumerRecords.forEach(crv -> {
                    Long topicLag = processTopicLag(crv.offset(), crv.topic());

                    kafkaLagProcessor.addKafkaTopicLag(new KafkaMonitorMetrics(DateTime.now().getMillis(), topicLag, crv.topic()));

                    logger.info("Topic {} lag: {} ", crv.topic(), topicLag);
                    try {
                        kafkaElasticsearchManager.saveKafkaMessageToElastic(crv.value());
                    } catch (Exception e) {
                        logger.error("Error while saving to Elasticsearch: {}", e.getMessage());
                    }
                });
            }
        }
    }

    public long processTopicLag(long offset, String topicName) {
        List<TopicPartition> partitions = consumer.partitionsFor(topicName)
                .stream().map(p -> new TopicPartition(p.topic(), p.partition()))
                .collect(Collectors.toList());

        Map<TopicPartition, Long> endOffsets = consumer.endOffsets(partitions);

        long topicLag = 0;
        for (Map.Entry<TopicPartition, Long> endOffset : endOffsets.entrySet()) {
            Long currentOffset = offset;
            long partitionLag = endOffset.getValue() - currentOffset;
            topicLag += partitionLag;
        }
        return topicLag;
    }


}
