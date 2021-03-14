package kep.main.KEP.elasticsearch.repository;

import kep.main.KEP.model.KafkaMessage;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KafkaMessageRepository extends ElasticsearchRepository<KafkaMessage, String> {
    List<KafkaMessage> findAllBySenderUserIdAndReceiverUserIdOrderById(Long senderId, Long receiverId);
}
