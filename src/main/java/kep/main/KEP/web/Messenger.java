package kep.main.KEP.web;

import kep.main.KEP.kafka.KafkaMessageElasticsearchProcessor;
import kep.main.KEP.kafka.KafkaMessageSenderProcessor;
import kep.main.KEP.model.KafkaMessage;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/messenger")
public class Messenger {

    private final KafkaMessageSenderProcessor kafkaMessageSenderProcessor;
    private final KafkaMessageElasticsearchProcessor kafkaMessageElasticsearchProcessor;

    public Messenger(KafkaMessageSenderProcessor kafkaMessageSenderProcessor, KafkaMessageElasticsearchProcessor kafkaMessageElasticsearchProcessor) {
        this.kafkaMessageSenderProcessor = kafkaMessageSenderProcessor;
        this.kafkaMessageElasticsearchProcessor = kafkaMessageElasticsearchProcessor;
    }

    @RequestMapping("/send")
    public void produceMessageAndSaveItToElastic(@RequestBody(required = false) KafkaMessage kafkaMessage) throws ExecutionException, InterruptedException {
        kafkaMessageSenderProcessor.startProducing(kafkaMessage);
//        kafkaMessageReceiverProcessor.start(kafkaMessage.senderUserId, kafkaMessage.receiverUserId);
    }

    @GetMapping("/receive/{senderId}/{receiverId}")
    public List<KafkaMessage> loadMessages(@PathVariable Long senderId, @PathVariable Long receiverId) {
        kafkaMessageElasticsearchProcessor.kafkaElasticsearchReceiver();
        return kafkaMessageElasticsearchProcessor.loadFromElasticsearch(senderId, receiverId);
    }
}
