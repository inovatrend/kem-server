package kep.main.KEP.elasticsearch.service;

import kep.main.KEP.model.KafkaBlockUser;

import java.util.List;

public interface KafkaElasticsearchBlockUserManager {

    void saveBlockedUserToElastic(KafkaBlockUser kafkaBlockUser);

    List<KafkaBlockUser> findAllBySenderUserId(Long senderId);
}
