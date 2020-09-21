package kep.main.KEP.elasticsearch;

import kep.main.KEP.model.KafkaMessage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KafkaElasticsearchManagerImpl implements KafkaElasticsearchManager {
    private final KafkaMessageRepository kafkaMessageRepository;

    public KafkaElasticsearchManagerImpl(KafkaMessageRepository kafkaMessageRepository) {
        this.kafkaMessageRepository = kafkaMessageRepository;
    }

    public List<KafkaMessage> loadAllMessagesForUser(Long senderId, Long receiverId) {
        return kafkaMessageRepository.findAllBySenderUserIdAndReceiverUserIdOrderById(senderId, receiverId);
    }

    @Override
    public void saveKafkaMessageToElastic(KafkaMessage kafkaMessage) {
        kafkaMessageRepository.save(kafkaMessage);
    }

}
