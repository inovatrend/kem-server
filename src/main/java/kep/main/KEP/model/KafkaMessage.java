package kep.main.KEP.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "message")
public class KafkaMessage {

    @Id
    @Field(type = FieldType.Keyword)
    @JsonProperty("id")
    public String id;

    @JsonProperty("message")
    public String message;

    @JsonProperty("senderUserId")
    public Long senderUserId;

    @JsonProperty("receiverUserId")
    public Long receiverUserId;
}
