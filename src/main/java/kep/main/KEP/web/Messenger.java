package kep.main.KEP.web;

import kep.main.KEP.kafka.KafkaMessageReceiverProcessor;
import kep.main.KEP.kafka.KafkaMessageSenderProcessor;
import kep.main.KEP.model.KafkaMessage;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/messenger")
public class Messenger {

    private final KafkaMessageSenderProcessor kafkaMessageSenderProcessor;
    private final KafkaMessageReceiverProcessor kafkaMessageReceiverProcessor;

    public Messenger(KafkaMessageSenderProcessor kafkaMessageSenderProcessor, KafkaMessageReceiverProcessor kafkaMessageReceiverProcessor) {
        this.kafkaMessageSenderProcessor = kafkaMessageSenderProcessor;
        this.kafkaMessageReceiverProcessor = kafkaMessageReceiverProcessor;
    }


    @RequestMapping("/send")
    public void produceMessage(@RequestBody(required = false) KafkaMessage kafkaMessage) throws ExecutionException, InterruptedException {
        kafkaMessageSenderProcessor.startProducing(kafkaMessage);
        kafkaMessageReceiverProcessor.start(kafkaMessage.senderUserId, kafkaMessage.receiverUserId);
    }

    @GetMapping("/receive/{senderId}/{receiverId}")
    public List<KafkaMessage> consumeMessage( @PathVariable Long senderId, @PathVariable Long receiverId) {
//        kafkaMessageReceiverProcessor.start(senderId, receiverId);
         return kafkaMessageReceiverProcessor.receive(senderId, receiverId);
    }
}
