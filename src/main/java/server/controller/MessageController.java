package server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.messaging.PrivateMessage;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public MessageController(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @PostMapping("/private")
    public void sendPrivateMessage(@RequestBody PrivateMessage message) {
        simpMessagingTemplate.convertAndSendToUser(message.getRecipient(), "/queue/private", message);
        System.out.println("Sent private message from " + message.getSender() + " to " + message.getRecipient() + ": " + message.getContent());
    }
}
