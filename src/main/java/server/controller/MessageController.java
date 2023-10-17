package server.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class MessageController {

    @MessageMapping("/send-message")
    @SendTo("/topic/messages")
    public String sendMessage(String message) {
        System.out.println("Received message: " + message);
        return message;
    }
}
