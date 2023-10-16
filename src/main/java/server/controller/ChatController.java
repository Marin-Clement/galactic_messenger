package server.controller;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import server.messaging.PrivateMessage;

@Controller
public class ChatController {
    private SimpMessagingTemplate simpMessagingTemplate;

    public ChatController(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @MessageMapping("/private")
    public PrivateMessage sendPrivateMessage(PrivateMessage message) {
        // Log the received message
        System.out.println("Received private message from " + message.getSender() + " to " + message.getRecipient() + ": " + message.getContent());

        // Send the message to the recipient
        simpMessagingTemplate.convertAndSendToUser(message.getRecipient(), "/queue/private", message);

        // Create and return a response message
        PrivateMessage responseMessage = new PrivateMessage();
        responseMessage.setSender("Server");
        responseMessage.setRecipient(message.getSender());
        responseMessage.setContent("Your message has been received and processed.");

        return responseMessage;
    }
}
