package client.messaging;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Message {
    private String sender;
    private String content;
    private String recipient;
    private boolean isPrivate;

    @JsonCreator
    public Message(
            @JsonProperty("sender") String sender,
            @JsonProperty("recipient") String recipient,
            @JsonProperty("content") String content,
            @JsonProperty("isPrivate") boolean isPrivate
            ) {
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.isPrivate = isPrivate;
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

    public boolean getIsPrivate() {
        return isPrivate;
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

    public void setIsPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }
}