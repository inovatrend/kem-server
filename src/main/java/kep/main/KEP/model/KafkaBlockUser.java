package kep.main.KEP.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "kafka_block_user")
public class KafkaBlockUser {

    @Id
    @Field(type = FieldType.Keyword)
    @JsonProperty("id")
    public String id;

    @JsonProperty("senderUserId")
    public Long senderUserId;

    @JsonProperty("blockUserId")
    public Long blockUserId;

    @JsonProperty("username")
    public String username;
}
