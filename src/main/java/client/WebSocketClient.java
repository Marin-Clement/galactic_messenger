package client;

import ch.qos.logback.core.net.server.Client;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import server.messaging.PrivateMessage;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Scanner;


public class WebSocketClient {
    String username = "Not logged in";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    private final int port = 8080;
    private final String serverUrl = "http://localhost:" + port + "/api/";

    public static void main(String[] args) {
        ClientController cc = new ClientController();
        cc.connectAndRecieve();
    }
}