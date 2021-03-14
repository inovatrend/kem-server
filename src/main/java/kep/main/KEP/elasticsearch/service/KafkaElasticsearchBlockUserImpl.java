package kep.main.KEP.elasticsearch.service;

import kep.main.KEP.elasticsearch.repository.KafkaBlockUserRepository;
import kep.main.KEP.model.KafkaBlockUser;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KafkaElasticsearchBlockUserImpl implements KafkaElasticsearchBlockUserManager {

    private final KafkaBlockUserRepository kafkaBlockUserRepository;

    public KafkaElasticsearchBlockUserImpl(KafkaBlockUserRepository kafkaBlockUserRepository) {
        this.kafkaBlockUserRepository = kafkaBlockUserRepository;
    }

    @Override
    public void saveBlockedUserToElastic(KafkaBlockUser kafkaBlockUser) {
        kafkaBlockUserRepository.save(kafkaBlockUser);
    }

    @Override
    public List<KafkaBlockUser> findAllBySenderUserId(Long senderUserId) {
        return kafkaBlockUserRepository.findAllBySenderUserId(senderUserId);
    }
}
