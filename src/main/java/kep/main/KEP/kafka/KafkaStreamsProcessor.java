package kep.main.KEP.kafka;

import org.springframework.stereotype.Service;

@Service
public class KafkaStreamsProcessor {

    private final KafkaElasticUtils kafkaElasticUtils;

    public KafkaStreamsProcessor(KafkaElasticUtils kafkaElasticUtils) {
        this.kafkaElasticUtils = kafkaElasticUtils;
    }


    //TO BE USED FOR BLOCK SYSTEM!
//    public void start(Long senderId, Long receiverId) {
//        Properties streamConfig = kafkaElasticUtils.createPropertiesKafkaStreams("message-processor",
//                Serdes.StringSerde.class,
//                Serdes.StringSerde.class,
//                1);
//
//        StreamsBuilder builder = new StreamsBuilder();
//
//        KTable<String, String> sender = builder.table(kafkaElasticUtils.messageTopicStorage + "-" + receiverId);
//        KStream<String, String> recipient = builder.stream(kafkaElasticUtils.messageTopicStorage + "-" + senderId);
//
//        KStream<String, String> joined = recipient.join(sender, (senderResult, receiverResult) -> senderResult + receiverResult);
//
//        joined.to(kafkaElasticUtils.messageTopicStorage + "-" + receiverId);
//
//        KafkaStreams kStream = new KafkaStreams(builder.build(), streamConfig);
//        kStream.start();
//    }

}
