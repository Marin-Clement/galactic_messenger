package client;

import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Scanner;

public class WebSocketClient {

    public static void main(String[] args) {
        WebSocketClient client = new WebSocketClient();
        client.connectAndSend();
    }

    public void connectAndSend() {
        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(
                Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient()))));

        StompSessionHandler sessionHandler = new MyStompSessionHandler();
        StompSession stompSession;
        try {
            stompSession = stompClient.connect("ws://localhost:8080/messages", sessionHandler).get();
            System.out.println("Connected to WebSocket Server");

            // Subscribe to the /topic/messages to receive messages from the server
            stompSession.subscribe("/topic/messages", new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return byte[].class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        System.out.println("Received message: " + new String((byte[]) payload));
                    }
            });

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("Enter a message to send (or type 'exit' to quit): ");
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("exit")) {
                    break;
                }
                // Send the message to the server after encoding it as bytes
                stompSession.send("/app/send-message", input.getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class MyStompSessionHandler extends StompSessionHandlerAdapter {
        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            System.out.println("Connected to WebSocket Server");
        }
    }
}
