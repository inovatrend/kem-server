package kep.main.KEP.web;

import kep.main.KEP.kafka.KafkaMessageElasticsearchProcessor;
import kep.main.KEP.kafka.KafkaMessageSenderProcessor;
import kep.main.KEP.kafka.KafkaStreamsProcessor;
import kep.main.KEP.model.KafkaBlockUser;
import kep.main.KEP.model.KafkaMessage;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messenger")
public class MessengerController {

    private final KafkaMessageSenderProcessor kafkaMessageSenderProcessor;
    private final KafkaMessageElasticsearchProcessor kafkaMessageElasticsearchProcessor;



    public MessengerController(KafkaMessageSenderProcessor kafkaMessageSenderProcessor, KafkaMessageElasticsearchProcessor kafkaMessageElasticsearchProcessor) {
        this.kafkaMessageSenderProcessor = kafkaMessageSenderProcessor;
        this.kafkaMessageElasticsearchProcessor = kafkaMessageElasticsearchProcessor;
    }

    @RequestMapping("/send")
    public void produceMessage(@RequestBody(required = false) KafkaMessage kafkaMessage) {
        kafkaMessageSenderProcessor.startProducing(kafkaMessage);
    }

    @GetMapping("/receive/{senderId}/{receiverId}")
    public List<KafkaMessage> saveMessageToElasticAndLoadMessageToUser(@PathVariable Long senderId, @PathVariable Long receiverId) {
        kafkaMessageElasticsearchProcessor.saveMessageToElasticAndProcessTopicLag();
        return kafkaMessageElasticsearchProcessor.loadFromElasticsearch(senderId, receiverId);
    }


}
