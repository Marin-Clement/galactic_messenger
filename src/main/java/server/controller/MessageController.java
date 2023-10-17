package server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import server.component.UserSessionManager;
import server.messaging.PrivateMessage;

@Controller
public class MessageController {
    private UserSessionManager userSessionManager;

    @Autowired
    public MessageController(UserSessionManager userSessionManager) {
        this.userSessionManager = userSessionManager;
    }

    @MessageMapping("/send-message")
    @SendTo("/topic/messages")
    public PrivateMessage sendMessage(PrivateMessage message) {
        String sender = message.getSender();

        // Check if the sender is a connected user
        if (userSessionManager.isUserConnected(sender)) {
            System.out.println("Received message: " + message.getContent() + " from " + sender + " to " + message.getRecipient());
            return message;
        } else {
            System.out.println("Received message from an unconnected user: " + message.getContent() + " to " + "(" + message.getRecipient() + ")");
            return null;
        }
    }
}
