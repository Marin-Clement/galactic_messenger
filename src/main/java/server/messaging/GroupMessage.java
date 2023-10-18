package server.messaging;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GroupMessage {
    private String sender;
    private String content;
    private String group;

    @JsonCreator
    public GroupMessage(
            @JsonProperty("sender") String sender,
            @JsonProperty("content") String content,
            @JsonProperty("group") String group
    ) {
        this.sender = sender;
        this.content = content;
        this.group = group;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public String getGroup() {
        return group;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}