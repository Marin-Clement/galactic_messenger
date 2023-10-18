package server.messaging;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PrivateMessage {
    private String sender;
    private String recipient;
    private String content;

    @JsonCreator
    public PrivateMessage(
            @JsonProperty("sender") String sender,
            @JsonProperty("recipient") String recipient,
            @JsonProperty("content") String content
    ) {
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
    }

    public PrivateMessage(String username, String content) {
        this.sender = username;
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getContent() {
        return content;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
