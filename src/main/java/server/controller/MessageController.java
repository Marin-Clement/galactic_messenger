package server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import server.component.UserSessionManager;
import server.messaging.GroupMessage;

@Controller
public class MessageController {
    private final UserSessionManager userSessionManager;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public MessageController(UserSessionManager userSessionManager, SimpMessagingTemplate messagingTemplate) {
        this.userSessionManager = userSessionManager;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/send-message")
    public String sendMessage(PrivateMessage message) {
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
    public String sendGroupMessage(GroupMessage message) {
        String sender = message.getSender();
        String group = message.getGroup();

        // Check if the sender is a connected user
        if (userSessionManager.isUserConnected(sender)) {
            String destination = "/topic/groups/" + group;

            messagingTemplate.convertAndSend(destination, message);
            System.out.println("Sent message: " + message.getContent() + " from " + sender + " to " + group);
            return "Message sent.";
        } else {
            System.out.println("Sender is not connected: " + sender);
            return "Sender is not connected.";
        }
    }
}
