package kep.main.KEP.kafka;

import kep.main.KEP.model.KafkaMessage;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
public class KafkaMessageReceiverProcessor {

    private final KafkaUtils kafkaUtils;

    public KafkaMessageReceiverProcessor(KafkaUtils kafkaUtils) {
        this.kafkaUtils = kafkaUtils;
    }

    public void start(Long senderId, Long receiverId) {

        Properties streamConfig = kafkaUtils.createPropertiesKafkaStreams("message-processor",
                Serdes.StringSerde.class,
                Serdes.StringSerde.class,
                1);

        StreamsBuilder builder = new StreamsBuilder();

        KTable<String, String> sender = builder.table(kafkaUtils.messageTopicStorage + "-" + senderId + receiverId, Consumed.with(Serdes.String(), Serdes.String()));
        KStream<String, String> recipient = builder.stream(kafkaUtils.messageTopicStorage + "-" + receiverId + senderId, Consumed.with(Serdes.String(), Serdes.String()));

        recipient.join(sender, (senderResult, receiverResult) -> senderResult + receiverResult).to(kafkaUtils.messageTopicStorage, Produced.with(Serdes.String(), Serdes.String()));

        KafkaStreams kStream = new KafkaStreams(builder.build(), streamConfig);
        kStream.start();
    }

    public List<KafkaMessage> receive(Long senderId, Long receiverId) {
        Consumer<String, KafkaMessage> consumer = kafkaUtils.createKafkaConsumer(receiverId.toString(), new StringDeserializer(), new JsonDeserializer<>(KafkaMessage.class));

        List<String> topics = new ArrayList<>();
        topics.add(kafkaUtils.messageTopicStorage + "-" + senderId + receiverId);

        consumer.subscribe(topics);
        List<KafkaMessage> consumerRecordValues = new ArrayList<>();

        ConsumerRecords<String, KafkaMessage> consumerRecords = consumer.poll(Duration.ofMillis(100));
        consumerRecords.forEach(crv -> {
            consumerRecordValues.add(crv.value());
        });

        consumer.close();
        return consumerRecordValues;
    }
}
