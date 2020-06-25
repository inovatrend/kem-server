package kep.main.KEP.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;
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

    public void start(Long userId) {
         Properties streamConfig = kafkaUtils.createPropertiesKafkaStreams("user-coordination-processor",
                Serdes.String().getClass(),
                JsonSerde.class,
                1);
        StreamsBuilder builder = new StreamsBuilder();

        KStream newInputStream = builder.stream(kafkaUtils.messageTopicStorage);

        KGroupedStream groupedStream = newInputStream.groupBy((key, value) -> key.equals(userId));

        groupedStream.count().toStream().to(kafkaUtils.messageTopicStorage);

        KafkaStreams kStream = new KafkaStreams(builder.build(), streamConfig);
        kStream.start();
    }

    public ConsumerRecords receive(Long userId) {
        KafkaConsumer consumer = kafkaUtils.createKafkaConsumer(userId.toString(), StringDeserializer.class, JsonDeserializer.class);

        List<String> topics = new ArrayList<>();
        topics.add(kafkaUtils.messageTopicStorage);

        consumer.subscribe(topics);
        return consumer.poll(Duration.ofSeconds(1));
    }
}
