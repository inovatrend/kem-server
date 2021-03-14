package kep.main.KEP.elasticsearch.repository;

import kep.main.KEP.model.KafkaBlockUser;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KafkaBlockUserRepository extends ElasticsearchRepository<KafkaBlockUser, String> {

    List<KafkaBlockUser> findAllBySenderUserId(Long senderUserId);
}
