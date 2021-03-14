package kep.main.KEP.kafka;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class KafkaStreamsProcessor {

    private final KafkaUtils kafkaUtils;

    public KafkaStreamsProcessor(KafkaUtils kafkaUtils) {
        this.kafkaUtils = kafkaUtils;
    }

    public void blockUser(Long senderId, Long blockUser) {
        Properties streamConfig = kafkaUtils.createPropertiesKafkaStreams("block-processor",
                Serdes.StringSerde.class,
                Serdes.StringSerde.class,
                1);

        StreamsBuilder builder = new StreamsBuilder();

        KTable<String, String> sender = builder.table(kafkaUtils.messageTopicStorage + "-" + blockUser);
        KStream<String, String> recipient = builder.stream(kafkaUtils.messageTopicStorage + "-" + senderId);

        KStream<String, String> joined = recipient.join(sender, (senderResult, receiverResult) -> senderResult + receiverResult);

        joined.to(kafkaUtils.blockTopic);

        KafkaStreams kStream = new KafkaStreams(builder.build(), streamConfig);
        kStream.start();
    }

}
