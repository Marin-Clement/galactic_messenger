package server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import server.component.UserSessionManager;
import server.component.GroupManager;
import server.messaging.Message;

@Controller
public class MessageController {
    private final UserSessionManager userSessionManager;

    private final GroupManager groupManager;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public MessageController(UserSessionManager userSessionManager, GroupManager groupManager, SimpMessagingTemplate messagingTemplate) {
        this.userSessionManager = userSessionManager;
        this.groupManager = groupManager;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/send-message")
    public String sendMessage(Message message) {
        String sender = message.getSender();

        // Check if the sender is a connected user
        if (userSessionManager.isUserConnected(sender)) {
            String recipient = message.getRecipient();
            String destination = "/topic/messages/" + recipient;

            if (userSessionManager.isUserConnected(recipient)) {
                messagingTemplate.convertAndSend(destination, message);
                System.out.println("Sent message: " + message.getContent() + " from " + sender + " to " + recipient);
                return "Message sent.";
            } else {
                System.out.println("Recipient is not connected: " + recipient);
                return "Recipient is not connected.";
            }
        } else {
            System.out.println("Sender is not connected: " + sender);
            return "Sender is not connected.";
        }
    }

    @MessageMapping("/send-group-message")
    public String sendGroupMessage(Message message) {
        String sender = message.getSender();
        String group = message.getRecipient();

        // * Check if the sender is not a connected user
        if (!userSessionManager.isUserConnected(sender)) {
            System.out.println("Sender is not connected: " + sender);
            return "Sender is not connected.";
        }

        // * Check if the group doesn't exist
        if (!groupManager.isGroupExists(group)) {
            System.out.println("Group does not exist: " + group);
            return "Group does not exist.";
        }

        // * Check if the sender is not in the group
        if (!groupManager.isUserInGroup(group, sender)) {
            System.out.println("Sender is not in the group: " + sender);
            return "Sender is not in the group.";
        }

        // * Send the message to all members of the group
        for (String recipient : groupManager.getGroupMembers(group)) {
            String destination = "/topic/messages/" + recipient;
            messagingTemplate.convertAndSend(destination, message);
        }

        System.out.println("Sent group message: " + message.getContent() + " from " + sender + " to " + group);
        return "Group message sent.";
    }

}
