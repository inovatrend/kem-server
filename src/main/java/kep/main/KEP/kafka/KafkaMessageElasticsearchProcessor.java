package kep.main.KEP.kafka;

import kep.main.KEP.elasticsearch.KafkaMessageRepository;
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

@Service
public class KafkaMessageElasticsearchProcessor {

    public static final String GROUP_ID = "1";
    private final KafkaUtils kafkaUtils;

    private final KafkaMessageRepository kafkaMessageRepository;

    private Consumer<String, KafkaMessage> consumer = null;

    public KafkaMessageElasticsearchProcessor(KafkaUtils kafkaUtils, KafkaMessageRepository kafkaMessageRepository) {
        this.kafkaUtils = kafkaUtils;
        this.kafkaMessageRepository = kafkaMessageRepository;
    }

    @PostConstruct
    private void createKafkaConsumerOnStartup() {
        consumer = kafkaUtils.createKafkaConsumer(GROUP_ID, new StringDeserializer(), new JsonDeserializer<>(KafkaMessage.class));

        List<String> topics = new ArrayList<>();
        topics.add(kafkaUtils.messageTopicStorage);

        consumer.subscribe(topics);
    }

    public void kafkaElasticsearchReceiver() {
        ConsumerRecords<String, KafkaMessage> consumerRecords = consumer.poll(Duration.ofMillis(100));

        if (consumerRecords.count() > 0) {
            consumerRecords.forEach(crv -> {
                kafkaMessageRepository.save(crv.value());
            });
        } else {
            consumer.wakeup();
        }
    }

    public List<KafkaMessage> loadFromElasticsearch(Long senderId, Long receiverId) {
        List<KafkaMessage> resultMessageList = new ArrayList<>();

        List<KafkaMessage> receiverMessageList = kafkaMessageRepository.findAllBySenderUserIdAndReceiverUserIdOrderById(receiverId, senderId);
        List<KafkaMessage> senderMessageList = kafkaMessageRepository.findAllBySenderUserIdAndReceiverUserIdOrderById(senderId, receiverId);

        resultMessageList.addAll(senderMessageList);
        resultMessageList.addAll(receiverMessageList);

        if (resultMessageList.size() > 0) {
            resultMessageList.sort(Comparator.comparing(kafkaMessage -> kafkaMessage.id));
        }

        return resultMessageList;
    }

}
