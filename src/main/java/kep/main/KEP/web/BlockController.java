package kep.main.KEP.web;

import kep.main.KEP.kafka.KafkaBlockUserElasticsearchProcessor;
import kep.main.KEP.kafka.KafkaStreamsProcessor;
import kep.main.KEP.model.KafkaBlockUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/block")
public class BlockController {
    private final KafkaStreamsProcessor kafkaStreamsProcessor;
    private final KafkaBlockUserElasticsearchProcessor kafkaBlockUserElasticsearchProcessor;

    public BlockController(KafkaStreamsProcessor kafkaStreamsProcessor, KafkaBlockUserElasticsearchProcessor kafkaBlockUserElasticsearchProcessor) {
        this.kafkaStreamsProcessor = kafkaStreamsProcessor;
        this.kafkaBlockUserElasticsearchProcessor = kafkaBlockUserElasticsearchProcessor;
    }

    @RequestMapping("/{senderId}/{blockUserId}")
    public void blockUser(@PathVariable Long senderId, @PathVariable Long blockUserId) {
        kafkaStreamsProcessor.blockUser(senderId, blockUserId);
    }

    @GetMapping("/blockUsers/{senderUserId}")
    public List<KafkaBlockUser> getBlockedUsers(@PathVariable Long senderUserId) {
        return kafkaBlockUserElasticsearchProcessor.loadBlockedUserFromElastic(senderUserId);
    }

}
