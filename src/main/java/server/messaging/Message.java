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
            @JsonProperty("content") String content,
            @JsonProperty("recipient") String recipient
    ) {
        this.sender = sender;
        this.content = content;
        this.recipient = recipient;
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