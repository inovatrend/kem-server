package kep.main.KEP.elasticsearch;

import kep.main.KEP.model.KafkaMessage;

import java.util.List;

public interface KafkaElasticsearchManager {
    List<KafkaMessage> loadAllMessagesForUser(Long senderId, Long receiverId);

    void saveKafkaMessageToElastic(KafkaMessage kafkaMessage);
}
