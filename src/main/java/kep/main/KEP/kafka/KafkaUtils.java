package kep.main.KEP.kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class KafkaUtils {
    public AdminClient kafkaAdmin;

    @Value(value = "${kafka.bootstrap.servers}")
    public String bootstrapServers = "127.0.0.1:9092";

    @Value(value = "${elastic.index}")
    public String elasticIndex;

    @Value(value = "${default.topic.replication.factor}")
    public String defaultReplicaitonFactor = "3";

    @Value(value = "${streaming.state.store.dir}")
    private String streamingStateStoreDir;

    String messageTopicStorage = "message-topic";
    String blockTopic = "block-topic";
    Long messageTopicStorageRetentionMS = 15552000000L;


    private final ReentrantLock createTopicLock = new ReentrantLock();
    ConcurrentHashMap existingTopics = new ConcurrentHashMap();

    public void init() throws ExecutionException, InterruptedException {
        createTopicIfNotExist(messageTopicStorage, messageTopicStorageRetentionMS, defaultReplicaitonFactor);
        createTopicIfNotExist(blockTopic, messageTopicStorageRetentionMS, defaultReplicaitonFactor);
    }

    public void createTopicIfNotExist(String topicName, Long messageTopicStorageRetentionMS,
                                      String defaultReplicaitonFactor) throws InterruptedException, ExecutionException {
        synchronized (createTopicLock) {
            if (existingTopics.contains(topicName)) {
                boolean topicExists = kafkaAdmin.listTopics().names().get().contains(topicName);

                if (!topicExists) {
                    Map<String, String> topicConfMap = new HashMap<>();
                    topicConfMap.put(TopicConfig.RETENTION_MS_CONFIG, messageTopicStorageRetentionMS.toString());
                    topicConfMap.put(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE);
                    int messageTopicStorageNumPartitions = 3;
                    NewTopic topic = new NewTopic(topicName, messageTopicStorageNumPartitions,
                            Short.parseShort(defaultReplicaitonFactor))
                            .configs(topicConfMap);

                    List<NewTopic> resultTopicList = new ArrayList<>();
                    resultTopicList.add(topic);

                    kafkaAdmin.createTopics(resultTopicList);
                }
            }
        }
    }

    public KafkaProducer createKafkaProducer(String ack, Class<StringSerializer> keySerializer, Class<KafkaJsonSerializer> valueSerializer) {
        Properties producerProperties = new Properties();

        producerProperties.put(ProducerConfig.ACKS_CONFIG, ack);
        producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        producerProperties.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        producerProperties.put(ProducerConfig.RETRIES_CONFIG, 10);
        producerProperties.put(ProducerConfig.BATCH_SIZE_CONFIG, 50000);
        producerProperties.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 500);
        producerProperties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        producerProperties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        return new KafkaProducer<>(producerProperties);
    }

    public Consumer createKafkaConsumer(String groupId, StringDeserializer keyDeserializer, JsonDeserializer valueDeserializer) {
        Properties consumerProperties = new Properties();

        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        consumerProperties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1000);
        return new KafkaConsumer<>(consumerProperties, keyDeserializer, valueDeserializer);
    }

    public Properties createPropertiesKafkaStreams(String applicationId, Class<Serdes.StringSerde> keySerde, Class<Serdes.StringSerde> valueSerde, int threads) {
        Properties streamProperties = new Properties();

        streamProperties.put(StreamsConfig.STATE_DIR_CONFIG, streamingStateStoreDir);
        streamProperties.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        streamProperties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        streamProperties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, keySerde);
        streamProperties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, valueSerde);
        streamProperties.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, threads);
        streamProperties.put(ProducerConfig.LINGER_MS_CONFIG, 1000);
        return streamProperties;
    }
}

