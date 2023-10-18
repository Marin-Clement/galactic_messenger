package server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import server.component.UserSessionManager;
import server.messaging.PrivateMessage;

@Controller
public class MessageController {
    private UserSessionManager userSessionManager;
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    public MessageController(UserSessionManager userSessionManager, SimpMessagingTemplate messagingTemplate) {
        this.userSessionManager = userSessionManager;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/send-message")
    public void sendMessage(PrivateMessage message) {
        String sender = message.getSender();

        // Check if the sender is a connected user
        if (userSessionManager.isUserConnected(sender)) {
            String recipient = message.getRecipient();
            String destination = "/topic/messages/" + recipient;

            if (userSessionManager.isUserConnected(recipient)) {
                messagingTemplate.convertAndSend(destination, message);
                System.out.println("Sent message: " + message.getContent() + " from " + sender + " to " + recipient);
            } else {
                System.out.println("Recipient is not connected: " + recipient);
                // Handle the case where the recipient is not connected
            }
        } else {
            System.out.println("Sender is not connected: " + sender);
            // Handle the case where the sender is not connected
        }
    }
}
