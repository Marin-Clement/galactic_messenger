package server.messaging;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Message {
    private String sender;
    private String content;
    private String recipient;

    @JsonCreator
    public Message(
            @JsonProperty("sender") String sender,
            @JsonProperty("recipient") String recipient,
            @JsonProperty("content") String content
            ) {
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }
}